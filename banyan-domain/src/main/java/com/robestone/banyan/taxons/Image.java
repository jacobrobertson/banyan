package com.robestone.banyan.taxons;

public class Image {

	private int entryId;
	
	private int tinyWidth;
	private int tinyHeight;
	private int previewWidth;
	private int previewHeight;
	private int detailWidth;
	private int detailHeight;
	private String filePath;
	
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
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String link) {
		this.filePath = link;
	}
}
