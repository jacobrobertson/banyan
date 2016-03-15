package com.robestone.species.tapestry.components;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.robestone.species.Example;
import com.robestone.species.ExampleGroup;
import com.robestone.species.IExamplesService;
import com.robestone.species.LogHelper;
import com.robestone.species.tapestry.pages.Search;
import com.robestone.species.tapestry.services.SearchContext;

public class NavigationBar {

	@SessionState
	private SearchContext searchContext;
	@Inject
	private IExamplesService examplesService;
	@InjectPage
	private Search searchPage;
	
	@Parameter(required = false, value = "false")
	private boolean showExamplesCaption;
	
	protected SearchContext getSearchContext() {
		return searchContext;
	}
	public String getCrunchedIds() {
		return getSearchContext().getLeavesCrunchedIds();
	}
	public boolean isFocusOnSearch() {
		return getSearchContext().isFocusOnSearch();
	}
	
	public String getSearch() {
		return getSearchContext().getSearch();
	}
	public void setSearch(String search) {
		getSearchContext().setSearch(search);
	}

	public Object onActionFromTree(String crunchedIds) {
		LogHelper.speciesLogger.info("NavigationBar.onActionFromTree." + crunchedIds);
		getSearchContext().setCrunchedIds(crunchedIds);
		return searchPage;
	}
	public Object onActionFromRandom() {
		getSearchContext().setRandom();
		return searchPage;
	}
	public Object onActionFromStartOver() {
		LogHelper.speciesLogger.info("onActionFromStartOver");
		getSearchContext().startOver();
		return Search.NAME;
	}

	public Object onActionFromTree() {
		return searchPage;
	}
	public void onActionFromSearch(String search) {
		LogHelper.speciesLogger.info("onActionFromSearch." + search);
		setSearch(search);
		onSelectedFromSearch();
	}
	public void onSelectedFromSearch() {
		getSearchContext().search();
	}
	public Object onSuccess() {
		LogHelper.speciesLogger.info("NavigationBar.onSuccess");
		return searchPage;
	}
	public String getExampleCaption() {
		String ids = getSearchContext().getLeavesCrunchedIds();
		ExampleGroup exampleGroup = examplesService.findExampleGroupByCrunchedIds(ids);
		if (exampleGroup == null) {
			return null;
		} else { 
			Example example = examplesService.findExampleByCrunchedIds(ids);
			if (exampleGroup.isShowExampleGroupName()) {
				return exampleGroup.getCaption() + " " + example.getCaption();
			} else {
				return example.getCaption();
			}
		}
	}
	public boolean isShowExampleCaptions() {
		return showExamplesCaption && (getExampleCaption() != null);
	}
	
}
