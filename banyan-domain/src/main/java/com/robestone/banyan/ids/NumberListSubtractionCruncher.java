package com.robestone.banyan.ids;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Jacob Robertson
 */
public class NumberListSubtractionCruncher implements NumberListCruncher {

	private boolean useSubtraction = true;
	private char subtractionIndicator = '-';
	private char rebaseIndicator = '.';
	private char padChangeDelimiter = '_';
	private int minPadSize;
	private int[] minValues;

	private char zeroChar;
	
	private NumberBuilder builder = NumberBuilder.R62;
	
	/**
	 * @param delimiter Indicates that we increase the padsize.  use -1 to indicate no delimiter wanted.
	 */
	public NumberListSubtractionCruncher(int padSize) {
		this.minPadSize = padSize;
		minValues = new int[10];
		for (int i = 0; i < minValues.length; i++) {
			minValues[i] = (int) Math.pow(builder.getRadix(), i + 1);
		}
		zeroChar = builder.getZeroChar();
	}
	public List<Integer> toList(String s) {
		List<Integer> list = new ArrayList<Integer>();
		toCollection(s, list);
		return list;
	}
	public Set<Integer> toSet(String s) {
		Set<Integer> set = new HashSet<Integer>();
		toCollection(s, set);
		return set;
	}
	private void toCollection(String s, Collection<Integer> collection) {
		int subPos = s.indexOf(subtractionIndicator);
		if (subPos < 0) {
			subPos = s.indexOf(rebaseIndicator);
		}
		int toAdd = 0;
		boolean useSub = subPos >= 0;
		int padSize = minPadSize;
		int len = s.length();
		for (int i = 0; i < len;) {
			char c = s.charAt(i);
			if (c == rebaseIndicator) {
				if (padSize == 1) {
					padSize = minPadSize;
				} else if (padSize >= minPadSize) {
					padSize = 1;
				} else {
					throw new IllegalArgumentException("Not expecting " + rebaseIndicator + " at pos " + i + " of string " + s);
				}
				i++;
			} else if (c == subtractionIndicator) {
				padSize--;
				i++;
			} else if (c == padChangeDelimiter) {
				padSize++;
				i++;
			} else {
				String sub = s.substring(i, i + padSize);
				int val = builder.toBigInteger(sub).intValue();
				if (useSub) {
					val += toAdd;
					toAdd = val;
				}
				collection.add(val);
				i += padSize;
			}
		}
	}
	public String toString(Collection<Integer> nums) {
		List<Integer> lnums = new ArrayList<Integer>(nums);
		Collections.sort(lnums);
		if (!useSubtraction || lnums.size() == 1) {
			return toStringUnsorted(lnums, minPadSize);
		} else {
			return toStringWithSubtraction(lnums);
		}
	}
	private String toStringWithSubtraction(List<Integer> nums) {
		boolean usedSymbol = false;
		int last = 0;
		StringBuilder buf = new StringBuilder();
		int currentPad = this.minPadSize;
		int nsize = nums.size();
		for (int i = 0; i < nsize; i++) {
			int num = nums.get(i);
			int sub = num - last;
			last = num;
			num = sub;
			String s = toString(num, false, minPadSize);
			int len = s.length();
			int padDiff = currentPad - len;
			if (padDiff > 0) {
				// may do sub, may do rebase
				// examples for when minPadSize = 3
				// no matter what the x and y, if diff = 1, use one minus
				// xxxxx-yyyy : diff == 1, use minus
				// xxxx-yyy : diff == 1, use minus
				// xxx-yy : diff == 1, use minus
				// xx-y : diff == 1, use minus
				// use rebase to indicate either len == 1 or len == minPadSize, but only in certain cases
				// xxxxx.y : x >= minPadSize, y == 1 - use rebase
				// xxxx.y : x >= minPadSize, y == 1 - use rebase
				// xxx.y : x >= minPadSize, y == 1 - use rebase
				// in cases where we can't use rebase, we have to use --
				// xxxxx--yyy : x > minPadSize, y != 1 - use --
				// xxxxx---yy : x > minPadSize, y != 1- have to use --- (better to do .- but for now that's not a real scenario)
				// xxxx--yy : x > minPadSize, y != 1 - use --
				if (padDiff == 1) {
					// first determine if we should just zero pad it instead
					boolean zeroPad = false;
					// if it's the last number, can't compare the next number
					if (i < nsize - 1) {
						// look at the next number to see if it will just go back up again
						int nextNum = nums.get(i + 1) - last;
						int maxNext = minValues[len - 1];
						if (nextNum >= maxNext) {
							zeroPad = true;
						}
					} else {
						// the last number, so zero pad to look nicer
						zeroPad = true;
					}
					if (zeroPad) {
						buf.append(zeroChar);
						len++;
					} else {
						buf.append(subtractionIndicator);
						usedSymbol = true;
					}
				} else if (currentPad >= minPadSize && len == 1) {
					buf.append(rebaseIndicator);
					usedSymbol = true;
				} else {
					for (int j = 0; j < padDiff; j++) {
						buf.append(subtractionIndicator);
					}
					usedSymbol = true;
				}
			} else if (padDiff < 0) {
				// may do plus, may do rebase
				// examples for when minPadSize == 3
				// no matter what the x and y, if diff = 1, use one plus (_)
				// x_yy : diff == -1, use _
				// xx_yyy : diff == -1, use _
				// in the very specific case here
				// x.yyy we can use rebase
				// for every other case, we have to use one or more pluses (_)
				// xxx__yyyyy, x___yyyy, xx__yyyy, x__yyy, etc
				if (padDiff == -1) {
					buf.append(padChangeDelimiter);
				} else if (currentPad == 1 && len == minPadSize) {
					buf.append(rebaseIndicator);
					usedSymbol = true;
				} else {
					for (int j = 0; j < -padDiff; j++) {
						buf.append(padChangeDelimiter);
					}
				}
			} else {
				// no action needed...
			}
			currentPad = len;
			buf.append(s);
		}
		String other = toStringUnsorted(nums, minPadSize);
		int olen = other.length();
		if (!usedSymbol) {
			// we need this case, because otherwise when we parse it, we won't look for subtraction
			if (buf.length() + 1 < olen) { 
				buf.append(subtractionIndicator);
				return buf.toString();
			} else {
				return other;
			}
		} else if (olen <= buf.length()) {
			// this covers some very rare cases, plus makes it so w prefer non-subtraction style (easier on eyes)
			return other;
		} else {
			return buf.toString();
		}
	}
	private String toStringUnsorted(List<Integer> nums, int minPadSize) {
		StringBuilder buf = new StringBuilder();
		int minValue = minValues[minPadSize - 1];
		for (int n: nums) {
			while (n >= minValue) {
				buf.append(padChangeDelimiter);
				minPadSize++;
				minValue = minValues[minPadSize - 1];
			}
			String one = toString(n, true, minPadSize);
			buf.append(one);
		}
		return buf.toString();
	}
	private String toString(int n, boolean pad, int minPadSize) {
		String s = builder.toString(n);
		if (pad) {
			s = StringUtils.leftPad(s, minPadSize, zeroChar);
		}
		return s;
	}
	
}
