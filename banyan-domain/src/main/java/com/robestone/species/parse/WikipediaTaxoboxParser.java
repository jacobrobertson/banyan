package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.robestone.species.CommonNameSimilarityChecker;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LogHelper;
import com.robestone.species.Rank;

public class WikipediaTaxoboxParser {

	public static final String W = "\\w\\p{L}\\s\\.\\-_\\&%";
//	private Pattern taxoboxPattern = Pattern.compile("\\{\\{\\s*Taxobox(.*?)\\s*\\}\\}");
	private Pattern textBoxPattern = Pattern.compile(
			"<textarea.*?wpTextbox1.*?>"
			+ "(.*?)"
			+ "</textarea>"
			);
	private Pattern latinNamePattern0 = Pattern.compile("'*([\\p{L}\\s?]+)'*");
	private Pattern latinNamePattern2 = Pattern.compile("'''([\\p{L}\\s?]+)'''");
	private Pattern latinNamePattern = Pattern.compile("''" + latinNamePattern2 + "''");
	private Pattern latinNamePattern3 = Pattern.compile("''([\\p{L}\\s?]+)''");
	private Pattern latinNamePattern4 = Pattern.compile("''\\[\\[([\\p{L}\\s?]+)\\]\\]''");
	private Pattern commonNamePattern = Pattern.compile(
//			| name = Ghost slug&lt;br /&gt;''Selenochlamys ysbryda''
//			| name = Banana slug
			// Shiva's Sunbeam
			"((?:[" + W + "]+'?[" + W + "]+))"
//			"([" + W + "]+)"
			);
	private Pattern imagePattern = Pattern.compile(
			"(?:File:)?([" + W + "\\(\\),\\'–]+)"
			);
	// TODO - picks up wrong one on these
	// image_caption = ''[[Omphalotus nidiformis|O. nidiformis]]''<br> on ''[[Banksia serrata]]'' stump,<br> [[Picnic Point, New South Wales]]
	// image_caption = An adult ''[[Euborellia annulipes]]'', or ''Ringlegged earwig'', taken in Gunfire range, [[Kahoolawe]], 
	private Pattern imageSpeciesDepictedPattern = Pattern.compile(
//			| image_caption = ''[[Laevicaulis alte]]''
			"''\\[\\[([" + W + "]+)\\]\\]''"
			);
	
	private Pattern imageSpeciesDepictedPattern2 = Pattern.compile(
			"\\[\\[([" + W + "]+)\\]\\]"
			);
	private Pattern imageSpeciesDepictedPattern3 = Pattern.compile(
			"''([" + W + "]+)''"
			);
	private Pattern parentLatinNamePattern = Pattern.compile(
			"'*\\[*(?:[\\p{L}\\s]+\\|)?([\\p{L}\\s]+)\\]*'*"
	);

	// Rhinochimaeridae - | image = Harriotta raleighana (Narrownose chimaera).gif
	// Serinus scotops - Forest Canary (Serinus scotops) facing left, side view.jpg
	private String[] commonNameFromImageNamePatterns = {
			".*?LATIN_NAME\\s+\\(([" + W + "]+)\\).*",
			".*?([" + W + "]+)\\s+\\(LATIN_NAME\\).*",
	};
	
	private WikipediaCommonNamePatternParser commonNameParser = new WikipediaCommonNamePatternParser();

	private static String replaceHtmlEntities(String page) {
		page = StringUtils.replace(page, "&ndash;", "–");
		page = StringUtils.replace(page, "&amp;", "&");
		page = StringUtils.replace(page, "&lt;", "<");
		page = StringUtils.replace(page, "&gt;", ">");
		page = StringUtils.replace(page, "&quot;", "\"");
		
		return page;
	}
	
