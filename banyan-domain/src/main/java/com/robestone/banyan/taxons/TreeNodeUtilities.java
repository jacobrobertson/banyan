package com.robestone.banyan.taxons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.robestone.banyan.ids.IdCruncher;
import com.robestone.banyan.util.EntityMapper;

public class TreeNodeUtilities {

	public static final IdCruncher CRUNCHER = IdCruncher.withSubtraction(IdCruncher.R62_CHARS);

	public static <T extends TreeNode<T>> Collection<T> getEntriesForEntry(T root) {
		return getEntries(root);
	}
	public static <T extends TreeNode<T>> Set<T> getEntries(T root, Set<Integer> ids) {
		Set<T> entries = new HashSet<>();
		for (Integer id : ids) {
			T e = findEntry(root, id);
			entries.add(e);
		}
		return entries;
	}
	public static <T extends TreeNode<T>> T findEntry(T entry, int id) {
		if (entry.getId().equals(id)) {
			return entry;
		}
		if (entry.hasChildren()) {
			for (T child: entry.getChildren()) {
				T found = findEntry(child, id);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	public static <T extends TreeNode<T>> Set<T> getEntries(T root) {
		Set<T> entries = new HashSet<>();
		addEntries(root, entries, new HashSet<>());
		return entries;
	}
	protected static <T extends TreeNode<T>> Collection<Integer> getIds(Collection<T> entries) {
		Set<Integer> ids = new HashSet<Integer>();
		for (T e: entries) {
			ids.add(e.getId());
		}
		return ids;
	}
	/**
	 * @param safeIds to prevent any recursive tree
	 */
	private static <T extends TreeNode<T>> void addEntries(T entry, Set<T> entries, Set<Integer> safeIds) {
		entries.add(entry);
		safeIds.add(entry.getId());
		if (entry.hasChildren()) {
			for (T child: entry.getChildren()) {
				if (child == null) {
					throw new IllegalArgumentException("Child is null for parent: " + entry.getId() + "/" + entry.getLatinName());
				}
				if (!safeIds.contains(child.getId())) {
					addEntries(child, entries, safeIds);
				}
			}
		}
	}

	public static <T extends TreeNode<T>> Set<T> getLeaves(T root) {
		return getLeaves(root, NO_IDS);
	}
	private static <T extends TreeNode<T>> Set<T> getLeaves(T root, Set<Integer> toExclude) {
		Set<T> entries = new HashSet<>();
		addLeaves(root, entries, toExclude);
		return entries;
	}
	private static <T extends TreeNode<T>> void addLeaves(T entry, Set<T> entries, Set<Integer> toExclude) {
		if (isLeaf(entry, toExclude)) {
			entries.add(entry);
		} else {
			for (T child: entry.getChildren()) {
				if (!toExclude.contains(child.getId())) {
					addLeaves(child, entries, toExclude);
				}
			}
		}
	}
	private static <T extends TreeNode<T>> boolean isLeaf(T entry, Set<Integer> toExclude) {
		int count = entry.getLoadedChildrenSize();
		if (count == 0) {
			return true;
		}
		for (T child: entry.getChildren()) {
			if (toExclude.contains(child.getId())) {
				count--;
			}
		}
		return (count == 0);
	}
	
	/**
	 * As long as one ancestor is extinct, returns true.
	 */
	public static boolean isAncestorExtinct(AnalyzableTreeNode e) {
		AnalyzableTreeNode p = e.getParent();
		if (p == null) {
			return false;
		}
		if (p.isExtinct()) {
			return true;
		}
		return isAncestorExtinct(p);
	}

	
	private static final Set<Integer> NO_IDS = new HashSet<Integer>();
	public static <T extends TreeNode<T>> Collection<Integer> getLeavesIds(T entry) {
		return getLeavesIds(entry, NO_IDS);
	}
	protected static <T extends TreeNode<T>> Collection<Integer> getLeavesIds(T entry, Set<Integer> toExclude) {
		Collection<T> leaves = getLeaves(entry, toExclude);
		Collection<Integer> ids = getIds(leaves);
		return ids;
	}
	public static <T extends TreeNode<T>> Set<Integer> getIds(T root) {
		Set<Integer> ids = new HashSet<Integer>();
		addIds(root, ids);
		return ids;
	}
	public static <T extends TreeNode<T>> void addIds(T entry, Set<Integer> ids) {
		ids.add(entry.getId());
		if (entry.hasChildren()) {
			for (T child: entry.getChildren()) {
				addIds(child, ids);
			}
		}
	}
	public static <T extends TreeNode<T>> String getCrunchedIdsForAncestors(T current) {
		Collection<Integer> ids = new HashSet<Integer>();
		while (current != null) {
			ids.add(current.getId());
			current = current.getParent();
		}
		return getCrunchedIds(ids);
	}
	public static <T extends TreeNode<T>> String getCrunchedLeavesIds(T entry) {
		Collection<Integer> ids = getLeavesIds(entry);
		return getCrunchedIds(ids);
	}
	public static String getCrunchedIds(Collection<Integer> ids) {
		String cids = CRUNCHER.toString(ids);
		return cids;
	}

	/**
	 * Hook it all together, sort, and return the root.
	 */
	public static <T extends TreeNode<T>> Tree<T> buildTree(T e) {
		return buildTree(getEntries(e));
	}
	/**
	 * Hook it all together, sort, and return the root.
	 */
	public static <T extends TreeNode<T>> Tree<T> buildTree(Collection<T> col) {
		Map<Integer, T> map = new HashMap<>();
		for (T entry: col) {
			map.put(entry.getId(), entry);
		}
		return buildTree(map);
	}
	/**
	 * Hook it all together, sort, and return the root.
	 */
	public static <T extends TreeNode<T>> Tree<T> buildTree(Map<Integer, T> map) {
		// ensure each child list is empty before re-hooking
		for (T e: map.values()) {
			e.setChildren(null);
		}
		// hook them together
		for (T e: map.values()) {
			T parent = map.get(e.getParentId());
			if (parent != null) {
				e.setParent(parent);
				if (parent.getChildren() == null) {
					parent.setChildren(new ArrayList<T>());
				}
				parent.getChildren().add(e);
			}
		}
		
		// find the top
		T top = null;
		for (T e: map.values()) {
			if (e.getParent() == null) {
				// candidate for top
				if (top == null || TaxonService.TREE_OF_LIFE_ID.equals(e.getId())) {
					top = e;
				}
			}
		}
		sort(top);
		return new Tree<T>(top, map);
	}
	
	public static final TreeNodeComparator COMP = new TreeNodeComparator();
	protected static <T extends TreeNode<T>> void sort(T entry) {
		if (entry.hasChildren()) {
			Collections.sort(entry.getChildren(), COMP);
			for (T child: entry.getChildren()) {
				sort(child);
			}
		}
	}
	public static <T extends AnalyzableTreeNode> void cleanEntries(Collection<T> entries) {
		for (AnalyzableTreeNode entry: entries) {
			cleanEntry(entry);
		}
	}
	/**
	 * Set the "clean" properties.
	 * @param entry
	 */
	public static void cleanEntry(AnalyzableTreeNode entry) {
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
	

}
