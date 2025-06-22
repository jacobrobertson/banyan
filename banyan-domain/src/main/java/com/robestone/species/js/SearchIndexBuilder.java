package com.robestone.species.js;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Tree;
import com.robestone.species.parse.AbstractWorker;
import com.robestone.util.html.EntityMapper;

public class SearchIndexBuilder extends AbstractWorker {

	public static void main(String[] args) throws Exception {
//		new SearchIndexBuilder(5, 3, 4, false).run();
		new SearchIndexBuilder().run();
	}

	private int topMatchesMax;
	private String letters = "abcdefghijklmnopqrstuvwxyz".toUpperCase();
	private int minKeyLen;
	private int maxKeyLen;
	private boolean saveFile;
	private int minKeysPerFile = 200;
	private int testListSize = 0; //1_000;
	
	private List<CandidateEntry> candidates;
	private Map<String, Set<CandidateEntry>> subKeyBuckets = new HashMap<String, Set<CandidateEntry>>();

	private static final int MAX_KEY_LEN = 12;
	
	public SearchIndexBuilder() {
		this(15, 3, MAX_KEY_LEN, true);
	}
	public SearchIndexBuilder(int topMatchesMax, int minKeyLen, int maxKeyLen, boolean saveFile) {
		this(topMatchesMax, minKeyLen, maxKeyLen, saveFile, null);
	}
	public SearchIndexBuilder(int topMatchesMax, int minKeyLen, int maxKeyLen, boolean saveFile, List<CandidateEntry> candidates) {
		this.topMatchesMax = topMatchesMax;
		this.minKeyLen = minKeyLen;
		this.maxKeyLen = maxKeyLen;
		this.saveFile = saveFile;
		this.candidates = candidates;
	}
	public void run() throws Exception {
		createCandidates();
		iterateOverKeys();
	}
	private static class KeyEntry {
		String key;
		String longestDuplicateChildKey;
		int topScore = 0;
		int allLocalsCount = 0;
		boolean outputted = false;
		List<CandidateEntry> topMatches = new ArrayList<CandidateEntry>();
		List<KeyEntry> children = new ArrayList<KeyEntry>();
		
		/**
		 * Only add if it is actually longer - which I think it might never be?
		 */
		void pullUpDuplicateChildKey(KeyEntry child) {
			String childKey = child.longestDuplicateChildKey;
			if (childKey == null) {
				childKey = child.key;
			}
			if (longestDuplicateChildKey == null) {
				longestDuplicateChildKey = childKey;
			} else if (longestDuplicateChildKey.length() < childKey.length()) {
				// TODO validate this condition is needed
				longestDuplicateChildKey = childKey;
			}
		}
	}
	public static class CandidateName {
		String searchName;
		String prettyName;
		boolean image;
		boolean isLatin;
	}
	public static class CandidateEntry implements Comparable<CandidateEntry> {
		CompleteEntry entry;
		List<CandidateName> names = new ArrayList<CandidateName>();
		int score;
		int matchNameScorePart;
		String matchedName;
		
		@Override
		public int compareTo(CandidateEntry c) {
			return (int) (c.score - score);
		}
		
