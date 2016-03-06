package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Rank;
import com.robestone.species.SpeciesService;

public class WikiSpeciesParser {

	public static final String OKINA = String.valueOf((char) 0x02BB);

	private Pattern rankPattern = Pattern.compile("(?:\\s+|<dd>|<p>|<li>)†?" +
			getRanksPatternPart(false) +
			"[\\s:†]*(?:<i>|<b>|\\?)*[\\s†]*(?:<span class=\"subfamily\">)?\"?<strong class=\"selflink\">");
	private Pattern extinctPattern = Pattern.compile(":([\\s†]*)?(?:<i>|<b>)*([\\s†]*)<strong class=\"selflink\">");
	private Pattern commonNamePattern = Pattern.compile("<b>English:</b>(?:</span>)?\\s*(.*?)\\s*(?:</div>|<br />)");
	private Pattern commonNamePattern2 = Pattern.compile("<li>en:\\s*(.*?)</li>");
//	private Pattern depictedPattern = Pattern.compile("<div class=\"thumbcaption\">\\s*<div class=\"magnify\">\\s*<a href=\"/wiki/File:.*?\" class=\"internal\" title=\"Enlarge\">\\s*<img src=\".*?\" width=\".*?\" height=\".*?\" alt=\"\" /></a></div>\\s*<i><a href=\"/wiki/.*?\" title=\"(.*?)\">");
	private Pattern depictedPattern = Pattern.compile("title=\"Enlarge\">\\s*<img src=\".*?\" width=\".*?\" height=\".*?\" alt=\"\" /></a></div>\\s*<a href=\"/w.*?/.*?\" (?:class=\".*?\"\\s*)?title=\".*?\">(.*?)<");
//	                                                                                                                                                <a href="/w/index.php?title=Onniella&amp;action=edit&amp;redlink=1" class="new" title="Onniella (page does not exist)">Onniella</a></i></div>
	private Pattern imageLinkPattern = Pattern.compile("//upload.wikimedia.org/wikipedia/commons/(.*?)\"");
	
