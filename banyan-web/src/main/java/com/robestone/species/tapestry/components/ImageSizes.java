package com.robestone.species.tapestry.components;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.annotations.Parameter;

import com.robestone.species.Entry;

public class ImageSizes {

	@Parameter(required = true)
	private Collection<Entry> entries;
	private Entry renderEntry;
	
	public Collection<Entry> getEntries() {
		return entries;
	}
	public void setEntries(Collection<Entry> entries) {
		this.entries = entries;
	}
	public Entry getRenderEntry() {
		return renderEntry;
	}
	public void setRenderEntry(Entry renderEntry) {
		this.renderEntry = renderEntry;
	}
	public String getRenderLink() {
		return renderEntry.getId().toString();
	}
	public String getRenderCaption() {
		String title = TreeComponent.getHoverTitle(renderEntry);
		if (title != null) {
			// the one thing that needs escaping
			title = StringUtils.replace(title, "\"", "\\\"");
			title = title.replace('<', '[');
			title = title.replace('>', ']');
		}
		return title;
	}
	
}
