package com.robestone.species;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CommonNameSplitter {
	
	/**
	 * This length or shorter don't bother splitting.
	 */
	private static final int MAX_KEEP_LENGTH = 25;
	private static final int NORMALIZING_KEEP_LENGTH = 5;
	private static final String[] STRIPS = {
		"or ", "and "
	};
	
	private int maxKeepLength = MAX_KEEP_LENGTH;
	
	public void assignCommonNames(CompleteEntry entry) {
		List<String> split = splitCommonName(entry, maxKeepLength);
		entry.setCommonNames(split);
		if (split != null) {
			// do a normal join
			entry.setCommonName(joinCommonNames(entry.getCommonName(), split));
		} else {
			// test for a "normalizing" rather than a join
			split = splitCommonName(entry, NORMALIZING_KEEP_LENGTH);
			if (split != null) {
				entry.setCommonName(joinCommonNames(entry.getCommonName(), split));
			}
		}
	}
	public void joinCommonName(CompleteEntry entry) {
		String joined = joinCommonNames(entry.getCommonName(), entry.getCommonNames());
		entry.setCommonName(joined);
	}
	public String joinCommonNames(String commonName, List<String> commonNames) {
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
	 * Used by test?
	 */
	List<String> splitCommonName(Entry entry) {
		return splitCommonName(entry, maxKeepLength);
	}
	/**
	 * @return null if it can't be broken up
	 */
	public List<String> splitCommonName(Entry entry, int maxLength) {
		String commonName = entry.getCommonName();
		if (commonName == null) {
			return null;
		}
		if (maxLength > 0 && maxLength >= commonName.length()) {
			return null;
		}
		List<String> names = splitCommonName(commonName);
		if (names == null) {
			return null;
		}
		names = filterBoring(names, entry);
		names = fixNames(names);
		// fixes some rare cases where it comes back empty
		if (names != null && names.isEmpty()) {
			names = null;
		}
		return names;
	}
	private List<String> fixNames(List<String> names) {
		List<String> fixed = new ArrayList<String>();
		for (String name: names) {
			name = EntryUtilities.fixCommonName(name);
			fixed.add(name);
		}
		return fixed;
	}
	private List<String> filterBoring(List<String> names, Entry entry) {
		String latinNameClean = EntryUtilities.getClean(entry.getLatinName(), false);
		List<String> filtered = new ArrayList<String>();
		for (String name: names) {
			String clean = EntryUtilities.getClean(name, false);
			boolean boring = CommonNameSimilarityChecker.isCommonNameCleanBoring(clean, latinNameClean);
			if (!boring) {
				filtered.add(name);
			}
		}
		return filtered;
	}
	private List<String> splitCommonName(String name) {
		String[] split = StringUtils.split(name, ",;/:");
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
	private String[] splitByOr(String name) {
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
	private String trimPart(String part) {
		part = StringUtils.trim(part);
		for (String trim: STRIPS) {
			part = StringUtils.removeStart(part, trim);
		}
		return part;
	}
	public void setMaxKeepLength(int maxKeepLength) {
		this.maxKeepLength = maxKeepLength;
	}
}
