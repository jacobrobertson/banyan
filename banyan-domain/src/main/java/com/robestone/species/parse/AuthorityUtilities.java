package com.robestone.species.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.robestone.species.Rank;

public class AuthorityUtilities {
	private static Pattern[] authTypes = getAuthTypes();
	public static Pattern[] getAuthTypes() {
		String[] authTypes = {
				"([A-Za-z]+_)?Taxon_Authorities", "Repositories", "Sources",
//				"Entomologists", "Botanists", "Lichenologists",	"Palaeontologists", "Paleobotanists", "Ichthyologists",
				"[A-Za-z_]+ists",
				"ISSN"};
		Pattern[] patterns = new Pattern[authTypes.length];
		for (int i = 0; i < authTypes.length; i++) {
			patterns[i] = Pattern.compile("<a href=\"/wiki/Category\\:" + authTypes[i]);
		}
		return patterns;
	}
	private static String[] authHints = {
			"<span class=\"mw-headline\" id=\"Authored_taxa\">Authored taxa</span>",
			"<span class=\"mw-headline\" id=\"Described_taxa\">Described taxa</span>",
			"<span class=\"mw-headline\" id=\"works_include\">works include</span>",
			"<span class=\"mw-headline\" id=\"work_include\">works include</span>",
			"<span class=\"mw-headline\" id=\"work_include\">work include</span>",
			"<span class=\"mw-headline\" id=\"works_including\">works including</span>",
	};
	public static boolean isAuthorityPage(String latinName, String page) {
		
		Rank rank;
		try {
			rank = Rank.valueOfWithAlternates(latinName);
		} catch (Exception e) {
			rank = null;
		}
		if (rank != null && Rank.Virus != rank) {
			return true;
		}
		
		if (latinName.startsWith("ISSN")) {
			return true;
		}
		for (Pattern authType: authTypes) {
			Matcher m = authType.matcher(page);
			if (m.find()) {
				return true;
			}
		}
		for (String hint: authHints) {
			int find = StringUtils.indexOfIgnoreCase(page, hint);
			if (find > 0) {
				return true;
			}
		}
		
		// because some hints might not be conclusive, we only check them if there is also no taxobox
		boolean hasTaxoBox = page.contains("id=\"Taxonavigation\">Taxonavigation");
		if (!hasTaxoBox) {
			String[] authHints2 = {
					"id=\"Publications\">Publications",
					"<li><b>Dates:</b>",
					"<li><b>Dates</b>", // <li><b>Dates</b> 1758-1759, 2 vols. [2: 825-1384]</li>
			};
			for (String hint: authHints2) {
				int find = StringUtils.indexOfIgnoreCase(page, hint);
				if (find > 0) {
					return true;
				}
			}
			
			// if we got this far that means no taxobox, so if we also see no ranks, it's a slam-dunk
			if (!hasRanks(page)) {
				return true;
			}
			
		}
		
		// CAN'T DO -- some good pages are also disambiguation
//		if (page.contains("<a href=\"/wiki/Category:Disambiguation_pages\"")) {
//			return true;
//		}
		return false;
	}
	private static Pattern ranksPattern = Pattern.compile(
			"(\\s|>)" +
			WikiSpeciesParser.getRanksPatternPart(true) +
			"(:|<|\\s)"
			);
	private static boolean hasRanks(String page) {
		Matcher m = ranksPattern.matcher(page);
		return m.find();
	}

}
