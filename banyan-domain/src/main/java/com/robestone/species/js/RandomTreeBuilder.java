package com.robestone.species.js;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.SpeciesService;
import com.robestone.species.parse.AbstractWorker;
import com.robestone.species.parse.ImagesCreater;
import com.robestone.species.parse.ImagesCreater.ImageInfo;

public class RandomTreeBuilder extends AbstractWorker {
	

	public Collection<Entry> buildRandomTree(Integer pinnedId) throws Exception {
		Collection<Entry> entries = buildRandomTreeByMatching(pinnedId);
		if (entries == null) {
			entries = buildRandomTreeForLeaves(pinnedId);
		}
		if (entries != null) {
			unpinAncestorsWithSameImage(entries);
			unpinWithNoImage(entries);
		}
		return entries;
	}
	
	private void unpinWithNoImage(Collection<Entry> entries) {
		for (Entry e : entries) {
			if (e.getImage() == null) {
				e.setPinned(false);
			}
		}
	}
	private void unpinAncestorsWithSameImage(Collection<Entry> entries) {
		for (Entry e : entries) {
			unpinAncestorsWithSameImage(e);
		}
	}
	private void unpinAncestorsWithSameImage(Entry entry) {
		if (entry.getImage() == null) {
			return;
		}
		ImageInfo ii = ImagesCreater.toImageInfo(entry);
		String image = ii.getFilePathRelative();
		Entry p = entry.getParent();
		while (p != null) {
			if (p.getImage() != null) {
				String pimage = ImagesCreater.toImageInfo(p).getFilePathRelative();
				if (pimage.equals(image)) {
					p.setPinned(false);
				}
			}
			
			p = p.getParent();
		}
	}
	
	// not sure why I need to do this, but apparently some interesting species aren't hooked in
	private boolean isValidEntry(Integer iid) throws Exception {
		int id;
		while (true) {
			id = iid;
			if (id == -1) {
				return false;
			} else if (id == SpeciesService.TREE_OF_LIFE_ID) {
				return true;
			}
			Entry e = speciesService.findEntry(id);
			iid = e.getParentId();
			if (iid == null) {
				return false;
			}
		}
	}
	
	/**
	 * Since we couldn't find the matching tree, just look for any tree with 4 leaves.
	 * But no more than 2 leaves from a given parent.
	 */
	private Collection<Entry> buildRandomTreeForLeaves(Integer pinnedId) throws Exception {
		
		if (!isValidEntry(pinnedId)) {
			return null;
		}
		
		List<Entry> entries = new ArrayList<>();
		Entry pinned = speciesService.findEntry(pinnedId);
		entries.add(pinned);
		
		// first try to get two children
		findInterestingChildren(pinned);
		if (pinned.getChildren().size() > 1) {
			entries.add(pinned.getCompleteEntryChildren().get(0));
			entries.add(pinned.getCompleteEntryChildren().get(1));
		}
		
		// one sibling
		Entry parent = speciesService.findEntry(pinned.getParentId());
		findInterestingChildren(parent);
		for (Entry sibling : parent.getCompleteEntryChildren()) {
			if (sibling.getId().equals(pinned.getId())) {
				continue;
			}
			entries.add(sibling);
			break;
		}
		
		Entry nextParent = parent;
		while (entries.size() < 5) {
			// one cousin, and a nephew.  If not, go up one level
			Entry gparent = speciesService.findEntry(nextParent.getParentId());
			findInterestingChildren(gparent);
			for (Entry uncle : gparent.getCompleteEntryChildren()) {
				if (uncle.getId().equals(nextParent.getId())) {
					continue;
				}
				findInterestingChildren(uncle);
				if (!uncle.getCompleteEntryChildren().isEmpty()) {
					entries.add(uncle);
					entries.add(uncle.getCompleteEntryChildren().get(0));
				}
				break;
			}
			nextParent = gparent;
		}
		
		Set<Integer> ids = new HashSet<>();
		for (Entry e : entries) {
			ids.add(e.getId());
		}
		Entry root = speciesService.findTreeForNodes(ids);
		Set<Entry> set = EntryUtilities.getEntries(root);
		for (Entry e : set) {
			if (ids.contains(e.getId())) {
				e.setPinned(true);
			}
		}
		
		return set;
	}

