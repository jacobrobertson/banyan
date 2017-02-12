package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.robestone.species.BoringPruner;
import com.robestone.species.CompleteEntry;
import com.robestone.species.DerbyDataSource;
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
		DerbyDataSource.dbPath = "D:\\banyan-db\\derby";
		new MiscWorker().
		research
//		runM
//		run
//		testGigantopithecus
//		run2
		();
	}
	
	
	public void run() throws Exception {
//		speciesService.assignParentIdsForNullOrMissingId();
		speciesService.fixParents();
//		speciesService.recreateCleanNames();
//		new RecentChangesUpdater().crawlParseStatus();
//		speciesService.assignParentIdsForNullOrMissingId();
//		new WikiSpeciesTreeFixer(speciesService).run();
	}
	public void runM() throws Exception {
		RecentChangesUpdater rcu = new RecentChangesUpdater();
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
		CompleteEntry entry = speciesService.findEntryByLatinName(name, true);
		while (entry != null && entry.getParentId() != null) {
			CompleteEntry parent = speciesService.findEntryById(entry.getParentId(), true);
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
				entry = (CompleteEntry) entry.getChildren().get(0);
			} else {
				entry = null;
			}
		}
		
	}
	
	public void run2() {
		
		new WikiSpeciesTreeFixer(speciesService).run();
		
//		speciesService.assignParentIdsByParentLatinName();
		
		Tree tree = speciesService.findCompleteTreeFromPersistence();
		
		Collection<CompleteEntry> all = tree.getEntriesMap().values();
		for (CompleteEntry e: all) {
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
		CompleteEntry e = speciesService.findEntryByLatinName("Gigantopithecus", true);
		CompleteEntry p = speciesService.findEntryByLatinName(e.getParentLatinName());
		e.setParentId(p.getId());
		Collection<CompleteEntry> entries = new ArrayList<CompleteEntry>();
		entries.add(e);
		entries.add(p);
		Tree tree = EntryUtilities.buildTree(entries);
		BoringPruner pruner = new BoringPruner();
		pruner.prune(tree);
	}
}
