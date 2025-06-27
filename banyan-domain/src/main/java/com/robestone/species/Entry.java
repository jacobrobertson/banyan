package com.robestone.species;

import java.util.List;


public class Entry {

	private List<Entry> children;
	private Entry parent;
	private EntryProperties props;
	private int collapsedCount;
	private boolean pinned;
	
	public Entry() {
		props = new EntryProperties();
	}
	public Entry(EntryProperties props) {
		this.props = props;
	}
	public Entry(Rank rank, String commonName, String latinName) {
		this();
		setRank(rank);
		setCommonName(commonName);
		setLatinName(latinName);
	}
	public EntryProperties getEntryProperties() {
		return props;
	}
	public void setInterestingCrunchedIds(CrunchedIds crunchedInterestingIds) {
		props.crunchedInterestingIds = crunchedInterestingIds;
	}
	public CrunchedIds getInterestingCrunchedIds() {
		return props.crunchedInterestingIds;
	}
	public int getInterestingChildCount() {
		return props.interestingChildCount;
	}
	public void setInterestingChildCount(int interestingChildCount) {
		props.interestingChildCount = interestingChildCount;
	}
	public String getCommonName() {
		return props.commonName;
	}
	public void setCommonName(String commonName) {
		props.commonName = commonName;
	}
	public List<String> getCommonNames() {
		return props.commonNames;
	}
	public void setCommonNames(List<String> commonNames) {
		props.commonNames = commonNames;
	}
	public String getLatinName() {
		return props.latinName;
	}
	public void setLatinName(String latinName) {
		props.latinName = latinName;
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
	public List<Entry> getChildren() {
		return children;
	}
	public List<Entry> getCompleteEntryChildren() {
		return children;
	}
	public void setChildren(List<Entry> children) {
		this.children = children;
	}
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}
	public Rank getRank() {
		return props.rank;
	}
	public void setRank(Rank rank) {
		props.rank = rank;
	}
	public Entry getCompleteEntryParent() {
		return parent;
	}
	public Entry getParent() {
		return parent;
	}
	public void setParent(Entry parent) {
		this.parent = parent;
	}
	public Integer getId() {
		return props.id;
	}
	public void setId(Integer id) {
		props.id = id;
	}
	public Image getImage() {
		return props.image;
	}
	public void setImage(Image image) {
		props.image = image;
	}
	public boolean isExtinct() {
		return props.extinct;
	}
	public void setExtinct(boolean extinct) {
		props.extinct = extinct;
	}
	public Integer getParentId() {
		return props.parentId;
	}
	public void setParentId(Integer parentId) {
		props.parentId = parentId;
	}
	public int getPersistedChildCount() {
		return props.persistedChildCount;
	}
	public void setPersistedChildCount(int persistedChildCount) {
		props.persistedChildCount = persistedChildCount;
	}
	public String getCommonNameClean() {
		if (props.commonNameClean == null && props.commonName != null) {
			props.commonNameClean = EntryUtilities.getClean(props.commonName, false);
		}
		return props.commonNameClean;
	}
	public void setCommonNameClean(String commonNameClean) {
		props.commonNameClean = commonNameClean;
	}
	public String getCommonNameCleanest() {
		return props.commonNameCleanest;
	}
	public void setCommonNameCleanest(String commonNameCleanest) {
		props.commonNameCleanest = commonNameCleanest;
	}
	public String getLatinNameClean() {
		if (props.latinNameClean == null && props.latinName != null) {
			props.latinNameClean = EntryUtilities.getClean(props.latinName, false);
		}
		return props.latinNameClean;
	}
	public void setLatinNameClean(String latinNameClean) {
		props.latinNameClean = latinNameClean;
	}
	public String getLatinNameCleanest() {
		return props.latinNameCleanest;
	}
	public void setLatinNameCleanest(String latinNameCleanest) {
		props.latinNameCleanest = latinNameCleanest;
	}
	public void setParentLatinName(String parentLatinName) {
		props.parentLatinName = parentLatinName;
	}
	public Integer getInterestingParentId() {
		return props.interestingParentId;
	}
	public void setInterestingParentId(Integer interestingParentId) {
		props.interestingParentId = interestingParentId;
	}
	public Integer getDepictedId() {
		return props.depictedId;
	}
	public void setDepictedId(Integer depictedId) {
		props.depictedId = depictedId;
	}
	public String getParentLatinName() {
		if (parent == null) {
			return props.parentLatinName;
		} else {
			return parent.getLatinName();
		}
	}
	public boolean isCommonNameBoring() {
		if (props.commonNameBoring == null) {
			props.commonNameBoring = 
				CommonNameSimilarityChecker.isCommonNameCleanBoring(
						getCommonNameClean(), getLatinNameClean());
		}
		return props.commonNameBoring.booleanValue();
	}
	public boolean isAncestorExtinct() {
		return EntryUtilities.isAncestorExtinct(this);
	}
	/**
	 * Make "interesting" attributes into normal ones.
	 */
	public void copyInterestingAttributes() {
		props.copyInterestingAttributes();
	}
	public String getImageLink() {
		if (getImage() == null) {
			return null;
		}
		return getImage().getLink();
	}
	public void setImageLink(String link) {
		if (link != null) {
			if (props.image == null) {
				props.image = new Image();
			}
			props.image.setLink(link);
		}
	}
	public void setLinkedImageId(int linkedImageId) {
		props.linkedImageId = linkedImageId;
	}
	public String toString() {
		return getId() + "/" + getLatinName();
	}
	public String getDepictedLatinName() {
		return props.depictedLatinName;
	}
	public void setDepictedLatinName(String depictedLatinName) {
		props.depictedLatinName = depictedLatinName;
	}
	public boolean isBoring() {
		return props.isBoring;
	}
	public void setBoring(boolean boring) {
		props.isBoring = boring;
	}
	public boolean isCommonNameSharedWithSiblings() {
		return props.isCommonNameSharedWithSiblings;
	}
	public void setCommonNameSharedWithSiblings(boolean shared) {
		props.isCommonNameSharedWithSiblings = shared;
	}
	public boolean isCollapsed() {
		return collapsedCount > 0;
	}
	public void setCollapsedCount(int collapsedCount) {
		this.collapsedCount = collapsedCount;
	}
	public int getCollapsedCount() {
		return collapsedCount;
	}
	public boolean isPinned() {
		return pinned;
	}
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}
	
}
