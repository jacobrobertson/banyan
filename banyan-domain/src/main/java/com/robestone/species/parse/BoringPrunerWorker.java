package com.robestone.species.parse;

import com.robestone.species.BoringPruner;
import com.robestone.species.Entry;
import com.robestone.species.Tree;

public class BoringPrunerWorker extends AbstractWorker {

	public static void main(String[] args) {
		boolean persist = false;
		new BoringPrunerWorker().run(persist);
	}
	public void run(boolean persist) {
		Tree tree = speciesService.findCompleteTreeFromPersistence();
		BoringPruner pruner = new BoringPruner();
		
//		pruner.logger.setLevel(Level.DEBUG);
		
		pruner.prune(tree);

//		printTree(tree.getRoot(), 0);
		
		if (persist) {
			speciesService.updateFromBoringWork(pruner.getInteresting(), pruner.getBoring());
		}
		
	}
	
	public static void printTree(Entry root, int depth) {
		
		System.out.print("                                                                    ".substring(0, depth));
		System.out.println(root.getId() + "." + root.getLatinName() + " / " + root.getCommonName());
		
		if (root.getChildren() == null) {
			return;
		}
		for (Entry child: root.getChildren()) {
			printTree(child, depth + 1);
		}
	}
	
}
