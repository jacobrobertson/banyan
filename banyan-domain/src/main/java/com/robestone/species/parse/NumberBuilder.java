package com.robestone.species.parse;

import java.math.BigInteger;

public class NumberBuilder {

	public static void main(String[] args) {
		System.out.println(Long.MAX_VALUE);
	}

	private static final String R62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final NumberBuilder R62 = new NumberBuilder(R62_CHARS);

	private String radixChars;
	private String zero;
	private char zeroChar;
	private BigInteger radixBigInteger;
	private int radix;
	
	private int[] charToInt;
	private BigInteger[] charToBigInteger;

	public NumberBuilder(String radixChars) {
		this.zeroChar = radixChars.charAt(0);
		this.zero = String.valueOf(zeroChar);
		this.radixChars = radixChars;
		this.radix = radixChars.length();
		this.radixBigInteger = new BigInteger(String.valueOf(radix));
		
		// this isn't efficient, but it doesn't matter much
		int maxCharVal = 0;
		for (int i = 0; i < radixChars.length(); i++) {
			char c = radixChars.charAt(i);
			if (maxCharVal < c) {
				maxCharVal = c;
			}
		}
		charToInt = new int[maxCharVal + 1];
		charToBigInteger = new BigInteger[maxCharVal + 1];
		for (int i = 0; i < radixChars.length(); i++) {
			char c = radixChars.charAt(i);
			charToInt[c] = i;
			charToBigInteger[c] = BigInteger.valueOf(i);
		}
		
	}
	
	public String toString(int i) {
		return toString(new BigInteger(String.valueOf(i)));
	}
	public String toString(long l) {
		return toString(new BigInteger(String.valueOf(l)));
	}
	public String toString(String s) {
		return toString(new BigInteger(s));
	}
	/**
	 * Does not left pad with zeros.
	 * Another method will do that, as needed by context.
	 */
	public String toString(BigInteger b) {
		String s;
		if (BigInteger.ZERO.compareTo(b) == 0) {
			s = zero;
		} else {
			StringBuilder buf = new StringBuilder();
			while (b.compareTo(BigInteger.ZERO) > 0) {
				BigInteger next = b.divide(radixBigInteger);
				BigInteger diff = b.subtract(next.multiply(radixBigInteger));
				buf.insert(0, toChar(diff.intValue()));
				b = next;
			}
			s = buf.toString();
		}
		return s;
	}
	public BigInteger toBigInteger(String s) {
		int len = s.length() - 1;
		BigInteger val = BigInteger.ZERO;
		for (int i = 0; i <= len; i++) {
			char c = s.charAt(i);
			BigInteger numberForChar = charToBigInteger[c];
			BigInteger pow = radixBigInteger.pow(len - i);
			BigInteger next = numberForChar.multiply(pow);
			val = val.add(next);
		}
		return val;
	}
	/**
	 * One char can never be more than an int
	 */
	public int toInt(char c) {
		return charToInt[c];
	}
	public char toChar(int n) {
		return radixChars.charAt(n);
	}
	public int getRadix() {
		return radix;
	}
	public char getZeroChar() {
		return zeroChar;
	}
}
