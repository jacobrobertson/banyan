package com.robestone.banyan.workers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.banyan.taxons.TaxonService;
import com.robestone.banyan.taxons.Tree;
import com.robestone.banyan.util.LogHelper;
import com.robestone.banyan.wikispecies.Entry;
import com.robestone.banyan.wikispecies.EntryUtilities;
import com.robestone.banyan.wikispecies.WikiSpeciesCache;
import com.robestone.banyan.wikispecies.WikiSpeciesCrawler;

public class TreeReporter extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new TreeReporter().
//		runTreeReport(1, 1, 100);
		loopReport();
//		getLinksToCrawl();
//		new RecentChangesUpdater().crawlTreeReport();
	}

	public Set<String> getLinksToCrawl() {
		int maxResults = 5000;
		List<Tree<Entry>> trees = runTreeReport(1, 10, maxResults);
		Set<String> allLinks = new HashSet<String>();
		LogHelper.speciesLogger.debug("getLinksToCrawl.trees." + trees.size());
		int count = 0;
		for (Tree<Entry> t: trees) {
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
		int minInteresting = 2;
		int minTreeNodes = 20;
		runTreeReport(minInteresting, minTreeNodes, -1);
	}
	public List<Tree<Entry>> runTreeReport(int minInteresting, int minTreeNodes, int maxResults) {
		LogHelper.speciesLogger.debug("runTreeReport.findAllEntriesForTreeReport");
		Collection<Entry> entries = getWikiSpeciesService().findEntriesForTreeReport();
		LogHelper.speciesLogger.debug("runTreeReport.findAllEntriesForTreeReport." + entries.size());
		
		for (Entry e: entries) {
			if (e.getInterestingParentId() != null) {
				e.setParentId(e.getInterestingParentId());
			}
		}
		
		LogHelper.speciesLogger.debug("runTreeReport.buildTree");
		Tree<Entry> tree = EntryUtilities.buildTree(entries);
		LogHelper.speciesLogger.debug("runTreeReport.buildTree." + tree.size());
		LogHelper.speciesLogger.debug("runTreeReport.findDisconnectedTrees");
		List<Tree<Entry>> trees = EntryUtilities.findDisconnectedTrees(tree);
		Collections.sort(trees, new TreeComp());
		List<Tree<Entry>> filteredTrees = new ArrayList<>();
		for (Tree<Entry> t: trees) {
			int countInteresting = countInteresting(t);
			if (countInteresting < minInteresting && t.size() < minTreeNodes) {
				continue;
			}
			Entry root = t.getRoot();
			System.out.println(root.getLatinName() + "." + t.size() + "/" + countInteresting);
			if (TaxonService.TREE_OF_LIFE_ID.equals(t.getRoot().getId())) {
				continue;
			}
			filteredTrees.add(t);
			if (maxResults > 0 && filteredTrees.size() > maxResults) {
				break;
			}
		}
		return filteredTrees;
	}
	private class TreeComp implements Comparator<Tree<Entry>> {
		@Override
		public int compare(Tree<Entry> o1, Tree<Entry> o2) {
			int comp = countInteresting(o2) - countInteresting(o1);
			if (comp != 0) {
				return comp;
			}
			return o2.size() - o1.size();
		}
	}
	private int countInteresting(Tree<Entry> tree) {
		int count = 0;
		for (Entry e: tree.getNodesList()) {
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
		Collection<Entry> entries = getWikiSpeciesService().findEntriesForTreeReport();
		EntryUtilities.buildTree(entries);
		LogHelper.speciesLogger.debug("loopReport>" + entries.size());
		int count = 0;
		for (Entry e: entries) {
			Entry root = EntryUtilities.getRoot(e);
			if (root == null) {
				System.out.println("\"" + e.getLatinName() + "\", // " + count++);
			}
		}
	}

}
