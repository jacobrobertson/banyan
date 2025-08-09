package com.robestone.banyan.wikispecies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.robestone.banyan.taxons.Image;
import com.robestone.banyan.taxons.Tree;
import com.robestone.banyan.taxons.TreeNodeUtilities;
import com.robestone.banyan.util.LogHelper;
import com.robestone.banyan.workers.CommonNameSimilarityChecker;

public class EntryUtilities extends TreeNodeUtilities {

	public static String getCrunchedIdsForClose(Entry root, Integer toClose) {
		Collection<Integer> ids = getLeavesIdsForClose(root, toClose);
		return getCrunchedIds(ids);
	}
	public static String getCrunchedIdsForHideChildren(Entry root, Integer toClose) {
		Collection<Integer> ids = getLeavesIdsForHideChildren(root, toClose);
		return getCrunchedIds(ids);
	}
	public static Collection<Integer> getLeavesIdsForHideChildren(Entry root, Integer parentId) {
		// get ids to exclude
		Entry parent = findEntry(root, parentId);
		Set<Integer> toExclude = getIds(parent);
		toExclude.remove(parentId);
		// find leaves
		Collection<Integer> ids = getLeavesIds(root, toExclude);
		return ids;
	}
	public static Collection<Integer> getLeavesIdsForClose(Entry root, Integer toClose) {
		// get ids to exclude
		Entry entry = findEntry(root, toClose);
		Set<Integer> toExclude = getIds(entry);
		// find leaves
		Collection<Integer> ids = getLeavesIds(root, toExclude);
		return ids;
	}
	

	public static Map<Integer, Entry> toMap(Entry root) {
		Map<Integer, Entry> map = new HashMap<Integer, Entry>();
		addToMap(map, root);
		return map;
	}
	private static void addToMap(Map<Integer, Entry> map, Entry entry) {
		map.put(entry.getId(), entry);
		if (entry.hasChildren()) {
			for (Entry child: entry.getChildren()) {
				addToMap(map, child);
			}
		}
	}
	public static Set<Integer> removeChildren(Entry root, int id) {
		doRemoveChildren(root, id);
		sort(root);
		return getIds(root);
	}
	static boolean doRemoveChildren(Entry root, int id) {
		if (root.getId() == id) {
			if (root.hasChildren()) {
				root.getChildren().clear();
			}
			return true;
		} else {
			if (root.hasChildren()) {
				for (Entry child: root.getChildren()) {
					boolean removed = doRemoveChildren(child, id);
					if (removed) {
						return true;
					}
				}
			}
			return false;
		}
	}
	public static Set<Integer> remove(Entry root, int id) {
		doRemove(root, id);
		sort(root);
		return getIds(root);
	}
	/**
	 * Assumes the top root can't be removed...
	 */
	static boolean doRemove(Entry root, int id) {
		if (root.hasChildren()) {
			Entry found = null;
			for (Entry child: root.getChildren()) {
				if (child.getId() == id) {
					found = child;
					break;
				}
			}
			if (found != null) {
				root.getChildren().remove(found);
				return true;
			}
			for (Entry child: root.getChildren()) {
				boolean removed = doRemove(child, id);
				if (removed) {
					return true;
				}
			}
		}
		return false;
	}
	public static Set<Integer> focus(Entry root, int id) {
		
		// find entry
		Entry entry = findEntry(root, id);
		// find all children ids
		Set<Integer> ids = getIds(entry);
		// find all parent ids
		Entry parent = entry.getParent();
		while (parent != null) {
			ids.add(parent.getId());
			parent = parent.getParent();
		}
		
		// remove all other ids from the tree
		Set<Integer> toRemove = getIds(root);
		toRemove.removeAll(ids);
		for (Integer removeId: toRemove) {
			doRemove(root, removeId);
		}
		
		sort(root);
		return getIds(root);
	}
	
