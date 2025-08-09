package com.robestone.banyan.json;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.robestone.banyan.taxons.Taxon;
import com.robestone.banyan.taxons.TaxonNode;
import com.robestone.banyan.taxons.TaxonService;
import com.robestone.banyan.taxons.Tree;
import com.robestone.banyan.taxons.TreeNodeUtilities;
import com.robestone.banyan.workers.AbstractWorker;
import com.robestone.banyan.workers.ImagesWorker;
import com.robestone.banyan.workers.ImagesWorker.ImageInfo;

public class RandomTreeBuilder extends AbstractWorker {
	
	public static void main(String[] args) throws Exception {
		new RandomTreeBuilder().
//		outputCleanedList();
		buildRandomFiles();
	}
	
	private SearchIndexBuilder searcher;
	
	public void outputCleanedList() throws Exception {
		List<String> terms = getListFromFile();
		List<String> spaces = new ArrayList<String>();
		for (String term : terms) {
			addVariants(term, spaces);
		}
		for (String term : terms) {
			boolean duplicate = false;
			for (String check : spaces) {
				if (term.startsWith(check)) {
					duplicate = true;
					break;
				}
			}
			if (!duplicate) {
				System.out.println(term.trim());
			}
		}
	}
	private void addVariants(String term, List<String> spaces) {
		spaces.add(term + " ");
//		String[] split = StringUtils.split(term);
//		String current = "";
//		for (int i = 0; i < split.length; i++) {
//			current = (current + " " + split[i]).trim();
//			spaces.add(current + " ");
//		}
	}
	
	public Collection<TaxonNode> buildRandomTree(Integer pinnedId) throws Exception {
		Collection<TaxonNode> entries = buildRandomTreeByMatching(pinnedId);
		if (entries == null) {
			entries = buildRandomTreeForLeaves(pinnedId);
		}
		if (entries != null) {
			unpinAncestorsWithSameImage(entries);
			unpinWithNoImage(entries);
		}
		return entries;
	}
	
	private void unpinWithNoImage(Collection<TaxonNode> entries) {
		for (TaxonNode e : entries) {
			if (e.getImage() == null) {
				e.setPinned(false);
			}
		}
	}
	private void unpinAncestorsWithSameImage(Collection<TaxonNode> entries) {
		for (TaxonNode e : entries) {
			unpinAncestorsWithSameImage(e);
		}
	}
	private void unpinAncestorsWithSameImage(TaxonNode entry) {
		if (entry.getImage() == null) {
			return;
		}
		ImageInfo ii = ImagesWorker.toImageInfo(entry);
		String image = ii.getUrlBasePath();
		TaxonNode p = entry.getParent();
		while (p != null) {
			if (p.getImage() != null) {
				String pimage = ImagesWorker.toImageInfo(p).getUrlBasePath();
				if (pimage.equals(image)) {
					p.setPinned(false);
				}
			}
			
			p = p.getParent();
		}
	}
	
	// not sure why I need to do this, but apparently some interesting species aren't hooked in
	private boolean isValidTaxon(Integer iid) throws Exception {
		int id;
		while (true) {
			id = iid;
			if (id == -1) {
				return false;
			} else if (id == TaxonService.TREE_OF_LIFE_ID) {
				return true;
			}
			Taxon e = getTaxonService().findTaxonById(id);
			iid = e.getParentTaxonId();
			if (iid == null) {
				return false;
			}
		}
	}
	
