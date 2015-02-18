package com.robestone.species.tapestry.components;

import org.apache.tapestry5.annotations.Parameter;

import com.robestone.species.Entry;
import com.robestone.species.UrlIdUtilities;

public class DetailEntry {
	
	@Parameter(required = true)
	private Entry entry;
	
	@Parameter(value = "true")
	private boolean preview;
	
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	public Entry getEntry() {
		return entry;
	}
	public String getEntryClass() {
		if (preview && entry.getImageLink() != null) {
			return "preview";
		} else {
			return "non-preview";
		}
	}
	public boolean isShowImage() {
		return preview && entry.getImageLink() != null;
	}
	public String getEntryName() {
		return entry.getId().toString();
	}
	public String getUrlId() {
		return UrlIdUtilities.getUrlId(entry);
	}
	public String getRenderThumbUrl() {
		return TreeComponent.getThumbnailUrl(entry);
	}
	public String getHoverTitle() {
		if (entry.getImageLink() == null) {
			// TODO need to figure out how to hide this attribute if it's null
			return TreeComponent.getHoverTitle(entry);
		} else {
			// don't want a hover title - will do preview pop-up
			return "";
		}
	}
	public String getShortenedRenderableCommonName() {
		return TreeComponent.getShortenedRenderableCommonName(entry);
	}
}
