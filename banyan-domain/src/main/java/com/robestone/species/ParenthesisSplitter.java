package com.robestone.species;

import org.apache.commons.lang3.StringUtils;

public class ParenthesisSplitter {

	/**
	 * @return the cleaned up part
	 */
	public static String removeUnwantedParenthenticalParts(String toCheck) {
		String[] split = split(toCheck);
		if (split == null) {
			return toCheck;
		}
		String right = split[1];
		for (CommonNameHint hint: CommonNameHint.hints) {
			if (hint.getText().equals(right)) {
				return split[0];
			}
		}
		return toCheck;
	}
	private static String[] split(String toSplit) {
		if (toSplit == null) {
			return null;
		}
		int pos = toSplit.indexOf('(');
		if (pos < 0) {
			return null;
		}
		String left = toSplit.substring(0, pos).trim();
		String right = toSplit.substring(pos + 1);
		right = StringUtils.strip(right, ") ");
		return new String[] {left, right};
	}
	
}
