package com.robestone.species.parse;

import java.util.Collection;
import java.util.List;

import com.robestone.species.CompleteEntry;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LogHelper;
import com.robestone.species.Tree;

public class TreeReporter extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new TreeReporter().
//		runTreeReport
		loopReport
		();
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

	public void loopReport() throws Exception {
		LogHelper.speciesLogger.debug("loopReport>");
		Collection<CompleteEntry> entries = speciesService.findEntriesForTreeReport();
		EntryUtilities.buildTree(entries);
		LogHelper.speciesLogger.debug("loopReport>" + entries.size());
		int count = 0;
		for (CompleteEntry e: entries) {
			CompleteEntry root = EntryUtilities.getRoot(e);
			if (root == null) {
				System.out.println("\"" + e.getLatinName() + "\", // " + count++);
			}
		}
	}

}