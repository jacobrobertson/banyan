package com.robestone.species.parse;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.robestone.species.CompleteEntry;
import com.robestone.species.LogHelper;

/**
 * A utility to help jump start an empty DB, it will crawl all children without a parent id, and keep going, but never crawling new links, just parent latin names.
 */
public class ChildToParentCrawler extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new ChildToParentCrawler().showChildAncestry("Animalia");
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
	
	/**
	 * Find all children with no parent Id and try and crawl all those.
	 * @throws Exception 
	 */
	public void crawlAllParents() throws Exception {
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(true);
		
//		Collection<String> names = speciesService.findAllUnmatchedParentNames();
		Collection<CompleteEntry> names = speciesService.findEntriesWithInvalidParent();
		
		LogHelper.speciesLogger.info("crawlAllParents.found." + names.size());

		int count = 0;
		for (CompleteEntry name : names) {
			count++;
			if (name.getLatinName().toLowerCase().contains("vir")) {
				continue;
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
