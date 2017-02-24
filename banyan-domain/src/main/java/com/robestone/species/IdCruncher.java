package com.robestone.species;

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
public class IdCruncher {

	private boolean useSubtraction = false;
	private char subtractionIndicator = '-';
	private char rebaseIndicator = '.';
	private String chars;
	private String zero;
	private char zeroChar;
	private int radix;
	private int minPadSize;
	private char padChangeDelimiter = '_';
	private int[] minValues;
	
	public static final String R62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	/**
	 * 62 is for 0-9, a-z, A-Z.
	 * 3 means padSize of 3.
	 */
	public static final IdCruncher R62_3 = 
		new IdCruncher(R62_CHARS, 3);
	public static final IdCruncher R26_4 = 
		new IdCruncher("abcdefghijklmnopqrstuvwxyz", 4);
	
	/**
	 * @param delimiter Indicates that we increase the padsize.  use -1 to indicate no delimiter wanted.
	 */
	public IdCruncher(String chars, int padSize) {
		this.zeroChar = chars.charAt(0);
		this.zero = String.valueOf(zeroChar);
		this.chars = chars;
		this.radix = chars.length();
		this.minPadSize = padSize;
		minValues = new int[10];
		for (int i = 0; i < minValues.length; i++) {
			minValues[i] = (int) Math.pow(radix, i + 1);
		}
	}
	public static IdCruncher withSubtraction(String chars) {
		// we choose pad of 3 as the default, 
		// but that is only used for uncrunching...
		// for crunching, it uses 1, and for minvalues
		IdCruncher ids = new IdCruncher(chars, 3);
		ids.useSubtraction = true;
		return ids;
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
				int val = toInt(sub);
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
			String s = toString(num, false);
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
	public int toInt(String s) {
		int len = s.length() - 1;
		int val = 0;
		for (int i = 0; i <= len; i++) {
			char c = s.charAt(i);
			int n = toInt(c);
			val += (n * (int) Math.pow(radix, len - i));
		}
		return val;
	}
	public int toInt(char c) {
		return chars.indexOf(c);
	}
	/**
	 * Converts just one number - will not take delimiter into consideration.
	 */
	public String toString(int n) {
		return toString(n, true);
	}
	private String toString(int n, boolean pad) {
		return toString(n, pad, this.minPadSize);
	}
	private String toString(int n, boolean pad, int padSize) {
		String s;
		if (n == 0) {
			s = zero;
		} else {
			StringBuilder buf = new StringBuilder();
			while (n > 0) {
				int next = n / radix;
				int diff = n - (next * radix);
				buf.insert(0, toChar(diff));
				n = next;
			}
			s = buf.toString();
		}
		if (pad) {
			String padded = StringUtils.leftPad(s, padSize, zero);
			return padded;
		} else {
			return s;
		}
	}
	public char toChar(int n) {
		return chars.charAt(n);
	}
	
}
