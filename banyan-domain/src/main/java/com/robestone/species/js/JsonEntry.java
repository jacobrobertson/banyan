package com.robestone.species.js;

import java.util.List;

public class JsonEntry {

	private Integer id;
	private List<String> cnames;
	private String lname;
	private Integer parentId;
	private String img;
	private Integer tHeight;
	private Integer tWidth;
	private Integer pHeight;
	private Integer pWidth;
	private List<Integer> childrenIds;
	private List<Integer> showMoreLeafIds;
	private List<Integer> showMoreOtherIds;
	
	private boolean extinct;
	private boolean ancestorExtinct;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public List<String> getCnames() {
		return cnames;
	}
	public void setCnames(List<String> cnames) {
		this.cnames = cnames;
	}
	public String getLname() {
		return lname;
	}
	public void setLname(String lname) {
		this.lname = lname;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public Integer gettHeight() {
		return tHeight;
	}
	public void settHeight(Integer tHeight) {
		this.tHeight = tHeight;
	}
	public Integer gettWidth() {
		return tWidth;
	}
	public void settWidth(Integer tWidth) {
		this.tWidth = tWidth;
	}
	public Integer getpHeight() {
		return pHeight;
	}
	public void setpHeight(Integer pHeight) {
		this.pHeight = pHeight;
	}
	public Integer getpWidth() {
		return pWidth;
	}
	public void setpWidth(Integer pWidth) {
		this.pWidth = pWidth;
	}
	public List<Integer> getChildrenIds() {
		return childrenIds;
	}
	public void setChildrenIds(List<Integer> childrenIds) {
		this.childrenIds = childrenIds;
	}
	public List<Integer> getShowMoreLeafIds() {
		return showMoreLeafIds;
	}
	public void setShowMoreLeafIds(List<Integer> showMoreLeafIds) {
		this.showMoreLeafIds = showMoreLeafIds;
	}
	public List<Integer> getShowMoreOtherIds() {
		return showMoreOtherIds;
	}
	public void setShowMoreOtherIds(List<Integer> showMoreOtherIds) {
		this.showMoreOtherIds = showMoreOtherIds;
	}
	public boolean isExtinct() {
		return extinct;
	}
	public void setExtinct(boolean extinct) {
		this.extinct = extinct;
	}
	public boolean isAncestorExtinct() {
		return ancestorExtinct;
	}
	public void setAncestorExtinct(boolean ancestorExtinct) {
		this.ancestorExtinct = ancestorExtinct;
	}
	
}