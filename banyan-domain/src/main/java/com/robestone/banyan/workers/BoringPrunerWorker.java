package com.robestone.banyan.workers;

import com.robestone.banyan.taxons.TaxonNode;
import com.robestone.banyan.taxons.Tree;

public class BoringPrunerWorker extends AbstractWorker {

	public static void main(String[] args) {
		boolean persist = true;
		new BoringPrunerWorker().run(false, persist);
	}
	public void run(boolean fixCommonNames, boolean persist) {
		if (fixCommonNames) {
			getTaxonService().fixBoringCommonNames();
		}
		
		Tree<TaxonNode> tree = getTaxonService().findCompleteTreeFromPersistence();
		printTree(tree.getRoot(), 0);
		System.out.println("----------------------------------------------------------------------");
		
		BoringPruner pruner = new BoringPruner();
		
//		pruner.logger.setLevel(Level.DEBUG);
		
		pruner.prune(tree);

		printTree(tree.getRoot(), 0);
		
		if (persist) {
			getTaxonService().updateFromBoringWork(pruner.getInteresting(), pruner.getBoring());
		}
		
	}
	
	public static void printTree(TaxonNode root, int depth) {
		
		System.out.print("                                                                                                                                        "
				.substring(0, depth));
		System.out.println(root.getId() + "." + root.getLatinName() + " / " + root.getCommonName());
		
		if (root.getChildren() == null) {
			return;
		}
		for (TaxonNode child: root.getChildren()) {
			printTree(child, depth + 1);
		}
	}
	
}