	private Collection<Entry> buildRandomTreeByMatching(Integer pinnedId) throws Exception {
	
		List<Entry> entries = new ArrayList<>();
		Entry pinned = speciesService.findEntry(pinnedId);
	
		addParents(4, entries, pinned);
		
		List<Entry> children = speciesService.findChildren(pinnedId);
		entries.addAll(children);
		for (Entry child : children) {
			List<Entry> gchildren = speciesService.findChildren(child.getId());
			entries.addAll(gchildren);
		}
		
		MatchResult result = null;
		for (Entry root : entries) {
			// get the entry again, because we will be changing the children
			root = speciesService.findEntry(root.getId());
			result = matchTree(root, pinnedId);
			// ensure that the tree actually matches the pattern
			if (result.matched && result.pinnedInIds) {
				break;
			}
		}
		if (result != null && result.matched && result.pinnedInIds) {
			// get the ancestors
			Entry root = speciesService.findTreeForNodes(result.getAllRecursiveAllIds());
			Set<Entry> set = EntryUtilities.getEntries(root);
			Set<Integer> pinnedIds = result.getAllRecursivePinnedIds();
			
			unpinAncestors(root, pinnedId, pinnedIds);
			markPinned(root, pinnedIds);
			return set;
		} else {
			return null;
		}
	}

	private void markPinned(Entry root, Set<Integer> pinnedIds) {
		if (pinnedIds.contains(root.getId())) {
			root.setPinned(true);
		} else {
			root.setPinned(false);
		}
		if (root.getCompleteEntryChildren() != null) {
			for (Entry e : root.getCompleteEntryChildren()) {
				markPinned(e, pinnedIds);
			}
		}
	}

	/**
	 * Tree will look like this, with any one of the nodes being the original pinned node. O means shown * means Pinned.
	 * Any of the "O" could end up being the original pinned, in which case it is also pinned.
	 *         O1 (r)
	 *        / \
	 *       *2  *3
	 *      / \   \
	 *     O4  O5  *6
	 *    / \
	 *   *7  *8
	 */
	private MatchResult matchTree(Entry r, Integer pinnedId) {
		findInterestingChildren(r);
		
		MatchResult result = new MatchResult(r, "1");
		boolean rootPinned = pinnedId.equals(r.getId());
		boolean pinnedFound = rootPinned;
		List<Entry> children = r.getCompleteEntryChildren();
		
		// look for all possible node2 matches - we need to keep all since we don't
		// know which of node3 will be selected
		List<MatchResult> results2 = new ArrayList<>();
		for (Entry child : children) {
			MatchResult maybe2 = matchTreeNode2(child, pinnedId, pinnedFound);
			if (maybe2.matched) {
				pinnedFound = pinnedFound || maybe2.isPinnedInIdsRecursive(pinnedId);
				results2.add(maybe2);
			}
		}
	
		boolean pinnedIn3 = false;
		List<MatchResult> results3 = new ArrayList<>();
		for (Entry child : children) {
			MatchResult maybe3 = matchTreeNode3(child, pinnedId, pinnedFound);
			if (maybe3.matched) {
				pinnedFound = pinnedFound || maybe3.isPinnedInIdsRecursive(pinnedId);
				if (maybe3.isPinnedInIdsRecursive(pinnedId)) {
					pinnedIn3 = true;
				}
				results3.add(maybe3);
			}
		}
	
		// at this level we don't bother continuing if pinned not found
		if (pinnedFound && !results2.isEmpty() && !results3.isEmpty()) {
			List<Entry> nodes2and3 = new ArrayList<>();
			
			MatchResult selectedR2 = null;
			MatchResult selectedR3 = null;
			if (rootPinned) {
				// we don't care which of the two branches we take
				selectedR2 = results2.get(0);
				selectedR3 = results3.get(0);
			} else if (pinnedIn3) {
				selectedR2 = results2.get(0);
				for (MatchResult r3 : results3) {
					if (r3.isPinnedInIdsRecursive(pinnedId)) {
						selectedR3 = r3;
						break;
					}
				}
			} else {
				selectedR3 = results3.get(0);
				for (MatchResult r2 : results2) {
					if (r2.isPinnedInIdsRecursive(pinnedId)) {
						selectedR2 = r2;
						break;
					}
				}
			}
			
			r.setChildren(nodes2and3);
			result.childResults.add(selectedR2);
			result.childResults.add(selectedR3);
			result.pinnedInIds = true;
			result.selectedRoot = r;
			result.allIds.add(r.getId());
			result.allIds.add(selectedR2.selectedRoot.getId());
			result.allIds.add(selectedR3.selectedRoot.getId());
			result.pinnedIds.add(selectedR2.selectedRoot.getId());
			result.pinnedIds.add(selectedR3.selectedRoot.getId());
			result.matched = true;
		}
		
		return result;
	}