		public void addName(String searchName, String prettyName, boolean isLatin, boolean image) {
			CandidateName n = new CandidateName();
			n.searchName = searchName;
			n.prettyName = prettyName;
			n.isLatin = isLatin;
			n.image = image;
			names.add(n);
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

	public void setCandidates(List<CandidateEntry> candidates) {
		this.candidates = candidates;
	}
	void createCandidates() throws Exception {
		final Tree tree = buildTree();
		List<CompleteEntry> entries = tree.getEntries();
		List<CandidateEntry> entryNames = toCandidates(entries);
		
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
		this.candidates = entryNames;
	}
	/**
	 * The whole purpose of this method is to ensure the search index has the exact same entries as the partition.
	 * We don't care about the "tree" just the entries, so we don't worry about hooking it all together.
	 */
	private Tree buildTree() {
		Node nroot = JsonBuilder.buildTree(speciesService);
		
		Map<Integer, CompleteEntry> map = new HashMap<Integer, CompleteEntry>();
		CompleteEntry root = buildTree(nroot, map);

		Tree tree = new Tree(root, map);
		return tree;
	}
	private CompleteEntry buildTree(Node node, Map<Integer, CompleteEntry> map) {
		CompleteEntry entry = speciesService.findEntry(node.getId());
		map.put(entry.getId(), entry);
		for (Node child : node.getChildren()) {
			CompleteEntry centry = buildTree(child, map);
			centry.setParent(entry);
		}
		return entry;
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
	
	private void createBuckets() {
		System.out.println("createBuckets>");
		createBuckets("");
		System.out.println("createBuckets<");
	}
	private void createBuckets(String current) {
		for (int i = 0; i < letters.length(); i++) {
			char c = letters.charAt(i);
			String next = current + c;
			if (next.length() < minKeyLen) {
				createBuckets(next);
			} else if (next.length() < maxKeyLen) {
				Set<CandidateEntry> bucket = subKeyBuckets.get(next);
				if (bucket == null) {
					bucket = new HashSet<CandidateEntry>();
					subKeyBuckets.put(next, bucket);
				}
				for (CandidateEntry candidate : candidates) {
					for (CandidateName name : candidate.names) {
						if (name.searchName.indexOf(next) >= 0) {
							bucket.add(candidate);
							break;
						}
					}
				}
			}
		}
	}
	
	public void iterateOverKeys() throws Exception {
		createBuckets();
		KeyEntry root = new KeyEntry();
		root.key = "";
		iterateOverKeys(root);
	}
	
	private int iterateOverKeys(KeyEntry key) throws Exception {
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
				foundMatches = buildKey(nextKey);
			}

			// iterate if a match, or if it's a short key
			if (foundMatches) {
				// we only go deeper if we found a match, otherwise there's no point
				int nextFound = iterateOverKeys(nextKey);
				found += nextFound;
				
				if (nextFound > 0) {
					key.children.add(nextKey);
				}
			}
		}
		
		// TODO - I think this is where I should remove duplicate children (same entry, but with longer local keys mapped)
		//			that way it happens before the counting takes place
		// 			and because we do it every time, we only need to look at the children
		// NO - this is preventing "strawberryg" from working - if they type it in and that key doesn't exist, it doesn't know what to do
		identifyDuplicateChildKeys(key);
		
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
	
	/**
	 * Example - we should remove the second one, it's redundant.
	 * TODO - what I actually want to do is create an indicator in some way to show the redundancy, as deep as it can go
	 * example - "strawberryg|uava" - means anything from strawberryg onwards is the same result (but not strawberryz, etc)
	 * "alasiasandb" : [
			 {"id" : 369882, "ids" : "_1ydQ", "name" : "Central Asia Sand Boa" }
		], 
		"alasiasandbo" : [
			 {"id" : 369882, "ids" : "_1ydQ", "name" : "Central Asia Sand Boa" }
		], 
	 */
	private void identifyDuplicateChildKeys(KeyEntry key) throws Exception {
		if (key.topMatches.size() != 1) {
			return;
		}
		CandidateEntry entry = key.topMatches.get(0);
		for (KeyEntry child : key.children) {
			if (child.topMatches.size() == 1 && child.topMatches.get(0).entry == entry.entry) {
				child.topMatches.clear();
				key.pullUpDuplicateChildKey(child);
			}
		}
	}
	
	private boolean buildKey(KeyEntry key) {
		if (key.key.equals("ANTS")) {
			System.out.println("buildKey > " + key.key);
		}
		
		Set<CandidateEntry> intersection = createIntersection(key);
		
		for (CandidateEntry candidate : intersection) {
			for (CandidateName name : candidate.names) {
				int score = score(name, key.key);
				if (score > 0 && (score > key.topScore || key.topMatches.size() <= topMatchesMax)) {
					CandidateEntry copy = candidate.copy(score, name.prettyName);
					if (score > key.topScore) {
						key.topScore = score;
					}
					key.topMatches.add(copy);
					Collections.sort(key.topMatches);
					if (key.topMatches.size() > topMatchesMax) {
						key.topMatches.remove(topMatchesMax);
					}
					// break now - we don't want the id in the list twice
					break;
				}
			}
		}

		boolean found = !key.topMatches.isEmpty();
		if (found) {
//			System.out.println("visitKey." + key.key + "." + key.topMatches.size());
		}
//		System.out.println("buildKey < " + key.key + "." + key.allLocalsCount);
		
		return found;
	}
	Set<String> getSubKeys(String key) {
		Set<String> subkeys = new HashSet<String>();
		if (key.length() < minKeyLen) {
			return subkeys;
		}
		int maxPos = key.length() - minKeyLen + 1;
		for (int i = 0; i < maxPos; i++) {
			String subKey = key.substring(i, i + minKeyLen);
			subkeys.add(subKey);
		}
		return subkeys;
	}
	private Set<CandidateEntry> createIntersection(KeyEntry key) {
		Set<String> subkeys = getSubKeys(key.key);
		Set<CandidateEntry> intersection = null;
		for (String subkey : subkeys) {
			Set<CandidateEntry> bucket = subKeyBuckets.get(subkey);
			if (intersection == null) {
				intersection = new HashSet<CandidateEntry>(bucket);
			} else {
				intersection.retainAll(bucket);
			}
		}
		return intersection;
	}
	
	private int score(CandidateName name, String key) {
		int score = 0;
		if (name.searchName.equals(key)) {
			// in this case we have an exact match, these are absolutely the highest
			if (name.isLatin) {
				score = 10_000_000;
			} else {
				score = 20_000_000;
			}
		} else if (name.searchName.startsWith(key)) {
			if (name.isLatin) {
				score = 1_000_000;
			} else {
				score = 2_000_000;
			}
		} else if (containsKeyAsName(name.searchName, key)) {
			if (name.isLatin) {
				score = 100_000;
			} else {
				score = 200_000;
			}
		} else if (name.searchName.contains(key)) {
			if (name.isLatin) {
				score = 10_000;
			} else {
				score = 20_000;
			}
		}

		// give a boost based on how short the name is it's matching
		if (score > 0) {
			score += ((float) key.length() / (float) name.searchName.length()) * 1000;
			// slight preference if there is an image - this will only help in rare common name cases
			if (name.image) {
				score += 10;
			}
		}
		
		return score;
	}
	private boolean containsKeyAsName(String name, String key) {
		return (" " + name + " ").contains(" " + key + " ");
	}
	
	public CandidateEntry toCandidate(CompleteEntry entry) {
		CandidateEntry candidate = new CandidateEntry();
		candidate.entry = entry;
		
		String latin = cleanSearchName(entry.getLatinName());
		if (latin != null) {
			candidate.addName(latin, entry.getLatinName(), true, entry.getImageLink() != null);
		}
		
		String common = cleanSearchName(entry.getCommonName());
		if (common != null) {
			candidate.addName(common, entry.getCommonName(), false, entry.getImageLink() != null);
		}
		
		return candidate;
	}
	private String cleanSearchName(String name) {
		if (name == null) {
			return null;
		}
		name = EntityMapper.replaceUnparseableCharacters(name, ' ');
		try {
			name = EntityMapper.convertToSearchText(name);
		} catch (Exception e) {
			// means there's something bad going on maybe?
			e.printStackTrace();
		}
		name = name.toUpperCase();
		name = StringUtils.replacePattern(name, "[^A-Z]", " ");
		name = StringUtils.remove(name, ' ');
		return name;
	}
	private List<CandidateEntry> toCandidates(List<CompleteEntry> entries) {
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
		
		// remove any without matches
		List<String> localKeyList = new ArrayList<String>();
		for (String localKey : localKeys.keySet()) {
			List<CandidateEntry> keysLocals = localKeys.get(localKey);
			if (!keysLocals.isEmpty()) {
				localKeyList.add(localKey);
			}
		}
		Collections.sort(localKeyList);
		
		// local can be empty - can optimize this later if needed, but won't be a big problem with the grouping
		buf.append("\t\"local\" : {\n");
		int count = 1;
		for (String localKey : localKeyList) {
			List<CandidateEntry> keysLocals = localKeys.get(localKey);
			appendLocalIds(localKey, keysLocals, buf);
			if (count != localKeyList.size()) {
				buf.append(", ");
			}
			buf.append("\n");
			count++;
		}
		buf.append("\t},\n");
		
		Collections.sort(remoteKeys);
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
		buf.append("],\n");
		
		buf.append("\t\"entries\" : {\n");
		// output unique candidates
		Set<Integer> foundIds = new HashSet<Integer>();
		for (String localKey : localKeyList) {
			List<CandidateEntry> some = localKeys.get(localKey);
			for (CandidateEntry entry : some) {
				if (foundIds.add(entry.entry.getId())) {
					appendEntry(buf, entry);
					buf.append(",\n");
				}
			}
			count++;
		}
		if (!foundIds.isEmpty()) {
			buf.setLength(buf.length() - 2);
		}
		buf.append("\n\t}\n");
		
		
		buf.append("}");
		return buf.toString();
	}

	private void appendLocalIds(String key, List<CandidateEntry> localKeys, StringBuilder buf) {
		buf.append("\t\t\"");
		buf.append(key.toLowerCase());
		buf.append("\" : ");

		List<Integer> ids = localKeys.stream().map(ce -> ce.entry.getId()).collect(Collectors.toList());
		// this is implementation dependent, but I think it's safe?
		buf.append(ids);
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
	
	private void appendEntry(StringBuilder buf, CandidateEntry c) {
		buf.append("\t\t\"");
		buf.append(c.entry.getId());
		buf.append("\" : { \"ids\" : \"");
		String ids = EntryUtilities.getCrunchedIdsForAncestors(c.entry);
		buf.append(ids); // crunched ids all the way up
//		buf.append("\", \"name\" : \"");
//		buf.append(JsonParser.escape(c.matchedName));
		buf.append("\", \"latin\" : \"");
		buf.append(JsonParser.escape(c.entry.getLatinName()));
		buf.append("\"");
		if (c.entry.getCommonName() != null) {
			buf.append(", \"common\" : \"");
			buf.append(JsonParser.escape(c.entry.getCommonName()));
			buf.append("\"");
		}
		buf.append(" }");
	}

	
	private void saveKeyFile(KeyEntry key) throws Exception {

//		if (key.key.toLowerCase().equals("nul")) {
//			// TODO just force this into a fully remote file - it probably will be anyways once I start that.
//			// I could also do some special case, like @nul or something, but would have to handle that in javascript too
//			return;
//		}

		// we are visiting this key to see if it should be saved
		countLocals(key);
		
		// these cannot be saved to drive, so skip and they will get combined elsewhere
		boolean isNul = key.key.toLowerCase().startsWith("nul");
		if (isNul) {
			return;
		}
		boolean isRoot = (key.key.length() == 0);
		if (!isRoot && key.allLocalsCount < minKeysPerFile) {
			return;
		}
		System.out.println("saveKeyFile." + key.key + "." + key.allLocalsCount);
		
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
