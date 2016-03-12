package com.robestone.species;

import junit.framework.TestCase;

public class PluralMakerTest extends TestCase {

	private static final PluralMaker pluralMaker = new PluralMaker();
	
	public void test() {
		doTestBoth("fly", "flies");
		doTestBoth("dress", "dresses");
		doTestBoth("frizbee", "frizbees");
		doTestBoth("lion", "lions");
	}
	
	private void doTestBoth(String singular, String plural) {
		String foundPlural = pluralMaker.getPluralAlternate(singular);
		assertEquals(plural, foundPlural);
		String foundSingular = pluralMaker.getPluralAlternate(plural);
		assertEquals(singular, foundSingular);
	}
	
}