	/**
		 *       *2 (r)
		 *      / \
		 *     O4  O5
		 *    / \
		 *   *7  *8
		 */
		private MatchResult matchTreeNode2(Entry r, Integer pinnedId, boolean pinnedFound) {
			findInterestingChildren(r);
	
			MatchResult result = new MatchResult(r, "2");
			boolean rootPinned = pinnedId.equals(r.getId());
			pinnedFound = pinnedFound || rootPinned;
			List<Entry> children = r.getCompleteEntryChildren();
			MatchResult result4 = null;
			for (Entry child : children) {
				MatchResult maybeResult4 = matchTreeNode4(child, pinnedId, pinnedFound);
				if (maybeResult4.matched) {
					result4 = maybeResult4;
					pinnedFound = pinnedFound || maybeResult4.pinnedInIds;
					// we can stop as soon as we find a match and the pinned is found, otherwise keep looking
					if (pinnedFound) {
						break;
					}
				}
			}
			if (result4 != null && result4.matched) {
				// first try to fill node 5 with the pinned
				Entry node5 = null;
				if (!pinnedFound) {
					for (Entry child : children) {
						if (child == result4.selectedRoot) {
							continue;
						}
						pinnedFound = pinnedId.equals(child.getId());
						if (pinnedFound) {
							node5 = child;
							break;
						}
					}
				}
	
				if (node5 == null) {
					// just grab the first child 
					for (Entry child : children) {
						if (child == result4.selectedRoot) {
							continue;
						}
						node5 = child;
						break;
					}				
				}
				if (node5 != null) {
					List<Entry> nodes4And5 = new ArrayList<>();
					nodes4And5.add(result4.selectedRoot);
					nodes4And5.add(node5);
					result.childResults.add(result4);
	//				result.allIds.add(r.getId()); // parent will count this node
	//				result.pinnedIds.add(r.getId());
					result.allIds.add(node5.getId());
					result.allIds.add(result4.selectedRoot.getId());
	//				result.allIds.addAll(result4.allIds);
	//				result.pinnedIds.addAll(result4.pinnedIds);
					r.setChildren(nodes4And5);
					result.matched = true;
					result.pinnedInIds = 
							(result4.selectedRoot.getId().equals(pinnedId) || node5.getId().equals(pinnedId));
				}
			}
			
			return result;
		}

	/**
		 *   *3
		 *    \
		 *     *6
		 */
		private MatchResult matchTreeNode3(Entry r, Integer pinnedId, boolean pinnedFound) {
			findInterestingChildren(r);
	
			MatchResult result = new MatchResult(r, "3");
			boolean rootPinned = pinnedId.equals(r.getId());
			pinnedFound = pinnedFound || rootPinned;
			List<Entry> children = r.getCompleteEntryChildren();
			
			// if possible get the pinned child
			Entry node6 = null;
			if (!pinnedFound) {
				for (Entry child : children) {
					pinnedFound = pinnedFound || pinnedId.equals(child.getId());
					if (pinnedFound) {
						node6 = child;
						break;
					}
				}
			}
			if (node6 == null) {
				for (Entry child : children) {
					node6 = child;
					result.matched = true;
					break;
				}
			}
	
			if (result.matched) {
	//			result.allIds.add(r.getId()); // parent will count this node
				result.allIds.add(node6.getId());
				result.pinnedIds.add(node6.getId());
				result.pinnedInIds = node6.getId().equals(pinnedId);
			}
			
			return result;
		}

