package com.robestone.banyan.taxons;

public class Taxon {

	private Integer taxonId;
	private Integer parentTaxonId;
	private Rank rank;
	private boolean extinct;
	private String commonName;
	private String latinName;
	private CrunchedIds interestingCrunchedIds;
	private Image image;

	private String commonNameClean;
	private String commonNameCleanest;
	private String latinNameClean;
	private String latinNameCleanest;
	
	private Integer interestingParentTaxonId;
	private boolean boring;

	public boolean isBoring() {
		return boring;
	}
	public void setBoring(boolean boring) {
		this.boring = boring;
	}
	public Integer getInterestingParentTaxonId() {
		return interestingParentTaxonId;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	public CrunchedIds getInterestingCrunchedIds() {
		return interestingCrunchedIds;
	}
	public void setInterestingCrunchedIds(CrunchedIds interestingCrunchedIds) {
		this.interestingCrunchedIds = interestingCrunchedIds;
	}
	public Integer getParentTaxonId() {
		return parentTaxonId;
	}
	public void setParentTaxonId(Integer parentTaxonId) {
		this.parentTaxonId = parentTaxonId;
	}
	
	public void setInterestingParentTaxonId(Integer interestingParentTaxonId) {
		this.interestingParentTaxonId = interestingParentTaxonId;
	}
	public Integer getTaxonId() {
		return taxonId;
	}
	public void setTaxonId(Integer taxonId) {
		this.taxonId = taxonId;
	}
	public String getLatinName() {
		return latinName;
	}
	public void setLatinName(String latinName) {
		this.latinName = latinName;
	}
	public Rank getRank() {
		return rank;
	}
	public void setRank(Rank rank) {
		this.rank = rank;
	}
	public boolean isExtinct() {
		return extinct;
	}
	public void setExtinct(boolean extinct) {
		this.extinct = extinct;
	}
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	public String getCommonNameClean() {
		return commonNameClean;
	}
	public void setCommonNameClean(String commonNameClean) {
		this.commonNameClean = commonNameClean;
	}
	public String getCommonNameCleanest() {
		return commonNameCleanest;
	}
	public void setCommonNameCleanest(String commonNameCleanest) {
		this.commonNameCleanest = commonNameCleanest;
	}
	public String getLatinNameClean() {
		return latinNameClean;
	}
	public void setLatinNameClean(String latinNameClean) {
		this.latinNameClean = latinNameClean;
	}
	public String getLatinNameCleanest() {
		return latinNameCleanest;
	}
	public void setLatinNameCleanest(String latinNameCleanest) {
		this.latinNameCleanest = latinNameCleanest;
	}

}
