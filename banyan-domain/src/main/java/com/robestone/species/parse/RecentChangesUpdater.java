package com.robestone.species.parse;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;

import com.robestone.species.LogHelper;
import com.robestone.species.WikiSpeciesTreeFixer;

public class RecentChangesUpdater extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		RecentChangesUpdater recent = new RecentChangesUpdater();
		
		if (args != null && args.length > 0) {
			recent.maxChanges = Integer.parseInt(args[0]);
		}
		
		boolean crawlNewLinks = true;
		boolean runMaintenance = true;
		
		if (crawlNewLinks) {
			recent.crawlNewLinks();
		}
		if (runMaintenance) {
			recent.runMaintenance();
		}
	}
	
	private int maxOldLinks = 1000;
	private int maxChanges = 5000;
	private int maxDays = 10;
	
	public void runAll() throws Exception {
		LogHelper.speciesLogger.info("RecentChangesUpdater.runAll");
		crawlNewLinks();
		runMaintenance();
	}
	
	public void runMaintenance() throws Exception {
		LogHelper.speciesLogger.info("RecentChangesUpdater.runMaintenance");
		//*
		// any changes that "fix" what the crawling found
		new WikiSpeciesTreeFixer(speciesService).run();
		
		// fix the parents (i.e. set ids for parent latin name) 
		speciesService.fixParents();
		
		// cleanup extinct (might have added children of extinct parents)
		speciesService.fixExtinct();
		 
		// clean names
		// TODO - I don't know what purpose this accomplishes unless I've updated the naming logic, but maybe there's another reason for this
//		speciesService.recreateCleanNames();
		 
		// run full boring suite
		BoringWorker.main(null);
		//*/
		// create new interesting crunched ids
		InterestingSubspeciesWorker.logger.setLevel(Level.INFO);
		new InterestingSubspeciesWorker().run();
		
		new SiblingsWithSameCommonNamesAnalyzer().run();
		
		new LinkedImagesWorker().run();
		
		// run this now - sometimes things in the examples get boring,
		// and this will fix it
		new ExamplesCruncherWorker().run();
		
		// download the images - has to be last due to "System.exit"
		ThumbnailDownloader.main(null);
	}

	public void crawlNewLinks() throws Exception {
		Set<String> allLinks = new HashSet<String>();

		String url = "https://species.wikimedia.org/w/index.php?title=Special:RecentChanges&days=" +
				+ maxDays + "&limit=" + maxChanges
				+ "&namespace=0";
		LogHelper.speciesLogger.info("url." + url);
		String page = WikiSpeciesCache.getPageForUrl(url, 100);
		
		Set<String> parsedLinks = WikiSpeciesCrawler.parseLinks(page);
		LogHelper.speciesLogger.info("crawlNewLinks.found." + parsedLinks.size());
		
		allLinks.addAll(parsedLinks);
			
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.pushStoredLinks(allLinks, true);
		crawler.crawl();
	}
	public void crawlOldLinks() throws Exception {
		Collection<String> oldestLinks = parseStatusService.findOldestLinks(maxOldLinks);
		LogHelper.speciesLogger.info("oldestLinks." + oldestLinks.size());

		Set<String> allLinks = new HashSet<String>();
		allLinks.addAll(oldestLinks);

		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.pushStoredLinks(allLinks);
		crawler.crawl();
	}

}
