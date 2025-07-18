package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.robestone.species.BoringPruner;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Tree;
import com.robestone.species.WikiSpeciesTreeFixer;



/**
 * Just so I can run whatever service commands I like...
 * 
 * @author jacob
 */
public class MiscWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new MiscWorker().
//		crawlEntriesWithCommonNameAndNoImage();
//		research();
//		runM
//		run();
//		testGigantopithecus
//		run2
		speciesService.fixParents();
	}
	
	
	public void run() throws Exception {
		speciesService.fixParents();
//		speciesService.recreateCleanNames();
//		new RecentChangesUpdater().crawlParseStatus();
//		speciesService.assignParentIdsForNullOrMissingId();
//		new WikiSpeciesTreeFixer(speciesService).run();
	}
	public void runM() throws Exception {
		MaintenanceWorker rcu = new MaintenanceWorker();
		rcu.setDownloadImages(false);
		rcu.setRecreateCleanNames(false);
		rcu.runMaintenance();
	}
	public void research() {
		String[] names = {"Rajomorphii", "Myliobatidae", "Pristis zijsron"};
		for (String name: names) {
			showParents(name);
		}
	}
	public void showParents(String name) {
		Set<Integer> ids = new HashSet<Integer>();
		String tab = "";
		Entry entry = speciesService.findEntryByLatinName(name, true);
		while (entry != null && entry.getParentId() != null) {
			Entry parent = speciesService.findEntryById(entry.getParentId(), true);
			System.out.println(tab + entry.getId() + ", " + entry.getLatinName() + ", " + entry.getCommonName() + 
					" > " + entry.getParentId() + "(" + entry.getParentLatinName() + ") > " + parent.getLatinName());
			ids.add(entry.getId());
			entry = parent;
			/*
			String to = speciesService.findRedirectTo(parent.getLatinName());
			if (to != null) {
				parent = speciesService.findEntryByLatinName(entry.getParentLatinName(), true);
				entry = speciesService.findEntryByLatinName(to, true);
				System.out.println(tab + "TO - " + to + "(" + entry.getId() + ")" + "/" + parent.getLatinName() + "(" + parent.getId() + ")");
			} else if (entry.getParentLatinName() != null) {
				entry = speciesService.findEntryByLatinName(entry.getParentLatinName(), true);
			} else {
				entry = null;
			}
			*/
			tab = tab + "   ";
		}
		System.out.println("--------------");
		entry = speciesService.findTreeForNodes(ids);
		while (entry != null) {
			System.out.println(
					entry.getId() + ", " + entry.getLatinName() + ", " + entry.getCommonName()
					+ ", " + entry.isBoring() + ", " + entry.getParentId() + "/" + entry.getInterestingParentId()
					);
			if (entry.getChildren() != null && !entry.getChildren().isEmpty()) {
				entry = (Entry) entry.getChildren().get(0);
			} else {
				entry = null;
			}
		}
		
	}
	
	public void run2() throws Exception {
		
		new WikiSpeciesTreeFixer(speciesService).run();
		
//		speciesService.assignParentIdsByParentLatinName();
		
		Tree tree = speciesService.findCompleteTreeFromPersistence();
		
		Collection<Entry> all = tree.getEntriesMap().values();
		for (Entry e: all) {
			print("all.", e);
		}
		//*
		BoringPruner pruner = new BoringPruner();
		
//		pruner.logger.setLevel(Level.DEBUG);
		
		pruner.prune(tree);

//		speciesService.updateFromBoringWorkMarkInteresting();
		for (Entry e: pruner.getInteresting()) {
			print("interesting.", e);
		}
		for (Entry e: pruner.getBoring()) {
			print("boring.", e);
		}
		
//		printTree(tree.getRoot(), 0);
		
//		speciesService.updateFromBoringWorkMarkBoring(pruner.getBoring());
		//*/
	}
	private static void print(String reason, Entry e) {
		System.out.println(reason + e.getId() + "." + e.getLatinName() + " > " + e.getParentId());
	}
	public void testGigantopithecus() {
		Entry e = speciesService.findEntryByLatinName("Gigantopithecus", true);
		Entry p = speciesService.findEntryByLatinName(e.getParentLatinName());
		e.setParentId(p.getId());
		Collection<Entry> entries = new ArrayList<Entry>();
		entries.add(e);
		entries.add(p);
		Tree tree = EntryUtilities.buildTree(entries);
		BoringPruner pruner = new BoringPruner();
		pruner.prune(tree);
	}
	
	public void crawlEntriesWithCommonNameAndNoImage() throws Exception {
		Collection<Entry> entries = speciesService.findEntriesWithCommonNameAndNoImage();
		System.out.println("crawlEntriesWithCommonNameAndNoImage.willCrawl." + entries.size());
		Set<String> names = new HashSet<String>();
		for (Entry entry: entries) {
			String common = entry.getCommonName().trim();
			if (common.length() > 0) {
//				System.out.println("crawlEntriesWithCommonNameAndNoImage.willCrawl." + common + " / " + entry.getLatinName());
				names.add(entry.getLatinName());
			}
		}
		entries = null;
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(true);
		
		crawler.pushOnlyTheseNames(names);
		crawler.crawl();

	}
	
}
