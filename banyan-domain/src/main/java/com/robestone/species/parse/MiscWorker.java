package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;

import com.robestone.species.BoringPruner;
import com.robestone.species.CompleteEntry;
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
//		run
//		testGigantopithecus
		run2
		();
	}
	
	
	public void run() throws Exception {
//		speciesService.assignParentIdsForNullOrMissingId();
		speciesService.fixParents();
//		new RecentChangesUpdater().crawlParseStatus();
//		speciesService.assignParentIdsForNullOrMissingId();
//		new WikiSpeciesTreeFixer(speciesService).run();
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