	private static String getRanksPatternPart(boolean nonCapture) {
		StringBuilder buf = new StringBuilder();
		Rank[] ranks = Rank.values();
		for (Rank rank: ranks) {
			for (String name: rank.getNames()) {
				name = getEscapedName(name);
				if (buf.length() == 0) {
					if (nonCapture) {
						buf.append("(?:");
					} else {
						buf.append("(");
					}
				} else {
					buf.append("|");
				}
				buf.append(name);
			}
		}
		buf.append(")");
		return buf.toString();
	}
	private static String getEscapedName(String t) {
		t = t.replaceAll("\\(", "\\\\(");
		t = t.replaceAll("\\)", "\\\\)");
		t = t.replaceAll("\\?", "\\\\?");
		t = t.replaceAll("\\.", "\\\\.");
		return t;
	}
	private String getSmallerPage(String text) {
		int pos = text.indexOf("<div class=\"printfooter\">");
		if (pos > 0) {
			text = new String(text.substring(0, pos));
		}
		pos = text.indexOf("</head>");
		if (pos > 0) {
			text = text.substring(pos);
		}
		return text;
	}
	public static String cleanPage(String text) {
		// not sure why I need to do this, but...
		text = StringUtils.replace(text, "\n", " ");
		text = StringUtils.replace(text, "\r", " ");
		text = StringUtils.replace(text, "&#160;", " ");
		text = StringUtils.replace(text, "{{okina}}", OKINA);
		
		while (true) {
			int len = text.length();
			text = StringUtils.replace(text, "  ", " ");
			if (text.length() == len) {
				break;
			}
		}
		
		return text;
	}
	public CompleteEntry parse(String name, String text) {
		CompleteEntry entry = parse(name, name, text, true);
		
		// incertae sedis
		if (entry == null) {
			String newName = getNameNoIncertaeSedis(name);
			if (newName != null) {
				// in this section, handle these cases
				// if the latin name is Sordariales incertae sedis, we check for
				//	  Sordariales
				//    Incertae sedis
				// TODO there might be other patterns
				entry = parse(name, INCERTAE_SEDIS, text, true);
				if (entry == null) {
					entry = parse(name, INCERTAE_SEDIS.toLowerCase(), text, true);
				}
				if (entry == null) {
					entry = parse(name, newName, text, true);
				}
			}
		}
		
		// cases like Centropogon (Campanulaceae)
		if (entry == null) {
			String[] names = getNamesNoParens(name);
			if (names != null) {
				entry = parse(name, names[0], text, true);
				// for now, don't check the name in parens until we have an actual test case for it
//				if (entry == null) {
//					entry = parse(name, names[1], text, true);
//				}
			}
		}
		
		// handle abbreviated names
		if (entry == null) {
			List<String> newNames = getLatinAbbreviations(name);
			if (newNames != null) {
				for (String newName: newNames) {
					entry = parse(name, newName, text, true);
					if (entry != null) {
						break;
					}
				}
			}
		}
		
		return entry;
	}
	private String[] getNamesNoParens(String name) {
		int pos = name.indexOf("(");
		if (pos < 0) {
			return null;
		}
		String left = name.substring(0, pos - 1).trim();
		String right = name.substring(pos + 1);
		right = StringUtils.removeEnd(right, ")");
		return new String[] {left, right};
	}
	private String getRank(String latinName, String text) {
		String rank = getGroup(rankPattern, text, 1);
		if (rank == null) {
			String ename = getEscapedName(latinName);
			Pattern rankPattern2 = Pattern.compile(getRanksPatternPart(false) + "[ :†]+(?:<i>)?(?:<b>)?" + ename);
			rank = getGroup(rankPattern2, text, 1);
		}
		return rank;
	}
	private static final String INCERTAE_SEDIS = "Incertae sedis";
	private String getNameNoIncertaeSedis(String latinName) {
		if (latinName.equalsIgnoreCase(INCERTAE_SEDIS)) {
			return null;
		}
		int pos = latinName.toLowerCase().indexOf(INCERTAE_SEDIS.toLowerCase());
		if (pos == 0) {
			return latinName.substring(INCERTAE_SEDIS.length()).trim();
		} else if (pos > 0) {
			return latinName.substring(0, pos - 1).trim();
		} else {
			return null;
		}
	}
	public CompleteEntry parse(String name, String text, boolean checkVernacularParser) {
		return parse(name, name, text, checkVernacularParser);
	}
	public CompleteEntry parse(String pageNameLatin, String selfLinkName, String text, boolean checkVernacularParser) {
		String fullText = text;
		text = preProcessRedirectSelfLinks(text);
		text = preProcessAbbreviations(text);
		text = preProcessCleanOther(text);
		text = getSmallerPage(text);
		text = getSimplifiedPage(text);
		text = cleanPage(text);
		
		selfLinkName = StringUtils.replace(selfLinkName, "_", " ");
	
		// took the self link check out because of C. latrans
		String latinName = selfLinkName; //getGroup(selfLinkPattern, text, 1);
//		if (!name.equals(latinName)) {
//			return null;
//		}
		String rank = getRank(latinName, text);
		if (rank == null) {
			return null;
		}
		
		String extinct = getGroup(extinctPattern, text, 1);
		if (extinct == null) {
			extinct = getGroup(extinctPattern, text, 2);
		}
		extinct = StringUtils.trimToNull(extinct);
		
		CompleteEntry parent;
		if (SpeciesService.isTopLevelRank(latinName)) {
			parent = new CompleteEntry(null, null, "Tree of Life");
		} else {
			parent = getParent(text, latinName, rank);
			if (parent == null) {
				return null;
			}
		}
				
		String commonName = getGroup(commonNamePattern, text);
		if (commonName == null) {
			commonName = getGroup(commonNamePattern2, text);
		}
		if (commonName == null && checkVernacularParser) {
			commonName = VernacularCrawler.getSidebar(latinName, fullText);
		}
		if (commonName != null) {
			commonName = removeExtraFromCommonName(commonName);
			commonName = EntryUtilities.fixCommonName(commonName);
		}
		
		String imageLink = getImage(text);
		String depictedImage = getGroup(depictedPattern, text);
		
		CompleteEntry results = new CompleteEntry();
		results.setRank(Rank.valueOfWithAlternates(rank));
		results.setCommonName(commonName);
		results.setLatinName(pageNameLatin);
		results.setParent(parent);
		results.setImageLink(imageLink);
		results.setExtinct(extinct != null);
		results.setDepictedLatinName(depictedImage);
		
		cleanNameCharacters(results);
		
		return results;
	}
	private String removeExtraFromCommonName(String commonName) {
		int pos = commonName.indexOf('[');
		if (pos < 0) {
			return commonName;
		}
		return commonName.substring(0, pos);
	}
	public CompleteEntry getParent(String text, String latinName, String rank) {
//		text = getRankSection(text);
		CompleteEntry parent = null;
		Pattern parentPattern = getParentPattern(rank, latinName, true);
		String parentLatinName = getGroup(parentPattern, text, 1);
		if (parentLatinName != null) {
			parentLatinName = parentLatinName.replaceAll("_", " ");
			parent = new CompleteEntry(null, null, parentLatinName);
		}
		// wrapping the grandparent hunt in here means we only look recursively until
		// we find an existing parent.  For now, maybe don't worry about the fact
		// that this is recursive
		if (parent == null) {
			// see if it doesn't exist, and if we can get a grandparent while we're at it
			parentPattern = getParentPattern(rank, latinName, false);
			parentLatinName = getGroup(parentPattern, text, 1);
			// this check gets us out of some recursive issues
			if (parentLatinName != null && parentLatinName.equals(latinName)) {
				parentLatinName = null;
			}
			if (parentLatinName != null) {
				parent = new CompleteEntry(null, null, parentLatinName);
//				LogHelper.speciesLogger.info("red parent." + parentLatinName);
				// get the parent rank too
				String ename = getEscapedName(parentLatinName);
				Pattern parentRankPattern = Pattern.compile(
						getRanksPatternPart(false) +
						"(:| )\\s*<a href=\"/w/index.php\\?title=" + ename);
				String parentRankString = getGroup(parentRankPattern, text, 1);
//				LogHelper.speciesLogger.info("parent rank." + parentRankString);
				if (parentRankString != null) {
					Rank parentRank = Rank.valueOfWithAlternates(parentRankString);
					parent.setRank(parentRank);
					// Keep going as long as we find "red" parents
					CompleteEntry gparent = getParent(text, parentLatinName, parentRankString);
					parent.setParent(gparent);
				}
			}
		}
		cleanNameCharacters(parent);
		return parent;
	}
	/**
	 * Convert
	 * Some latin name
	 * to
	 * S. latin name
	 * 
	 * Also 
	 * Sceloporus grammicus microlepidotus
	 * to
	 * S. g. microlepidotus
	 * and
	 * S. grammicus microlepidotus
	 * 
	 * Also
	 * Sciacharis (Sciacharis) antennalis
	 * S. (S.) antennalis
	 */
	public static List<String> getLatinAbbreviations(String latin) {
		int pos = latin.indexOf(' ');
		if (pos < 0) {
			return null;
		}
		List<String> all = new ArrayList<String>();
		String left = latin.substring(0, pos);
		
		boolean parens = left.startsWith("(");
		if (parens) {
			left = left.substring(1, left.length() - 1);
		}
		
		String abbrev = left.charAt(0) + ".";
		if (parens) {
			abbrev = "(" + abbrev + ") ";
		} else {
			abbrev = abbrev + " ";
		}
		String right = latin.substring(pos + 1);
		all.add(abbrev + right);
		List<String> rightAbbreviations = getLatinAbbreviations(right);
		if (rightAbbreviations != null) {
			for (String rightAbbrev: rightAbbreviations) {
				all.add(abbrev + rightAbbrev);
			}
		}
		return all;
	}
		/*
		String[] split = latin.split(" ");
		if (split.length == 1) {
			return null;
		}
		List<String> names = new ArrayList<String>();
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < split.length; i++) {
			if (i > 0) {
				buf.append(" ");
			}
			if (i == split.length - 1) {
				buf.append(split[i]);
			} else {
				buf.append(split[i].charAt(0));
				buf.append('.');
			}
		}
		return buf.toString();
	}
		*/

