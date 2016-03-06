package com.robestone.species;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.robestone.util.html.EntityMapper;

public class EntryUtilities {

	private static final EntryComparator COMP = new EntryComparator();
	public static final IdCruncher CRUNCHER = IdCruncher.withSubtraction(IdCruncher.R62_CHARS);

	public static String getCrunchedIdsForClose(Entry root, Integer toClose) {
		Collection<Integer> ids = EntryUtilities.getLeavesIdsForClose(root, toClose);
		return getCrunchedIds(ids);
	}
	public static String getCrunchedIdsForHideChildren(Entry root, Integer toClose) {
		Collection<Integer> ids = EntryUtilities.getLeavesIdsForHideChildren(root, toClose);
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
	
	public static void cleanEntries(Collection<CompleteEntry> entries) {
		for (CompleteEntry entry: entries) {
			cleanEntry(entry);
		}
	}
	/**
	 * Set the "clean" properties.
	 * @param entry
	 */
	public static void cleanEntry(CompleteEntry entry) {
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
	static Map<Integer, CompleteEntry> toMap(CompleteEntry root) {
		Map<Integer, CompleteEntry> map = new HashMap<Integer, CompleteEntry>();
		addToMap(map, root);
		return map;
	}
	private static void addToMap(Map<Integer, CompleteEntry> map, CompleteEntry entry) {
		map.put(entry.getId(), entry);
		if (entry.hasChildren()) {
			for (CompleteEntry child: entry.getCompleteEntryChildren()) {
				addToMap(map, child);
			}
		}
	}
	public static Collection<? extends Entry> getEntriesForEntry(Entry root) {
		return getEntries((CompleteEntry) root);
	}
	public static Set<CompleteEntry> getEntries(CompleteEntry root) {
		Set<CompleteEntry> entries = new HashSet<CompleteEntry>();
		addEntries(root, entries);
		return entries;
	}
	static Collection<Integer> getIds(Collection<? extends Entry> entries) {
		Set<Integer> ids = new HashSet<Integer>();
		for (Entry e: entries) {
			ids.add(e.getId());
		}
		return ids;
	}
	private static void addEntries(CompleteEntry entry, Set<CompleteEntry> entries) {
		entries.add(entry);
		if (entry.hasChildren()) {
			for (CompleteEntry child: entry.getCompleteEntryChildren()) {
				if (child == null) {
					throw new IllegalArgumentException("Child is null for parent: " + entry.getId() + "/" + entry.getLatinName());
				}
				addEntries(child, entries);
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
		Collection<CompleteEntry> leaves = getLeaves((CompleteEntry) entry, toExclude);
		Collection<Integer> ids = getIds(leaves);
		return ids;
	}
	public static Set<CompleteEntry> getLeaves(CompleteEntry root) {
		return getLeaves(root, NO_IDS);
	}
	private static Set<CompleteEntry> getLeaves(CompleteEntry root, Set<Integer> toExclude) {
		Set<CompleteEntry> entries = new HashSet<CompleteEntry>();
		addLeaves(root, entries, toExclude);
		return entries;
	}
	private static void addLeaves(CompleteEntry entry, Set<CompleteEntry> entries, Set<Integer> toExclude) {
		if (isLeaf(entry, toExclude)) {
			entries.add(entry);
		} else {
			for (CompleteEntry child: entry.getCompleteEntryChildren()) {
				if (!toExclude.contains(child.getId())) {
					addLeaves(child, entries, toExclude);
				}
			}
		}
	}
	private static boolean isLeaf(CompleteEntry entry, Set<Integer> toExclude) {
		int count = entry.getLoadedChildrenSize();
		if (count == 0) {
			return true;
		}
		for (CompleteEntry child: entry.getCompleteEntryChildren()) {
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
		if (entry.getId() == id) {
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
		name = StringUtils.removeEnd(name, ".");
		
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
	 * TODO implement - part of enhancements
	 */
	public static String getImpliedCommonName(Entry root) {
		Set<String> tokens = new HashSet<String>();
		addCommonNameTokens(root, tokens);
		
		return null;
	}
	private static void addCommonNameTokens(Entry entry, Set<String> tokens) {
		String name = entry.getCommonName();
		if (name != null) {
			String[] split = name.split(" ");
			for (String one: split) {
				tokens.add(one);
			}
		}
		if (entry.hasChildren()) {
			for (Entry child: entry.getChildren()) {
				addCommonNameTokens(child, tokens);
			}
		}
	}
	/**
	 * Hook it all together, sort, and return the root.
	 */
	public static Tree buildTree(CompleteEntry e) {
		return buildTree(getEntries(e));
	}
	public static Tree buildTree(Entry e) {
		return buildTree((CompleteEntry) e);
	}
	/**
	 * Hook it all together, sort, and return the root.
	 */
	public static Tree buildTree(Collection<CompleteEntry> col) {
		Map<Integer, CompleteEntry> map = new HashMap<Integer, CompleteEntry>();
		for (CompleteEntry entry: col) {
			map.put(entry.getId(), entry);
		}
		return buildTree(map);
	}
	/**
	 * Hook it all together, sort, and return the root.
	 */
	public static Tree buildTree(Map<Integer, CompleteEntry> map) {
		// ensure each child list is empty before re-hooking
		for (CompleteEntry e: map.values()) {
			e.setChildren(null);
		}
		// hook them together
		for (CompleteEntry e: map.values()) {
			CompleteEntry parent = map.get(e.getParentId());
			if (parent != null) {
				e.setParent(parent);
				if (parent.getChildren() == null) {
					parent.setChildren(new ArrayList<CompleteEntry>());
				}
				parent.getCompleteEntryChildren().add(e);
			}
		}
		
		// find the top
		CompleteEntry top = null;
		for (CompleteEntry e: map.values()) {
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
}
