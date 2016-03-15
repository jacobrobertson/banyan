package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.CompleteEntry;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LogHelper;
import com.robestone.species.SpeciesService;
import com.robestone.species.Tree;

public class TreeReporter extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new TreeReporter().
//		runTreeReport
		loopReport
//		getLinksToCrawl
		();
//		new RecentChangesUpdater().crawlTreeReport();
	}

	public Set<String> getLinksToCrawl() {
		int maxResults = 5000;
		List<Tree> trees = runTreeReport(1, maxResults);
		Set<String> allLinks = new HashSet<String>();
		LogHelper.speciesLogger.debug("getLinksToCrawl.trees." + trees.size());
		int count = 0;
		for (Tree t: trees) {
			String name = t.getRoot().getLatinName();
			LogHelper.speciesLogger.debug("getLinksToCrawl." + (count++) + "." + name);
			try {
				String page = WikiSpeciesCache.CACHE.readFile(name, true);
				Set<String> links = WikiSpeciesCrawler.parseLinks(page);
				allLinks.addAll(links);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		LogHelper.speciesLogger.debug("getLinksToCrawl." + allLinks.size());
		return allLinks;
	}
	
	public void runTreeReport() {
		int minInteresting = 3;
		runTreeReport(minInteresting, -1);
	}
	public List<Tree> runTreeReport(int minInteresting, int maxResults) {
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
		Collections.sort(trees, new TreeComp());
		List<Tree> filteredTrees = new ArrayList<Tree>();
		for (Tree t: trees) {
			if (SpeciesService.TREE_OF_LIFE_ID.equals(t.getRoot().getId())) {
				continue;
			}
			int countInteresting = countInteresting(t);
			if (countInteresting < minInteresting) {
				continue;
			}
			CompleteEntry root = t.getRoot();
			System.out.println(root.getLatinName() + "." + t.size() + "/" + countInteresting);
			filteredTrees.add(t);
			if (maxResults > 0 && filteredTrees.size() > maxResults) {
				break;
			}
		}
		return filteredTrees;
	}
	private class TreeComp implements Comparator<Tree> {
		@Override
		public int compare(Tree o1, Tree o2) {
			int comp = countInteresting(o2) - countInteresting(o1);
			if (comp != 0) {
				return comp;
			}
			return o2.size() - o1.size();
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
