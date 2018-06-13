package com.robestone.species.js;

import java.util.ArrayList;
import java.util.List;

public class Node {

	private Integer id;
	private List<Integer> childIds;
	private String fileKey;
	private String filePath;
	private Node parent;
	private JsonEntry entry;
	
	private List<Node> partition = new ArrayList<>();
	
	private int totalDescendants;
	
	private List<Node> children = new ArrayList<>();
	
	public Node(JsonEntry entry, Integer id, List<Integer> childIds) {
		this.id = id;
		this.childIds = childIds;
		this.entry = entry;
	}
	public JsonEntry getEntry() {
		return entry;
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
	public List<Node> getPartition() {
		return partition;
	}
	public String toString() {
		return String.valueOf(id);
	}
	public String getFileKey() {
		return fileKey;
	}
	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}
	
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public Node getParent() {
		return parent;
	}
	public void setParent(Node parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		return (this == obj);
	}
	
	public boolean isInAncestorPartition() {
		Node p = parent;
		while (p != null) {
			if (p.partition.contains(this)) {
				return true;
			}
			p = p.parent;
		}
		return false;
	}
}
