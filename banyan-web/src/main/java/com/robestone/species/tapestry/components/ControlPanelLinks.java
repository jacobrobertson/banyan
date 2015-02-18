package com.robestone.species.tapestry.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.tapestry5.annotations.Parameter;

import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.UrlIdUtilities;

/**
 * @author jacob
 */
public class ControlPanelLinks extends AbstractTreeComponent {

	private Logger logger = Logger.getLogger(ControlPanelLinks.class);
	
	private static final String NO = "false";
	
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
	public String getRenderClose() {
		return getRenderLink(EntryUtilities.getCrunchedIdsForClose(getRoot(), renderEntry.getId()));
	}
	/**
	 * Show children.
	 */
	public String getRenderShow() {
		if (isShowChildrenNeeded(renderEntry)) {
			String cids = getCrunchedIdsForShowChildren(renderEntry);
			return getRenderLink(cids);
		} else {
			return NO;
		}
	}
	private boolean isFocusNeeded(Entry entry) {
		Entry parent = entry;
		while ((parent = parent.getParent()) != null) {
			if (parent.getLoadedChildrenSize() > 1) {
				return true;
			}
		}
		return false;
	}

	public String getRenderDetail() {
		return UrlIdUtilities.getUrlId(renderEntry);
	}
	public String getRenderShowMore() {
		if (isShowMoreNeeded(renderEntry)) {
			// get the current leaves, combine with crunched ids, then combine
			Collection<Integer> rootIds = EntryUtilities.getLeavesIds(getRoot());
			logger.debug("getRenderShowMore >");
			logger.debug("getRenderShowMore.rootIds." + rootIds);
			Set<Integer> moreIds = renderEntry.getInterestingCrunchedIds().getIds();
			logger.debug("getRenderShowMore.moreIds." + moreIds);
			Set<Integer> allIds = new HashSet<Integer>(moreIds);
			allIds.addAll(rootIds);
			logger.debug("getRenderShowMore.allIds." + allIds);
			String finalIds = EntryUtilities.CRUNCHER.toString(allIds);
			return getRenderLink(finalIds);
		} else {
			return NO;
		}
	}
	/**
	 * I.e. "show 1 (more) child"
	 */
	public String getRenderShowCaption() {
		if (isShowChildrenNeeded(renderEntry)) {
			return "'" + getShowChildrenCaption(renderEntry) + "'";
		} else {
			return NO;
		}
	}
	/**
	 * I.e. "Show 1 more species"
	 */
	public String getRenderShowMoreCaption() {
		if (isShowMoreNeeded(renderEntry)) {
			Collection<Integer> rootIds = EntryUtilities.getIds(getRoot());
			Set<Integer> moreIds = renderEntry.getInterestingCrunchedIds().getIds();
			Set<Integer> diffIds = new HashSet<Integer>(moreIds);
			diffIds.removeAll(rootIds);
			int len = diffIds.size();
			return "'Show " + len + " More Species'";
		} else {
			return NO;
		}
	}
	public String getRenderFocus() {
		if (isFocusNeeded(renderEntry)) {
			return getRenderLink(EntryUtilities.getCrunchedLeavesIds(renderEntry));
		} else {
			return NO;
		}
	}
	/**
	 * Hide children.
	 */
	public String getRenderHide() {
		if (renderEntry.getLoadedChildrenSize() < 1) {
			return NO;
		} else {
			return getRenderLink(EntryUtilities.getCrunchedIdsForHideChildren(getRoot(), renderEntry.getId()));
		}
	}
	private String getRenderLink(String ids) {
		return "'search.tree/" + ids + "'";
	}
}
