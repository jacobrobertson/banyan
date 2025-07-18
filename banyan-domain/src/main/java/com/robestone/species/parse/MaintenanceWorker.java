package com.robestone.species.parse;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;

import com.robestone.species.Entry;
import com.robestone.species.LogHelper;
import com.robestone.species.WikiSpeciesTreeFixer;
import com.robestone.species.js.JsonWorker;
import com.robestone.species.js.SearchIndexBuilder;

/**
 * This is the main class to run to get new entries into the database.
 * Also includes many utilies and options useful for troubleshooting and repairing issues.
 * 
 * @author jacob
 */
public class MaintenanceWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		MaintenanceWorker recent = new MaintenanceWorker();

		recent.maxRecentChangesLinks = 300;
		recent.maxOldLinks = 10000;
		
		boolean doEverything = !true; // when true - overrides all below
		
		boolean recreateCleanNames = false; // this is a long-running worker, and needed only after big updates
		boolean crawlNewLinks = !true;
		boolean crawlOldLinks = !true;
		boolean runMaintenance = true;
		boolean downloadImages = true;
		boolean runMaintenanceOnly = !true;
		boolean runPeriodicMaintenance = !true;
		boolean runJs = true;
		
		// this should be true for nightly/weekly refreshes
		// this should be false when you have already built the clean DB
		boolean crawlParseStatus = !true;
		
		recent.downloadImages = downloadImages || doEverything;
		recent.recreateCleanNames = recreateCleanNames || doEverything;
		
		if (!runMaintenanceOnly || doEverything) {
			if (crawlParseStatus || doEverything) {
				LogHelper.speciesLogger.info("RecentChangesUpdater.main.crawlParseStatus");
				recent.crawlParseStatus();
				recent.crawlTreeReport();
			}
			if (crawlNewLinks || doEverything) {
				LogHelper.speciesLogger.info("RecentChangesUpdater.main.crawlNewLinks");
				recent.crawlNewLinks();
			}
			if (crawlOldLinks || doEverything) {
				LogHelper.speciesLogger.info("RecentChangesUpdater.main.crawlOldLinks");
				recent.crawlOldLinks();
			}
		}
		if (runMaintenance || runMaintenanceOnly || doEverything) {
			LogHelper.speciesLogger.info("RecentChangesUpdater.main.runMaintenance");
			recent.runMaintenance();
			recent.runReports();
		}
		if (runPeriodicMaintenance) {
			new RedirectWorker().recrawlRedirects();
		}
		if (runJs || doEverything) {
			// JS will break if not all images are downloaded, and this is a pretty fast check if they're already done
			new ImagesWorker().downloadAll(true, false);
			new JsonWorker().runMaintenance();
			new SearchIndexBuilder().run();
		}
	}
	
	private int maxOldLinks = 1000;
	private int maxRecentChangesLinks = 5000;
	private int maxDays = 1;
	private boolean downloadImages;
	private boolean recreateCleanNames;
	
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
		
		new VirusWorker().makeVirusesInteresting();
		
		// any changes that "fix" what the crawling found
		new WikiSpeciesTreeFixer(speciesService).run();
		
		// fix the parents (i.e. set ids for parent latin name) 
		speciesService.fixParents();
		
		// cleanup extinct (might have added children of extinct parents)
		speciesService.fixExtinct();
		 
		// clean names
		// TODO - I don't know what purpose this accomplishes unless I've updated the naming logic, but maybe there's another reason for this
		// I added this back in, because quite a few things are missing the clean names, and I don't know why.  Safest to leave it
		if (recreateCleanNames) {
			speciesService.recreateCleanNames();
		}

		new ImagesWorker().runMaintenance();
		
		// run full boring suite
		new BoringPrunerWorker().run(true, true);
		
		// create new interesting crunched ids
		InterestingSubspeciesWorker.logger.setLevel(Level.INFO);
		new InterestingSubspeciesWorker().run();
		
		new SiblingsWithSameCommonNamesAnalyzer().run();
		
		// we run this one after the boring work, because it will give a false positive
		// TODO I'm skipping this for now, because it's not giving me good results
