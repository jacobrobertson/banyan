package com.robestone.species.tapestry.components;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.robestone.species.CrunchedIds;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.ISpeciesService;
import com.robestone.species.parse.InterestingSubspeciesWorker;

public class AbstractTreeComponent {

	@Parameter(required = true)
	private Entry root;

	@Inject
	private ISpeciesService speciesService;

	public Entry getRoot() {
		return root;
	}

	public void setRoot(Entry entry) {
		this.root = entry;
	}
	public ISpeciesService getSpeciesService() {
		return speciesService;
	}
	protected String getShowChildrenCaption(Entry entry) {
		String sub;
		int num;
		if (entry.getLoadedChildrenSize() == 0) {
			num = entry.getPersistedChildCount();
			sub = String.valueOf(num);
		} else {
			int diff = entry.getPersistedChildCount() - entry.getLoadedChildrenSize();
			num = diff;
			sub = diff + " More";
		}
		String children;
		if (num == 1) {
			children = "Child";
		} else {
			children = "Children";
		}
		return "Show " + sub + " " + children;
	}
	protected String getCrunchedIdsForShowChildren(Entry entry) {
		// get leaves ids
		Collection<Integer> leaves = EntryUtilities.getLeavesIds(getRoot());
		// remove this guy (can't be a leaf anymore)
		leaves.remove(entry.getId());
		// get child ids
		Collection<Integer> childIds = getSpeciesService().findChildrenIds(entry.getId());
		// add together
		leaves.addAll(childIds);
		String cid = EntryUtilities.CRUNCHER.toString(leaves);
		return cid;
	}
	protected boolean isShowChildrenNeeded(Entry entry) {
		int diff = entry.getPersistedChildCount() - entry.getLoadedChildrenSize();
		return (diff > 0);
	}
	protected boolean isShowMoreNeeded(Entry entry) {
		if (entry.getInterestingCrunchedIds() == null) {
			return false;
		}
		CrunchedIds cids = entry.getInterestingCrunchedIds();
		
		// find out if the crunched ids are a subset of the existing ids
		List<Integer> interestingIds = cids.getIds();
		Set<Integer> currentTreeIds = EntryUtilities.getIds(getRoot());
		if (currentTreeIds.containsAll(interestingIds)) {
			return false;
		}
		
		// see if the crunched ids are a subset of the show children ids
		// - the special case is if we're saying "show 55 children", then it would be nice to have another option
		Collection<Integer> childIds = getSpeciesService().findChildrenIds(entry.getId());
		if (childIds.size() <= InterestingSubspeciesWorker.MAX_INTERESTING_CHILD_COUNT &&
				childIds.containsAll(interestingIds)) {
			return false;
		}
		
		return true;
	}

}
