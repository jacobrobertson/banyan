package com.robestone.species.parse;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.robestone.species.WikiSpeciesTreeFixer;

public class RecentChangesUpdater extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		RecentChangesUpdater recent = new RecentChangesUpdater();
		
		if (args != null && args.length > 0) {
			recent.maxChanges = Integer.parseInt(args[0]);
		}
		if (args != null && args.length > 1) {
			recent.maxOldestLinks = Integer.parseInt(args[1]);
		}
		
		recent.run();
	}
	
	private int maxChanges = 1000;
	private int maxOldestLinks = 0; // doing in other job now
	private int maxDays = 2;
	
	public void run() throws Exception {
		run(true, maxOldestLinks);
	}
	public void run(boolean newLinks, int oldLinks) throws Exception {
		System.out.println("RecentChangesUpdater.run");
		// get all links from most recent page
		// run the crawler with those links
		crawlLinks(newLinks, oldLinks);

		// any changes that "fix" what the crawling found
		new WikiSpeciesTreeFixer(speciesService).fixReplacedBy();
		
		// fix the parents (i.e. set ids for parent latin name) 
		speciesService.fixParents();
		
		// cleanup extinct (might have added children of extinct parents)
		speciesService.fixExtinct();
		 
		// clean names
		speciesService.recreateCleanNames();
		 
		// run full boring suite
		BoringWorker.main(null);
		
		// create new interesting crunched ids
		new InterestingSubspeciesWorker().run();
		
		new SiblingsWithSameCommonNamesAnalyzer().run();
		
		new LinkedImagesWorker().run();
		
		// run this now - sometimes things in the examples get boring,
		// and this will fix it
		new ExamplesCruncherWorker().run();
		
		// download the images - has to be last due to "System.exit"
		ThumbnailDownloader.main(null);
	}

	public void crawlLinks(boolean newLinks, int oldLinks) {
		Set<String> allLinks = new HashSet<String>();

		if (newLinks) {
			String url = "http://species.wikimedia.org/w/index.php?title=Special:RecentChanges&days=" +
					+ maxDays + "&limit=" + maxChanges
					+ "&namespace=0";
			System.out.println("url." + url);
			String page = WikiSpeciesCrawler.getPageForUrl(url, 100);
			
			Set<String> parsedLinks = WikiSpeciesCrawler.parseLinks(page);
			System.out.println("crawlNewLinks.found." + parsedLinks.size());
			
			allLinks.addAll(parsedLinks);
		}
		
		Collection<String> oldestLinks = parseStatusService.findOldestLinks(oldLinks);
		System.out.println("oldestLinks." + oldestLinks.size());

		allLinks.addAll(oldestLinks);
		
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.pushStoredLinks(allLinks, newLinks);
		crawler.crawl();
	}

}