	public static List<Tree<Entry>> findDisconnectedTrees(Tree<Entry> tree) {
		List<Tree<Entry>> trees = new ArrayList<>();
		Entry root = tree.getRoot();
		Set<String> rootNames = new HashSet<String>();
		rootNames.add(root.getLatinName());
		int count = 0;
		int logEvery = 5000;
		Map<Integer, Entry> map = tree.getNodesMap();
		for (Integer id: map.keySet()) {
			count++;
			Entry e = map.get(id);
			Entry p = getRoot(e);
			if (count++ % logEvery == 0) {
				LogHelper.speciesLogger.debug(
						"findDisconnectedTrees.log." + count + "." + 
						e.getLatinName() + "." + e.getId() + "." + e.getParentId());
			}
			if (p == null) {
				LogHelper.speciesLogger.debug(
						"findDisconnectedTrees.loop." + e.getLatinName() + "." + e.getId());
				continue;
			}
			String rootKey = p.getParentLatinName();
			if (!rootNames.contains(rootKey)) {
				Tree<Entry> t = buildTree(p);
//				LogHelper.speciesLogger.debug(
//						"findDisconnectedTrees.tree." + p.getLatinName() + "." + p.getId() + "/" + t.size());
				rootNames.add(rootKey);
				trees.add(t);
			}
		}
		
		Collections.sort(trees, new Comparator<Tree<Entry>>() {
				@Override
				public int compare(Tree<Entry> o1, Tree<Entry> o2) {
					return o2.size() - o1.size();
				}
		});
		
		return trees;
	}
	public static Entry getRoot(Entry e) {
		// need a way to check that I don't get in a loop
		Set<Integer> ids = new HashSet<Integer>();
		Entry parent = null;
		while (e != null) {
			if (ids.contains(e.getId())) {
				return null;
			}
			parent = e;
			ids.add(e.getId());
			e = e.getParent();
		}
		return parent;
	}
	public static boolean isEntryRecursable(Entry entry) {
		return (
			entry.getLoadedChildrenSize() == 1
		);		
	}

	/**
	 * Given a branch of the tree, gather as much of it into a chain, until you hit the last node that can't be chained.
	 * a
	 *  b
	 *   c
	 *    d
	 *     e
	 *      f
	 *       g
	 *       h
	 *  In this example, we can chain a-f, but because f has 2 children we have to stop
	 */
	public static List<Entry> toChain(Entry branch) {
		List<Entry> chain = new ArrayList<Entry>();
		Entry next = branch;
		while (true) {
			chain.add(next);
			if (isEntryRecursable(next)) {
				next = next.getChildren().get(0);
			} else {
				break;
			}
		}
		return chain;
	}
	
	public static List<Entry> collapseList(List<Entry> entries) {
		return collapseList(entries, true);
	}
	/**
	 * This converts a long chain of entries into the shortened chain
	 */
	public static List<Entry> collapseList(List<Entry> entries, boolean isFirstAndLastInList) {
		int size = entries.size();
		if ((isFirstAndLastInList && size < 5) || size < 3) {
			return entries;
		}
		
		List<Entry> collapsed = new ArrayList<Entry>();
		int first = 0;
		int last = size;
		if (isFirstAndLastInList) {
			collapsed.add(entries.get(0));
			first = 1;
			last--;
		}
		
		List<Entry> sub = new ArrayList<Entry>();
		// look for any sub-lists we can collapse
		for (int i = first; i < last; i++) {
			Entry e = entries.get(i);
			boolean boring = CommonNameSimilarityChecker.isCommonNameBoring(e);
			if (!boring) {
				// add this one to the list, and check if we can crunch the last ones
				if (!sub.isEmpty()) {
					Entry collapsedEntry = collapseListToOne(sub);
					collapsed.add(collapsedEntry);
					sub.clear();
				}
				collapsed.add(e);
				continue;
			} else {
				sub.add(e);
			}
		}

		// add any remaining
		if (!sub.isEmpty()) {
			Entry collapsedEntry = collapseListToOne(sub);
			collapsed.add(collapsedEntry);
		}
		
		if (isFirstAndLastInList) {
			collapsed.add(entries.get(size - 1));
		}
		
		return collapsed;
	}
	/**
	 * Only call this method once you've determine the list should be collapsed to one Entry
	 * return Entry with correct collapse name
	 */
	public static Entry collapseListToOne(List<Entry> entries) {
		Entry best = null;
		
		// look for first non-boring common name
		for (Entry e: entries) {
			if (!CommonNameSimilarityChecker.isCommonNameBoring(e)) {
				best = e;
				break;
			}
		}
		// next try any common name
		if (best == null) {
			for (Entry e: entries) {
				if (e.getCommonName() != null) {
					best = e;
					break;
				}
			}
		}
		// next get first latin name
		if (best == null) {
			best = entries.get(0);
		}
		
		Image image = best.getImage();
		if (image == null) {
			for (Entry e: entries) {
				if (e.getImage() != null) {
					image = e.getImage();
				}
			}
		}
		
		EntryProperties props = new EntryProperties(best.getEntryProperties());
		props.image = image;
		Entry e = new Entry(props);
		e.setCollapsedCount(entries.size() - 1);
		return e;
	}
	
}
