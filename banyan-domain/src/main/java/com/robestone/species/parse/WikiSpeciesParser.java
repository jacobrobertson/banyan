package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Rank;

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
	/*
	// GOOD IMAGE
	// <div class="thumbinner" style="width:252px;"><a href="/wiki/File:Moeritherium.jpg" class="image">
	 * <img alt="" src="//upload.wikimedia.org/wikipedia/commons/thumb/9/97/Moeritherium.jpg/250px-Moeritherium.jpg" 
	 * width="250" height="157" 
	 * class="thumbimage" 
	 * srcset="//upload.wikimedia.org/wikipedia/commons/thumb/9/97/Moeritherium.jpg/375px-Moeritherium.jpg 1.5x, 
	 * //upload.wikimedia.org/wikipedia/commons/thumb/9/97/Moeritherium.jpg/500px-Moeritherium.jpg 2x" 
	 * data-file-width="672" data-file-height="421" /></a>
	// STUB IMAGE - TO REMOVE
	// <td><a href="/wiki/File:Eristalis_tenax_auf_Tragopogon_pratensis_01.JPG" class="image">
	 * <img alt="Eristalis tenax auf Tragopogon pratensis 01.JPG" 
	 * src="//upload.wikimedia.org/wikipedia/commons/thumb/3/36/Eristalis_tenax_auf_Tragopogon_pratensis_01.JPG/120px-Eristalis_tenax_auf_Tragopogon_pratensis_01.JPG" 
	 * width="120" height="90" 
	 * srcset="//upload.wikimedia.org/wikipedia/commons/thumb/3/36/Eristalis_tenax_auf_Tragopogon_pratensis_01.JPG/180px-Eristalis_tenax_auf_Tragopogon_pratensis_01.JPG 1.5x, 
	 * //upload.wikimedia.org/wikipedia/commons/thumb/3/36/Eristalis_tenax_auf_Tragopogon_pratensis_01.JPG/240px-Eristalis_tenax_auf_Tragopogon_pratensis_01.JPG 2x" 
	 * data-file-width="4208" data-file-height="3156" />
	 * </a></td>
	 * 
	 */
	private Pattern imageLinkPattern = Pattern.compile("<img.*?//upload.wikimedia.org/wikipedia/commons/(.*?)\".*?>");
	private String[] imageLinkStubHints = {
			"width=\"120\" height=\"90\"",
			"Eristalis_tenax_auf_Tragopogon_pratensis", // I can include this because that image isn't used for that species
	};
	
	private static String ranksPatternPartCapture = doGetRanksPatternPart(false);
	private static String ranksPatternPartNonCapture = doGetRanksPatternPart(true);
	public static String getRanksPatternPart(boolean nonCapture) {
		if (nonCapture) {
			return ranksPatternPartNonCapture;
		} else {
			return ranksPatternPartCapture;
		}
	}
	private static String doGetRanksPatternPart(boolean nonCapture) {
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
	public static String getEscapedName(String t) {
		t = t.replaceAll("\\(", "\\\\(");
		t = t.replaceAll("\\)", "\\\\)");
		t = t.replaceAll("\\?", "\\\\?");
		t = t.replaceAll("\\.", "\\\\.");
		t = t.replaceAll("\\+", "\\\\+");
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
	private boolean isEntryOkay(CompleteEntry e) {
		if (e == null) {
			return false;
		}
		if (e.getRank() == null || e.getRank() == Rank.Error) {
			return false;
		}
		if (e.getParentLatinName() == null && e.getParent() == null) {
			return false;
		}
		return true;
	}
	public CompleteEntry parse(String name, String text) {
		CompleteEntry entry = parse(name, name, text, true);
		
		// incertae sedis
		if (!isEntryOkay(entry)) {
			String newName = getNameNoIncertaeSedis(name);
			if (newName != null) {
				// in this section, handle these cases
				// if the latin name is Sordariales incertae sedis, we check for
				//	  Sordariales
				//    Incertae sedis
				// TODO there might be other patterns
				entry = parse(name, INCERTAE_SEDIS, text, true);
				if (!isEntryOkay(entry)) {
					entry = parse(name, INCERTAE_SEDIS.toLowerCase(), text, true);
				}
				if (!isEntryOkay(entry)) {
					entry = parse(name, newName, text, true);
				}
			}
		}
		
		// cases like Centropogon (Campanulaceae)
		if (!isEntryOkay(entry)) {
			String[] names = getNamesNoParens(name);
			if (names != null) {
				// Centropogon (Campanulaceae) => Centropogon
				entry = parse(name, names[0], text, true);
				// Paederus (Anomalopaederus) => Anomalopaederus 
				if (!isEntryOkay(entry)) {
					entry = parse(name, names[1], text, true);
				}
			}
		}
		
		// pages like Chordata Craniata - which should have been named Craniata (Chordata)
		if (!isEntryOkay(entry) && name.indexOf(' ') > 0) {
			String[] split = name.split(" ");
			if (split.length == 2 && !split[0].equals(split[1])) {
				// try it both ways
				entry = parse(name, split[0], text, true);
				if (!isEntryOkay(entry)) {
					entry = parse(name, split[1], text, true);
				}
			}
		}
		
		// handle abbreviated names
		if (!isEntryOkay(entry)) {
			List<String> newNames = getLatinAbbreviations(name);
			if (newNames != null) {
				for (String newName: newNames) {
					entry = parse(name, newName, text, true);
					if (isEntryOkay(entry)) {
						break;
					}
				}
			}
		}

		// handle "Cossina Cossina"
		if (!isEntryOkay(entry) && name.indexOf(' ') > 0) {
			String[] split = name.split(" ");
			if (split.length == 2 && split[0].equals(split[1])) {
				entry = parse(name, split[0], text, true);
			}
		}
		
		// special hybrid "x" \u00d7
		if (!isEntryOkay(entry) && name.indexOf('\u00d7') >= 0) {
			String newName = name.replace("\u00d7", " x ");
			newName = newName.replace("  ", " ");
			entry = parse(name, newName, text, true);
		}

		// handle "Cohort Dictyoptera"
		if (!isEntryOkay(entry)) {
			String newName = removeRankFromFront(name);
			if (newName != null) {
				entry = parse(name, newName, text, true);
			}
		}
		
		// "Unassigned Calliptaminae"
		if (!isEntryOkay(entry) && name.startsWith("Unassigned ")) {
			entry = parse(name, "Unassigned", text, true);
		}
		
		// Aptinus pyranaeus => Aptinus (Aptinus) pyranaeus
		int pos;
		if (!isEntryOkay(entry) && (pos = name.indexOf(' ')) > 0) {
			String left = name.substring(0, pos);
			String right = name.substring(pos + 1);
			String newName = left + " (" + left + ") " + right;
			entry = parse(name, newName, text, true);
		}

		// Author names in species page name
		if (!isEntryOkay(entry)) {
			List<String[]> list = splitAuthorNameFromSpeciesPageName(name);
			for (String[] split: list) {
				entry = parse(name, split[0], text, true);
			}
		}
		
		// " ser. " - Sedum ser. Cepaea => Cepaea
		if (!isEntryOkay(entry)) {
			pos = name.indexOf(" ser. ");
			if (pos > 0) {
				String newName = name.substring(pos + 6);
				entry = parse(name, newName, text, true);
			}
		}

		// "Tilapia" busumana or Tilapia "busumana"
		if (!isEntryOkay(entry) && (pos = name.indexOf(' ')) > 0) {
			String left = name.substring(0, pos);
			String right = name.substring(pos + 1);
			entry = parse(name, "\"" + left + "\" " + right, text, true);
			if (!isEntryOkay(entry)) {
				entry = parse(name, left + " \"" + right + "\"", text, true);
			}
		}
		
		// Asclepias curassavica L. - the "L." is a special pattern
		if (!isEntryOkay(entry) && name.endsWith(" L.")) {
			String newName = name.substring(0, name.length() - 3);
			entry = parse(name, newName, text, true);
		}
		
		// Bombus (Psithyrus) citrinus > Bombus (Psithyrus) citrinus
		if (!isEntryOkay(entry) && (pos = name.indexOf('(')) > 0) {
			int pos2 = name.indexOf(')');
			if (pos2 > 0) {
				String left = name.substring(0, pos).trim();
				String right = name.substring(pos + 1).trim();
				if (right.length() > 0) {
					String newName = left + " " + right;
					entry = parse(name, newName, text, true);
				}
			}
		}
		
		return entry;
	}
	
	/**
	 * Tara Molina
	 * Zygomyia submarginata Harrison
	 * Xerophyllum Michx.
	 * Strychnos pungens Soler.
	 * Halothamnus Jaubert & Spach
	 */
	private static Pattern splitAuthorPattern = Pattern.compile(
			"[A-Z][a-z]+(-[A-Z][a-z]+)?\\.?" +
			// second name only if "x & y"
			"( & [A-Z][a-z]+(-[A-Z][a-z]+)?\\.?)?"
			);
	public static List<String[]> splitAuthorNameFromSpeciesPageName(String name) {
		List<String[]> list = new ArrayList<String[]>();
		String[] result = null;
		int pos;
		if ((pos = name.lastIndexOf(' ')) > 0) {
			result = doSplitAuthorNameFromSpeciesPageName(name, pos);
			if (result != null) {
				list.add(result);
			}
		}
		if ((pos = name.indexOf(' ')) > 0) {
			result = doSplitAuthorNameFromSpeciesPageName(name, pos);
			if (result != null) {
				list.add(result);
			}
		}
		return list;
	}
	private static String[] doSplitAuthorNameFromSpeciesPageName(String name, int pos) {
		String left = name.substring(0, pos);
		String right = name.substring(pos + 1);
		if (splitAuthorPattern.matcher(right).matches()) {
			return new String[] {left, right};
		}
		return null;
	}
	
	private String removeRankFromFront(String name) {
		int pos = name.indexOf(' ');
		if (pos > 0) {
			String left = name.substring(0, pos);
			try {
				Rank.valueOfWithAlternates(left);
				return name.substring(pos + 1);
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		return null;
		
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
		text = preProcessRedirectSelfLinks(selfLinkName, text);
		text = preProcessAbbreviations(text);
		text = preProcessCleanOther(text);
		text = getSmallerPage(text);
		text = getSimplifiedPage(text);
		text = preProcessEmptyRanks(text);
		text = preProcessNumberedRanks(text);
		text = cleanPage(text);
		text = preProcessDoubleRanks(text);
		text = VirusUtilities.preProcessVirusGroups(pageNameLatin, text);
		
		selfLinkName = StringUtils.replace(selfLinkName, "_", " ");
	
		String latinName = selfLinkName;
		String rank = getRank(latinName, text);

		String extinct = getGroup(extinctPattern, text, 1);
		if (extinct == null) {
			extinct = getGroup(extinctPattern, text, 2);
		}
		extinct = StringUtils.trimToNull(extinct);
		
		CompleteEntry parent;
		// I'm removing this logic, and instead will use the WikiSpeciesTreeFixer
//		if (SpeciesService.isTopLevelRank(latinName)) {
//			parent = SpeciesService.TREE_OF_LIFE_ENTRY;
//		} else 
		if (rank != null) {
			parent = getParent(text, latinName, rank);
		} else {
			parent = null;
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
		if (rank != null) {
			results.setRank(Rank.valueOfWithAlternates(rank));
		} else {
			results.setRank(Rank.Error);
		}
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
		if (parent != null && parent.getRank() == null) {
			parent.setRank(Rank.Error);
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
	 * 
	 * Mus musculus
	 * Mus (M.) musculus
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
		if (!parens) {
			all.add(left + " (" + abbrev.trim() + ") " + right);
		}
		return all;
	}
	public static Pattern getParentPattern(String childRank, String childLatin, boolean normal) {
		String w = "(?:[\\p{L}\\(\\)_\\. \\&%#0-9;'\\/\\-\\+\\:]|</?i>)+"; // &#160;
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
	private boolean isStubImage(String text) {
		for (String test: imageLinkStubHints) {
			if (text.contains(test)) {
				return true;
			}
		}
		return false;
	}
	private String getImage(String page) {
		Matcher matcher = imageLinkPattern.matcher(page);
		while (matcher.find()) {
			String match = matcher.group();
			if (isStubImage(match)) {
				continue;
			}
			String imageLink = matcher.group(1);
			if (imageLink.indexOf("-logo.") > 0) {
				continue;
			} else if (imageLink.indexOf("Achtung.svg") > 0) {
				continue;
			} else if (imageLink.indexOf("Disambig") > 0) {
				continue;
			} else if (imageLink.indexOf("Help-") > 0) {
				continue;
			} else if (imageLink.indexOf("Merge-") > 0) {
				continue;
			} else if (imageLink.indexOf("_important.") > 0) {
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
			} else if (imageLink.indexOf("Status_iucn") >= 0) {
				continue;
			} else if (imageLink.indexOf("-logo-") >= 0) {
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
		return p;
	}
	/**
	 * Cladus: Unidentata Episquamata</p>
	 * Familia: -<br />
	 * Familia:<br />
	 * Genus: unassigned<br />
	 * Subgenus: <i>none</i><br />
	 * --- but I can ignore the <i> because that's already been cleaned
	 */
	private String removeEmptyRanksPattern = getRanksPatternPart(false) + 
			"\\s*:\\s*(\\-|none|not divided|unassigned|Unidentata[a-zA-Z ]*)?\\s*(?:<br\\s*/>|</p>)";
	private String preProcessEmptyRanks(String p) {
		p = p.replaceAll(removeEmptyRanksPattern, "");
		return p;
	}
	
	/**
	 * Cladus (2): ...
	 */
	private static final Pattern preProcessNumberedRanksPattern = Pattern.compile(
			getRanksPatternPart(false) + "\\s*\\([0-9]+\\)\\s*:");
	private String preProcessNumberedRanks(String page) {
		String fixedPage = page;
		Matcher m = preProcessNumberedRanksPattern.matcher(page);
		while (m.find()) {
			String toReplace = m.group();
			String replaceWith = m.group(1) + ":";
			fixedPage = fixedPage.replace(toReplace, replaceWith);
		}
		return fixedPage;
	}
	private void cleanNameCharacters(CompleteEntry e) {
		if (e == null) {
			return;
		}
		e.setLatinName(cleanCharacters(e.getLatinName()));
		e.setCommonName(cleanCharacters(e.getCommonName()));
	}
	/**
	 * This was breaking the virus name that needs a + in the name "Group ... (+)"
	 */
	public static String cleanCharacters(String c) {
		if (c == null) {
			return null;
		}
		int pos;
		if ((pos = c.indexOf('+')) > 0) {
			String left = c.substring(0, pos);
			String right = c.substring(pos + 1);
			return cleanCharacters(left) + '+' + cleanCharacters(right);
		} else {
			c = EntryUtilities.urlDecode(c);
		}
		return c;
	}
	/**
	 * I can only do it in very specific cases, because otherwise it might be the parent, etc.
	 * <i><a href="/wiki/X_Y_Z" title="X Y Z" class="mw-redirect">X Y Z</a></i></p>
	 * replace with
	 * <i><strong class="selflink">X Y Z</strong></i><br />
	 */
	public static String preProcessRedirectSelfLinks(String name, String page) {
		String regexName = getEscapedName(name);
		Pattern pattern = Pattern.compile("<i><a href=\"/wiki/.*?\" title=\"" + regexName + "\" class=\"mw-redirect\">.*?</a></i>(?:</p>|<br ?/>)");
		String fixedPage = page;
		Matcher m = pattern.matcher(page);
		while (m.find()) {
			String toReplace = m.group();
			String replaceWith = "<i><strong class=\"selflink\">" + name + "</strong></i><br />";
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
	private static Pattern doubleRanksPattern = Pattern.compile(
			ranksPatternPartCapture + "\\s*/\\s*" + ranksPatternPartCapture + "\\s*:"
			);
	/**
	 * Some pages have ranks like "Megaclassis/Superclassis".
	 * I don't want to try and solve that in the regex.
	 */
	private static String preProcessDoubleRanks(String page) {
		String fixedPage = page;
		Matcher m = doubleRanksPattern.matcher(page);
		while (m.find()) {
			String toReplace = m.group();
			String replaceWith = m.group(1) + ":";
			fixedPage = fixedPage.replace(toReplace, replaceWith);
		}
		return fixedPage;
	}
}
