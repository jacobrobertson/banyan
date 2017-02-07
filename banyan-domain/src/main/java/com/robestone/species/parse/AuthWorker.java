package com.robestone.species.parse;

import java.util.List;

import com.robestone.species.LogHelper;

public class AuthWorker extends AbstractWorker {

	public static void main(String[] args) {
		new AuthWorker().setStatusForRedirect();
	}
	
	public void setStatusForRedirect() {
		LogHelper.speciesLogger.info("setStatusForRedirect");
		int count = 0;
		// get all parse that are AUTH
		List<ParseStatus> auths = parseStatusService.findAllAuth();

		for (ParseStatus auth: auths) {
			// get the redirect
			List<String> redirects = speciesService.findRedirectFrom(auth.getLatinName());
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
