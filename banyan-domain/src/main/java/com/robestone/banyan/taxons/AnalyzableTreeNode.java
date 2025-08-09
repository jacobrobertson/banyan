package com.robestone.banyan.taxons;

public interface AnalyzableTreeNode {

	AnalyzableTreeNode getParent();
	
	Integer getId();
	boolean isExtinct();

	void setLatinNameCleanest(String latinNameCleanest);
	String getLatinNameCleanest();

	void setLatinNameClean(String latinNameClean);
	String getLatinNameClean();

	void setCommonNameCleanest(String commonNameCleanest);
	String getCommonNameCleanest();

	void setCommonNameClean(String commonNameClean);
	String getCommonNameClean();

	void setLatinName(String latinName);
	String getLatinName();

	void setCommonName(String commonName);
	String getCommonName();

}
