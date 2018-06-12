package com.robestone.species.js;

import java.util.ArrayList;
import java.util.List;

public class Node {

	private Integer id;
	private List<Integer> childIds;
	private int totalDescendants;
	
	private List<Node> children = new ArrayList<>();
	
	public Node(Integer id, List<Integer> childIds) {
		this.id = id;
		this.childIds = childIds;
	}
	public Integer getId() {
		return id;
	}
	public List<Integer> getChildIds() {
		return childIds;
	}
	public List<Node> getChildren() {
		return children;
	}
	public int getTotalDescendants() {
		return totalDescendants;
	}
	public void setTotalDescendants(int totalDescendants) {
		this.totalDescendants = totalDescendants;
	}
	
	public String toString() {
		return String.valueOf(id);
	}
	
}
