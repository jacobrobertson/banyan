package com.robestone.species.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.robestone.species.Rank;
import com.robestone.species.parse.WikiSpeciesProblemsFinder.FileTester;

/**
 * Looks for pages with a rank name misspelled by exactly one letter
 * 
 * Species
 * 		species
 * 		sPecies
 * 		Sbecies
 * 
 * @author jacob
 */
public class RankMutationSearcher implements FileTester {

	public static void main(String[] args) throws Exception {
		new WikiSpeciesProblemsFinder(new RankMutationSearcher()).run();
	}
	
	private List<PatternPair> patterns;
	public RankMutationSearcher() {
		patterns = buildPatterns();
	}
	
	@Override
	public void testFile(File file, String contents) throws Exception {
		doTestFile(file, contents, true);
	}
	public void doTestFile(File file, String contents, boolean allowDownload) throws Exception {
	
//		System.out.println(file.getName());
		contents = getTaxo(contents);
		if (contents == null) {
			return;
		}
		for (PatternPair p: patterns) {
			if (p.mutationPattern == null) {
				continue;
			}
			Matcher m = p.mutationPattern.matcher(contents);
			if (m.find()) {
				String group = m.group();
				// check that it doesn't match the others
				boolean matchesOther = false;
				for (PatternPair other: patterns) {
					if (other.rankPattern == null) {
						continue;
					}
					Matcher gm = other.rankPattern.matcher(group);
					if (gm.matches()) {
						matchesOther = true;
						break;
					}
				}
				if (!matchesOther) {
					if (allowDownload) {
						String name = file.getName();
						name = name.substring(0, name.indexOf("."));
						name = name.replaceAll("_", "");
						contents = WikiSpeciesCache.CACHE.readFile(name, true);
						doTestFile(file, contents, false);
					} else {
						System.out.println(file.getName() + " // " + m.group().trim() + " // " + p.mutationPattern);
					}
				} else {
//					System.out.println(".... no ... " + file.getName() + " // " + m.group().trim() + " // " + p.mutationPattern);
				}
			}
		}
	}
	private List<PatternPair> buildPatterns() {
		List<PatternPair> patterns = new ArrayList<PatternPair>();
		for (Rank rank: Rank.values()) {
			if (rank.isValidRank()) {
				Pattern mp = buildPattern(rank, true);
				Pattern rp = buildPattern(rank, false);
				PatternPair pair = new PatternPair();
				pair.mutationPattern = mp;
				pair.rankPattern = rp;
				patterns.add(pair);
			}
		}
		return patterns;
	}
	private Pattern buildPattern(Rank rank, boolean mutate) {
		List<String> parts;
		if (mutate) {
			parts = buildMutations(rank);
		} else {
			parts = new ArrayList<String>();
			for (String name: rank.getNames()) {
				if (isClean(name)) {
					parts.add(name);
				}
			}
		}
		
		if (parts.isEmpty()) {
			return null;
		}
		
		StringBuilder buf = new StringBuilder();
		
		for (String s: parts) {
			if (buf.length() == 0) {
				buf.append("([^a-zA-Z]\\s|>)(");
			} else {
				buf.append("|");
			}
			buf.append(s);
		}
		buf.append(")\\s*:");
		
		return Pattern.compile(buf.toString());
	}
	private List<String> buildMutations(Rank rank) {
		Set<String> names = rank.getNames();
		List<String> mutations = new ArrayList<String>();
		for (String name: names) {
			if (isClean(name)) {
				List<String> some = buildMutations(name);
				mutations.addAll(some);
			}
		}
		return mutations;
	}
	private List<String> buildMutations(String name) {
		List<String> mutations = new ArrayList<String>();
		
		for (int i = 0; i < name.length(); i++) {
			String left = name.substring(0, i);
			String right;
			if (i == name.length() - 1) {
				right = "";
			} else {
				right = name.substring(i + 1);
			}
			char c = name.charAt(i);
			String mutation = left + "[^" + c + "]" + right;
			mutations.add(mutation);
//			System.out.println(name + " > " + mutation);
		}
		
		return mutations;
	}
	private boolean isClean(String name) {
		if ("section".equalsIgnoreCase(name)) {
			return false;
		}
		if ("group".equalsIgnoreCase(name)) {
			return false;
		}
		return StringUtils.isAlpha(StringUtils.remove(name, ' '));
	}
	private String getTaxo(String page) {
		// id="Taxonavigation">Taxonavigation</span>
		int i1 = page.indexOf("id=\"Taxonavigation\">Taxonavigation</span>");
		if (i1 < 0) {
			return null;
		}
		
		// <h2><span class="mw-headline"
		int i2 = page.indexOf("<h2><span class=\"mw-headline\"", i1);
		if (i2 < 0) {
			i2 = page.indexOf("<div class=\"printfooter\">", i1);
		}
		if (i2 < 0) {
			i2 = page.length() - 1;
		}
		page = page.substring(i1, i2);
		return page;
	}
	private class PatternPair {
		Pattern mutationPattern;
		Pattern rankPattern;
	}
	
}
