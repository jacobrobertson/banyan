package com.robestone.species;

import java.util.HashSet;
import java.util.Set;

public class Example {

	private String caption;
	private String terms;
	private String simpleTitle;
	private String crunchedIds;
	private int groupId;
	private int id;
	private String depictedTerm;
	private Image depictedImage;
	private Set<String> pinnedTerms;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getTerms() {
		return terms;
	}
	public void setTerms(String terms) {
		pinnedTerms = new HashSet<>();
		StringBuilder buf = new StringBuilder();
		String[] termsSplit = terms.split(",");
		for (String term : termsSplit) {
			if (term.startsWith("$")) {
				term = term.substring(1);
				depictedTerm = term;
			}
			if (term.startsWith("!")) {
				term = term.substring(1);
			} else {
				pinnedTerms.add(term);
			}
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append(term);
		}
		this.terms = buf.toString();
	}
	public String getCrunchedIds() {
		return crunchedIds;
	}
	public void setCrunchedIds(String crunchedIds) {
		this.crunchedIds = crunchedIds;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getSimpleTitle() {
		return simpleTitle;
	}
	public void setSimpleTitle(String simpleTitle) {
		this.simpleTitle = simpleTitle;
	}
	public String getDepictedTerm() {
		return depictedTerm;
	}
	public Image getDepictedImage() {
		return depictedImage;
	}
	public void setDepictedImage(Image depictedImage) {
		this.depictedImage = depictedImage;
	}
	public Set<String> getPinnedTerms() {
		return pinnedTerms;
	}
}
