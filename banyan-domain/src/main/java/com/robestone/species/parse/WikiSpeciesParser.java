package com.robestone.species.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Rank;
import com.robestone.species.SpeciesService;

public class WikiSpeciesParser {

	public static final String OKINA = String.valueOf((char) 0x02BB);

//	private Pattern redirectedPattern = Pattern.compile("\\(Redirected from <a href=");
	private Pattern rankPattern = Pattern.compile("(?:\\s+|(?:<dd>)|(?:<p>))" +
			getRanksPatternPart(false) +
			"[\\s:�]*(?:<i>|<b>|\\?)*[\\s�]*(?:<span class=\"subfamily\">)?\"?<strong class=\"selflink\">");
	private Pattern extinctPattern = Pattern.compile(":([\\s�]*)?(?:<i>|<b>)*([\\s�]*)<strong class=\"selflink\">");
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
	private static String getEscapedName(String name) {
		name = name.replace("(", "\\(");
		name = name.replace(")", "\\)");
		name = name.replace(".", "\\.");
		return name;
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
		return text;
	}
	public CompleteEntry parse(String name, String text) {
		return parse(name, text, true);
	}
	public CompleteEntry parse(String name, String text, boolean checkVernacularParser) {
		String fullText = text;
		text = getSmallerPage(text);
		text = getSimplifiedPage(text);
		text = cleanPage(text);
		
		name = StringUtils.replace(name, "_", " ");
	
		// took the self link check out because of C. latrans
		String latinName = name; //getGroup(selfLinkPattern, text, 1);
//		if (!name.equals(latinName)) {
//			return null;
//		}
		String rank = getGroup(rankPattern, text, 1);
		if (rank == null) {
			String ename = getEscapedName(latinName);
			Pattern rankPattern2 = Pattern.compile(getRanksPatternPart(false) + "[ :]+(?:<i>)?(?:<b>)?" + ename);
			rank = getGroup(rankPattern2, text, 1);
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
		results.setLatinName(latinName);
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
	private CompleteEntry getParent(String text, String latinName, String rank) {
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
//				System.out.println("red parent." + parentLatinName);
				// get the parent rank too
				String ename = getEscapedName(parentLatinName);
				Pattern parentRankPattern = Pattern.compile(
						getRanksPatternPart(false) +
						"(:| )\\s*<a href=\"/w/index.php\\?title=" + ename);
				String parentRankString = getGroup(parentRankPattern, text, 1);
//				System.out.println("parent rank." + parentRankString);
				if (parentRankString != null) {
					Rank parentRank = Rank.valueOfWithAlternates(parentRankString);
					parent.setRank(parentRank);
					// Keep going as long as we find "red" parents
					CompleteEntry gparent = getParent(text, parentLatinName, parentRankString);
					parent.setParent(gparent);
				}
			}
		}
		if (parent == null) {
			int pos = latinName.indexOf("incertae sedis");
			if (pos > 1) {
				latinName = latinName.substring(pos);
				parent = getParent(text, latinName, rank);
			}
		}
		cleanNameCharacters(parent);
		return parent;
	}
	private String escapeRegEx(String t) {
		t = t.replaceAll("\\(", "\\\\(");
		t = t.replaceAll("\\)", "\\\\)");
		return t;
	}
	private Pattern getParentPattern(String childRank, String childLatin, boolean normal) {
		String w = "(?:[\\p{L}\\(\\)_\\. \\&%#0-9;']|</?i>)+"; // &#160;
		String preName;
		String postName;
		if (normal) {
			preName = "/wiki/";
			postName = "\" ";
		} else {
			preName = "/w/index.php\\?title=";
			postName = "&amp;action=edit&amp;redlink=1\" class=\"new\" "; 
		}
		Pattern parentPattern = Pattern.compile(
				getRanksPatternPart(true) +
				"[:\\s�]*" +
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
				childRank + 
				"(:|\\s)+" +
				"[^:]*" +
				//"(<.*?>)*\\s*('\\[\\?)*\\s*" +
				escapeRegEx(childLatin)
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
			} else if (imageLink.toUpperCase().indexOf("POTY_") > 0) {
				continue;
			} else if (imageLink.toUpperCase().endsWith(".SVG")) {
				// not sure how many of these there are, and I see that 
				// some wiki icons are sneaking in, with weird names,
				// and this might be the only way to stop them.
				continue;
			} else if (imageLink.toUpperCase().endsWith(".GIF")) {
				// TODO might change this later - but there are thumbnail issues
				// with gifs, and technically wikimedia forbids gifs
				continue;
			}
			return imageLink;
		}
		return null;
	}
	private String getSimplifiedPage(String p) {
		p = p.replaceAll("</?i>", "");
//		p = p.replaceAll("</?[a-z0-9]{1,6}( ?/)?>", "");
		// Ordo: not divided<br />
		p = p.replaceAll("\\p{L}+:\\s*not divided<br ?/?>", "");
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
////				System.out.println(c + " => " + c.charAt(i) + " | " + ((int) c.charAt(i)));
//				int ch = c.charAt(i);
//				if (ch > 255) {
//					System.out.println(c + "(" + i + ") => " + c.charAt(i) + " | " + ((int) c.charAt(i)));
//					c = StringUtils.replaceChars(c, (char) ch, '_');
//				}
//			}
		}
		return c;
	}
}
