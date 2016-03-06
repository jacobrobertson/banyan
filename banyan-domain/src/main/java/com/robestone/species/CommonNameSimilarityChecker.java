package com.robestone.species;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


public class CommonNameSimilarityChecker {

	private static List<String> suffixes = buildSuffixes();
	
	private CommonNameSimilarityChecker() {}
	
	private static List<String> buildSuffixes() {
		String[] strings = {
		"a",
		"d",
		"e",
		"o",
		"s",
		
		"ae",
		"an",
		"as",
		"ea",
		"es",
		"ia",
		"id",
		"is",
		"us",

		"ata",
		"ans",
		"dae",
		"ids",
		"ina",
		
		"ales",
		"idae",
		"inae",
		"ines",
		"odea",
				
		"aceae",
		"ineae",
		"oidea",
		"tidae",
		"tinae",

		"etales",
		
		// these are "experimental" because they might be too aggressive
		// they represent common latin things that mean something more concrete
		// or in some cases, they are very specific versions of the endings given above
		
		// -- with latin meanings
		"donta",
		"gnathi",
		
		// -- specific cases
		"iformes",
		"morpha",
		"morphia",
		"myoidea",
		"odinae",
		};
		List<String> suffixes = new ArrayList<String>();
		for (String one: strings) {
			suffixes.add(one.toUpperCase());
		}
		return suffixes;
	}
	
	/**
	 * Checks things like plural.
	 * Assumes these are already clean. (not cleanest though?)
	 */
	public static boolean isFirstBoringNextToSecond(String first, String second) {
		if (first == second) {
			return true;
		}
		if (first == null) {
			return true;
		}
		if (second == null) {
			return false;
		}
		// now we know neither is null
		
		if (first.equals(second)) {
			return true;
		}
	
		// if either is a plural of the other, then the "first" is boring
		if (isFirstPluralOfSecond(first, second)) {
			return true;
		}
		if (isFirstPluralOfSecond(second, first)) {
			return true;
		}
		return false;
	}
	private static boolean isFirstPluralOfSecond(String first, String second) {
		// TODO need to see if we're worried about "flies > fly", or "lioness > lionesses"
		// if so - add those the the parts along with null/S, and check them both
		return (first + "S").equals(second);
	}
	
	/**
	 * Only call this if you don't have the clean names available.
	 */
	public boolean isCommonNameBoring(Entry entry) {
		String cnc = EntryUtilities.getClean(entry.getCommonName(), false);
		String lnc = EntryUtilities.getClean(entry.getLatinName(), false);
		boolean boring = isCommonNameCleanBoring(cnc, lnc);
		return boring;
	}
	public static boolean isCommonNameCleanBoring(String commonNameClean, String latinNameClean) {
		// I shouldn't have to check this, 
		// but it makes this method more robust and reusable in different places
		if (commonNameClean == null) {
			return true;
		}
		boolean boring = isOneNameSubset(commonNameClean, latinNameClean);
		if (boring)	{
			return true;
		}
		String latinName = getFixedLatinName(latinNameClean);
		boring = isCommonNameBoringWithParts(commonNameClean, latinName);
		if (boring)	{
			return true;
		}
		boring = areNamesVerySimilar(commonNameClean, latinName);
		return boring;
	}
	private static boolean isCommonNameBoringWithParts(String c, String l) {
		for (String s1: suffixes) {
			for (String s2: suffixes) {
				if (s1.equals(s2)) {
					continue;
				}
				if (isCommonNameBoring(c, l, s1, s2)) {
					return true;
				}
				if (isCommonNameBoring(c, l, s2, s1)) {
					return true;
				}
			}
		}
		return false;
	}
	private static boolean isCommonNameBoring(String c, String l, String s1, String s2) {
		String commonName = trim(c, s1);
		String latinName = trim(l, s2);
		return commonName.equals(latinName);
	}
	private static String getFixedLatinName(String l) {
		int pos = l.indexOf('(');
		if (pos > 0) {
			l = l.substring(0, pos).trim();
		}
		return l;
	}
	private static String trim(String name, String part) {
		if (part == null) {
			return name;
		}
		if (name.endsWith(part)) {
			return StringUtils.removeEnd(name, part);
		}
		return name;
	}
	
	/**
	 * This covers these types of things:
	 * 
	 * Fruncia (Frunciata)
	 * Fruncia (Fruncia Verlupa)
	 * 
	 * Note that adding this check makes any pair with a null unnecessary.
	 */
	private static boolean isOneNameSubset(String first, String second) {
		return 
			isFirstTheStartOfSecond(first, second)
			|| isFirstTheStartOfSecond(second, first);
	}
	private static boolean isFirstTheStartOfSecond(String first, String second) {
		return first.startsWith(second);
	}
	
	private static boolean areNamesVerySimilar(String c, String l) {
		int maxDiff = 2;
		int diff = StringUtils.getLevenshteinDistance(c, l);
		return (diff < maxDiff);
	}
	
}