	/**
		 *     O4 (r)
		 *    / \
		 *   *7  *8
		 */
		private MatchResult matchTreeNode4(Entry r, Integer pinnedId, boolean pinnedFound) {
			findInterestingChildren(r);
	
			MatchResult result = new MatchResult(r, "4");
			boolean rootPinned = pinnedId.equals(r.getId());
			boolean pinnedIn7or8 = false;
			pinnedFound = pinnedFound || rootPinned;
			List<Entry> children = r.getCompleteEntryChildren();
			if (children == null || children.size() < 2) {
				return result;
			}
			List<Entry> nodes7And8 = new ArrayList<>();
			if (pinnedFound) {
				// we don't care which children we choose
				nodes7And8 = children.subList(0, 2);
				result.matched = true;
			} else {
				// try to find pinned if possible
				nodes7And8 = new ArrayList<>();
				// if one of these is the pinned, be sure to add it first
				for (Entry child : children) {
					pinnedFound = pinnedId.equals(child.getId());
					if (pinnedFound) {
						pinnedIn7or8 = true;
						nodes7And8.add(child);
						break;
					}
				}
				// start over and fill until we reach 2
				for (Entry child : children) {
					if (!nodes7And8.contains(child)) {
						nodes7And8.add(child);
					}
					if (nodes7And8.size() > 1) {
						break;
					}
				}
				if (nodes7And8.size() > 1) {
					// we matched the tree, doesn't mean we found the pinned
					result.matched = true;
				}
			}
			
			if (result.matched ) {
				r.setChildren(nodes7And8);
				for (Entry node : nodes7And8) {
					result.allIds.add(node.getId());
					result.pinnedIds.add(node.getId());
				}
	//			result.allIds.add(r.getId()); // parent will count this node
				result.pinnedInIds = pinnedIn7or8;
			}
			
			return result;
		}

	private void addParents(int count, List<Entry> entries, Entry e) {
		for (int i = 0; i < count; i++) {
			Integer pid = e.getParentId();
			if (pid == null) {
				// in this case, the effort is probably doomed anyways...
				return;
			}
			Entry p = speciesService.findEntry(pid);
			entries.add(p);
			e = p;
		}
	}

	private void findInterestingChildren(Entry e) {
		if (e.getCompleteEntryChildren() != null) {
			// sometimes things are done in a loop, no need to redo this
			return;
		}
		List<Entry> children = speciesService.findChildren(e.getId());
		List<Entry> interesting = filterToInteresting(children);
		e.setChildren(interesting);
	}

	public String toRandomFileName(String query, Integer id) {
		query = query.toLowerCase();
		StringBuilder buf = new StringBuilder();
		
		for (int i = 0; i < query.length(); i++) {
			char c = query.charAt(i);
			int pos = "abcdefghijklmnopqrstuvwxyz".indexOf(c);
			if (pos < 0) {
				buf.append("-");
			} else {
				buf.append(c);
			}
		}
		
		buf.append("-");
		buf.append(id);
		return buf.toString();
	}

	// ensure that the one we care about is not going to be hidden by a pinned ancestor
	// in the future I might fix this, but for now, you can only have one pinned in a "chain"
	private void unpinAncestors(Entry root, Integer pinnedId, Set<Integer> pinnedIds) {
		Entry pinnedEntry = EntryUtilities.findEntry(root, pinnedId);
		Entry parent = pinnedEntry.getParent();
		// it's not an issue for parents with two children
		while (parent != null && parent.getChildren().size() <= 1) {
			pinnedIds.remove(parent.getId());
			parent = parent.getParent();
		}
	}

