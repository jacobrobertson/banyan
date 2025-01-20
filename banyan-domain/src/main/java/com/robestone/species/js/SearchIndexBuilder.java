package com.robestone.species.js;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Tree;
import com.robestone.species.parse.AbstractWorker;

public class SearchIndexBuilder extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new SearchIndexBuilder(10, 3, 6, true).iterateOverKeys();
	}

	private int topMatchesMax;
	private String letters = "abcdefghijklmnopqrstuvwxyz".toUpperCase();
	private int minKeyLen;
	private int maxKeyLen;
	private boolean saveFile;
	private int minKeysPerFile = 200;
	private int testListSize = 1_000;
	
	public SearchIndexBuilder(int topMatchesMax, int minKeyLen, int maxKeyLen, boolean saveFile) {
		this.topMatchesMax = topMatchesMax;
		this.minKeyLen = minKeyLen;
		this.maxKeyLen = maxKeyLen;
		this.saveFile = saveFile;
	}
	private static class KeyEntry {
		String key;
		int topScore = 0;
		int allLocalsCount = 0;
		boolean outputted = false;
		List<CandidateEntry> topMatches = new ArrayList<CandidateEntry>();
		List<KeyEntry> children = new ArrayList<KeyEntry>();
	}
	public static class CandidateEntry implements Comparable<CandidateEntry> {
		CompleteEntry entry;
		Map<String, String> names = new HashMap<>();
		int score;
		int matchNameScorePart;
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

	public void iterateOverKeys() throws Exception {
		final Tree tree = speciesService.findInterestingTreeFromPersistence();
		List<CompleteEntry> entries = tree.getEntries();
		List<CandidateEntry> entryNames = getCandidates(entries);
		
		System.out.println("iterateOverKeys.found0." + entryNames.size());
		if (testListSize > 0) {
			Collections.shuffle(entryNames);
			List<CandidateEntry> subNames = entryNames.subList(0, testListSize);
			final Map<Integer, CandidateEntry> ids = new HashMap<>();
			subNames.forEach(c -> ids.put(c.entry.getId(), c));
			subNames.forEach(c -> addParents(c, tree, ids));
			entryNames = new ArrayList<>(ids.values());
		}
		System.out.println("iterateOverKeys.found." + entryNames.size());
		

		iterateOverKeys(entryNames);
	}
	void addParents(CandidateEntry c, Tree tree, Map<Integer, CandidateEntry> ids) {
		Integer pid = c.entry.getParentId();
		if (pid != null && !ids.containsKey(pid)) {
			CompleteEntry pe = tree.get(pid);
			CandidateEntry pc = toCandidate(pe);
			ids.put(pid, pc);
			addParents(pc, tree, ids);
		}
		
	}
	public void iterateOverKeys(List<CandidateEntry> candidates) throws Exception {
		KeyEntry root = new KeyEntry();
		root.key = "";
		iterateOverKeys(root, candidates);
	}
	
	private int iterateOverKeys(KeyEntry key, List<CandidateEntry> candidates) throws Exception {
		if (key.key.length() > maxKeyLen) {
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
//				&&
//				(key.key.length() >= minKeyLen || key.key.length() == 0)
				) {
			found++;
			// decide whether to save this or skip for now - we only want to save files when we can club them together
			saveKeyFile(key);
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
	
	private String toString(KeyEntry key, List<String> remoteKeys, Map<String, List<CandidateEntry>> localKeys) {
		StringBuilder buf = new StringBuilder("{\n");
		
		Collections.sort(remoteKeys);
		List<String> localKeyList = new ArrayList<String>(localKeys.keySet());
		Collections.sort(localKeyList);
		
		// local can be empty - can optimize this later if needed, but won't be a big problem with the grouping
		buf.append("\t\"local\" : {\n");
		int count = 1;
		for (String localKey : localKeyList) {
			List<CandidateEntry> keysLocals = localKeys.get(localKey);
			appendLocalEntries(localKey, keysLocals, buf);
			if (count != localKeyList.size()) {
				buf.append(", ");
			}
			buf.append("\n");
			count++;
		}
		buf.append("\t},\n");
		
		buf.append("\t\"remote\" : [");
		count = 1;
		for (String remoteKey : remoteKeys) {
			buf.append("\"");
			buf.append(remoteKey.toLowerCase());
			buf.append("\"");
			if (count != remoteKeys.size()) {
				buf.append(", ");
			}
			count++;
		}
		buf.append("]\n");
		
		buf.append("}");
		return buf.toString();
	}

	private void appendLocalEntries(String key, List<CandidateEntry> localKeys, StringBuilder buf) {
		buf.append("\t\t\"");
		buf.append(key.toLowerCase());
		buf.append("\" : [\n");

		int count = 1;
		for (CandidateEntry c : localKeys) {
			appendMatchedName(buf, c);
			if (count != localKeys.size()) {
				buf.append(",");
			}
			buf.append("\n");
			count++;
		}
		
		buf.append("\t\t]");
	}
	
	private void countLocals(KeyEntry key) {
		key.allLocalsCount = key.topMatches.size();
		for (KeyEntry child : key.children) {
			// all children have already been counted, I just need to add to this one
			key.allLocalsCount += child.allLocalsCount;
		}
	}

	/**
	 * Only called once we know the counts are there, so no need to check thresholds, etc.
	 */
	private void gatherRemotesAndLocals(KeyEntry key, List<String> remoteKeys, Map<String, List<CandidateEntry>> localKeys) {
		localKeys.put(key.key, key.topMatches);
		for (KeyEntry child : key.children) {
			if (child.outputted) {
				// don't recurse, it's already been outputted
				remoteKeys.add(child.key);
			} else {
				gatherRemotesAndLocals(child, remoteKeys, localKeys);
			}
		}
	}
	
	private void appendMatchedName(StringBuilder buf, CandidateEntry c) {
		buf.append("\t\t\t {\"id\" : ");
		buf.append(c.entry.getId());
		buf.append(", \"ids\" : \"");
		String ids = EntryUtilities.getCrunchedIdsForAncestors(c.entry);
		buf.append(ids); // crunched ids all the way up
		buf.append("\", \"name\" : \"");
		buf.append(JsonParser.escape(c.matchedName));
		buf.append("\" }");
	}
	
	private void saveKeyFile(KeyEntry key) throws Exception {

		if (key.key.toLowerCase().equals("nul")) {
			// TODO just force this into a fully remote file - it probably will be anyways once I start that.
			// I could also do some special case, like @nul or something, but would have to handle that in javascript too
			return;
		}

		// we are visiting this key to see if it should be saved
		countLocals(key);
		boolean isRoot = (key.key.length() == 0);
		if (!isRoot && key.allLocalsCount < minKeysPerFile) {
			return;
		}
		
		List<String> remoteKeys = new ArrayList<>();
		Map<String, List<CandidateEntry>> localKeys = new HashMap<String, List<CandidateEntry>>();
		gatherRemotesAndLocals(key, remoteKeys, localKeys);

		String json = toString(key, remoteKeys, localKeys);
		System.out.println(json);
		if (!saveFile) {
			return;
		}
		String path = "";
		String fileName;
		if (isRoot) {
			fileName = "@root.json";
		} else {
			String name = key.key.toLowerCase();
			// from ABCDEF to A/AB/ABC/ABCDE/ABCDEF
			int pos = 1;
			while (pos <= maxKeyLen && pos < name.length()) {
				String left = name.substring(0, pos);
				path += ("/" + left);
				pos++;
			}
			fileName = name + ".json";
		}

		String folder = JsonBuilder.outputDir + "/s" + path + "/" + fileName;
		
		File file = new File(folder);
		FileUtils.writeStringToFile(file, json, Charset.defaultCharset());
		key.outputted = true;
	}

}
