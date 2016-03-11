package com.robestone.species.parse;

import java.util.Collection;
import java.util.List;

import com.robestone.species.BoringPruner;
import com.robestone.species.CompleteEntry;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LogHelper;
import com.robestone.species.Tree;



/**
 * Just so I can run whatever service commands I like...
 * 
 * @author jacob
 */
public class MiscWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new MiscWorker().
		runTreeReport
//		run
		();
	}
	
	
	public void run() throws Exception {
		new RecentChangesUpdater().crawlParseStatus();
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
	
	public void runTreeReport() {
		LogHelper.speciesLogger.debug("runTreeReport.findAllEntriesForTreeReport");
		Collection<CompleteEntry> entries = speciesService.findEntriesForTreeReport();
		LogHelper.speciesLogger.debug("runTreeReport.findAllEntriesForTreeReport." + entries.size());
		
		for (CompleteEntry e: entries) {
			if (e.getInterestingParentId() != null) {
				e.setParentId(e.getInterestingParentId());
			}
		}
		
		LogHelper.speciesLogger.debug("runTreeReport.buildTree");
		Tree tree = EntryUtilities.buildTree(entries);
		LogHelper.speciesLogger.debug("runTreeReport.buildTree." + tree.size());
		LogHelper.speciesLogger.debug("runTreeReport.findDisconnectedTrees");
		List<Tree> trees = EntryUtilities.findDisconnectedTrees(tree);
		int minInteresting = 10;
		for (Tree t: trees) {
			int countInteresting = countInteresting(t);
			if (countInteresting < minInteresting) {
				continue;
			}
			CompleteEntry root = t.getRoot();
			System.out.println(root.getLatinName() + "." + t.size() + "/" + countInteresting);
		}
	}
	private int countInteresting(Tree tree) {
		int count = 0;
		for (CompleteEntry e: tree.getEntries()) {
			if (isInteresting(e)) {
				count++;
			}
		}
		return count;
	}
	private boolean isInteresting(Entry entry) {
		return entry.getImageLink() != null || entry.getCommonName() != null;
	}

}
