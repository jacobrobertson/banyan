package com.robestone.species;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.robestone.util.html.EntityMapper;

public class EntryUtilities {

	private static final EntryComparator COMP = new EntryComparator();
	public static final IdCruncher CRUNCHER = IdCruncher.withSubtraction(IdCruncher.R62_CHARS);
	public static final String COMMON_NAME_FROM_DESCENDENTS_INDICATOR = "...";

	public static String getCrunchedIdsForClose(Entry root, Integer toClose) {
		Collection<Integer> ids = getLeavesIdsForClose(root, toClose);
		return getCrunchedIds(ids);
	}
	public static String getCrunchedIdsForHideChildren(Entry root, Integer toClose) {
		Collection<Integer> ids = getLeavesIdsForHideChildren(root, toClose);
		return getCrunchedIds(ids);
	}
	public static String getCrunchedIdsForAncestors(Entry current) {
		Collection<Integer> ids = new HashSet<Integer>();
		while (current != null) {
			ids.add(current.getId());
			current = current.getParent();
		}
		return getCrunchedIds(ids);
	}
	
	public static Set<Entry> getEntries(Entry root, Set<Integer> ids) {
		Set<Entry> entries = new HashSet<Entry>();
		for (Integer id : ids) {
			Entry e = findEntry(root, id);
			entries.add(e);
		}
		return entries;
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
	
	public static void cleanEntries(Collection<Entry> entries) {
		for (Entry entry: entries) {
			cleanEntry(entry);
		}
	}
	/**
	 * Set the "clean" properties.
	 * @param entry
	 */
	public static void cleanEntry(Entry entry) {
		entry.setLatinNameClean(getClean(entry.getLatinName(), false));
		entry.setLatinNameCleanest(getClean(entry.getLatinName(), true));
		entry.setCommonNameClean(getClean(entry.getCommonName(), false));
		entry.setCommonNameCleanest(getClean(entry.getCommonName(), true));
	}
	public static String getClean(String value, boolean cleanest) {
		if (value == null) {
			return null;
		}
		value = value.toUpperCase();
		value = EntityMapper.convertToSearchText(value, ' ');
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (Character.isLetter(c)) {
				buf.append(c);
			} else if (!cleanest) {
				// add a space, only if there isn't already one
				String temp = buf.toString().trim();
				buf = new StringBuilder(temp);
				buf.append(" ");
			}
		}
		return buf.toString().trim();
	}
	
	private static void sort(Entry entry) {
		if (entry.hasChildren()) {
			Collections.sort(entry.getChildren(), COMP);
			for (Entry child: entry.getChildren()) {
				sort(child);
			}
		}
	}
	static Map<Integer, Entry> toMap(Entry root) {
		Map<Integer, Entry> map = new HashMap<Integer, Entry>();
		addToMap(map, root);
		return map;
	}
	private static void addToMap(Map<Integer, Entry> map, Entry entry) {
		map.put(entry.getId(), entry);
		if (entry.hasChildren()) {
			for (Entry child: entry.getCompleteEntryChildren()) {
				addToMap(map, child);
			}
		}
	}
	public static Collection<Entry> getEntriesForEntry(Entry root) {
		return getEntries((Entry) root);
	}
	public static Set<Entry> getEntries(Entry root) {
		Set<Entry> entries = new HashSet<Entry>();
		addEntries(root, entries, new HashSet<>());
		return entries;
	}
	static Collection<Integer> getIds(Collection<Entry> entries) {
		Set<Integer> ids = new HashSet<Integer>();
		for (Entry e: entries) {
			ids.add(e.getId());
		}
		return ids;
	}
	/**
	 * @param safeIds to prevent any recursive tree
	 */
	private static void addEntries(Entry entry, Set<Entry> entries, Set<Integer> safeIds) {
		entries.add(entry);
		safeIds.add(entry.getId());
		if (entry.hasChildren()) {
			for (Entry child: entry.getCompleteEntryChildren()) {
				if (child == null) {
					throw new IllegalArgumentException("Child is null for parent: " + entry.getId() + "/" + entry.getLatinName());
				}
				if (!safeIds.contains(child.getId())) {
					addEntries(child, entries, safeIds);
				}
			}
		}
	}
	public static String getCrunchedLeavesIds(Entry entry) {
		Collection<Integer> ids = getLeavesIds(entry);
		return getCrunchedIds(ids);
	}
	public static String getCrunchedIds(Collection<Integer> ids) {
		String cids = CRUNCHER.toString(ids);
		return cids;
	}
	private static final Set<Integer> NO_IDS = new HashSet<Integer>();
	public static Collection<Integer> getLeavesIds(Entry entry) {
		return getLeavesIds(entry, NO_IDS);
	}
	private static Collection<Integer> getLeavesIds(Entry entry, Set<Integer> toExclude) {
		Collection<Entry> leaves = getLeaves((Entry) entry, toExclude);
		Collection<Integer> ids = getIds(leaves);
		return ids;
	}
	public static Set<Entry> getLeaves(Entry root) {
		return getLeaves(root, NO_IDS);
	}
	private static Set<Entry> getLeaves(Entry root, Set<Integer> toExclude) {
		Set<Entry> entries = new HashSet<Entry>();
		addLeaves(root, entries, toExclude);
		return entries;
	}
	private static void addLeaves(Entry entry, Set<Entry> entries, Set<Integer> toExclude) {
		if (isLeaf(entry, toExclude)) {
			entries.add(entry);
		} else {
			for (Entry child: entry.getCompleteEntryChildren()) {
				if (!toExclude.contains(child.getId())) {
					addLeaves(child, entries, toExclude);
				}
			}
		}
	}
	private static boolean isLeaf(Entry entry, Set<Integer> toExclude) {
		int count = entry.getLoadedChildrenSize();
		if (count == 0) {
			return true;
		}
		for (Entry child: entry.getCompleteEntryChildren()) {
			if (toExclude.contains(child.getId())) {
				count--;
			}
		}
		return (count == 0);
	}
	
