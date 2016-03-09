package com.robestone.species;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tree {

	private CompleteEntry root;
	private Map<Integer, CompleteEntry> map;

	public Tree(CompleteEntry root, Map<Integer, CompleteEntry> map) {
		this.root = root;
		this.map = map;
	}

	public CompleteEntry get(Integer id) {
		return map.get(id);
	}

	public CompleteEntry getRoot() {
		return root;
	}
	public List<CompleteEntry> getEntries() {
		return new ArrayList<CompleteEntry>(map.values());
	}
	public Map<Integer, CompleteEntry> getEntriesMap() {
		return map;
	}
	public int size() {
		return map.size();
	}
}
