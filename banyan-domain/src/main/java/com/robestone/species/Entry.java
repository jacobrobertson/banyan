package com.robestone.species;

import java.util.List;


public interface Entry {

	String getDepictedLatinName();
	Integer getDepictedId();
	
	String getCommonName();
	List<String> getCommonNames();

	String getLatinName();

	/**
	 * Counts just the children it has in the list here
	 */
	int getLoadedChildrenSize();

	List<? extends Entry> getChildren();

	boolean hasChildren();

	Rank getRank();

	Entry getParent();

	Integer getId();

	Image getImage();
	String getImageLink();

	boolean isExtinct();

	Integer getParentId();

	int getPersistedChildCount();

	/**
	 * As long as one ancestor is extinct, returns true.
	 */
	boolean isAncestorExtinct();
	
	EntryProperties getEntryProperties();
	CrunchedIds getInterestingCrunchedIds();
	void setInterestingCrunchedIds(CrunchedIds ids);
	
	/**
	 * For rendering purposes - is the common name very similar to the latin name?
	 * @return
	 */
	boolean isCommonNameBoring();
	boolean isBoring();
	
	boolean isCommonNameSharedWithSiblings();
	void setCommonNameSharedWithSiblings(boolean shared);
	
	boolean isCollapsed();
	/**
	 * Meaning how many other entries were collapsed, not counting this one.
	 */
	int getCollapsedCount();
	/**
	 * @param collapsedCount Meaning how many other entries were collapsed, not counting this one.
	 */
	void setCollapsedCount(int collapsedCount);
}
