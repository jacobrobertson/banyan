package com.robestone.species.tapestry.pages;

import org.apache.tapestry5.ioc.annotations.Inject;

import com.robestone.species.IExamplesService;

public class Examples extends AbstractPage {

	@Inject
	private IExamplesService examplesService;

	public IExamplesService getExamplesService() {
		return examplesService;
	}
}
