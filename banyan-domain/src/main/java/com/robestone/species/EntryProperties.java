package com.robestone.species;

import java.util.List;



public class EntryProperties {
	
	String depictedLatinName;
	Integer depictedId;
	
	Integer id;
	Rank rank;
	boolean extinct;
	Integer parentId;

	String commonName;
	List<String> commonNames;
	
	Boolean commonNameBoring;
	boolean isBoring;
	
	boolean isCommonNameSharedWithSiblings;
	
	String latinName;
	
	String parentLatinName;
	
	int persistedChildCount;
	int interestingChildCount;
	Integer interestingParentId;

	String commonNameClean;
	String commonNameCleanest;
	String latinNameClean;
	String latinNameCleanest;
	
	Image image;
	int linkedImageId;
	
	CrunchedIds crunchedInterestingIds;
	
	public void copyInterestingAttributes() {
		this.persistedChildCount = this.interestingChildCount;
		this.parentId = this.interestingParentId;
	}
	
}
