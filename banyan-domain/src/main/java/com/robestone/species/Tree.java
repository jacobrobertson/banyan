package com.robestone.species;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tree {

	private Entry root;
	private Map<Integer, Entry> map;

	public Tree(Entry root, Map<Integer, Entry> map) {
		this.root = root;
		this.map = map;
	}

	public Entry get(Integer id) {
		return map.get(id);
	}

	public Entry getRoot() {
		return root;
	}
	public List<Entry> getEntries() {
		return new ArrayList<Entry>(map.values());
	}
	public Map<Integer, Entry> getEntriesMap() {
		return map;
	}
	public int size() {
		return map.size();
	}
}
