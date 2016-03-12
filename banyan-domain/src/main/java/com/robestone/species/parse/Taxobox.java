package com.robestone.species.parse;

import com.robestone.species.Rank;

public class Taxobox {

	private String image;
	private String imageSpeciesDepicted;
	private String binomialAuthorityRaw;
	
	private String commonName;
	private String latinName;
	private String parentLatinName;
	
	private Rank rank;
	
	public String getBinomialAuthorityRaw() {
		return binomialAuthorityRaw;
	}
	public void setBinomialAuthorityRaw(String binomialAuthorityRaw) {
		this.binomialAuthorityRaw = binomialAuthorityRaw;
	}
	public String getImageSpeciesDepicted() {
		return imageSpeciesDepicted;
	}
	public void setImageSpeciesDepicted(String imageSpeciesDepicted) {
		this.imageSpeciesDepicted = imageSpeciesDepicted;
	}

	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	public String getLatinName() {
		return latinName;
	}
	public String getLatinNameFormatted() {
		if (latinName == null) {
			return null;
		}
		return latinName.replace(" ", "_");
	}
	public void setLatinName(String latinName) {
		this.latinName = latinName;
	}
	public String getParentLatinName() {
		return parentLatinName;
	}
	public void setParentLatinName(String parentLatinName) {
		this.parentLatinName = parentLatinName;
	}
	public Rank getRank() {
		return rank;
	}
	public void setRank(Rank rank) {
		this.rank = rank;
	}
	
}
