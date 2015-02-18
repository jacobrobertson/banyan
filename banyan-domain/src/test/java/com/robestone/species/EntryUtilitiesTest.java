package com.robestone.species;

import com.robestone.util.html.EntityMapper;

import junit.framework.TestCase;

public class EntryUtilitiesTest extends TestCase {

	public void testCleanWithSymbols() {
		doTestCleanWithSymbols("funk[465]y", "FUNKOY", false);
		doTestCleanWithSymbols("funk [465] y", "FUNKOY", true);
		doTestCleanWithSymbols("funk [465]y", "FUNK OY", false);
		doTestCleanWithSymbols("funk [4655]y", "FUNK Y", false);
	}
	private void doTestCleanWithSymbols(String toTest, String expect, boolean cleanest) {
		String latin = EntityMapper.convertToSymbolsText(toTest, true);
		String clean = EntryUtilities.getClean(latin, cleanest);
		assertEquals(expect, clean);
	}
	
}
