package com.robestone.banyan.json;

import java.util.ArrayList;
import java.util.List;

public class JsonNode {

	private Integer id;
	private List<Integer> childIds;
	private String fileKey;
	private String filePath;
	private JsonNode parent;
	private JsonEntry entry;
	
	private List<JsonNode> partition = new ArrayList<>();
	
	private int totalDescendants;
	
	private List<JsonNode> children = new ArrayList<>();
	
	public JsonNode(JsonEntry entry, Integer id, List<Integer> childIds) {
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
	public List<JsonNode> getChildren() {
		return children;
	}
	public int getTotalDescendants() {
		return totalDescendants;
	}
	public void setTotalDescendants(int totalDescendants) {
		this.totalDescendants = totalDescendants;
	}
	public List<JsonNode> getPartition() {
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
	public JsonNode getParent() {
		return parent;
	}
	public void setParent(JsonNode parent) {
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
		JsonNode p = parent;
		while (p != null) {
			if (p.partition.contains(this)) {
				return true;
			}
			p = p.parent;
		}
		return false;
	}
}
