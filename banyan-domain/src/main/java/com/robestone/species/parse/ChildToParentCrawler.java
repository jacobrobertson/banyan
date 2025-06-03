package com.robestone.species.parse;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.robestone.species.CompleteEntry;
import com.robestone.species.LogHelper;

/**
 * A utility to help jump start an empty DB, it will crawl all children without a parent id, and keep going, but never crawling new links, just parent latin names.
 */
public class ChildToParentCrawler extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new ChildToParentCrawler().
//		fixChildrenWithWrongParentId();
//		crawlAllParents();
//		removeBrokenLinksToParentId();
		showChildAncestry("Amphibia");
	}
	
	public void showChildAncestry(String child) {
		Set<Integer> ids = new HashSet<Integer>();
		doShowChildAncestry(child, ids);
		CompleteEntry tree = speciesService.findTreeForNodes(ids);
		BoringPrunerWorker.printTree(tree, 0);
	}
	public void doShowChildAncestry(String child, Set<Integer> ids) {
		CompleteEntry e = speciesService.findEntryByLatinName(child, true);
		if (e.getId() != null) {
			ids.add(e.getId());
			LogHelper.speciesLogger.info(
					"showChildAncestry." + child + "." + e.getId() + 
					" >> " + e.getParentLatinName() + "." + e.getParentId() + "." + e.getInterestingParentId());
			if (e.getParentLatinName() != null) {
				doShowChildAncestry(e.getParentLatinName(), ids);
			}
		}
	}
	
	public void removeBrokenLinksToParentId() throws Exception {
		Collection<CompleteEntry> all = speciesService.findEntriesForTreeReport();
		Map<Integer, CompleteEntry> map = new HashMap<Integer, CompleteEntry>();
		all.forEach(e -> map.put(e.getId(), e));
		all.forEach(e -> {
			if (e.getParentId() != null) {
				CompleteEntry p = map.get(e.getParentId());
				if (p == null) {
					LogHelper.speciesLogger.info("removeBrokenLinksToParentId." + e.getLatinName() + " > " + e.getParentLatinName());
					speciesService.updateParentIdToNull(e);
				}
			}
		});
	}
	
	/**
	 * This is caused by a corrupted DB - should not need to be called ever.
	 */
	public void fixChildrenWithWrongParentId() throws Exception {
		Collection<CompleteEntry> allEntries = speciesService.findEntriesWithBasicParentInfo();
		System.out.println("fixChildrenWithWrongParentId." + allEntries.size());
		
		Map<String, CompleteEntry> mapByLatinName = new HashMap<String, CompleteEntry>();
		allEntries.forEach(e -> mapByLatinName.put(e.getLatinName(), e));
		
		for (CompleteEntry child: allEntries) {
			if (child.getParentLatinName() == null) {
				// this isn't the problem we're solving here
				continue;
			}
			CompleteEntry parent = mapByLatinName.get(child.getParentLatinName());
			if (parent == null) {
				// this isn't the problem we're solving here
				continue;
			}
//			System.out.println("fixChildrenWithWrongParentId." + child.getLatinName() + "." + child.getId());
			if (!parent.getId().equals(child.getParentId())) {
				System.out.printf(
						"fixChildrenWithWrongParentId.child=%s(%s)/%s(%s) vs parent=%s(%s)\n",
						child.getLatinName(), child.getId(),
						child.getParentLatinName(), child.getParentId(),
						parent.getLatinName(), parent.getId()
						);
				child.setParentId(parent.getId());
				speciesService.updateParentId(child);
			}
		}
	}
	
	
	/**
	 * Find all children with no parent Id and try and crawl all those.
	 * @throws Exception 
	 */
	public void crawlAllParents() throws Exception {
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		// crawler.setForceNewDownloadForCache(true);
		
//		Collection<String> names = speciesService.findAllUnmatchedParentNames();
		Collection<CompleteEntry> names = speciesService.findEntriesWithInvalidParent();
		
		LogHelper.speciesLogger.info("crawlAllParents.found." + names.size());

		int count = 0;
		for (CompleteEntry name : names) {
			count++;
			if (name.getLatinName().toLowerCase().contains("vir")) {
//				continue;
			}
			ParseStatus ps = new ParseStatus();
			ps.setUrl(name.getLatinName());
			CompleteEntry results = crawler.crawlOne(ps, false);
			if (results != null && results.getId() != null) {
				LogHelper.speciesLogger.info("crawlAllParents." + count + "." + ps.getLatinName() + "." + results.getId());
			} else {
				LogHelper.speciesLogger.info("crawlAllParents." + count + "." + ps.getLatinName());
			}
		}
	}
	
}
