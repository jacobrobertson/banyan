package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;

import com.robestone.species.BoringPruner;
import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Tree;



/**
 * Just so I can run whatever service commands I like...
 * 
 * @author jacob
 */
public class MiscWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new MiscWorker().
//		run
		testGigantopithecus
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
		Tree tree = speciesService.findCompleteTreeFromPersistence();
		BoringPruner pruner = new BoringPruner();
		
//		pruner.logger.setLevel(Level.DEBUG);
		
		pruner.prune(tree);

//		printTree(tree.getRoot(), 0);
		
		speciesService.updateFromBoringWorkMarkBoring(pruner.getBoring());
		
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
