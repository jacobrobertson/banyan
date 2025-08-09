package com.robestone.banyan.wikispecies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.robestone.banyan.util.LogHelper;
import com.robestone.banyan.workers.AbstractWorker;

public class RedirectWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new RedirectWorker().recrawlRedirects();
	}
	
	/**
	 * Shouldn't have to do this everytime, but some things break occassionally, so we rerun this sometimes
	 * @throws Exception 
	 */
	public void recrawlRedirects() throws Exception {

		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(true);
		
		Map<String, String> fromsMap = getWikiSpeciesService().findAllRedirectFromsMap();
		Set<String> tosSet = new HashSet<String>(fromsMap.values());
		
		Collection<String> allLatin = getWikiSpeciesService().findAllLatinNames();
		List<String> toRecrawl = new ArrayList<String>();
		for (String latin : allLatin) {
			if (fromsMap.containsKey(latin) || tosSet.contains(latin)) {
				toRecrawl.add(latin);
				String to = fromsMap.get(latin);
				if (to != null) {
					toRecrawl.add(to);
				}
			}
		}
		
		Collections.sort(toRecrawl);
		LogHelper.speciesLogger.debug("recrawlRedirects.froms." + toRecrawl.size());
		
		int count = 0;
		int max = toRecrawl.size();
		for (String latin : toRecrawl) {
			getWikiSpeciesService().deleteRedirect(latin);
			LogHelper.speciesLogger.debug("recrawlRedirects." + (count++) + "/" + max + "." + latin);
			ParseStatus ps = new ParseStatus();
			ps.setUrl(latin);
			crawler.crawlOne(ps, false);
		}
	}
	
}
