package com.robestone.species.tapestry.pages;

import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry5.annotations.IncludeStylesheet;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.robestone.species.ISpeciesService;
import com.robestone.species.LogHelper;
import com.robestone.species.tapestry.services.SearchContext;

@IncludeStylesheet("context:style/species.css")
@IncludeJavaScriptLibrary( {
	"context:js/jquery-1.3.1.js",
	"context:js/species.js"
})
public class AbstractPage {

	@SessionState
	private SearchContext searchContext;

	@Inject
	private ISpeciesService speciesService;
	
	public ISpeciesService getSpeciesService() {
		return speciesService;
	}
	protected SearchContext getSearchContext() {
		return searchContext;
	}
	public Object onSuccess() {
		LogHelper.speciesLogger.info("AbstractPage.onSuccess");
		return Search.NAME;
	}
	public Object onActionFromTree(String crunchedIds) {
		LogHelper.speciesLogger.info(getClass().getSimpleName() + ".onActionFromTree." + crunchedIds);
		getSearchContext().setCrunchedIds(crunchedIds);
		return Search.NAME;
	}
	
}
