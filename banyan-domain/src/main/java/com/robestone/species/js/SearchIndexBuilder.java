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
	private int testListSize = -1;//1_000;
	
	private List<CandidateEntry> candidates;
	private Map<String, Set<CandidateEntry>> subKeyBuckets = new HashMap<String, Set<CandidateEntry>>();

	private static final int MAX_KEY_LEN = 30;
	
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
		int topScore = 0;
		Set<Integer> allDownstreamEntryIds = new HashSet<Integer>();
		boolean outputted = false;
		List<CandidateEntry> topMatches = new ArrayList<CandidateEntry>();
		List<KeyEntry> children = new ArrayList<KeyEntry>();
		public void fillEntryIds() {
			for (CandidateEntry match : topMatches) {
				allDownstreamEntryIds.add(match.entry.getId());
			}
		}
		/**
		 * This is a memory fix - too large of a tree being kept in memory - need to prune the tree once we're done
		 * @param key
		 */
		void dispose() {
			allDownstreamEntryIds = null;
			topMatches = null;
			children = null;
		}
	}
	public static class CandidateName {
		String cleanName;
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
		
		public List<CandidateName> getNames() {
			return names;
		}
		
		public CandidateName addName(String searchName, String prettyName, String cleanName, boolean isLatin, boolean image) {
			CandidateName n = new CandidateName();
			n.searchName = searchName;
			n.prettyName = prettyName;
			n.cleanName = cleanName;
			n.isLatin = isLatin;
			n.image = image;
			names.add(n);
			return n;
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
		
		System.out.println(">createCandidates.found." + entryNames.size());
//		if (testListSize > 0) {
//			Collections.shuffle(entryNames);
//			List<CandidateEntry> subNames = entryNames.subList(0, testListSize);
//			final Map<Integer, CandidateEntry> ids = new HashMap<>();
//			subNames.forEach(c -> ids.put(c.entry.getId(), c));
//			subNames.forEach(c -> addParents(c, tree, ids));
//			entryNames = new ArrayList<>(ids.values());
//		}
		System.out.println("<createCandidates.found." + entryNames.size());
		this.candidates = entryNames;
	}
	/**
	 * The whole purpose of this method is to ensure the search index has the exact same entries as the partition.
	 * We don't care about the "tree" just the entries, so we don't worry about hooking it all together.
	 */
	private Tree buildTree() {
		Node nroot = JsonBuilder.buildTree(speciesService);
		if (testListSize > 0) {
			pruneTree(nroot);
		}
		
		Map<Integer, CompleteEntry> map = new HashMap<Integer, CompleteEntry>();
		CompleteEntry root = buildTree(nroot, map);

		Tree tree = new Tree(root, map);
		return tree;
	}
	private void pruneTree(Node nroot) {
		List<Node> current = new ArrayList<Node>();
		int total = 1;
		current.add(nroot);
		while (true) {
			List<Node> next = getAllChildren(current);
			current = next;
			total += next.size();
			if (total > testListSize) {
				break;
			}
		}
		// these are the last round we collected, we need to get rid of the children
		for (Node node : current) {
			node.getChildren().clear();
			node.getChildIds().clear();
		}

	}
	private List<Node> getAllChildren(List<Node> next) {
		List<Node> all = new ArrayList<Node>();
		for (Node node : next) {
			all.addAll(node.getChildren());
		}
		return all;
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

		// visit only on terminal nodes, or when returning from one
		if (!key.topMatches.isEmpty() || !key.children.isEmpty()) {
			found++;
			// decide whether to save this or skip for now - we only want to save files when we can club them together
			saveKeyFile(key);
		}
		return found;
	}
	
	private boolean buildKey(KeyEntry key) {
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
	
	public static int score(CandidateName name, String key) {
		int score = 0;
		if (name.searchName.equals(key)) {
			// in this case we have an exact match, these are absolutely the highest
			score = 10_000_000;
		} else {
			if (name.searchName.startsWith(key)) {
				score += 1_000_000;
			}
			if (containsKeyAsName(name.cleanName, key)) {
				score += 100_000;
			} 
			if (name.searchName.contains(key)) {
				score += 10_000;
			}
		}
		if (!name.isLatin) {
			score *= 2;
		}
		
		// give a boost based on how short the name is that it's matching
		if (score > 0) {
			score += ((float) key.length() / (float) name.searchName.length()) * 1000;
			// slight preference if there is an image - this will only help in rare common name cases
			if (name.image) {
				score += 10;
			}
		}
		
		return score;
	}
	private static boolean containsKeyAsName(String name, String key) {
		return (" " + name + " ").contains(" " + key + " ");
	}
	
	public CandidateEntry toCandidate(CompleteEntry entry) {
		CandidateEntry candidate = new CandidateEntry();
		candidate.entry = entry;
		
		String latin = cleanSearchName(entry.getLatinName(), true);
		if (latin != null) {
			String clean = cleanSearchName(entry.getLatinName(), false);
			candidate.addName(latin, entry.getLatinName(), clean, true, entry.getImageLink() != null);
		}
		
		String common = cleanSearchName(entry.getCommonName(), true);
		if (common != null) {
			String clean = cleanSearchName(entry.getCommonName(), false);
			candidate.addName(common, entry.getCommonName(), clean, false, entry.getImageLink() != null);
		}
		
		return candidate;
	}
	private String cleanSearchName(String name, boolean removeSpaces) {
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
		if (removeSpaces) {
			name = StringUtils.remove(name, ' ');
		}
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

	
	/**
	 * Combining duplicate child keys.
	 * Why this is wanted - because then I can expand the search key length out to 20 or more and for the most part it's not going to create too many more files
	 * 
		"jia" : [257430, 329822, 74487, 77283, 205510, 173452, 15106, 308682, 222967, 150404, 65059, 116423, 188518, 79289, 49638], 
		"jiaa" : [188516], 
		"jiaan" : [188516], 
		"jiaang" : [188516], 
		"jiaangu" : [188516], 
		"jiaangus" : [188516], 
		"jiaangust" : [188516], 
		"jiaangusti" : [188516], 
		"jiaangustif" : [188516], 
		"jiaangustifo" : [188516], 
		"jiab" : [65059], 
		"jiabu" : [65059], 
		"jiabum" : [65059], 
		"jiabumu" : [65059], 
		"jiabumui" : [65059], 
		"jiae" : [116423, 89600], 
		"jiam" : [205510, 188520], 
		"jiama" : [205510], 
		"jiamar" : [205510], 
		
		What do we really want?  We want to take the above example, and combine when these conditions are met
		1. any key with a parent that has it as it's only child
		2. and has the exact same entries in the list (could be 1 or could be many)
		
		In these cases, "delete" the child and tell the parent to store the child's key as a duplicate 
		
		results:
		
		"jia" : [257430, 329822, 74487, 77283, 205510, 173452, 15106, 308682, 222967, 150404, 65059, 116423, 188518, 79289, 49638], 
		"jiaa|ngustifo" : [188516], 
		"jiab|umui" : [65059], 
		"jiae" : [116423, 89600], 
		"jiam" : [205510, 188520], 
		"jiama|r" : [205510], 
	 */

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
	
	private String toString(List<String> remoteKeys, Map<String, List<CandidateEntry>> localKeys) {
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

		appendLocalKeys(buf, localKeys, localKeyList);
		
		Collections.sort(remoteKeys);
		buf.append("\t\"remote\" : [");
		int count = 1;
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

	private void appendLocalKeys(StringBuilder buf, Map<String, List<CandidateEntry>> localKeys, List<String> localKeyList) {
		// local can be empty - can optimize this later if needed, but won't be a big problem with the grouping
		buf.append("\t\"local\" : {\n");
		
		
		 Map<String, Set<Integer>> treeKeys = buildLocalKeyTree(localKeyList, localKeys);
		 List<String> keyNamesList = new ArrayList<String>(treeKeys.keySet());
		 Collections.sort(keyNamesList);
		
		int count = 1;
		for (String localKey : keyNamesList) {

			buf.append("\t\t\"");
			buf.append(localKey.toLowerCase());
			buf.append("\" : ");

			Set<Integer> idsSet = treeKeys.get(localKey);
			List<Integer> idsList = new ArrayList<Integer>(idsSet);
			Collections.sort(idsList);
			// this is implementation dependent, but I think it's safe?
			buf.append(idsList);

			if (count != keyNamesList.size()) {
				buf.append(", ");
			}
			buf.append("\n");
			count++;
		}
		buf.append("\t},\n");
	}
	private static class LocalKey {
		String key;
		String longestKey;
		Set<Integer> ids = new HashSet<Integer>();
		List<LocalKey> children = new ArrayList<LocalKey>();
		LocalKey parent;
	}
	
	private Map<String, Set<Integer>> buildLocalKeyTree(List<String> localKeyList, Map<String, List<CandidateEntry>> localKeysToCandidates) {
		// Step 1 - build the tree
		Map<String, LocalKey> localKeysMap = new HashMap<String, LocalKey>();
		for (String key : localKeyList) {
			LocalKey lkey = buildLocalKey(key, localKeysToCandidates);
			localKeysMap.put(key, lkey);
		}
		for (String key : localKeyList) {
			LocalKey lkey = localKeysMap.get(key);
			LocalKey parent = localKeysMap.get(key.substring(0, key.length() - 1));
			if (parent != null) {
				parent.children.add(lkey);
				lkey.parent = parent;
			}
		}
		
		// Step 2 - prune each tree and combine the keys
		List<LocalKey> toPrune = new ArrayList<>();
		for (String key : localKeyList) {
			LocalKey lkey = localKeysMap.get(key);
			if (lkey.parent == null) {
				toPrune.add(lkey);
			}
		}
		boolean pruned = true;
		while (pruned) {
			pruned = false;
			for (LocalKey lkey : toPrune) {
				boolean pruneThis = pruneLocalKey(lkey);
				pruned = pruned || pruneThis;
			}
		}

		// Step 3 - grab all the keys
		Map<String, Set<Integer>> combinedKeys = new HashMap<String, Set<Integer>>();
		for (LocalKey lkey : toPrune) {
			addCombinedLocalKeys(lkey, combinedKeys);
		}
		
		return combinedKeys;
	}
	private void addCombinedLocalKeys(LocalKey key, Map<String, Set<Integer>> combinedKeys) {
		if (key.longestKey != null) {
			String combinedKey = key.key + "|" + key.longestKey.substring(key.key.length());
			combinedKeys.put(combinedKey, key.ids);
		} else {
			combinedKeys.put(key.key, key.ids);
		}
		for (LocalKey child : key.children) {
			addCombinedLocalKeys(child, combinedKeys);
		}
	}
	private boolean pruneLocalKey(LocalKey key) {
		boolean pruned = false;
		List<LocalKey> children = new ArrayList<LocalKey>(key.children);
		for (LocalKey child : children) {
			boolean prunedThis = pruneLocalKey(child);
			pruned = pruned || prunedThis;
		}
		if (	key.parent != null && key.parent.longestKey == null
				// the parent has just one child (this key), and it has the same ids
			 && key.parent.children.size() == 1 
			 && key.ids.size() == key.parent.ids.size() && key.ids.containsAll(key.parent.ids)) {
			String longest = key.longestKey;
			if (longest == null) {
				longest = key.key;
				key.longestKey = key.key;
			}
			key.parent.longestKey = longest;
			key.parent.children.remove(key);
			key.parent.children.addAll(key.children);
			key.parent = null;
			pruned = true;
		}
		return pruned;
	}
	private LocalKey buildLocalKey(String key, Map<String, List<CandidateEntry>> localKeys) {
		LocalKey lkey = new LocalKey();
		lkey.key = key;
		List<CandidateEntry> entries = localKeys.get(key);
		for (CandidateEntry ce : entries) {
			lkey.ids.add(ce.entry.getId());
		}
		return lkey;
	}

	private void countLocals(KeyEntry key) {
		key.fillEntryIds();
		for (KeyEntry child : key.children) {
			// all children have already been filled, I just need to add to this one
			if (!child.outputted) {
				key.allDownstreamEntryIds.addAll(child.allDownstreamEntryIds);
			}
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

		// we are visiting this key to see if it should be saved
		countLocals(key);
		
		// these cannot be saved to drive, so skip and they will get combined elsewhere
		boolean isNul = key.key.toLowerCase().startsWith("nul");
		if (isNul) {
			return;
		}
		boolean isRoot = (key.key.length() == 0);
		if (!isRoot && key.allDownstreamEntryIds.size() < minKeysPerFile) {
			return;
		}
		
		List<String> remoteKeys = new ArrayList<>();
		Map<String, List<CandidateEntry>> localKeys = new HashMap<String, List<CandidateEntry>>();
		gatherRemotesAndLocals(key, remoteKeys, localKeys);

		String json = toString(remoteKeys, localKeys);
		System.out.println(json);
		System.out.println("saveKeyFile." + key.key + "." + key.allDownstreamEntryIds.size());
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
		key.dispose();
	}
	

}
