package com.robestone.banyan.wikispecies;

import java.util.List;

import com.robestone.banyan.util.LogHelper;
import com.robestone.banyan.workers.AbstractWorker;

public class AuthWorker extends AbstractWorker {

	public static void main(String[] args) {
		new AuthWorker().setStatusForRedirect();
	}
	
	public void setStatusForRedirect() {
		int count = 0;
		// get all parse that are AUTH
		List<ParseStatus> auths = parseStatusService.findAllAuth();
		LogHelper.speciesLogger.info("setStatusForRedirect." + auths.size());

		for (ParseStatus auth: auths) {
			// get the redirect
			List<String> redirects = getWikiSpeciesService().findRedirectFrom(auth.getLatinName());
			for (String redirect: redirects) {
				// set the matching parse to AUTH
				boolean changed = parseStatusService.updateToAuth(redirect);
				if (changed) {
					LogHelper.speciesLogger.info("parseStatusService.updateToAuth." + (count++) + "." + redirect);
				}
			}
			
		}
		
	}
	
}