	public Taxobox parseHtmlPage(String originalLatinName, String page) {
		page = WikiSpeciesParser.cleanPage(page);
		page = replaceHtmlEntities(page);

		String edit = WikiSpeciesParser.getGroup(textBoxPattern, page);
		if (edit == null) {
			return null;
		}
		String taxoboxString = getTaxoboxString(edit);
		if (taxoboxString == null) {
			return null;
		}
		
		List<Line> lines = parseLines(taxoboxString);
		
		Taxobox box = new Taxobox();
		box.setCommonName(parseLineValue("name", lines, commonNamePattern));
		box.setImage(parseLineValue("image", lines, imagePattern));
		cleanImage(box);
		box.setLatinName(getLatinName(lines, originalLatinName));
		
		box.setImageSpeciesDepicted(getImageSpeciesDepicted(lines));
		fillParentLatinAndRanks(box, lines);
		
		if (box.getLatinName() == null) {
			box.setLatinName(originalLatinName);
		}
		ensureGoodCommonName(box);
		
		String commonName = box.getCommonName();
		if (commonName == null) {
			String nonTaxoPart = edit.substring(taxoboxString.length());
			commonName = commonNameParser.parse(box.getLatinName(), nonTaxoPart);
			if (commonName != null) {
				LogHelper.speciesLogger.info(">>>>> COMMON NAME PARSER: " + commonName);
			}
			box.setCommonName(commonName);
		}
		
		if (box.getImage() != null && box.getCommonName() == null) {
			box.setCommonName(getCommonNameFromImageName(box));
		}
		ensureGoodCommonName(box);
		
		return box;
	}
	private void cleanImage(Taxobox box) {
		String image = box.getImage();
		if (image != null) {
			if (image.toUpperCase().endsWith(".SVG")) {
				box.setImage(null);
			} else if (image.contains("Deleted")) {
				box.setImage(null);
			}
		}
	}
	private void ensureGoodCommonName(Taxobox box) {
		String cn = box.getCommonName();
		if (cn == null) {
			return;
		}
		if ("pagename".equals(cn)) {
			box.setCommonName(null);
			return;
		}
		if (cn.equalsIgnoreCase(box.getLatinName())) {
			box.setCommonName(null);
			return;
		}
		String cnc = EntryUtilities.getClean(cn, false);
		String lnc = EntryUtilities.getClean(box.getLatinName(), false);
		if (CommonNameSimilarityChecker.isCommonNameCleanBoring(cnc, lnc)) {
			box.setCommonName(null);
		}
	}
	public String getCommonNameFromImageName(Taxobox box) {
		for (String s: commonNameFromImageNamePatterns) {
			s = s.replace("LATIN_NAME", box.getLatinName());
			Pattern p = Pattern.compile(s);
			Matcher m = p.matcher(box.getImage());
			if (m.matches()) {
				String c = m.group(1);
				return c;
			}
		}
		return null;
	}

	private String parseLineValue(String key, List<Line> lines, Pattern pattern) {
		String value = getValue(key, lines);
		if (value == null) {
			return null;
		}
//		LogHelper.speciesLogger.info("parseLineValue." + key + "/" + value);
		String name = WikiSpeciesParser.getGroup(pattern, value);
		return name;
	}
	private String getTaxoboxString(String text) {
		String startToken = "{{Taxobox";
		int pos = text.indexOf(startToken);
		if (pos < 0) {
			return null;
		}
		pos += startToken.length();
		boolean done = false;
		int bpos = pos;
		while (!done) {
			int bposPre = text.indexOf("{{", bpos);
			bpos = text.indexOf("}}", bpos + 2);
			done = bposPre < 0 || bposPre > bpos;
		}
		return text.substring(pos, bpos);
	}
	private String getImageSpeciesDepicted(List<Line> lines) {
		String caption = getValue("image_description", lines);
		if (caption == null) {
			caption = getValue("image_caption", lines);
		}
		if (caption == null) {
			return null;
		}
		if (caption.startsWith("Illustration by")) {
			// this pattern never has the latin name in it
			return null;
		}
		String name = WikiSpeciesParser.getGroup(
				caption, imageSpeciesDepictedPattern3, imageSpeciesDepictedPattern, imageSpeciesDepictedPattern2);
		if (name == null) {
			return null;
		}
		if (name.startsWith("In ")) {
			return null;
		}
		if (isNonLatin(name)) {
			return null;
		}
		
		return name;
	}
	private String[] badImageNames = {"Forest", "Reserve", "Illustration"};
	private boolean isNonLatin(String caption) {
		for (int i = 0; i < badImageNames.length; i++) {
			if (caption.contains(badImageNames[i])) {
				return true;
			}
		}
		return false;
	}
	
