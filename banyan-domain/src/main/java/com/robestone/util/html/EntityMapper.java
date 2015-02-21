/*
 * Created on Dec 17, 2003
 */
package com.robestone.util.html;

import java.util.*;

/**
 * @author cjsr
 */
public class EntityMapper {

	private static final char HIGHEST_VALID_CHAR = 126; // '~'

	/*
	public static String convertToHTMLNameEntities(String text) {
		return parseAndRender(text, HTMLNameEntityRenderer);
	}
	public static String convertToHTMLNumericalEntities(String text) {
		return parseAndRender(text, HTMLNumericalEntityRenderer);
	}
	*/
	public static String convertToHTMLRenderedEntities(String text) {
		return parseAndRender(text, HTMLPrettyEntityRenderer, null);
	}
	public static String convertToSearchText(String text) {
		return parseAndRender(text, SearchTextEntityRenderer, null);
	}
	public static String convertToSearchText(String text, char def) {
		return parseAndRender(text, SearchTextEntityRenderer, String.valueOf(def));
	}
	public static String convertToSymbolsText(String text) {
		return parseAndRender(text, SymbolEntityRenderer, null);
	}
	public static String convertToSymbolsText(String text, boolean convertUnparseable) {
		return parseAndRender(text, SymbolEntityRenderer, null, convertUnparseable);
	}
	public static String convertToSymbolsText(String text, char def) {
		return parseAndRender(text, SymbolEntityRenderer, String.valueOf(def));
	}
	public static String convertToBracketEntities(String text) {
		return convertToBracketEntities(text, false);
	}
	public static String convertToBracketEntities(String text, boolean convertUnparseable) {
		return parseAndRender(text, BracketsEntityRenderer, null, convertUnparseable);
	}
	private static String parseAndRender(String text, EntityRenderer renderer, String def) {
		return parseAndRender(text, renderer, def, def != null);
	}
	private static String parseAndRender(String text, EntityRenderer renderer, String def, boolean convertUnparseable) {
		List list = parse(text, def, convertUnparseable);
		StringBuffer buf = new StringBuffer();
		int len = list.size();
		for (int i = 0; i < len; i++) {
			Object next = list.get(i);
			String parsed;
			if (next instanceof String) {
				parsed = (String) next;
			} else if (next instanceof Integer) {
				parsed = renderer.toString((Integer) next);
			} else {
				Entity e = (Entity) next;
				parsed = renderer.toString(e);
			}
			if (parsed == null) {
				parsed = def;
			}
			buf.append(parsed);
		}
		return buf.toString();
	}
	/**
	 * Doesn't handle error cases, where the entity is invalid
	 */
	private static List parse(String text, String def, boolean convertUnparseable) {
	    if (text == null) {
	        throw new IllegalArgumentException("Can't work with nulls!");
	    }
		StringBuffer buf = new StringBuffer();
		List list = new ArrayList();
		int len = text.length();
		for (int i = 0; i < len; i++) {
			Object entity = null;
			char c = text.charAt(i);
			if (c == '&') {
				int pos = text.indexOf(';', i);
				if (pos >= 0) {
					char percent = text.charAt(i + 1);
					if (percent == '#') {
						String number = text.substring(i + 2, pos);
						int integer = getNumber(number);
						entity = getEntityForNumber(integer, def);
					} else {
						String name = text.substring(i + 1, pos);
						entity = getEntityForName(name);
					}
					i = pos;
				} else {
					buf.append(c);
				}
			} else if (c == '[') {
				int pos = text.indexOf(']', i);
				if (pos > i) {
					String contents = text.substring(i + 1, pos);
					i = pos;
					entity = getEntityForBracketContents(contents, def, convertUnparseable);
				} else {
					buf.append('[');
				}
			} else if (isAppendableCharacter(c)) {
				buf.append(c);
			} else {
				int number = c;
				if (convertUnparseable) {
					entity = getEntityForNumber(number, new Integer(number));
				} else {
					entity = getEntityForNumber(number, null);
				}
			}
			if (entity != null || i == len - 1) {
				list.add(buf.toString());
			}
			if (entity != null) {
				list.add(entity);
				buf = new StringBuffer();
			}
		}
		return list;
	}
	private static Object getEntityForBracketContents(String contents, Object def, boolean convertUnparseable) {
		int integer = getNumber(contents);
		if (convertUnparseable) {
			def = new Integer(integer);
		}
		if (integer >= 0) {
			return getEntityForNumber(integer, def);
		} else {
			return getEntityForName(contents);
		}
	}
	private static int getNumber(String num) {
		try {
			return Integer.parseInt(num);
		} catch (Exception e) {
			return -1;
		}
	}
	private static boolean isAppendableCharacter(char c) {
		return c <= HIGHEST_VALID_CHAR;
	}
	public static Object getEntityForNumber(int number, Object def) {
		return getForKey(new Integer(number), numberMap, def);
	}
	public static Object getEntityForNumber(int number) {
		return getForKey(new Integer(number), numberMap, null);
	}
	public static Entity getEntityForName(String name) {
		return (Entity) getForKey(name, nameMap, null);
	}
	public static List getEntitiesForSearchText(String searchText) {
		return (List) getForKey(searchText, searchTextMap, null);
	}
	public static String replaceUnparseableCharacters(String string, char replaceWithChar) {
		StringBuilder buf = new StringBuilder();
		int len = string.length();
		for (int i = 0; i < len; i++) {
			char c = string.charAt(i);
			if (isAppendableCharacter(c)) {
				buf.append(c);
			} else {
				Object o = numberMap.get(new Integer((int) c));
				if (o == null) {
					buf.append(replaceWithChar);
				} else {
					buf.append(c);
				}
			}
		}
		return buf.toString();
	}
	private static Object getForKey(Object key, Map map, Object def) {
		Object o = map.get(key);
		if (o == null) {
			if (def == null) {
				throw new IllegalArgumentException("No entity found for key (" + key + ")");
			} else {
				return def;
			}
		} else {
			return o;
		}
	}
	public static List getEntities() {
	    return new ArrayList(nameMap.values());
	}
	private static Map numberMap = new HashMap();
	private static Map nameMap = new HashMap();
	/**
	 * Map<String, List<Entity>>
	 */
	private static Map searchTextMap = new HashMap();
		
