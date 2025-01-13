package com.robestone.species.js;

import java.util.ArrayList;
import java.util.List;

import com.robestone.species.CompleteEntry;
import com.robestone.species.js.SearchIndexBuilder.CandidateEntry;

import junit.framework.TestCase;

public class SearchIndexBuilderTest extends TestCase {

	public void testSimplest() {
		SearchIndexBuilder b = new SearchIndexBuilder();
		
		List<CandidateEntry> candidates = new ArrayList<>();
		toCandidateEntry("Helper", candidates, b);
		toCandidateEntry("Funny", candidates, b);
		
		b.iterateOverKeys(candidates);
	}

	private void toCandidateEntry(String name, List<CandidateEntry> candidates, SearchIndexBuilder b) {
		CompleteEntry entry = new CompleteEntry();
		entry.setLatinName(name);
		CandidateEntry candidate = b.toCandidate(entry);
		candidates.add(candidate);
	}
	
}
