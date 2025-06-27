package com.robestone.species.js;

import java.util.ArrayList;
import java.util.List;

import com.robestone.species.Entry;
import com.robestone.species.js.SearchIndexBuilder.CandidateEntry;
import com.robestone.species.js.SearchIndexBuilder.CandidateName;

import junit.framework.TestCase;

public class SearchIndexBuilderTest extends TestCase {

	public void testSimplest() throws Exception {
		SearchIndexBuilder b = new SearchIndexBuilder(5, 3, 6, false);
		
		List<CandidateEntry> candidates = new ArrayList<>();
		toCandidateEntry("Prince of Peec", candidates, b);
		toCandidateEntry("Helper", candidates, b);
		CandidateEntry test2 = toCandidateEntry("Funny", candidates, b);
		toCandidateEntry("Howitzer", candidates, b);
		toCandidateEntry("Prize Munny", candidates, b);
		toCandidateEntry("Junnyper", candidates, b);
		toCandidateEntry("Unnsterz", candidates, b);
		toCandidateEntry("MeMunn", candidates, b);
		toCandidateEntry("Trunnk", candidates, b);
		toCandidateEntry("No MorePrizeMunny", candidates, b);
		
		CandidateEntry test = testCandidateEntry("The United States of America", candidates, b, "UNN", 0);
		testCandidateEntry(test, "ATES", 10_166);
		testCandidateEntry(test, "STATES", 110_250);
		testCandidateEntry(test, "THE", 1_110_125);
		
		testCandidateEntry(test2, "FUNNY", 10_001_000);
		testCandidateEntry(test2, "FUN", 1_010_600);
		
		b.setCandidates(candidates);
		b.iterateOverKeys();
	}

	private CandidateEntry toCandidateEntry(String name, List<CandidateEntry> candidates, SearchIndexBuilder b) {
		return testCandidateEntry(name, candidates, b, null, -1);
	}
	
	public void testAntsPlants() {
		// "1685" : { "ids" : ".1_1vpF", "latin" : "Plantae", "common" : "Plants" },
		CandidateEntry e = new CandidateEntry();
		CandidateName pname = e.addName("PLANTS", "Plants", "PLANTS", false, false);
		int pscore = SearchIndexBuilder.score(pname, "ANTS");
		assertEquals(20_666, pscore);
		
		// "25150" : { "ids" : ".1_1vno-161h49_2G-K1_4u1s012a1o7X1r-137.3hP22a.1_rD01", "latin" : "Formicidae", "common" : "Ants" },
		CandidateName aname = e.addName("ANTS", "Ants", "ANTS", false, false);
		int ascore = SearchIndexBuilder.score(aname, "ANTS");
		assertEquals(20_001_000, ascore);
		
	}
	
	private CandidateEntry testCandidateEntry(String name, List<CandidateEntry> candidates, SearchIndexBuilder b, String key, int expectedScore) {
		Entry entry = new Entry();
		entry.setLatinName(name);
		entry.setId(Integer.valueOf(10));
		
		CandidateEntry candidate = b.toCandidate(entry);
		candidates.add(candidate);
		
		if (key != null) {
			testCandidateEntry(candidate, key, expectedScore);
		}
		
		return candidate;
	}
	private void testCandidateEntry(CandidateEntry candidate, String key, int expectedScore) {
		int score = SearchIndexBuilder.score(candidate.getNames().get(0), key);
		assertEquals("Score not correct", expectedScore, score);
	}
	
}