	static {
		addEntity("abrevema", -1, "?", "a", "a with both breve and macron");        // 00-1
		addEntity("ebrevema", -1, "?", "e");        // 00-1
		addEntity("ibrevema", -1, "?", "i");        // 00-1
		addEntity("obrevema", -1, "?", "o");        // 00-1
		addEntity("ubrevema", -1, "?", "u");        // 00-1
		addEntity("ilowdot", -1, "?", "i");         // 00-1
		addEntity("tlowmdot", -1, "?", "t");        // 00-1
		addEntity("inodotst", -1, "?", "i");        // 00-1
		addEntity("irevcmac", -1, "?", "c");        // 00-1
		addEntity("llowring", -1, "?", "l");        // 00-1
		addEntity("mlowring", -1, "?", "m");        // 00-1
		addEntity("nlowring", -1, "?", "n");        // 00-1
		addEntity("rlowring", -1, "?", "r");        // 00-1
		addEntity("rx", -1, "?", "rx");             // 00-1
		addEntity("saclod", -1, "?", "s");          // 00-1
		addEntity("quot", 34, "\"", "\"");          // 0022
		addEntity("amp", 38, "&", "&");             // 0026
		addEntity("lt", 60, "<", "<");              // 003C
		addEntity("gt", 62, ">", ">");              // 003E
		addEntity("tic?", 145, "?", "`");           // 0091
		addEntity("nbsp", 160, " ", "?");           // 00A0
		addEntity("iexcl", 161, "¡", "!");          // 00A1
		addEntity("cent", 162, "¢", "c");           // 00A2
		addEntity("pound", 163, "£", "L");          // 00A3
		addEntity("curren", 164, "¤", "?");         // 00A4
		addEntity("yen", 165, "¥", "Y");            // 00A5
		addEntity("brvbar", 166, "¦", "?");         // 00A6
		addEntity("sect", 167, "§", "?");           // 00A7
		addEntity("uml", 168, "¨", "?");            // 00A8
		addEntity("copy", 169, "©", "(c)");         // 00A9
		addEntity("ordf", 170, "ª", "?");           // 00AA
		addEntity("laquo", 171, "«", "?");          // 00AB
		addEntity("not", 172, "¬", "?");            // 00AC
		addEntity("shy", 173, "­", "?");            // 00AD
		addEntity("reg", 174, "®", "(R)");          // 00AE
		addEntity("macr", 175, "¯", "?");           // 00AF
		addEntity("deg", 176, "°", "?");            // 00B0
		addEntity("plusmn", 177, "±", "?");         // 00B1
		addEntity("sup2", 178, "²", "?");           // 00B2
		addEntity("sup3", 179, "³", "?");           // 00B3
		addEntity("micro", 181, "µ", "m");          // 00B5
		addEntity("para", 182, "¶", "P");           // 00B6
		addEntity("middot", 183, "·", "?");         // 00B7
		addEntity("cedil", 184, "¸", "?");          // 00B8
		addEntity("sup1", 185, "¹", "?");           // 00B9
		addEntity("ordm", 186, "º", "?");           // 00BA
		addEntity("raquo", 187, "»", "?");          // 00BB
		addEntity("frac14", 188, "¼", "?");         // 00BC
		addEntity("frac12", 189, "½", "?");         // 00BD
		addEntity("frac34", 190, "¾", "?");         // 00BE
		addEntity("iquest", 191, "¿", "?");         // 00BF
		addEntity("Agrave", 192, "À", "A");         // 00C0
		addEntity("Aacute", 193, "�?", "A");         // 00C1
		addEntity("Acirc", 194, "Â", "A");          // 00C2
		addEntity("Atilde", 195, "Ã", "A");         // 00C3
		addEntity("Auml", 196, "Ä", "A");           // 00C4
		addEntity("Aring", 197, "Å", "A");          // 00C5
		addEntity("AElig", 198, "Æ", "AE");         // 00C6
		addEntity("Ccedil", 199, "Ç", "C");         // 00C7
		addEntity("Egrave", 200, "È", "E");         // 00C8
		addEntity("Eacute", 201, "É", "E");         // 00C9
		addEntity("Ecirc", 202, "Ê", "E");          // 00CA
		addEntity("Euml", 203, "Ë", "E");           // 00CB
		addEntity("Igrave", 204, "Ì", "I");         // 00CC
		addEntity("Iacute", 205, "�?", "I");         // 00CD
		addEntity("Icirc", 206, "Î", "I");          // 00CE
		addEntity("Iuml", 207, "�?", "I");           // 00CF
		addEntity("ETH", 208, "�?", "D");            // 00D0
		addEntity("Ntilde", 209, "Ñ", "N");         // 00D1
		addEntity("Ograve", 210, "Ò", "O");         // 00D2
		addEntity("Oacute", 211, "Ó", "O");         // 00D3
		addEntity("Ocirc", 212, "Ô", "O");          // 00D4
		addEntity("Otilde", 213, "Õ", "O");         // 00D5
		addEntity("Ouml", 214, "Ö", "O");           // 00D6
		addEntity("times", 215, "×", "?");          // 00D7
		addEntity("Oslash", 216, "Ø", "O");         // 00D8
		addEntity("Ugrave", 217, "Ù", "U");         // 00D9
		addEntity("Uacute", 218, "Ú", "U");         // 00DA
		addEntity("Ucirc", 219, "Û", "U");          // 00DB
		addEntity("Uuml", 220, "Ü", "U");           // 00DC
		addEntity("Yacute", 221, "�?", "Y");         // 00DD
		addEntity("THORN", 222, "Þ", "?");          // 00DE
		addEntity("szlig", 223, "ß", "B");          // 00DF
		addEntity("agrave", 224, "à", "a");         // 00E0
		addEntity("aacute", 225, "á", "a");         // 00E1
		addEntity("acirc", 226, "â", "a");          // 00E2
		addEntity("atilde", 227, "ã", "a");         // 00E3
		addEntity("auml", 228, "ä", "a");           // 00E4
		addEntity("aring", 229, "å", "a");          // 00E5
		addEntity("aelig", 230, "æ", "ae");         // 00E6
		addEntity("ccedil", 231, "ç", "c");         // 00E7
		addEntity("egrave", 232, "è", "e");         // 00E8
		addEntity("eacute", 233, "é", "e");         // 00E9
		addEntity("ecirc", 234, "ê", "e");          // 00EA
		addEntity("euml", 235, "ë", "e");           // 00EB
		addEntity("igrave", 236, "ì", "i");         // 00EC
		addEntity("iacute", 237, "í", "i");         // 00ED
		addEntity("icirc", 238, "î", "i");          // 00EE
		addEntity("iuml", 239, "ï", "i");           // 00EF
		addEntity("eth", 240, "ð", "e");            // 00F0
		addEntity("ntilde", 241, "ñ", "n");         // 00F1
		addEntity("ograve", 242, "ò", "o");         // 00F2
		addEntity("oacute", 243, "ó", "o");         // 00F3
		addEntity("ocirc", 244, "ô", "o");          // 00F4
		addEntity("otilde", 245, "õ", "o");         // 00F5
		addEntity("ouml", 246, "ö", "o");           // 00F6
		addEntity("divide", 247, "÷", "?");         // 00F7
		addEntity("oslash", 248, "ø", "o");         // 00F8
		addEntity("ugrave", 249, "ù", "u");         // 00F9
		addEntity("uacute", 250, "ú", "u");         // 00FA
		addEntity("ucirc", 251, "û", "u");          // 00FB
		addEntity("uuml", 252, "ü", "u");           // 00FC
		addEntity("yacute", 253, "ý", "y");         // 00FD
		addEntity("thorn", 254, "þ", "?");          // 00FE
		addEntity("yuml", 255, "ÿ", "y");           // 00FF
		addEntity("Amacr", 256, "?", "A");          // 0100
		addEntity("amacr", 257, "?", "a");          // 0101
		addEntity("Abreve", 258, "?", "A");         // 0102
		addEntity("abreve", 259, "?", "a");         // 0103		
		addEntity("Acaron", 461, "?", "A");         // 0102		
		addEntity("acaron", 462, "?", "a");         // 0103
		
		// ?? - can't find acedil used/defined anywhere
//		addEntity("Acedil", 260, "?", "A");         // 0104
		addEntity("Aogonek", 260, "?", "A");        // 0104
//		addEntity("acedil", 261, "?", "a");         // 0105
		addEntity("aogonek", 261, "?", "a");        // 0105
				
		addEntity("Cacute", 262, "?", "C");         // 0106
		addEntity("cacute", 263, "?", "c");         // 0107
		addEntity("Ccaron", 268, "?", "C");         // 010C
		addEntity("ccaron", 269, "?", "c");         // 010D
		addEntity("Dcaron", 270, "?", "D");         // 010E
		addEntity("Dstrok", 272, "?", "D");         // 0110
		addEntity("dstrok", 273, "?", "d");         // 0111
		addEntity("Emacr", 274, "?", "E");          // 0112
		addEntity("emacr", 275, "?", "e");          // 0113
		addEntity("Ebreve", 276, "?", "E");         // 0114
		addEntity("ebreve", 277, "?", "e");         // 0115
		addEntity("Edot", 278, "?", "E");           // 0116
		addEntity("edot", 279, "?", "e");           // 0117
		
		// ?? - can't find ecedil used/defined anywhere
//		addEntity("Ecedil", 280, "?", "E");         // 0118
		addEntity("Eogon", 280, "?", "E");          // 0118
//		addEntity("ecedil", 281, "?", "e");         // 0119
		addEntity("eogon", 281, "?", "e");          // 0119
		
		addEntity("Ecaron", 282, "?", "E");         // 011A
		addEntity("ecaron", 283, "?", "e");         // 011B
		addEntity("Gcirc", 284, "?", "G");          // 011C
		addEntity("gcirc", 285, "?", "g");          // 011D
		addEntity("Gcaron", 286, "?", "G");         // 011E
		addEntity("gcaron", 287, "?", "g");         // 011F
		addEntity("Gdot", 288, "?", "G");           // 0120
		addEntity("gdot", 289, "?", "g");           // 0121
		addEntity("Gcedil", 290, "?", "G");         // 0122
		addEntity("gapos", 292, "?", "g");          // 0124
		addEntity("Imacr", 298, "?", "I");          // 012A
		addEntity("imacr", 299, "?", "i");          // 012B
		addEntity("Ibreve", 300, "?", "I");         // 012C
		addEntity("ibreve", 301, "?", "i");         // 012D
		addEntity("Icedil", 304, "?", "I");         // 0130
		addEntity("inodot", 305, "?", "i");         // 0131
		addEntity("Kcedil", 310, "?", "K");         // 0136
		addEntity("kcedil", 311, "?", "k");         // 0137
		addEntity("Lacute", 313, "?", "L");         // 0139
		addEntity("lacute", 314, "?", "l");         // 013A
		addEntity("Lcedil", 315, "?", "L");         // 013B
		addEntity("lcedil", 316, "?", "l");         // 013C
		
		// ??
		addEntity("Lslash", 321, "?", "L");         // 0141
		addAdditionalEntityName("Lstrok", 321);
		addEntity("lslash", 322, "?", "l");         // 0142
		addAdditionalEntityName("lstrok", 322);
		
		addEntity("Nacute", 323, "?", "N");         // 0143
		addEntity("nacute", 324, "?", "n");         // 0144
		addEntity("Ncedil", 325, "?", "N");         // 0145
		addEntity("ncedil", 326, "?", "n");         // 0146
		addEntity("Ncaron", 327, "?", "N");         // 0147
		addEntity("ncaron", 328, "?", "n");         // 0148
		addEntity("eng", 331, "?", "n");            // 014B
		addEntity("Omacr", 332, "?", "O");          // 014C
		addEntity("omacr", 333, "?", "o");          // 014D
		addEntity("Obreve", 334, "?", "O");         // 014E
		addEntity("obreve", 335, "?", "o");         // 014F
		addEntity("Odblac", 336, "?", "O");         // 0150
		addEntity("odblac", 337, "?", "o");         // 0151
		addEntity("OElig", 338, "Œ", "OE");         // 0152
		addEntity("oelig", 339, "œ", "oe");         // 0153
		addEntity("Rcedil", 342, "?", "R");         // 0156
		addEntity("rcedil", 343, "?", "r");         // 0157
		addEntity("Rcaron", 344, "?", "R");         // 0158
		addEntity("rcaron", 345, "?", "r");         // 0159
		addEntity("Sacute", 346, "?", "S");         // 015A
		addEntity("sacute", 347, "?", "s");         // 015B
		addEntity("Scedil", 350, "?", "S");         // 015E
		addEntity("scedil", 351, "?", "s");         // 015F
		addEntity("Scaron", 352, "Š", "S");         // 0160
		addEntity("scaron", 353, "?", "s");         // 0161
		addEntity("Tcedil", 354, "?", "T");         // 0162
		addEntity("tcedil", 355, "?", "t");         // 0163
		addEntity("Tcaron", 356, "?", "T");         // 0164
		addEntity("tcaron", 357, "?", "t");         // 0165
		addEntity("Umacr", 362, "?", "U");          // 016A
		addEntity("umacr", 363, "?", "u");          // 016B
		addEntity("Ubreve", 364, "?", "U");         // 016C
		addEntity("ubreve", 365, "?", "u");         // 016D
		addEntity("Uring", 366, "?", "U");          // 016E
		addEntity("uring", 367, "?", "u");          // 016F
		addEntity("Udblac", 368, "?", "U");         // 0170
		addEntity("udblac", 369, "?", "u");         // 0171
		addEntity("Ucedil", 370, "?", "U");         // 0172
		addEntity("ucedil", 371, "?", "u");         // 0173
		addEntity("Yuml", 376, "Ÿ", "Y");           // 0178
		addEntity("Zacute", 377, "?", "Z");         // 0179
		addEntity("zacute", 378, "?", "z");         // 017A
		addEntity("Zdot", 379, "?", "Z");           // 017B
		addEntity("zdot", 380, "?", "z");           // 017C
		addEntity("Zcaron", 381, "Ž", "Z");         // 017D
		addEntity("zcaron", 382, "ž", "z");         // 017E
		addEntity("irevC", 390, "?", "C");          // 0186
		addEntity("schwa", 399, "?", "e");          // 018F
		addEntity("fnof", 402, "ƒ", "f");           // 0192
		addEntity("Icaron", 463, "?", "I");         // 01CF
		addEntity("icaron", 464, "?", "i");         // 01D0
		addEntity("Ocaron", 465, "?", "O");         // 01D1
		addEntity("ocaron", 466, "?", "o");         // 01D2
		addEntity("Ucaron", 467, "?", "U");         // 01D3
		addEntity("ucaron", 468, "?", "u");         // 01D4
		addEntity("Aeligmac", 482, "?", "AE");      // 01E2
		addEntity("aeligmac", 483, "?", "ae");      // 01E3
		addEntity("Ymacr", 562, "?", "Y");          // 0232
		addEntity("ymacr", 563, "?", "y");          // 0233
		addEntity("irevc", 596, "?", "c");          // 0254
		addEntity("egr", 603, "?", "e");            // 025B
		addEntity("nsc", 628, "?", "N");            // 0274
		addEntity("hsmall", 688, "?", "h");         // 02B0
		addEntity("wsmall", 695, "?", "w");         // 02B7
		addEntity("ysmall", 696, "?", "y");         // 02B8
		addEntity("primemodifier", 697, "?", "'");  // 02B9
		addEntity("glottalstop", 700, "?", "'");    // 02BC
		addEntity("circ", 710, "ˆ", "?");           // 02C6
		addEntity("acute", 714, "?", "?");          // 02CA
		addEntity("grave", 715, "?", "`");          // 02CB
		addEntity("dotabove", 729, "?", ".");       // 02D9
		addEntity("tilde", 732, "˜", "?");          // 02DC
		addEntity("rough", 788, "?", "'");          // 0314
		addEntity("Alpha", 913, "?", "A");          // 0391
		addEntity("Beta", 914, "?", "B");           // 0392
		addEntity("Gamma", 915, "?", "G");          // 0393
		addEntity("Delta", 916, "?", "D");          // 0394
		addEntity("Epsilon", 917, "?", "E");        // 0395
		addEntity("Zeta", 918, "?", "Z");           // 0396
		addEntity("Eta", 919, "?", "H");            // 0397
		addEntity("Theta", 920, "?", "?");          // 0398
		addEntity("Iota", 921, "?", "I");           // 0399
		addEntity("Kappa", 922, "?", "K");          // 039A
		addEntity("Lambda", 923, "?", "L");         // 039B
		addEntity("Mu", 924, "?", "M");             // 039C
		addEntity("Nu", 925, "?", "N");             // 039D
		addEntity("Xi", 926, "?", "?");             // 039E
		addEntity("Omicron", 927, "?", "O");        // 039F
		addEntity("Pi", 928, "?", "?");             // 03A0
		addEntity("Rho", 929, "?", "P");            // 03A1
		addEntity("Sigma", 931, "?", "E");          // 03A3
		addEntity("Tau", 932, "?", "T");            // 03A4
		addEntity("Upsilon", 933, "?", "Y");        // 03A5
		addEntity("Phi", 934, "?", "?");            // 03A6
		addEntity("Chi", 935, "?", "X");            // 03A7
		addEntity("Psi", 936, "?", "Y");            // 03A8
		addEntity("Omega", 937, "?", "O");          // 03A9
		addEntity("iota", 943, "?", "i");           // 03AF
		addEntity("alpha", 945, "?", "a");          // 03B1
		addAdditionalEntityName("agr", 945); // means "small alpha, greek"
		addEntity("beta", 946, "?", "b");           // 03B2
		addEntity("gamma", 947, "?", "g");          // 03B3
		addEntity("delta", 948, "?", "d");          // 03B4
		addEntity("epsilon", 949, "?", "e");        // 03B5
		addEntity("zeta", 950, "?", "z");           // 03B6
		addEntity("eta", 951, "?", "?");            // 03B7
		addEntity("theta", 952, "?", "?");          // 03B8
		addEntity("iota", 953, "?", "i");           // 03B9
		addEntity("kappa", 954, "?", "k");          // 03BA
		addEntity("lambda", 955, "?", "l");         // 03BB
		addEntity("mu", 956, "?", "m");             // 03BC
		addEntity("nu", 957, "?", "v");             // 03BD
		addEntity("xi", 958, "?", "?");             // 03BE
		addEntity("omicron", 959, "?", "o");        // 03BF
		addEntity("pi", 960, "?", "p");             // 03C0
		addEntity("rho", 961, "?", "p");            // 03C1
		addEntity("sigmaf", 962, "?", "?");         // 03C2
		addEntity("sigma", 963, "?", "?");          // 03C3
		addEntity("tau", 964, "?", "?");            // 03C4
		addEntity("upsilon", 965, "?", "u");        // 03C5
		addEntity("phi", 966, "?", "?");            // 03C6
		addEntity("chi", 967, "?", "x");            // 03C7
		addEntity("psi", 968, "?", "?");            // 03C8
		addEntity("omega", 969, "?", "o");          // 03C9
		addEntity("thetasym", 977, "?", "?");       // 03D1
		addEntity("upsih", 978, "?", "Y");          // 03D2
		addEntity("piv", 982, "?", "W");            // 03D6
		addEntity("ayin", 1506, "a", "a");            // 00-1
		addEntity("ng", 1709, "?", "ng");           // 06AD
		addEntity("aleph", 2135, "a", "a");           // 00-1
		addAdditionalEntityName("alephsym", 2135);
		addEntity("quesnodo", 4800, "?", "?");      // 12C0
		addEntity("Dlowdot", 7692, "?", "D");       // 1E0C
		addEntity("dlowdot", 7693, "?", "d");       // 1E0D
		addEntity("Dlowmacr", 7694, "?", "D");      // 1E0E
		addEntity("dlowmacr", 7695, "?", "d");      // 1E0F
		addEntity("Hlowdot", 7716, "?", "H");       // 1E24
		addEntity("hlowdot", 7717, "?", "h");       // 1E25
		addEntity("Hlowbrev", 7722, "?", "H");      // 1E2A
		addEntity("hlowbrev", 7723, "?", "h");      // 1E2B
		addEntity("Klowdot", 7730, "?", "K");       // 1E32
		addEntity("klowdot", 7731, "?", "k");       // 1E33
		addEntity("Llowdot", 7734, "?", "L");       // 1E36
		addEntity("llowdot", 7735, "?", "l");       // 1E37
		addEntity("Macute", 7742, "?", "M");        // 1E3E
		addEntity("macute", 7743, "?", "m");        // 1E3F
		addEntity("Mdot", 7744, "?", "M");          // 1E40
		addEntity("mdot", 7745, "?", "m");          // 1E41
		addEntity("Mlowdot", 7746, "?", "M");       // 1E42
		addEntity("mlowdot", 7747, "?", "m");       // 1E43
		addEntity("Ndot", 7748, "?", "N");          // 1E44
		addEntity("ndot", 7749, "?", "n");          // 1E45
		addEntity("Nlowdot", 7750, "?", "N");       // 1E46
		addEntity("nlowdot", 7751, "?", "n");       // 1E47
		addEntity("Rlowdot", 7770, "?", "R");       // 1E5A
		addEntity("rlowdot", 7771, "?", "r");       // 1E5B
		addEntity("Rlowmacr", 7774, "?", "R");      // 1E5E
		addEntity("rlowmacr", 7775, "?", "r");      // 1E5F
		addEntity("Slowdot", 7778, "?", "S");       // 1E62
		addEntity("slowdot", 7779, "?", "s");       // 1E63
		addEntity("Tlowdot", 7788, "?", "T");       // 1E6C
		addEntity("tlowdot", 7789, "?", "t");       // 1E6D
		addEntity("Tlowmacr", 7790, "?", "T");      // 1E6E
		addEntity("tlowmacr", 7791, "?", "t");      // 1E6F
		addEntity("Zlowdot", 7826, "?", "Z");       // 1E92
		addEntity("zlowdot", 7827, "?", "z");       // 1E93
		addEntity("Etilde", 7868, "?", "E");        // 1EBC
		addEntity("etilde", 7869, "?", "e");        // 1EBD
		addEntity("Eacuteci", 7870, "?", "E");      // 1EBE
		addEntity("eacuteci", 7871, "?", "e");      // 1EBF
		addEntity("Ecirclow", 7878, "?", "E");      // 1EC6
		addEntity("ecirclow", 7879, "?", "e");      // 1EC7
		addEntity("Olowdot", 7884, "?", "O");       // 1ECC
		addEntity("olowdot", 7885, "?", "o");       // 1ECD
		addEntity("agrgrave", 8048, "?", "a");      // 1F70
		addEntity("ensp", 8194, "?", "?");          // 2002
		addEntity("emsp", 8195, "?", "?");          // 2003
		addEntity("thinsp", 8201, "?", "?");        // 2009
		addEntity("zwnj", 8204, "?", "?");          // 200C
		addEntity("zwj", 8205, "?", "?");           // 200D
		addEntity("lrm", 8206, "?", "?");           // 200E
		addEntity("rlm", 8207, "?", "?");           // 200F
		addEntity("ndash", 8211, "–", "?");         // 2013
		addEntity("mdash", 8212, "—", "?");         // 2014
		addEntity("lsquo", 8216, "‘", "?");         // 2018
		addEntity("rsquo", 8217, "’", "?");         // 2019
		addEntity("sbquo", 8218, "‚", "?");         // 201A
		addEntity("ldquo", 8220, "“", "?");         // 201C
		addEntity("rdquo", 8221, "�?", "?");         // 201D
		addEntity("bdquo", 8222, "„", "?");         // 201E
		addEntity("dagger", 8224, "†", "?");        // 2020
		addEntity("Dagger", 8225, "‡", "?");        // 2021
		addEntity("bull", 8226, "•", "?");          // 2022
		addEntity("hellip", 8230, "…", "?");        // 2026
		addEntity("permil", 8240, "‰", "?");        // 2030
		addEntity("prime", 8242, "?", "?");         // 2032
		addEntity("Prime", 8243, "?", "?");         // 2033
		addEntity("lsaquo", 8249, "‹", "?");        // 2039
		addEntity("rsaquo", 8250, "›", "?");        // 203A
		addEntity("oline", 8254, "?", "?");         // 203E
		addEntity("frasl", 8260, "?", "?");         // 2044
		addEntity("nsmall", 8319, "?", "n");        // 207F
		addEntity("0sub", 8320, "?", "0");          // 2080
		addEntity("1sub", 8321, "?", "1");          // 2081
		addEntity("2sub", 8322, "?", "2");          // 2082
		addEntity("3sub", 8323, "?", "3");          // 2083
		addEntity("4sub", 8324, "?", "4");          // 2084
		addEntity("5sub", 8325, "?", "5");          // 2085
		addEntity("6sub", 8326, "?", "6");          // 2086
		addEntity("7sub", 8327, "?", "7");          // 2087
		addEntity("8sub", 8328, "?", "8");          // 2088
		addEntity("9sub", 8329, "?", "9");          // 2089
		addEntity("euro", 8364, "€", "E");          // 20AC
		addEntity("image", 8465, "?", "I");         // 2111
		addEntity("weierp", 8472, "?", "?");        // 2118
		addEntity("real", 8476, "?", "?");          // 211C
		addEntity("trade", 8482, "™", "TM");        // 2122
		addEntity("alefsym", 8501, "?", "?");       // 2135
		addEntity("larr", 8592, "?", "?");          // 2190
		addEntity("uarr", 8593, "?", "?");          // 2191
		addEntity("rarr", 8594, "?", "?");          // 2192
		addEntity("darr", 8595, "?", "?");          // 2193
		addEntity("harr", 8596, "?", "?");          // 2194
		addEntity("crarr", 8629, "?", "?");         // 21B5
		addEntity("lArr", 8656, "?", "?");          // 21D0
		addEntity("uArr", 8657, "?", "?");          // 21D1
		addEntity("rArr", 8658, "?", "?");          // 21D2
		addEntity("dArr", 8659, "?", "?");          // 21D3
		addEntity("hArr", 8660, "?", "?");          // 21D4
		addEntity("forall", 8704, "?", "?");        // 2200
		addEntity("part", 8706, "?", "?");          // 2202
		addEntity("exist", 8707, "?", "?");         // 2203
		addEntity("empty", 8709, "?", "O");         // 2205
		addEntity("nabla", 8711, "?", "?");         // 2207
		addEntity("isin", 8712, "?", "E");          // 2208
		addEntity("notin", 8713, "?", "?");         // 2209
		addEntity("ni", 8715, "?", "?");            // 220B
		addEntity("prod", 8719, "?", "?");          // 220F
		addEntity("sum", 8721, "?", "?");           // 2211
		addEntity("minus", 8722, "?", "?");         // 2212
		addEntity("lowast", 8727, "?", "*");        // 2217
		addEntity("radic", 8730, "?", "?");         // 221A
		addEntity("prop", 8733, "?", "?");          // 221D
		addEntity("infin", 8734, "?", "?");         // 221E
		addEntity("ang", 8736, "?", "?");           // 2220
		addEntity("and", 8743, "?", "?");           // 2227
		addEntity("or", 8744, "?", "?");            // 2228
		addEntity("cap", 8745, "?", "?");           // 2229
		addEntity("cup", 8746, "?", "?");           // 222A
		addEntity("int", 8747, "?", "?");           // 222B
		addEntity("there4", 8756, "?", "?");        // 2234
		addEntity("sim", 8764, "?", "?");           // 223C
		addEntity("cong", 8773, "?", "?");          // 2245
		addEntity("asymp", 8776, "?", "?");         // 2248
		addEntity("ne", 8800, "?", "?");            // 2260
		addEntity("equiv", 8801, "?", "?");         // 2261
		addEntity("le", 8804, "?", "?");            // 2264
		addEntity("ge", 8805, "?", "?");            // 2265
		addEntity("sub", 8834, "?", "?");           // 2282
		addEntity("sup", 8835, "?", "?");           // 2283
		addEntity("nsub", 8836, "?", "?");          // 2284
		addEntity("sube", 8838, "?", "?");          // 2286
		addEntity("supe", 8839, "?", "?");          // 2287
		addEntity("oplus", 8853, "?", "O");         // 2295
		addEntity("otimes", 8855, "?", "?");        // 2297
		addEntity("perp", 8869, "?", "?");          // 22A5
		addEntity("sdot", 8901, "?", "?");          // 22C5
		addEntity("lceil", 8968, "?", "?");         // 2308
		addEntity("rceil", 8969, "?", "?");         // 2309
		addEntity("lfloor", 8970, "?", "?");        // 230A
		addEntity("rfloor", 8971, "?", "?");        // 230B
		addEntity("lang", 9001, "?", "?");          // 2329
		addEntity("rang", 9002, "?", "?");          // 232A
		addEntity("loz", 9674, "?", "?");           // 25CA
		addEntity("spades", 9824, "?", "?");        // 2660
		addEntity("clubs", 9827, "?", "?");         // 2663
		addEntity("hearts", 9829, "?", "?");        // 2665
		addEntity("diams", 9830, "?", "?");         // 2666

		//
		cleanSearchTextLists();
	}
	private static void addEntity(String name, int number, String symbol, String searchText) {
		addEntity(name, number, symbol, searchText, null);
	}
	private static void addEntity(String name, int number, String symbol, String searchText, String description) {
		// make sure it's the true symbol instead of what's shown in the java string above
		// because there's no easy way to verify that that symbol is correct, 
		// but the number will always be correct
		symbol = String.valueOf((char) number);
		Entity entity = new Entity(number, name, symbol, searchText, description);
		
		// validate it does't already exist
		if (number != -1) {
			Entity f = (Entity) numberMap.get(new Integer(number));
			if (f != null) {
				String m = "Can't register new Entity with same number: (" + entity + ") already exists as (" + f + ")";
				System.out.println(m);
				throw new IllegalArgumentException(m);
			}
		}
		
		nameMap.put(name, entity);
		numberMap.put(new Integer(number), entity);
		List list = (List) searchTextMap.get(searchText);
		if (list == null) {
			list = new ArrayList();
			searchTextMap.put(searchText, list);
		}
		list.add(entity);
	}
	private static void addAdditionalEntityName(String name, int number) {
		Object e = getEntityForNumber(number, null);
		nameMap.put(name, e);
	}
	private static void cleanSearchTextLists() {
		List keys = new ArrayList(searchTextMap.keySet());
		int len = keys.size();
		for (int i = 0; i < len; i++) {
			String key = (String) keys.get(i);
			List list = (List) searchTextMap.get(key);
			list = Collections.unmodifiableList(list);
			searchTextMap.put(key, list);
		}
	}
	private static abstract class EntityRenderer {
		abstract public String toString(Entity entity);
		public String toString(Integer num) {
			return null;
		}
	}
	static final HTMLNumericalEntityRenderer HTMLNumericalEntityRenderer = new HTMLNumericalEntityRenderer();
	private static class HTMLNumericalEntityRenderer extends EntityRenderer {
		public String toString(Entity entity) {
			return "&#" + entity.getNumber() + ";";
		}
		public String toString(Integer num) {
			return "&#" + num + ";";
		}
	}
	private static final HTMLPrettyEntityRenderer HTMLPrettyEntityRenderer = new HTMLPrettyEntityRenderer();
	private static class HTMLPrettyEntityRenderer extends EntityRenderer {
		private EntityHtmlRenderInformation EntityHtmlRenderInformation = new EntityHtmlRenderInformation();
		public String toString(Entity entity) {
			if (EntityHtmlRenderInformation.isNameRequired(entity)) {
				return HTMLNameEntityRenderer.toString(entity);
			} else if (EntityHtmlRenderInformation.isSearchTextRequired(entity)) { 
				return entity.getSearchText();
			} else {
				return HTMLNumericalEntityRenderer.toString(entity);
			}
		}
	}
	static final HTMLNameEntityRenderer HTMLNameEntityRenderer = new HTMLNameEntityRenderer();
	private static class HTMLNameEntityRenderer extends EntityRenderer {
		public String toString(Entity entity) {
			return "&" + entity.getName() + ";";
		}
	}
	private static final BracketsEntityRenderer BracketsEntityRenderer = new BracketsEntityRenderer();
	private static class BracketsEntityRenderer extends EntityRenderer {
		public String toString(Entity entity) {
			if (entity.isNumberUsed()) {
				return "[" + entity.getNumber() + "]";
			} else {
				return "[" + entity.getName() + "]";
			}
		}
		@Override
		public String toString(Integer num) {
			return "[" + num + "]";
		}
	}
	private static final SearchTextEntityRenderer SearchTextEntityRenderer = new SearchTextEntityRenderer();
	private static class SearchTextEntityRenderer extends EntityRenderer {
		public String toString(Entity entity) {
			return entity.getSearchText();
		}
	}
	private static final SymbolEntityRenderer SymbolEntityRenderer = new SymbolEntityRenderer();
	private static class SymbolEntityRenderer extends EntityRenderer {
		public String toString(Entity entity) {
			if (entity.getNumber() == -1) {
				return entity.getSearchText();
			} else {
				return entity.getSymbol();
			}
		}
		@Override
		public String toString(Integer num) {
			return String.valueOf((char) num.intValue());
		}
	}
}