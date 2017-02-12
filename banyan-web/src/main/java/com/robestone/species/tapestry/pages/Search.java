package com.robestone.species.tapestry.pages;

import java.util.Collection;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ExceptionReporter;

import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.IExamplesService;
import com.robestone.species.LogHelper;
import com.robestone.species.tapestry.components.NavigationBar;

/**
 * Start page of application species-tapestry.
 */
public class Search extends AbstractPage implements ExceptionReporter {

	public static final String NAME = "search";

	@Inject
	private IExamplesService examplesService;
	
	@InjectPage
	private Detail detail;
	
	@InjectComponent
	private NavigationBar navigationBar;
	
	/**
	 * Not persisted between requests.
	 */
	private String search;
	
	public NavigationBar getNavigationBar() {
		return navigationBar;
	}
	
	public boolean isShowWelcome() {
		return getSearchContext().getRoot().getLoadedChildrenSize() == 0;
	}
	
	public Detail onActionFromDetail(String id) {
		detail.onActionFromDetail(id);
		return detail;
	}
	
	public void reportException(Throwable exception) {
		getSearchContext().startOver();
		search = null;
		exception.printStackTrace();
	}
	
	public void onSelectedFromSearch() {
		LogHelper.speciesLogger.info("onSelectedFromSearch." + search);
		getSearchContext().search(search);
	}
	
	public Object onActionFromStartOver() {
		LogHelper.speciesLogger.info("onActionFromStartOver");
		getSearchContext().startOver();
		return this;
	}
	public Object onActionFromRandom() {
		getSearchContext().setRandom();
		return this;
	}
	
	public Entry getRoot() {
		Entry root = getSearchContext().getRoot();
		LogHelper.speciesLogger.info("getRoot." + root);
		return root;
	}
	
	public Collection<? extends Entry> getEntries() {
		return EntryUtilities.getEntriesForEntry(getSearchContext().getRoot());
	}

	/**
	 * Called by the form.
	 */
	public String getSearch() {
		return search;
	}
	/**
	 * Called by the form.
	 */
	public void setSearch(String search) {
		this.search = search;
	}
	public void onActivate(String crunchedIds) {
		getSearchContext().setCrunchedIds(crunchedIds);
	}
	public String onPassivate() {
		return getSearchContext().getLeavesCrunchedIds();
	}

	public String getSearchExample1() {
		return examplesService.getSearchExample1();
	}
	public String getSearchExample2() {
		return examplesService.getSearchExample2();
	}
	public String getSearchExample3() {
		return examplesService.getSearchExample3();
	}
	public String getSearchExample4() {
		return examplesService.getSearchExample4();
	}
	
}
