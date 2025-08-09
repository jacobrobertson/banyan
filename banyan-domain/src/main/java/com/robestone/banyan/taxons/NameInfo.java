package com.robestone.banyan.taxons;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.robestone.banyan.util.ParseUtilities;
import com.robestone.banyan.workers.CommonNameSimilarityChecker;

public class NameInfo {
	
	/**
	 * This length or shorter don't bother splitting.
	 */
	private static final int MAX_KEEP_LENGTH = 25;
	private static final int NORMALIZING_KEEP_LENGTH = 5;
	private static final String[] STRIPS = {
		"or ", "and "
	};
	
	private int maxKeepLength;
	private String commonNameOriginal;
	private String latinName;
	private String commonNameFixed;
	private List<String> commonNames;
	
	public NameInfo(String commonName, String latinName) {
		this(commonName, latinName, MAX_KEEP_LENGTH);
	}
	
	public NameInfo(String commonName, String latinName, int maxKeepLength) {
		this.commonNameOriginal = commonName;
		this.latinName = latinName;
		this.maxKeepLength = maxKeepLength;
		assignCommonNames();
	}
	
	public String getCommonNameOriginal() {
		return commonNameOriginal;
	}
	public String getLatinName() {
		return latinName;
	}
	public String getCommonNameFixed() {
		return commonNameFixed;
	}
	public List<String> getCommonNames() {
		return commonNames;
	}

	private void assignCommonNames() {
		commonNames = splitCommonName(commonNameOriginal, latinName, maxKeepLength);
		if (commonNames != null) {
			// do a normal join
			commonNameFixed = joinCommonNames(commonNameOriginal, commonNames);
		} else {
			// test for a "normalizing" rather than a join
			commonNames = splitCommonName(commonNameOriginal, latinName, NORMALIZING_KEEP_LENGTH);
			if (commonNames != null) {
				commonNameFixed = joinCommonNames(commonNameOriginal, commonNames);
			} else {
				commonNameFixed = commonNameOriginal;
			}
		}
	}
	private static String joinCommonNames(String commonName, List<String> commonNames) {
		if (commonNames == null) {
			return commonName;
		}
		StringBuilder buf = new StringBuilder();
		for (String name: commonNames) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append(name);
		}
		return buf.toString();
	}
	
	/**
	 * @return null if it can't be broken up
	 */
	private static List<String> splitCommonName(String commonName, String latinName, int maxLength) {
		if (commonName == null || 
				commonName.indexOf(ParseUtilities.COMMON_NAME_FROM_DESCENDENTS_INDICATOR) >= 0) {
			return null;
		}
		if (maxLength > 0 && maxLength >= commonName.length()) {
			return null;
		}
		List<String> names = splitCommonName(commonName);
		if (names == null) {
			return null;
		}
		names = filterBoring(names, latinName);
		names = fixNames(names);
		// fixes some rare cases where it comes back empty
		if (names != null && names.isEmpty()) {
			names = null;
		}
		return names;
	}
	private static List<String> fixNames(List<String> names) {
		List<String> fixed = new ArrayList<String>();
		for (String name: names) {
			name = ParseUtilities.fixCommonName(name);
			fixed.add(name);
		}
		return fixed;
	}
	private static List<String> filterBoring(List<String> names, String latinName) {
		String latinNameClean = TreeNodeUtilities.getClean(latinName, false);
		List<String> filtered = new ArrayList<String>();
		for (String name: names) {
			String clean = TreeNodeUtilities.getClean(name, false);
			boolean boring = CommonNameSimilarityChecker.isCommonNameCleanBoring(clean, latinNameClean);
			if (!boring) {
				filtered.add(name);
			}
		}
		return filtered;
	}
	private static List<String> splitCommonName(String name) {
		String[] split = StringUtils.split(name, ",;/:�");
		List<String> names = new ArrayList<String>();
		for (String s: split) {
			s = trimPart(s);
			String[] moreSplit = splitByOr(s);
			for (String s2: moreSplit) {
				s2 = trimPart(s2);
				names.add(s2);
			}
		}
		if (names.size() == 1) {
			return null;
		}
		return names;
	}
	private static String[] splitByOr(String name) {
		String[] moreSplit = StringUtils.splitByWholeSeparator(name, " or ");
		if (moreSplit.length == 1) {
			return moreSplit;
		}
		// ensure we don't split "red or blue tick"
		String left = moreSplit[0];
		int spacePos = left.indexOf(' ');
		if (spacePos < 0) {
			return new String[] {name};
		}
		return moreSplit;
	}
	private static String trimPart(String part) {
		part = StringUtils.trim(part);
		for (String trim: STRIPS) {
			part = StringUtils.removeStart(part, trim);
		}
		return part;
	}
}
