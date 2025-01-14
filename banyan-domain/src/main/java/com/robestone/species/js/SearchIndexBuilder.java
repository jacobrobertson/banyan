package com.robestone.species.js;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robestone.species.CompleteEntry;
import com.robestone.species.parse.AbstractWorker;

public class SearchIndexBuilder extends AbstractWorker {

	public static void main(String[] args) {
		new SearchIndexBuilder(5, 3, 4).iterateOverKeys();
	}

	private int topMatchesMax = 5;
	private String letters = "abcdefghijklmnopqrstuvwxyz".toUpperCase();
	private int minKeyLen = 3;
	private int maxKeyLen = 4;
	
	
	public SearchIndexBuilder() {
	}
	public SearchIndexBuilder(int topMatchesMax, int minKeyLen, int maxKeyLen) {
		this();
		this.topMatchesMax = topMatchesMax;
		this.minKeyLen = minKeyLen;
		this.maxKeyLen = maxKeyLen;
	}
	private static class KeyEntry {
		String key;
		int topScore;
		List<CandidateEntry> topMatches = new ArrayList<CandidateEntry>();
		List<KeyEntry> children = new ArrayList<KeyEntry>();
	}
	public static class CandidateEntry implements Comparable<CandidateEntry> {
		CompleteEntry entry;
		Map<String, String> names = new HashMap<>();
		int score;
		String matchedName;
		
		@Override
		public int compareTo(CandidateEntry c) {
			return (int) (c.score - score);
		}
		
		public CandidateEntry copy(int score, String matchedName) {
			CandidateEntry copy = new CandidateEntry();
			copy.score = score;
			copy.matchedName = matchedName;
			
			copy.entry = this.entry;
			copy.names = this.names;
			
			return copy;
		}
	}

	public void iterateOverKeys() {
		List<CandidateEntry> entryNames = getCandidates();
		
		// TEMP
		entryNames = entryNames.subList(0, 500);
		
		iterateOverKeys(entryNames);
	}
	public void iterateOverKeys(List<CandidateEntry> candidates) {
		KeyEntry root = new KeyEntry();
		root.key = "";
		iterateOverKeys(root, candidates);
	}
	
	private int iterateOverKeys(KeyEntry key, List<CandidateEntry> candidates) {
		if (key.key.length() == maxKeyLen) {
			return 0;
		}
		int found = 0;
		for (int i = 0; i < letters.length(); i++) {
			KeyEntry nextKey = new KeyEntry();
			nextKey.key = key.key + letters.charAt(i);
			// check the min length - no point to visit 1 letter?
			boolean foundMatches = true;
			if (nextKey.key.length() >= minKeyLen) {
				foundMatches = buildKey(nextKey, candidates);
			}

			// iterate if a match, or if it's a short key
			if (foundMatches) {
				// we only go deeper if we found a match, otherwise there's no point
				int nextFound = iterateOverKeys(nextKey, candidates);
				found += nextFound;
				
				if (nextFound > 0) {
					key.children.add(nextKey);
				}
			}
		}
		// visit only on terminal nodes, or when returning from one
		if (
				(!key.topMatches.isEmpty() || !key.children.isEmpty())
				&&
				(key.key.length() >= minKeyLen)
				) {
			found++;
			simpleOutput(key);
		}
		return found;
	}
	
	private boolean buildKey(KeyEntry key, List<CandidateEntry> candidates) {
//		System.out.println("visitKey." + key.key);
		
		for (CandidateEntry candidate : candidates) {
			for (String name : candidate.names.keySet()) {
				int score = score(name, key.key);
				if (score > 0 && (score >= key.topScore || key.topMatches.size() <= topMatchesMax)) {
					CandidateEntry copy = candidate.copy(score, candidate.names.get(name));
					key.topScore = score;
					key.topMatches.add(copy);
					
					// TODO this isn't accounting for two species with the same "non-boring" name
					// -- maybe I also need to stop looking at synthetic common names - just the original and the latin
					if (key.topMatches.size() > topMatchesMax) {
						Collections.sort(key.topMatches);
						key.topMatches.remove(topMatchesMax);
					}
					
					break;
				}
			}
		}

		boolean found = !key.topMatches.isEmpty();
		if (found) {
//			System.out.println("visitKey." + key.key + "." + key.topMatches.size());
		}
		
		return found;
	}
	
	private int score(String name, String key) {
		int score = 0;
		// TODO make more interesting later
		if (name.equals(key)) {
			score = 10000;
		} else {
			if (name.startsWith(key)) {
				score = 5000;
			} else if (name.contains(key)) {
				score = 1000;
			}
			if (score > 0) {
				score += ((float) key.length() / (float) name.length()) * 100;
			}
		}
		
		return score;
	}
	
	private List<CandidateEntry> getCandidates() {
		List<CompleteEntry> entries = speciesService.findEntriesForLuceneIndex();
		return getCandidates(entries);
	}
	public CandidateEntry toCandidate(CompleteEntry entry) {
		CandidateEntry candidate = new CandidateEntry();
		candidate.entry = entry;
		// TODO do I really need to check both of these?
		if (entry.getLatinNameCleanest() != null) {
			candidate.names.put(entry.getLatinNameCleanest(), entry.getLatinName());
		} else if (entry.getLatinName() != null) {
			candidate.names.put(entry.getLatinName().toUpperCase(), entry.getLatinName());
		}
		
		if (entry.getCommonNameCleanest() != null) {
			candidate.names.put(entry.getCommonNameCleanest(), entry.getCommonName());
		} else if (entry.getCommonName() != null) {
			candidate.names.put(entry.getCommonName().toUpperCase(), entry.getCommonName());
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
		}
		
		return candidates;
	}

	/*
	 
{
	"local" : { 

		"kit" : [ 
				{"id": 15418, "name": "Cute Kittens" }, 
				{"id": 15416, "name": "Flowering Kitz"}
		]
		
	},
	
	"remote" : [ "kits", "kitt", "kite" ]

}
	  
	 */
	
	private void simpleOutput(KeyEntry key) {
		String s = toString(key);
		System.out.println(s);
	}
	private String toString(KeyEntry key) {
		StringBuilder buf = new StringBuilder("{\n");
		if (!key.topMatches.isEmpty()) {
			buf.append("\t\"local\" : {\n");
			buf.append("\t\t\"");
			buf.append(key.key.toLowerCase());
			buf.append("\" : [\n");

			int count = 1;
			for (CandidateEntry c : key.topMatches) {
				buf.append("\t\t\t {\"id\" : ");
				buf.append(c.entry.getId());
				buf.append(", \"name\" : \"");
				buf.append(c.matchedName);
				buf.append("\" }");
				if (count != key.topMatches.size()) {
					buf.append(",");
				}
				buf.append("\n");

				count++;
			}
			
			buf.append("\t\t]\n");
			buf.append("\t}");
			if (!key.children.isEmpty()) {
				buf.append(",\n");
			}
		}
		if (!key.children.isEmpty()) {
			buf.append("\t\"remote\" : [");
			int count = 1;
			for (KeyEntry child : key.children) {
				buf.append("\"");
				buf.append(child.key.toLowerCase());
				buf.append("\"");
				if (count != key.children.size()) {
					buf.append(", ");
				}
				count++;
			}
			buf.append("]\n");
		}
		buf.append("\n");
		buf.append("}");
		return buf.toString();
	}
	
}