	public static Set<Integer> getIds(Entry root) {
		Set<Integer> ids = new HashSet<Integer>();
		addIds(root, ids);
		return ids;
	}
	public static void addIds(Entry entry, Set<Integer> ids) {
		ids.add(entry.getId());
		if (entry.hasChildren()) {
			for (Entry child: entry.getChildren()) {
				addIds(child, ids);
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
	
	public static Entry findEntry(Entry entry, int id) {
		if (entry.getId().equals(id)) {
			return entry;
		}
		if (entry.hasChildren()) {
			for (Entry child: entry.getChildren()) {
				Entry found = findEntry(child, id);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
	public static String fixCommonName(String name) {
		name = StringUtils.trimToNull(name);
		if (name == null) {
			return null;
		}
		name = name.replace("&#160;", "");
		name = name.replace("&amp;", "&");
		name = name.replace("<br />", ";");
		name = name.replace("<i>", "");
		name = name.replace("</i>", "");
		name = name.replace("`", "");
		name = name.replace("_", " ");
		
		if (name.indexOf("</a>") > 0) {
			name = name.substring(0, name.length() - 4);
			int pos = name.lastIndexOf(">");
			name = name.substring(pos + 1);
		}
		
		while (name.contains("  ")) {
			name = name.replace("  ", " ");
		}
		name = urlDecode(name);
		
		// remove "." from the end - pretty common
		if (name.indexOf(COMMON_NAME_FROM_DESCENDENTS_INDICATOR) < 0) {
			name = StringUtils.removeEnd(name, ".");
		}
		
		name = StringUtils.trimToNull(name);
		
		if (name != null) {
			char first = Character.toUpperCase(name.charAt(0));
			name = first + name.substring(1);
		}
		
		return name;
	}
	public static String urlDecode(String n) {
		try {
			return URLDecoder.decode(n, "UTF-8");
		} catch (Exception e) {
			// fails if "UTF-8" is invalid encoding!
			throw new RuntimeException(e);
		}
	}
	/**
	 * Hook it all together, sort, and return the root.
	 */
	public static Tree buildTree(Entry e) {
		return buildTree(getEntries(e));
	}
	/**
	 * Hook it all together, sort, and return the root.
	 */
	public static Tree buildTree(Collection<Entry> col) {
		Map<Integer, Entry> map = new HashMap<Integer, Entry>();
		for (Entry entry: col) {
			map.put(entry.getId(), entry);
		}
		return buildTree(map);
	}
	/**
	 * Hook it all together, sort, and return the root.
	 */
	public static Tree buildTree(Map<Integer, Entry> map) {
		// ensure each child list is empty before re-hooking
		for (Entry e: map.values()) {
			e.setChildren(null);
		}
		// hook them together
		for (Entry e: map.values()) {
			Entry parent = map.get(e.getParentId());
			if (parent != null) {
				e.setParent(parent);
				if (parent.getChildren() == null) {
					parent.setChildren(new ArrayList<Entry>());
				}
				parent.getCompleteEntryChildren().add(e);
			}
		}
		
		// find the top
		Entry top = null;
		for (Entry e: map.values()) {
			if (e.getParent() == null) {
				// candidate for top
				if (top == null || SpeciesService.TREE_OF_LIFE_ID.equals(e.getId())) {
					top = e;
				}
			}
		}
		sort(top);
		return new Tree(top, map);
	}
	/**
	 * As long as one ancestor is extinct, returns true.
	 */
	public static boolean isAncestorExtinct(Entry e) {
		Entry p = e.getParent();
		if (p == null) {
			return false;
		}
		if (p.isExtinct()) {
			return true;
		}
		return isAncestorExtinct(p);
	}
	
	public static List<Tree> findDisconnectedTrees(Tree tree) {
		List<Tree> trees = new ArrayList<Tree>();
		Entry root = tree.getRoot();
		Set<String> rootNames = new HashSet<String>();
		rootNames.add(root.getLatinName());
		int count = 0;
		int logEvery = 5000;
		Map<Integer, Entry> map = tree.getEntriesMap();
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
				Tree t = buildTree(p);
//				LogHelper.speciesLogger.debug(
//						"findDisconnectedTrees.tree." + p.getLatinName() + "." + p.getId() + "/" + t.size());
				rootNames.add(rootKey);
				trees.add(t);
			}
		}
		
		Collections.sort(trees, new Comparator<Tree>() {
				@Override
				public int compare(Tree o1, Tree o2) {
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
	
	public void markPinned(List<Integer> pins, Entry root) {
		// since pins are usually 
	}
	
}
