package com.robestone.species.js;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.robestone.species.CompleteEntry;
import com.robestone.species.parse.AbstractWorker;

public class SearchIndexBuilder extends AbstractWorker {

	public static void main(String[] args) {
		new SearchIndexBuilder(5, 3, 4).iterateOverKeys(new JsonOutputter());
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
	public static interface Visitor {
		void visit(KeyEntry key);
	}
	public static final Visitor SIMPLE_VISITOR = new Visitor() {
		@Override
		public void visit(KeyEntry key) {
			outputSimple(key);
		}
	};
	private static class KeyEntry {
		String key;
		int topScore;
		List<CandidateEntry> topMatches = new ArrayList<CandidateEntry>();
		List<KeyEntry> children = new ArrayList<KeyEntry>();
	}
	public static class CandidateEntry implements Comparable<CandidateEntry> {
		CompleteEntry entry;
		List<String> names = new ArrayList<>();
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

	public void iterateOverKeys(Visitor visitor) {
		List<CandidateEntry> entryNames = getCandidates();
		
		// TEMP
		entryNames = entryNames.subList(0, 500);
		
		iterateOverKeys(entryNames, visitor);
	}
	public void iterateOverKeys(List<CandidateEntry> candidates, Visitor visitor) {
		KeyEntry root = new KeyEntry();
		root.key = "";
		iterateOverKeys(root, candidates, visitor);
		
		outputSearchIndex(root, visitor);
	}
	
	public void outputSearchIndex(KeyEntry root, Visitor visitor) {
		root.key = "@root";
		doOutputSearchIndex(root, 0, visitor);
	}
	private void doOutputSearchIndex(KeyEntry key, int depth, Visitor visitor) {
		boolean lenShort = (key.key.length() < minKeyLen);
		if (key.topScore > 0 || depth == 0 || lenShort) {
			if (!lenShort) {
				visitor.visit(key);
			}
			key.children.forEach(c -> doOutputSearchIndex(c, depth + 1, visitor));
		}
	}

	private static void outputSimple(KeyEntry key) {
		System.out.print("root." + key.key + "." + key.topMatches.size() + ".");
		key.topMatches.forEach(m -> {
			System.out.print(m.matchedName + ", ");
		});
		System.out.println();
	}
	
	private void iterateOverKeys(KeyEntry key, List<CandidateEntry> candidates, Visitor visitor) {
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
				iterateOverKeys(nextKey, candidates, visitor);
			}
		}
	}
	
	private boolean visitKey(KeyEntry key, List<CandidateEntry> candidates) {
//		System.out.println("visitKey." + key.key);
		
		for (CandidateEntry candidate : candidates) {
			for (String name : candidate.names) {
				int score = score(name, key.key);
				if (score > 0 && (score >= key.topScore || key.topMatches.size() <= topMatchesMax)) {
					CandidateEntry copy = candidate.copy(score, name);
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
	
	public static class JsonOutputter implements Visitor {
		@Override
		public void visit(KeyEntry key) {
			String s = SearchIndexBuilder.toString(key);
			System.out.println(s);
		}
	}
	public static String toString(KeyEntry key) {
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
				buf.append(c.matchedName); // TODO this is going to be bad??
				if (count != key.topMatches.size()) {
					buf.append("\" },\n");
				} else {
					buf.append("\" }\n");
				}
				count++;
			}
			
			buf.append("\t\t]\n");
			buf.append("\t},\n");
		}
		buf.append("}");
		return buf.toString();
	}
	
}
