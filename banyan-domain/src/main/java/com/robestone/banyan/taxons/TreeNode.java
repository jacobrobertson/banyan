package com.robestone.banyan.taxons;

import java.util.List;

public interface TreeNode<T extends TreeNode<T>> extends AnalyzableTreeNode {

	T getParent();
	Integer getParentId();
	void setParent(T parent);
	List<T> getChildren();
	void setChildren(List<T> children);
	boolean hasChildren();
	int getLoadedChildrenSize();
}