	private Pattern getParentPattern(String childRank, String childLatin, boolean normal) {
		String w = "(?:[\\p{L}\\(\\)_\\. \\&%#0-9;'\\/\\-]|</?i>)+"; // &#160;
		String preName;
		String postName;
		if (normal) {
			preName = "/wiki/";
			postName = "\" ";
		} else {
			preName = "/w/index.php\\?title=";
			postName = "&amp;action=edit&amp;redlink=1\" class=\"new\" "; 
		}
		childRank = getEscapedName(childRank);
		childLatin = getEscapedName(childLatin);
		Pattern parentPattern = Pattern.compile(
				getRanksPatternPart(true) +
				"[:\\s†]*" +
				"[^:]*" +
//				"(?:<i>|<span class=\"\\p{L}+\">)*\\s*" +
//				"<a href=\""
				preName + "(" + w + ")" + postName +
				"[^:]*" +
//				"title=\"(" + w + ")\"(?: class=\"mw-redirect\")?>" +
//				"(?:<i>|<b>|\\s)*" +
//				"(" + w + ")" +
//				"(?:'|</i>|</a>|</b>|<br />|\\s|</p>|<dd>|</dd>|<dl>|</dl>|<p>|</span>)*" +
//				"(?:(?:<i>)?(?:\\p{L}+:\\s*not divided\\s*)(?:<i>)?(?:<br ?/?>\\s*))*" +
				"(?:" + getRanksPatternPart(true) + ":\\s*Unassigned<br\\s*/>\\s*)?" +
				childRank + 
				"(:|\\s)+" +
				"[^:]*" +
				//"(<.*?>)*\\s*('\\[\\?)*\\s*" +
				childLatin
				);
		return parentPattern;
	}
	public static String getGroup(String source, Pattern... patterns) {
		for (Pattern pattern: patterns) {
			String f = getGroup(pattern, source, 1);
			if (f != null) {
				return f;
			}
		}
		return null;
	}
	public static String getGroup(Pattern pattern, String source) {
		return getGroup(pattern, source, 1);
	}
	public static String getGroup(Pattern pattern, String source, int index) {
		Matcher matcher = pattern.matcher(source);
		if (matcher.find()) {
			return StringUtils.trimToNull(new String(matcher.group(index))); // memory leak fix?
		}
		return null;
	}
	
