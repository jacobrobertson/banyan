package com.robestone.species.js;

import java.util.ArrayList;
import java.util.List;

import com.robestone.species.CompleteEntry;
import com.robestone.species.parse.AbstractWorker;

public class SearchIndexBuilder extends AbstractWorker {

	public static void main(String[] args) {
		new SearchIndexBuilder().iterateOverKeys();
	}

	private int topMatchesMax = 5;
	private String letters = "abcdefghijklmnopqrstuvwxyz".toUpperCase();
	private int minKeyLen = 3;
	private int maxKeyLen = 4;
	
	private static class KeyEntry {
		String key;
		List<CandidateEntry> topMatches = new ArrayList<CandidateEntry>();
		List<KeyEntry> children = new ArrayList<KeyEntry>();
	}
	public static class CandidateEntry {
		CompleteEntry entry;
		List<String> names = new ArrayList<>();
	}

	public SearchIndexBuilder() {
	}
	
	public void iterateOverKeys() {
		List<CandidateEntry> entryNames = getCandidates();
		iterateOverKeys(entryNames);
	}
	public void iterateOverKeys(List<CandidateEntry> candidates) {
		
		KeyEntry root = new KeyEntry();
		root.key = "";
		iterateOverKeys(root, candidates);
	}
	private void iterateOverKeys(KeyEntry key, List<CandidateEntry> candidates) {
		if (key.key.length() == maxKeyLen) {
			return;
		}
		for (int i = 0; i < letters.length(); i++) {
			KeyEntry nextKey = new KeyEntry();
			nextKey.key = key.key + letters.charAt(i);
			// check the min length - no point to visit 1 letter?
			boolean foundMatches = true;
			if (nextKey.key.length() >= minKeyLen) {
				foundMatches = visitKey(nextKey, candidates);
			}
				
			if (foundMatches) {
				key.children.add(nextKey);
				// we only go deeper if we found a match, otherwise there's no point
				iterateOverKeys(nextKey, candidates);
			}
		}
	}
	
	private boolean visitKey(KeyEntry key, List<CandidateEntry> candidates) {
//		System.out.println("visitKey." + key.key);
		
		for (CandidateEntry candidate : candidates) {
			
			// check all versions of the name
			
			// TODO SCORE THESE
			for (String name : candidate.names) {
				if (name.contains(key.key)) {
					if (key.topMatches.size() <= topMatchesMax) {
						key.topMatches.add(candidate);
						break;
					}
				}
			}
		}

		boolean found = !key.topMatches.isEmpty();
		if (found) {
			System.out.println("visitKey." + key.key + "." + key.topMatches.size());
		}
		
		return found;
	}
	private List<CandidateEntry> getCandidates() {
		List<CompleteEntry> entries = speciesService.findEntriesForLuceneIndex();
		return getCandidates(entries);
	}
	public CandidateEntry toCandidate(CompleteEntry entry) {
		CandidateEntry candidate = new CandidateEntry();
		if (entry.getLatinNameCleanest() != null) {
			candidate.names.add(entry.getLatinNameCleanest());
		} else if (entry.getLatinName() != null) {
			candidate.names.add(entry.getLatinName().toUpperCase());
		}
		
		if (entry.getCommonNameCleanest() != null) {
			candidate.names.add(entry.getCommonNameCleanest());
		} else if (entry.getCommonName() != null) {
			candidate.names.add(entry.getCommonName().toUpperCase());
		}

		return candidate;
	}
	public List<CandidateEntry> getCandidates(List<CompleteEntry> entries) {
		List<CandidateEntry> candidates = new ArrayList<CandidateEntry>();
		
		// convert each one
		for (CompleteEntry entry : entries) {
			CandidateEntry candidate = toCandidate(entry);
			if (candidate != null) {
				candidates.add(candidate);
			}
			
			if (candidates.size() % 1000 == 0) {
				System.out.println("getCandidates." + candidates.size());
			}
		}
		
		return candidates;
	}
	
	
}