//		new CommonNameFromDescendentsWorker().run();
		
		new LinkedImagesWorker().run();
		
		// run this now - sometimes things in the examples get boring,
		// and this will fix it
		new ExamplesCruncherWorker().run();
		
		// download the images - has to be last due to "System.exit"
		// - I don't think it does that anymore... but doing last is fine?
		if (downloadImages) {
			new ImagesWorker().downloadAll(true, false);
		}
	}
	public void runReports() throws Exception {
		new TreeReporter().runTreeReport();
	}
	public void crawlTreeReport() throws Exception {
		crawlTreeReport(false);
	}
	public void crawlTreeReport(boolean repeat) throws Exception {
		TreeReporter reporter = new TreeReporter();
		Set<String> names = reporter.getLinksToCrawl();
		
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(false);
		
		while (!names.isEmpty()) {
			crawler.pushOnlyTheseNames(names);
			crawler.crawl(false);
			if (repeat) {
				runMaintenance();  // not sure if this is mandatory
				names = reporter.getLinksToCrawl();
			} else {
				break;
			}
		}
		
	}
	public void crawlParseStatus() throws Exception {
		// prior to running new links, also reset all broken links - the list should be getting pretty short
		new ParseDoneChanger().checkSpeciesNeedingWork(true);
		
		// now run it in cached mode so it won't take much time
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(false);
		crawler.pushAllFoundLinks();
		crawler.crawl();
	}
	/**
	 * THe intention is to take whatever tree we have in the DB, and focus on improving that.
	 */
	public void crawlAndMaintainTreeReport() throws Exception {
		
	}
	
	
	public void crawlNewLinks() throws Exception {
		Set<String> allLinks = new HashSet<String>();

		String url = "https://species.wikimedia.org/w/index.php?title=Special:RecentChanges&hidebots=0&days=" +
				+ maxDays + "&limit=" + maxRecentChangesLinks
				+ "&namespace=0";
		LogHelper.speciesLogger.info("url." + url);
		String page = WikiSpeciesCache.getPageForUrl(url, 100);
		
		Set<String> parsedLinks = WikiSpeciesCrawler.parseLinks(page);
		LogHelper.speciesLogger.info("crawlNewLinks.found." + parsedLinks.size());
		
		allLinks.addAll(parsedLinks);
			
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(true);
		crawler.pushOnlyTheseNames(allLinks);
		crawler.crawl();
	}
	
	public void crawlOldLinks() throws Exception {
		Collection<String> oldestLinks = parseStatusService.findOldestLinks(maxOldLinks);
		LogHelper.speciesLogger.info("oldestLinks." + oldestLinks.size());

		Set<String> allLinks = new HashSet<String>();
		allLinks.addAll(oldestLinks);

		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(true);
		
		// we want to do these regularly to ensure that the crawler is working as expected.
		// now is an okay time to do it
		addLatinNamesWithCommonNameAndNoImage(allLinks);
		
		crawler.pushOnlyTheseNames(allLinks);
		crawler.crawl();
	}
	private void addLatinNamesWithCommonNameAndNoImage(Set<String> names) throws Exception {
		Collection<Entry> entries = speciesService.findEntriesWithCommonNameAndNoImage();
		System.out.println("pushEntriesWithCommonNameAndNoImage.willCrawl." + entries.size());
		for (Entry entry: entries) {
			String common = entry.getCommonName().trim();
			if (common.length() > 0) {
				names.add(entry.getLatinName());
			}
		}
	}
	
	public void setDownloadImages(boolean downloadImages) {
		this.downloadImages = downloadImages;
	}
	public void setRecreateCleanNames(boolean recreateCleanNames) {
		this.recreateCleanNames = recreateCleanNames;
	}

}