	private String getImage(String page) {
		Matcher matcher = imageLinkPattern.matcher(page);
		while (matcher.find()) {
			String imageLink = matcher.group(1);
			if (imageLink.indexOf("-logo.svg") > 0) {
				continue;
			} else if (imageLink.indexOf("Achtung.svg") > 0) {
				continue;
			} else if (imageLink.indexOf("Disambig") > 0) {
				continue;
			} else if (imageLink.indexOf("Help-") > 0) {
				continue;
			} else if (imageLink.indexOf("Keep_tidy") > 0) {
				continue;
			} else if (imageLink.indexOf("_apps_") > 0) {
				continue;
			} else if (imageLink.indexOf("&quot;") > 0) {
				// happens when there is a video with embedded controls
				continue;
			} else if (imageLink.toUpperCase().indexOf("POTY_") > 0) {
				continue;
			} else if (
					   imageLink.toUpperCase().endsWith(".OGV")
					|| imageLink.toUpperCase().endsWith(".OGG")
					) {
				// video or sound
				continue;
			} else if (imageLink.toUpperCase().endsWith(".SVG")) {
				// not sure how many of these there are, and I see that 
				// some wiki icons are sneaking in, with weird names,
				// and this might be the only way to stop them.
				continue;
//			} else if (imageLink.toUpperCase().endsWith(".GIF")) {
//				// TODO might change this later - but there are thumbnail issues
//				// with gifs, and technically wikimedia forbids gifs
//				continue;
			} else if (imageLink.contains("x, ")) {
				// indicates it is a "srcset" of more than one image
				continue;
			}
			return imageLink;
		}
		return null;
	}
	private String getSimplifiedPage(String p) {
		p = p.replaceAll("</?i>", "");
		p = p.replaceAll("</abbr>", "");
//		p = p.replaceAll("</?[a-z0-9]{1,6}( ?/)?>", "");
		// Ordo: not divided<br />
		p = p.replaceAll("\\p{L}+:\\s*not divided<br ?/?>", "");
		p = removeEmptyRanks(p);
		return p;
	}
	private String removeEmptyRanksPattern = getRanksPatternPart(false) + "\\s*:\\s*(\\-|none|not divided|unassigned)?\\s*<br\\s*/>";
	/**
	 * Familia: -<br />
	 * Familia:<br />
	 * Genus: unassigned<br />
	 * Subgenus: <i>none</i><br />
	 * --- but I can ignore the <i> because that's already been cleaned
	 */
	private String removeEmptyRanks(String p) {
		p = p.replaceAll(removeEmptyRanksPattern, "");
		return p;
	}
	private void cleanNameCharacters(CompleteEntry e) {
		if (e == null) {
			return;
		}
		e.setLatinName(cleanCharacters(e.getLatinName()));
		e.setCommonName(cleanCharacters(e.getCommonName()));
	}
	public static String cleanCharacters(String c) {
		if (c != null) {
			c = EntryUtilities.urlDecode(c);
//			try {
//				c = new String(c.getBytes(), "UTF-8");
//				e.setCommonName(c);
//			} catch (UnsupportedEncodingException e1) {
//				throw new RuntimeException(e1);
//			}
			
			// don't do this anymore - using EntityMapper at the dao layer.
//			for (int i = 0; i < c.length(); i++) {
////				LogHelper.speciesLogger.info(c + " => " + c.charAt(i) + " | " + ((int) c.charAt(i)));
//				int ch = c.charAt(i);
//				if (ch > 255) {
//					LogHelper.speciesLogger.info(c + "(" + i + ") => " + c.charAt(i) + " | " + ((int) c.charAt(i)));
//					c = StringUtils.replaceChars(c, (char) ch, '_');
//				}
//			}
		}
		return c;
	}
	/**
	 * I can only do it in very specific cases, because otherwise it might be the parent, etc.
	 * <i><a href="/wiki/X_Y_Z" title="X Y Z" class="mw-redirect">X Y Z</a></i></p>
	 * replace with
	 * <i><strong class="selflink">X Y Z</strong></i><br />
	 */
	private static final Pattern redirectSelfLinksPattern = Pattern.compile("<i><a href=\"/wiki/(.*?)\" title=\"(.*?)\" class=\"mw-redirect\">(.*?)</a></i></p>");
	public static String preProcessRedirectSelfLinks(String page) {
		String fixedPage = page;
		Matcher m = redirectSelfLinksPattern.matcher(page);
		while (m.find()) {
			String toReplace = m.group();
			String replaceWith = "<i><strong class=\"selflink\">" + m.group(2) + "</strong></i><br />";
			fixedPage = fixedPage.replace(toReplace, replaceWith);
		}
		return fixedPage;
	}
	/**
	 * <i><strong class="selflink"><abbr title="Lycaena">L.</abbr>&#160; tityrus</strong></i><br />
	 * replace with
	 * <i><strong class="selflink">L.&#160; tityrus</strong></i><br />
	 */
	private static final Pattern preprocessAbbreviationsPattern = Pattern.compile("<abbr title=\".*?\">(.*?)</abbr>");
	private static String preProcessAbbreviations(String page) {
		String fixedPage = page;
		Matcher m = preprocessAbbreviationsPattern.matcher(page);
		while (m.find()) {
			String toReplace = m.group();
			String replaceWith = m.group(1);
			fixedPage = fixedPage.replace(toReplace, replaceWith);
		}
		return fixedPage;
	}
	private static String preProcessCleanOther(String page) {
		String[] others = {
				"(nomen dubium)", // just clean these out until I have a better strategy
				"(tentative)", // same thing - don't know what else to do for now
		};
		for (String other: others) {
			page = StringUtils.replace(page, other, "");
		}
		return page;
	}
}
