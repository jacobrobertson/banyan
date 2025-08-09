package com.robestone.banyan.wikispecies;

import java.util.List;

import com.robestone.banyan.taxons.CrunchedIds;
import com.robestone.banyan.taxons.Image;
import com.robestone.banyan.taxons.Rank;

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
	public EntryProperties() {
	}
	public EntryProperties(EntryProperties copy) {
		this.depictedLatinName = copy.depictedLatinName;
		this.depictedId = copy.depictedId;
		this.id = copy.id;
		this.rank = copy.rank;
		this.extinct = copy.extinct;
		this.parentId = copy.parentId;
		this.commonName = copy.commonName;
//		this.commonNames = copy.commonNames;
		this.commonNameBoring = copy.commonNameBoring;
		this.isBoring = copy.isBoring;
		this.isCommonNameSharedWithSiblings = copy.isCommonNameSharedWithSiblings;
		this.latinName = copy.latinName;
		this.parentLatinName = copy.parentLatinName;
		this.persistedChildCount = copy.persistedChildCount;
		this.interestingChildCount = copy.interestingChildCount;
		this.interestingParentId = copy.interestingParentId;
		this.commonNameClean = copy.commonNameClean;
		this.commonNameCleanest = copy.commonNameCleanest;
		this.latinNameClean = copy.latinNameClean;
		this.latinNameCleanest = copy.latinNameCleanest;
		this.image = copy.image;
		this.linkedImageId = copy.linkedImageId;
		this.crunchedInterestingIds = copy.crunchedInterestingIds;
	}
	
}
