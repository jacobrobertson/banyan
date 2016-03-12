package com.robestone.species;

import junit.framework.TestCase;

public class ParenthesisSplitterTest extends TestCase {

	public void test() {
		doTest("funk (house)", "funk (house)");
		doTest("funk (spider)", "funk");
		doTest("funk", "funk");
	}
	private void doTest(String test, String expect) {
		String found = ParenthesisSplitter.removeUnwantedParenthenticalParts(test);
		assertEquals(expect, found);
	}
	
}
