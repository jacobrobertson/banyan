package com.robestone.banyan.taxons;

import java.util.List;

public class TaxonNode extends Taxon implements TreeNode<TaxonNode> {

	private TaxonNode parent;
	private List<TaxonNode> children;
	
	// these properties are transient
	private boolean pinned;
	public boolean isPinned() {
		return pinned;
	}
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}
	
	@Override
	public Integer getParentId() {
		if (parent == null) {
			return getParentTaxonId();
		} else {
			return parent.getId();
		}
	}
	public TaxonNode getParent() {
		return parent;
	}
	public void setParent(TaxonNode parent) {
		this.parent = parent;
	}
	public List<TaxonNode> getChildren() {
		return children;
	}
	public void setChildren(List<TaxonNode> children) {
		this.children = children;
	}
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}
	@Override
	public Integer getId() {
		return getTaxonId();
	}
	/**
	 * Counts just the children it has in the list here
	 */
	public int getLoadedChildrenSize() {
		if (children == null) {
			return 0;
		}
		return children.size();
	}
}
