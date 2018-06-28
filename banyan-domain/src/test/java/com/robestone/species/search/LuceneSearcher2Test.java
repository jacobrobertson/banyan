package com.robestone.species.search;

import java.util.HashSet;
import java.util.Set;

import com.robestone.species.CompleteEntry;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LuceneSearcher;
import com.robestone.species.parse.Mocks;

public class LuceneSearcher2Test extends AbstractSearcherTester {

	private LuceneSearcher searcher;
	private Mocks mocks = new Mocks();
	
	@Override
	protected void setUp() throws Exception {
		Set<? extends Entry> entries = EntryUtilities.getEntries(
				(CompleteEntry) mocks.getRoot()); // EntryForName("Testing"));
		searcher = null;//new LuceneSearcher(entries);
	}
	
	public void testFinky() {
		doTest("finky", 
				"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8",
				"T1S", "T2S", "T3S", "T4S", "T5S", "T6S", "T7S", "T8S",
				"T1C", "T2C", "T3C", "T4C",
				"F1", "F2"
				);
	}
	public void testPhrase() {
		doTest("phunky phrase", 
				"P1", "P2", "P3", "P4", "P5", "P6"
				);
	}
	public void testFuzzyPhrase() {
		doTest("longerphrase", "F3", "F4");
		doTest("longer phrase", "F3", "F4");
	}
	public void testVenusFlyTrap() {
		doTest("venus fly trap", "V1");
		doTest("venus flytrap", "V1");
		doTest("venusfly trap", "V1");
		doTest("venusflytrap", "V1");
	}
	public void testLion() {
		String[] tests = { "lion", "lions"};
		doTest(tests, "Lion", "Lion tamarin", "Lionycteris");
	}
	public void testArcticWhale() {
		doTest("Arctic Whale", "Balaena", "Balaena mysticetus", "Arctocephalus");
	} 

	private void doTest(String[] tests, String... expectedNames) {
		for (String test: tests) {
			doTest(test, expectedNames);
		}
	}
	private void doTest(String test, String... expectedNames) {
		Set<Integer> existingIds = new HashSet<Integer>();
		for (String expectedName: expectedNames) {
			int id = searcher.search(test, existingIds);
			if (expectedName == null) {
				assertEquals("Should not have matched any entries", -1, id);
			} else {
				assertFalse("Did not match any entries", -1 == id);
				existingIds.add(id);
				CompleteEntry found = mocks.getTree().get(id);
				boolean commonEquals = expectedName.equals(found.getCommonName());
				boolean latinEquals = expectedName.equals(found.getLatinName());
				assertTrue("Names did not match:" + 
						expectedName + "!=" + found.getCommonName() + "/" + found.getLatinName(), 
						latinEquals || commonEquals);
			}
		}
	}
	@Override
	protected Integer doTestSearcherQuery(String queryString,
			String... expectedLatinNames) {
		doTest(queryString, expectedLatinNames);
		return null;
	}
}
