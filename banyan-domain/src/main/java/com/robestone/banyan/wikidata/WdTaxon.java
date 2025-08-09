package com.robestone.banyan.wikidata;

import java.util.List;

import com.robestone.banyan.taxons.Rank;

public class WdTaxon {

	private String qID;
	private String parentQid;
	private Rank rank;
	private boolean extinct;
	private String commonName;
	private String latinName;
	private List<WdImage> images;
	
	public List<WdImage> getImages() {
		return images;
	}
	public void setImages(List<WdImage> images) {
		this.images = images;
	}
	public String getLatinName() {
		return latinName;
	}
	public void setLatinName(String latinName) {
		this.latinName = latinName;
	}
	public String getQid() {
		return qID;
	}
	public void setQid(String qID) {
		this.qID = qID;
	}
	public String getParentQid() {
		return parentQid;
	}
	public void setParentQid(String parentQid) {
		this.parentQid = parentQid;
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
	
}