	/**
	 * Since we couldn't find the matching tree, just look for any tree with 4 leaves.
	 * But no more than 2 leaves from a given parent.
	 */
	private Collection<TaxonNode> buildRandomTreeForLeaves(Integer pinnedId) throws Exception {
		
		if (!isValidTaxon(pinnedId)) {
			return null;
		}
		
		List<TaxonNode> entries = new ArrayList<>();
		TaxonNode pinned = getTaxonService().findTaxonById(pinnedId);
		entries.add(pinned);
		
		// first try to get two children
		findInterestingChildren(pinned);
		if (pinned.getChildren().size() > 1) {
			entries.add(pinned.getChildren().get(0));
			entries.add(pinned.getChildren().get(1));
		}
		
		// one sibling
		TaxonNode parent = getTaxonService().findTaxonById(pinned.getParentTaxonId());
		findInterestingChildren(parent);
		for (TaxonNode sibling : parent.getChildren()) {
			if (sibling.getTaxonId().equals(pinned.getTaxonId())) {
				continue;
			}
			entries.add(sibling);
			break;
		}
		
		Taxon nextParent = parent;
		while (entries.size() < 5) {
			// one cousin, and a nephew.  If not, go up one level
			TaxonNode gparent = getTaxonService().findTaxonById(nextParent.getParentTaxonId());
			findInterestingChildren(gparent);
			for (TaxonNode uncle : gparent.getChildren()) {
				if (uncle.getTaxonId().equals(nextParent.getTaxonId())) {
					continue;
				}
				findInterestingChildren(uncle);
				if (!uncle.getChildren().isEmpty()) {
					entries.add(uncle);
					entries.add(uncle.getChildren().get(0));
				}
				break;
			}
			nextParent = gparent;
		}
		
		Set<Integer> ids = new HashSet<>();
		for (Taxon e : entries) {
			ids.add(e.getTaxonId());
		}
		Tree<TaxonNode> tree = getTaxonService().findTreeForTaxonIds(ids);
		for (TaxonNode e : tree.getNodesList()) {
			if (ids.contains(e.getTaxonId())) {
				e.setPinned(true);
			}
		}
		
		return tree.getNodesList();
	}

	private Collection<TaxonNode> buildRandomTreeByMatching(Integer pinnedId) throws Exception {
	
		List<TaxonNode> entries = new ArrayList<>();
		TaxonNode pinned = getTaxonService().findTaxonById(pinnedId);
	
		addParents(4, entries, pinned);
		
		List<TaxonNode> children = getTaxonService().findChildren(pinnedId);
		entries.addAll(children);
		for (TaxonNode child : children) {
			List<TaxonNode> gchildren = getTaxonService().findChildren(child.getTaxonId());
			entries.addAll(gchildren);
		}
		
		MatchResult result = null;
		for (TaxonNode root : entries) {
			// get the entry again, because we will be changing the children
			root = getTaxonService().findTaxonById(root.getTaxonId());
			result = matchTree(root, pinnedId);
			// ensure that the tree actually matches the pattern
			if (result.matched && result.pinnedInIds) {
				break;
			}
		}
		if (result != null && result.matched && result.pinnedInIds) {
			// get the ancestors
			Tree<TaxonNode> tree = getTaxonService().findTreeForTaxonIds(result.getAllRecursiveAllIds());
			Set<Integer> pinnedIds = result.getAllRecursivePinnedIds();
			
			unpinAncestors(tree.getRoot(), pinnedId, pinnedIds);
			markPinned(tree.getRoot(), pinnedIds);
			return tree.getNodesList();
		} else {
			return null;
		}
	}

