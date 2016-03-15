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

		boolean crawlNewLinks = false;
		boolean crawlOldLinks = false;
		boolean runMaintenance = true;
		boolean crawlParseStatus = true;
		
		if (args != null && args.length > 0) {
			recent.maxOldLinks = 0;
			recent.maxChanges = 0;
			recent.maxDays = 0;
			runMaintenance = false;
			crawlParseStatus = false;
			LogHelper.speciesLogger.info("RecentChangesUpdater.args");
			// maxOldLinks=10 maxChanges=2 maxDays=1 runMaintenance=false
			for (int i = 0; i < args.length; i++) {
				LogHelper.speciesLogger.info("RecentChangesUpdater.args." + i + "." + args[i]);
				String[] p = args[i].split("=");
				if (p[0].equals("maxOldLinks")) {
					recent.maxOldLinks = Integer.parseInt(p[1]);
				}
				if (p[0].equals("maxChanges")) {
					recent.maxChanges = Integer.parseInt(p[1]);
				}
				if (p[0].equals("maxDays")) {
					recent.maxDays = Integer.parseInt(p[1]);
				}
				if (p[0].equals("runMaintenance")) {
					runMaintenance = Boolean.parseBoolean(p[1]);
				}
				if (p[0].equals("crawlParseStatus")) {
					crawlParseStatus = Boolean.parseBoolean(p[1]);
				}
			}
			crawlNewLinks = recent.maxChanges > 0;
			crawlOldLinks = recent.maxOldLinks > 0;
		}
		
		if (crawlParseStatus) {
			recent.crawlParseStatus();
			recent.crawlTreeReport();
		}
		if (crawlNewLinks) {
			recent.crawlNewLinks();
		}
		if (crawlOldLinks) {
			recent.crawlOldLinks();
		}
		if (runMaintenance) {
			recent.runMaintenance();
		}
	}
	
	private int maxOldLinks = 1000;
	private int maxChanges = 5000;
	private int maxDays = 1;
	
	public void runAll() throws Exception {
		LogHelper.speciesLogger.info("RecentChangesUpdater.runAll");
		crawlNewLinks();
		crawlOldLinks(); // TODO was doing in another job, but haven't been doing that...
		runMaintenance();
	}
	
	public void runMaintenance() throws Exception {
		LogHelper.speciesLogger.info("RecentChangesUpdater.runMaintenance");
		
		// sets AUTH so we don't try and parse those again
		new AuthWorker().setStatusForRedirect();
		
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
		new BoringWorker().runBoringPrunerWorker();
		
		// create new interesting crunched ids
		InterestingSubspeciesWorker.logger.setLevel(Level.INFO);
		new InterestingSubspeciesWorker().run();
		
		new SiblingsWithSameCommonNamesAnalyzer().run();
		
		new LinkedImagesWorker().run();
		
		// run this now - sometimes things in the examples get boring,
		// and this will fix it
		new ExamplesCruncherWorker().run();
		
		// download the images - has to be last due to "System.exit"
		new ImagesCreater().downloadAll(true);
	}
	public void crawlTreeReport() throws Exception {
		Set<String> names = new TreeReporter().getLinksToCrawl();
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(false);
		crawler.pushStoredLinks(names, false);
		crawler.crawl();
	}
	public void crawlParseStatus() throws Exception {
		// prior to running new links, also reset all broken links - the list should be getting pretty short
		new ParseDoneChanger().checkSpeciesNeedingWork(true);
		
		// now run it in cached mode so it won't take much time
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(false);
		crawler.crawlStoredLinks();
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
		crawler.setForceNewDownloadForCache(true);
		crawler.pushStoredLinks(allLinks, false);
		crawler.crawl();
	}
	
	public void crawlOldLinks() throws Exception {
		Collection<String> oldestLinks = parseStatusService.findOldestLinks(maxOldLinks);
		LogHelper.speciesLogger.info("oldestLinks." + oldestLinks.size());

		Set<String> allLinks = new HashSet<String>();
		allLinks.addAll(oldestLinks);

		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(true);
		crawler.pushStoredLinks(allLinks);
		crawler.crawl();
	}

}
