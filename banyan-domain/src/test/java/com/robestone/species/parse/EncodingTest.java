package com.robestone.species.parse;

import org.apache.commons.lang3.CharEncoding;

import junit.framework.TestCase;

public class EncodingTest extends TestCase {

	public void testUpper() {
		String s = "Kulczy?ski";
		System.out.println(s.toUpperCase());
	}
	
	public void testChars() throws Exception {
		doTestChar(287);
		doTestChar(254);
		doTestChar(255);
		doTestChar(256);
		doTestChar('ö');
		doTestChar(699);
		doTestChar(8216);
	}
	public void doTestChar(int g) throws Exception {
		int[] latinNums = {1, 2, 4, 5, 7, 9, 13, 15};
		for (int n: latinNums) {
			output(g, "ISO-8859-" + n);
		}
		output(g, CharEncoding.US_ASCII);
		output(g, CharEncoding.UTF_16);
		output(g, CharEncoding.UTF_16BE);
		output(g, CharEncoding.UTF_16LE);
		output(g, CharEncoding.UTF_8);
	}
	public void output(int num, String encoding) throws Exception {
		byte[] g = {(byte) num};
		String s1 = new String(g, encoding);
		System.out.println(num + " -> " + s1 + " (" + encoding + ")");
	}
}
