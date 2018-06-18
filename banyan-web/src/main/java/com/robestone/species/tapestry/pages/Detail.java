package com.robestone.species.tapestry.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.tapestry5.annotations.Persist;

import com.robestone.species.Entry;
import com.robestone.species.EntryComparator;
import com.robestone.species.EntryUtilities;
import com.robestone.species.UrlIdUtilities;
import com.robestone.species.parse.ImagesCreater;
import com.robestone.species.tapestry.components.TreeComponent;

public class Detail extends AbstractPage {

	private static EntryComparator comp = new EntryComparator();
	
	@Persist
	private List<Entry> entries;
	
	@Persist
	private Entry entry;
	
	private Entry renderEntry;

	@Persist
	private List<List<? extends Entry>> columns;
	private List<? extends Entry> column;

	@Persist
	private List<Entry> allEntries;
	
	@Persist
	private Entry depictedEntry;
	
	public Entry getRenderEntry() {
		return renderEntry;
	}
	public void setRenderEntry(Entry renderEntry) {
		this.renderEntry = renderEntry;
	}
	public Detail onActionFromDetail(String urlId) {
		Integer intId = UrlIdUtilities.getIdFromUrlId(urlId);
		setId(intId);
		return this;
	}
	public List<List<? extends Entry>> getColumns() {
		return columns;
	}
	public void setColumn(List<? extends Entry> column) {
		this.column = column;
	}
	public List<? extends Entry> getColumn() {
		return column;
	}
	
	public void setId(Integer id) {
		// get the existing root
		entry = ensureIdInRoot(id);
		List<? extends Entry> children = getSpeciesService().findChildren(id);
		// build the columns and ranks
		columns = toColumns(children);
		entries = toList(entry);
		allEntries = new ArrayList<Entry>();
		allEntries.addAll(children);
		allEntries.addAll(entries);
		
		depictedEntry = getSpeciesService().findDepictedEntry(entry);
		if (depictedEntry != null) {
			allEntries.add(depictedEntry);
		}
	}
	private Entry ensureIdInRoot(Integer id) {
		Entry root = getSearchContext().getRoot();
		Entry e = EntryUtilities.findEntry(root, id);
		if (e == null) {
			// create the new root with the new id
			Collection<Integer> ids = new ArrayList<Integer>();
			ids.add(id);
			root = getSpeciesService().findTreeForNodes(ids, root);
			// set the new root for the context - so it gets the new child
			getSearchContext().setRoot(root);
			e = EntryUtilities.findEntry(root, id);
		}
		return e;
	}
	private static List<List<? extends Entry>> toColumns(List<? extends Entry> children) {
		if (CollectionUtils.isEmpty(children)) {
			return null;
		}
		Collections.sort(children, comp);
		List<List<? extends Entry>> columns = new ArrayList<List<? extends Entry>>();
		int colSize = 20;
		int maxCols = 3;
		int max = colSize * maxCols;
		
		int size = children.size();
		if (size > max) {
			size = max;
		}
		
		int maxRows = size / maxCols;
		
		int sizeHelper = size % maxCols;
		int index = 0;
		for (int i = 0; i < maxCols; i++) {
			int rows = maxRows;
			if (sizeHelper > 0) {
				sizeHelper--;
				rows++;
			}
			List<Entry> column = new ArrayList<Entry>();
			columns.add(column);
			for (int j = 0; j < rows; j++) {
				Entry e = children.get(index);
				column.add(e);
				index++;
			}
		}
		return columns;
	}
	private static List<Entry> toList(Entry entry) {
		List<Entry> list = new ArrayList<Entry>();
		while (true) {
			list.add(0, entry);
			if (entry.getParent() != null) {
				entry = entry.getParent();
			} else {
				break;
			}
		}
		return list;
	}
	public Entry getEntry() {
		return entry;
	}
	public List<Entry> getEntries() {
		return entries;
	}
	public String getQuery() {
		String q = "";
		if (entry.getCommonName() != null) {
			q += TreeComponent.getFullRenderableCommonName(entry);
			q += " ";
		}
		q += entry.getLatinName();
		q = toQuery(q);
		return q;
	}
	private String toQuery(String n) {
		n = n.replaceAll(" ", "+");
		return n;
	}
	public String getEntryQueryTitlePart() {
		String t;
		if (entry.getCommonName() != null) {
			t = TreeComponent.getShortenedRenderableCommonName(entry)
				+ " (" + entry.getLatinName() + ")";
		} else {
			t = entry.getLatinName();
		}
		return t;
	}
	public String getEntryDisplayName() {
		String cn = TreeComponent.getFullRenderableCommonName(entry);
		if (cn == null) {
			cn = entry.getLatinName();
		}
		return cn;
	}
	public String getEntryDisplayLatinName() {
		if (entry.getCommonName() == null) {
			return null;
		}
		return entry.getLatinName();
	}
	public String getImageSourceUrl() {
		return ImagesCreater.getImageSourceUrl(entry);
	}
	public String getEntryDetailImageUrl() {
		// we use "preview" instead of "detail" because
		// it was too expensive to store all preview images which were the largest
		return TreeComponent.getThumbnailUrl(getEntry(), "preview");
	}
	public List<? extends Entry> getAllEntries() {
		return allEntries;
	}
	public String getCrunchedIds() {
		return getSearchContext().getLeavesCrunchedIds();
	}
	public Entry getDepictedEntry() {
		return depictedEntry;
	}
}
