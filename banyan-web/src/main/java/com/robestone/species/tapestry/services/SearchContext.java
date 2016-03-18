package com.robestone.species.tapestry.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.IExamplesService;
import com.robestone.species.ISpeciesService;
import com.robestone.species.IdCruncher;
import com.robestone.species.LogHelper;
import com.robestone.species.SpeciesService;

public class SearchContext {

	private Entry root;
	private boolean focusOnSearch;
	private String search;
	private IdCruncher cruncher = EntryUtilities.CRUNCHER;
	
	@Inject
	private ISpeciesService speciesService;
	@Inject
	private IExamplesService examplesService;

	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		LogHelper.speciesLogger.info("setSearch." + search);
		this.search = search;
	}
	
	public void search() {
		search(search);
	}
	public void search(String search) {
		if (runCommand(search)) {
			return;
		}
		// get ids
		if (!StringUtils.isBlank(search)) {
			Collection<Integer> existingIds;
			if (root == null) {
				existingIds = new ArrayList<Integer>();
			} else {
				existingIds = EntryUtilities.getIds(root);
			}
			Set<Integer> searchIds = getService().findBestIds(search, existingIds);
			// merge with root
			root = getService().findTreeForNodes(searchIds, root);
		}
		this.search = search;
		focusOnSearch = true;
	}
	
	private boolean runCommand(String query) {
		if (query.indexOf("run:") == 0) {
			String command = query.substring(4);
			LogHelper.speciesLogger.info("runCommand." + command);
			if (command.equals("clear")) {
				getService().clearCache();
				examplesService.clearCache();
				// ensure the search index is rebuilt
				getService().findBestId("anything", new ArrayList<Integer>());
			}
			return true;
		}
		return false;
	}
	public void setRandom() {
		root = getService().findRandomTree(3);
		focusOnSearch = false;
	}
	public void setCrunchedIds(String crunchedIds) {
		LogHelper.speciesLogger.info("setCrunchedIds." + crunchedIds);
		List<Integer> ids;
		if (!StringUtils.isEmpty(crunchedIds)) {
			ids = cruncher.toList(crunchedIds);
		} else {
			ids = new ArrayList<Integer>();
		}
		root = getService().findTreeForNodes(ids, null);
		focusOnSearch = false;
	}
	public String getLeavesCrunchedIds() {
		if (root == null || root.getChildren() == null || root.getChildren().isEmpty()) {
			return null;
		}
		Collection<Integer> ids = EntryUtilities.getLeavesIds(root);
		String crunchedIds = cruncher.toString(ids);
		return crunchedIds;
	}
	public void startOver() {
		root = getService().findEntry(SpeciesService.TREE_OF_LIFE_ID);
		focusOnSearch = true;
	}

	public Entry getRoot() {
		if (root == null) {
			startOver();
		}
		return root;
	}
	public void setRoot(Entry root) {
		this.root = root;
	}
	private ISpeciesService getService() {
		return speciesService;
	}
	public boolean isFocusOnSearch() {
		return focusOnSearch;
	}

}