	private List<Entry> filterToInteresting(List<Entry> list) {
		List<Entry> interesting = new ArrayList<>();
		for (Entry entry : list) {
			// not sure how strict to be - we want to get as many of these working as possible
			if (!entry.isBoring() && entry.getImageLink() != null) {
				interesting.add(entry);
			}
		}
		Collections.shuffle(interesting); // so we don't get the same "uncles" for all similar species
		return interesting;
	}

	private class MatchResult {
		Set<Integer> allIds = new HashSet<>();
		Set<Integer> pinnedIds = new HashSet<>();
		Entry selectedRoot;
		boolean pinnedInIds;
		boolean matched;
		String name;
		
		public String toString() {
			return name;
		}
		
		public MatchResult(Entry selectedRoot, String name) {
			this.selectedRoot = selectedRoot;
			this.name = name;
		}
		List<MatchResult> childResults = new ArrayList<>();
		public Set<Integer> getAllRecursivePinnedIds() {
			Set<Integer> pinnedIds = new HashSet<>(this.pinnedIds);
			for (MatchResult r : childResults) {
				pinnedIds.addAll(r.getAllRecursivePinnedIds());
			}
			return pinnedIds;
		}
		public boolean isPinnedInIdsRecursive(Integer pinnedId) {
			boolean pinned = selectedRoot.getId().equals(pinnedId);
			for (MatchResult r : childResults) {
				pinned = pinned || r.isPinnedInIdsRecursive(pinnedId);
			}
			return pinned;
		}
		public Set<Integer> getAllRecursiveAllIds() {
			Set<Integer> allIds = new HashSet<>(this.allIds);
			for (MatchResult r : childResults) {
				allIds.addAll(r.getAllRecursiveAllIds());
			}
			return allIds;
		}
	}
	
	public void buildRandomFiles() throws Exception {
		// load the index
		List<String> lines = 
				FileUtils.readLines(new File("../banyan-js/src/main/resources/random-seed-list.txt"), Charset.defaultCharset());
		Set<String> terms = new HashSet<>();
		for (String line : lines) {
			terms.add(line.toLowerCase().trim());
		}
		System.out.println(terms.size() + " unique terms");

		for (String term : terms) {
			buildOneRandomFileFromQuery(term);
		}
		outputRandomFileIndex();
	}
	private void buildOneRandomFileFromQuery(String query) throws Exception {
		query = query.trim();
		Entry e = findEntryFromQuery(query);
		if (e != null) {
			System.out.println("buildOneRandomFileFromQuery." + query + "." + e.getId() + "/" + e.getLatinName());
			try {
				Collection<Entry> entries = buildRandomTree(e.getId());
				if (entries != null) {
					Map<Integer, Entry> imageEntries = JsonFileUtils.getLinkedImageEntries(entries, speciesService);
					List<JsonEntry> jentries = JsonFileUtils.toJsonEntries(entries, imageEntries, speciesService);
					String fileName = toRandomFileName(query, e.getId());
					JsonFileUtils.saveByFolders("r", fileName, jentries);
				}
			} catch (NullPointerException ex) {
				// for now we just swallow this - we can't do much, but it's only when there are other issues
				System.out.println("buildOneRandomFileFromQuery." + query + ".Failed");
			}
		}
	}
	private Entry findEntryFromQuery(String query) throws Exception {
		int id = speciesService.findBestId(query, new ArrayList<>());
		if (id >= 0) {
			Entry e = speciesService.findEntry(id);
			System.out.println(query + " >> " + e.getLatinName() + " // " + e.getCommonNames() + " // " + e.getCommonName());
			return e;
		} else {
			System.out.println(query + " >> Not Found");
			return null;
		}
	}
	public void outputRandomFileIndex() throws Exception {
		String fileName = "random-index";
		File dir = new File(JsonFileUtils.outputDir + "/r");
		List<String> names = new ArrayList<>();
		for (File f : dir.listFiles()) {
			String name = f.getName();
			int pos = name.indexOf('.');
			name = name.substring(0, pos);
			if (!fileName.equals(name)) {
				names.add(name);
			}
		}
		String file = "/r/" + fileName + ".json";
		JsonFileUtils.outputNamesListJsonFile(names, file);
	}

	
}


