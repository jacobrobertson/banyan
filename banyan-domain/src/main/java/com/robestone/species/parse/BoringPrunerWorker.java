package com.robestone.species.parse;

import com.robestone.species.BoringPruner;
import com.robestone.species.Tree;

public class BoringPrunerWorker extends AbstractWorker {

	public static void main(String[] args) {
		new BoringPrunerWorker().run();
	}
	public void run() {
		Tree tree = speciesService.findCompleteTreeFromPersistence();
		BoringPruner pruner = new BoringPruner();
		pruner.prune(tree);
	
		speciesService.updateFromBoringWork(pruner.getInteresting(), pruner.getBoring());
	}
	
}
