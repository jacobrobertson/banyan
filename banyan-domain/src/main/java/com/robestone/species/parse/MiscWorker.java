package com.robestone.species.parse;

import com.robestone.species.BoringPruner;
import com.robestone.species.Tree;



/**
 * Just so I can run whatever service commands I like...
 * 
 * @author jacob
 */
public class MiscWorker extends AbstractWorker {

	public static void main(String[] args) {
		new MiscWorker().run();
	}
	
	
	public void run() {
		speciesService.assignParentIdsForNullOrMissingId();
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
	
}