	private void markPinned(TaxonNode root, Set<Integer> pinnedIds) {
		if (pinnedIds.contains(root.getTaxonId())) {
			root.setPinned(true);
		} else {
			root.setPinned(false);
		}
		if (root.getChildren() != null) {
			for (TaxonNode e : root.getChildren()) {
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
	private MatchResult matchTree(TaxonNode r, Integer pinnedId) {
		findInterestingChildren(r);
		
		MatchResult result = new MatchResult(r, "1");
		boolean rootPinned = pinnedId.equals(r.getTaxonId());
		boolean pinnedFound = rootPinned;
		List<TaxonNode> children = r.getChildren();
		
		// look for all possible node2 matches - we need to keep all since we don't
		// know which of node3 will be selected
		List<MatchResult> results2 = new ArrayList<>();
		for (TaxonNode child : children) {
			MatchResult maybe2 = matchTreeNode2(child, pinnedId, pinnedFound);
			if (maybe2.matched) {
				pinnedFound = pinnedFound || maybe2.isPinnedInIdsRecursive(pinnedId);
				results2.add(maybe2);
			}
		}
	
		boolean pinnedIn3 = false;
		List<MatchResult> results3 = new ArrayList<>();
		for (TaxonNode child : children) {
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
			List<TaxonNode> nodes2and3 = new ArrayList<>();
			
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
			result.allIds.add(r.getTaxonId());
			result.allIds.add(selectedR2.selectedRoot.getTaxonId());
			result.allIds.add(selectedR3.selectedRoot.getTaxonId());
			result.pinnedIds.add(selectedR2.selectedRoot.getTaxonId());
			result.pinnedIds.add(selectedR3.selectedRoot.getTaxonId());
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
		private MatchResult matchTreeNode2(TaxonNode r, Integer pinnedId, boolean pinnedFound) {
			findInterestingChildren(r);
	
			MatchResult result = new MatchResult(r, "2");
			boolean rootPinned = pinnedId.equals(r.getTaxonId());
			pinnedFound = pinnedFound || rootPinned;
			List<TaxonNode> children = r.getChildren();
			MatchResult result4 = null;
			for (TaxonNode child : children) {
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
				TaxonNode node5 = null;
				if (!pinnedFound) {
					for (TaxonNode child : children) {
						if (child == result4.selectedRoot) {
							continue;
						}
						pinnedFound = pinnedId.equals(child.getTaxonId());
						if (pinnedFound) {
							node5 = child;
							break;
						}
					}
				}
	
				if (node5 == null) {
					// just grab the first child 
					for (TaxonNode child : children) {
						if (child == result4.selectedRoot) {
							continue;
						}
						node5 = child;
						break;
					}				
				}
				if (node5 != null) {
					List<TaxonNode> nodes4And5 = new ArrayList<>();
					nodes4And5.add(result4.selectedRoot);
					nodes4And5.add(node5);
					result.childResults.add(result4);
	//				result.allIds.add(r.getTaxonId()); // parent will count this node
	//				result.pinnedIds.add(r.getTaxonId());
					result.allIds.add(node5.getTaxonId());
					result.allIds.add(result4.selectedRoot.getTaxonId());
	//				result.allIds.addAll(result4.allIds);
	//				result.pinnedIds.addAll(result4.pinnedIds);
					r.setChildren(nodes4And5);
					result.matched = true;
					result.pinnedInIds = 
							(result4.selectedRoot.getTaxonId().equals(pinnedId) || node5.getTaxonId().equals(pinnedId));
				}
			}
			
			return result;
		}

	/**
		 *   *3
		 *    \
		 *     *6
		 */
		private MatchResult matchTreeNode3(TaxonNode r, Integer pinnedId, boolean pinnedFound) {
			findInterestingChildren(r);
	
			MatchResult result = new MatchResult(r, "3");
			boolean rootPinned = pinnedId.equals(r.getTaxonId());
			pinnedFound = pinnedFound || rootPinned;
			List<TaxonNode> children = r.getChildren();
			
			// if possible get the pinned child
			Taxon node6 = null;
			if (!pinnedFound) {
				for (Taxon child : children) {
					pinnedFound = pinnedFound || pinnedId.equals(child.getTaxonId());
					if (pinnedFound) {
						node6 = child;
						break;
					}
				}
			}
			if (node6 == null) {
				for (Taxon child : children) {
					node6 = child;
					result.matched = true;
					break;
				}
			}
	
			if (result.matched) {
	//			result.allIds.add(r.getTaxonId()); // parent will count this node
				result.allIds.add(node6.getTaxonId());
				result.pinnedIds.add(node6.getTaxonId());
				result.pinnedInIds = node6.getTaxonId().equals(pinnedId);
			}
			
			return result;
		}

	/**
		 *     O4 (r)
		 *    / \
		 *   *7  *8
		 */
		private MatchResult matchTreeNode4(TaxonNode r, Integer pinnedId, boolean pinnedFound) {
			findInterestingChildren(r);
	
			MatchResult result = new MatchResult(r, "4");
			boolean rootPinned = pinnedId.equals(r.getTaxonId());
			boolean pinnedIn7or8 = false;
			pinnedFound = pinnedFound || rootPinned;
			List<TaxonNode> children = r.getChildren();
			if (children == null || children.size() < 2) {
				return result;
			}
			List<TaxonNode> nodes7And8 = new ArrayList<>();
			if (pinnedFound) {
				// we don't care which children we choose
				nodes7And8 = children.subList(0, 2);
				result.matched = true;
			} else {
				// try to find pinned if possible
				nodes7And8 = new ArrayList<>();
				// if one of these is the pinned, be sure to add it first
				for (TaxonNode child : children) {
					pinnedFound = pinnedId.equals(child.getTaxonId());
					if (pinnedFound) {
						pinnedIn7or8 = true;
						nodes7And8.add(child);
						break;
					}
				}
				// start over and fill until we reach 2
				for (TaxonNode child : children) {
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
				for (Taxon node : nodes7And8) {
					result.allIds.add(node.getTaxonId());
					result.pinnedIds.add(node.getTaxonId());
				}
	//			result.allIds.add(r.getTaxonId()); // parent will count this node
				result.pinnedInIds = pinnedIn7or8;
			}
			
			return result;
		}

	private void addParents(int count, List<TaxonNode> entries, Taxon e) {
		for (int i = 0; i < count; i++) {
			Integer pid = e.getParentTaxonId();
			if (pid == null) {
				// in this case, the effort is probably doomed anyways...
				return;
			}
			TaxonNode p = getTaxonService().findTaxonById(pid);
			entries.add(p);
			e = p;
		}
	}

	private void findInterestingChildren(TaxonNode e) {
		if (e.getChildren() != null) {
			// sometimes things are done in a loop, no need to redo this
			return;
		}
		List<TaxonNode> children = getTaxonService().findChildren(e.getTaxonId());
		List<TaxonNode> interesting = filterToInteresting(children);
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
	private void unpinAncestors(TaxonNode root, Integer pinnedId, Set<Integer> pinnedIds) {
		TaxonNode pinnedTaxon = TreeNodeUtilities.findEntry(root, pinnedId);
		TaxonNode parent = pinnedTaxon.getParent();
		// it's not an issue for parents with two children
		while (parent != null && parent.getChildren().size() <= 1) {
			pinnedIds.remove(parent.getTaxonId());
			parent = parent.getParent();
		}
	}

	private List<TaxonNode> filterToInteresting(List<TaxonNode> list) {
		List<TaxonNode> interesting = new ArrayList<>();
		for (TaxonNode entry : list) {
			// not sure how strict to be - we want to get as many of these working as possible
			if (!entry.isBoring() && entry.getImage().getFilePath() != null) {
				interesting.add(entry);
			}
		}
		Collections.shuffle(interesting); // so we don't get the same "uncles" for all similar species
		return interesting;
	}

	private class MatchResult {
		Set<Integer> allIds = new HashSet<>();
		Set<Integer> pinnedIds = new HashSet<>();
		TaxonNode selectedRoot;
		boolean pinnedInIds;
		boolean matched;
		String name;
		
		public String toString() {
			return name;
		}
		
		public MatchResult(TaxonNode selectedRoot, String name) {
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
			boolean pinned = selectedRoot.getTaxonId().equals(pinnedId);
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
	
	public List<String> getListFromFile() throws Exception {
		// load the index
		List<String> lines = 
				FileUtils.readLines(new File("../banyan-js/src/main/resources/random-seed-list.txt"), Charset.defaultCharset());
		Set<String> terms = new HashSet<>();
		for (String line : lines) {
			terms.add(line.toLowerCase().trim());
		}
		List<String> list = new ArrayList<String>(terms);
		Collections.sort(list);
		return list;
	}
	public void buildRandomFiles() throws Exception {
		List<String> terms = getListFromFile();
		System.out.println(terms.size() + " unique terms");

		searcher = new SearchIndexBuilder();
		searcher.initForQueries();
		
		for (String term : terms) {
			buildOneRandomFileFromQuery(term);
		}
		outputRandomFileIndex();
	}
	private void buildOneRandomFileFromQuery(String query) throws Exception {
		query = query.trim();
		Taxon e = findTaxonFromQuery(query);
		if (e != null) {
			System.out.println("buildOneRandomFileFromQuery." + query + "." + e.getTaxonId() + "/" + e.getLatinName());
			try {
				Collection<TaxonNode> entries = buildRandomTree(e.getTaxonId());
				if (entries != null) {
					List<JsonEntry> jentries = JsonFileUtils.toJsonEntries(entries, getTaxonService());
					String fileName = toRandomFileName(query, e.getTaxonId());
					JsonFileUtils.saveByFolders("r", fileName, jentries);
				}
			} catch (NullPointerException ex) {
				// for now we just swallow this - we can't do much, but it's only when there are other issues
				System.out.println("buildOneRandomFileFromQuery." + query + ".Failed");
			}
		}
	}
	private Taxon findTaxonFromQuery(String query) throws Exception {
		Taxon e = searcher.findBestMatchByQuery(query);
		if (e != null) {
			System.out.println(query + " >> " + e.getLatinName() + " // " 
//		+ e.getCommonNames() + " // " 
					+ e.getCommonName());
		}
		return e;
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