	private String getLatinName(List<Line> lines, String originalLatinName) {
		String n = getLatinNameWithMatchExact(lines, originalLatinName);
		if (n != null) {
			return n;
		}
		n = getLatinNameWithMatchExact(lines, null);
		return n;
	}
	private String getLatinNameWithMatchExact(List<Line> lines, String matchExact) {
		String n = parseLineValue("binomial", lines, latinNamePattern0);
		if (n != null) {
			if (matchExact == null || matchExact.equals(n)) {
				return n;
			}
		}
		Pattern[] patterns = { latinNamePattern, latinNamePattern2, latinNamePattern3, latinNamePattern4 };
		for (Pattern pattern: patterns) {
			n = getLatinNameFromLines(lines, pattern, matchExact);
			if (n != null) {
				return n;
			}
		}
		return n;
	}		
	private String getLatinNameFromLines(List<Line> lines, Pattern pattern, String matchExact) {
		// do in reverse order - more likely to have parent also included like this
		for (int i = lines.size() - 1; i >= 0; i--) {
			Line line = lines.get(i);
			try {
				Rank.valueOfWithAlternates(line.getKey());
			} catch (Exception e) {
				// not a rank
				continue;
			}
			// if no value, can't match pattern
			if (line.getValue() == null) {
				continue;
			}
			String match = WikiSpeciesParser.getGroup(pattern, line.getValue());
			if (match != null) {
				if (matchExact == null || matchExact.equals(match)) {
					return match;
				}
			}
		}
		return null;
	}
	private void fillParentLatinAndRanks(Taxobox box, List<Line> lines) {
		// find the last rank listed
		int index = 0;
		int rankIndex = -1;
		Rank foundRank = null;
		for (Line line: lines) {
			// don't proceed once we get to certain types of things
			if (isLastLine(line.getKey())) {
				break;
			}
			String sRank = line.getKey();
			try {
				Rank maybeFoundRank = Rank.valueOfWithAlternates(sRank);
				if (!StringUtils.isEmpty(line.getValue())) {
					rankIndex = index;
					foundRank = maybeFoundRank;
				}
			} catch (Exception e) {
				// just swallow!
			}
			index++;
		}
		box.setRank(foundRank);
		// get the authority - could choose from species authority, etc
		String binomialRaw = getValue("binomial_authority", lines);
		if (binomialRaw == null && lines.size() > rankIndex + 1) {
			Line next = lines.get(rankIndex + 1);
			if (next.getKey().contains("_authority")) {
				binomialRaw = next.getValue();
			}
		}
		box.setBinomialAuthorityRaw(binomialRaw);
		// now, back up to parent
		index = rankIndex;
		while (index > 0) {
			index--;
			Line parentLine = lines.get(index);
			// it might be the parent's authority, etc...
			String sRank = parentLine.getKey();
			try {
				Rank.valueOfWithAlternates(sRank);
				String parentLatinName = WikiSpeciesParser.getGroup(parentLatinNamePattern, parentLine.getValue());
				box.setParentLatinName(parentLatinName);
				break;
			} catch (Exception e) {
				// in this case, we're not there yet...
			}
		}
	}
	private boolean isLastLine(String key) {
		// locate "end" line
		String[] tokens = {"_rank", "binomial"}; //, "_authority", "range_"
		for (String token: tokens) {
			if (key.indexOf(token) >= 0) {
				return true;
			}
		}
		return false;
	}
	private List<Line> parseLines(String taxobox) {
		taxobox = " " + taxobox;
		String[] slines = taxobox.split("\\s+\\|");
		List<Line> lines = new ArrayList<Line>();
		for (String sline: slines) {
			if (!StringUtils.isEmpty(sline)) {
				int epos = sline.indexOf('=');
				String key;
				String value;
				if (epos < 0) {
					key = sline.trim();
					value = null;
				} else {
					key = sline.substring(0, epos).trim();
					value = sline.substring(epos + 1).trim();
				}
				if ("{{PAGENAME}}".equals(value)) {
					value = null;
				}
				Line line = new Line(key, value);
//				LogHelper.speciesLogger.info("line." + line.getKey() + "/" + line.getValue());
				lines.add(line);
			}
		}
		return lines;
	}
	private String getValue(String key, List<Line> lines) {
		for (Line line: lines) {
			if (line.getKey().equals(key)) {
				return line.getValue();
			}
		}
		return null;
	}
	private class Line {
		private String key;
		private String value;
		public Line(String key, String value) {
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public String getValue() {
			return value;
		}
		@Override
		public String toString() {
			return key + "=" + value;
		}
	}
}
