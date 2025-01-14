package com.robestone.species.js;

import java.util.ArrayList;
import java.util.List;

import com.robestone.species.CompleteEntry;
import com.robestone.species.js.SearchIndexBuilder.CandidateEntry;

import junit.framework.TestCase;

public class SearchIndexBuilderTest extends TestCase {

	public void testSimplest() {
		SearchIndexBuilder b = new SearchIndexBuilder(5, 3, 6);
		
		List<CandidateEntry> candidates = new ArrayList<>();
		toCandidateEntry("Prince of Peec", candidates, b);
		toCandidateEntry("Helper", candidates, b);
		toCandidateEntry("Funny", candidates, b);
		toCandidateEntry("Howitzer", candidates, b);
		toCandidateEntry("Prize Munny", candidates, b);
		toCandidateEntry("Junnyper", candidates, b);
		toCandidateEntry("Unnsterz", candidates, b);
		toCandidateEntry("MeMunn", candidates, b);
		toCandidateEntry("Trunnk", candidates, b);
		toCandidateEntry("No MorePrizeMunny", candidates, b);
		toCandidateEntry("The United States of America", candidates, b);
		
		b.iterateOverKeys(candidates, SearchIndexBuilder.SIMPLE_VISITOR);
	}

	private void toCandidateEntry(String name, List<CandidateEntry> candidates, SearchIndexBuilder b) {
		CompleteEntry entry = new CompleteEntry();
		entry.setLatinName(name);
		CandidateEntry candidate = b.toCandidate(entry);
		candidates.add(candidate);
	}
	
}
