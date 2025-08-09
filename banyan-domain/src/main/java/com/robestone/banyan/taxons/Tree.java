package com.robestone.banyan.taxons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tree<T> {

	private T root;
	private Map<Integer, T> map;

	public Tree(T root, Map<Integer, T> map) {
		this.root = root;
		this.map = map;
	}

	public T get(Integer id) {
		return map.get(id);
	}

	public T getRoot() {
		return root;
	}
	public List<T> getNodesList() {
		return new ArrayList<T>(map.values());
	}
	public Map<Integer, T> getNodesMap() {
		return map;
	}
	public int size() {
		return map.size();
	}
}
