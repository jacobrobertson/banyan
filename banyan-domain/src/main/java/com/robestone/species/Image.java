package com.robestone.species;

import com.robestone.species.parse.ImagesCreater;

public class Image {

	private int entryId;
	
	private int tinyWidth;
	private int tinyHeight;
	private int previewWidth;
	private int previewHeight;
	private int detailWidth;
	private int detailHeight;
	private String link;
	private String imagePathPart;
	
	public int getEntryId() {
		return entryId;
	}
	public void setEntryId(int entryId) {
		this.entryId = entryId;
	}
	public int getTinyWidth() {
		return tinyWidth;
	}
	public void setTinyWidth(int tinyWidth) {
		this.tinyWidth = tinyWidth;
	}
	public int getTinyHeight() {
		return tinyHeight;
	}
	public void setTinyHeight(int tinyHeight) {
		this.tinyHeight = tinyHeight;
	}
	public int getPreviewWidth() {
		return previewWidth;
	}
	public void setPreviewWidth(int previewWidth) {
		this.previewWidth = previewWidth;
	}
	public int getPreviewHeight() {
		return previewHeight;
	}
	public void setPreviewHeight(int previewHeight) {
		this.previewHeight = previewHeight;
	}
	public int getDetailWidth() {
		return detailWidth;
	}
	public void setDetailWidth(int detailWidth) {
		this.detailWidth = detailWidth;
	}
	public int getDetailHeight() {
		return detailHeight;
	}
	public void setDetailHeight(int detailHeight) {
		this.detailHeight = detailHeight;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public void setLocalNameFromLatinName(String latinName) {
		int pos = link.lastIndexOf('.');
		String extension = link.substring(pos + 1);
		this.imagePathPart = ImagesCreater.getImagePathHashed(latinName) + "/" + latinName + "." + extension;
	}
	public String getImagePathPart() {
		return imagePathPart;
	}
}
