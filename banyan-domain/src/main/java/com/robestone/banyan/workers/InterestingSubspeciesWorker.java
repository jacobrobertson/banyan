package com.robestone.banyan.workers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.robestone.banyan.taxons.CrunchedIds;
import com.robestone.banyan.taxons.TaxonNode;
import com.robestone.banyan.taxons.Tree;
import com.robestone.banyan.wikispecies.EntryUtilities;

/**
 * Finds a set amount of the most interesting subspecies, and tracks those.
 * @author jacob
 */
public class InterestingSubspeciesWorker extends AbstractWorker {

	/*
	public static void main(String[] args) {
		InterestingSubspeciesWorker w = new InterestingSubspeciesWorker();
		if (args.length == 0) {
			int count = 0;
			int max = 100000;
			Tree tree = w.getWikiSpeciesService().findInterestingTreeFromPersistence();
			for (Entry e: tree.getEntries()) {
				if (e.getInterestingCrunchedIds() != null && e.getInterestingCrunchedIds().getCrunchedIds().length() > 0) {
					w.assignInterestingSubspecies(e);
					count++;
					if (count >= max) {
						break;
					}
				}
			}
		} else {
			w.run();
		}
	}
	*/
	
	public static final int MAX_INTERESTING_CHILD_COUNT = 10;
	public static final Logger logger = Logger.getLogger(InterestingSubspeciesWorker.class);

	public void run() {
		Tree<TaxonNode> tree = getTaxonService().findInterestingTreeFromPersistence();
		assignInterestingSubspecies(tree);
	}
	
	public void assignInterestingSubspecies(Tree<TaxonNode> tree) {
		logger.info("assignInterestingSubspecies > " + tree.size());
		int count = 0;
		int max = 10000;
		for (TaxonNode entry: tree.getNodesList()) {
			if (count++ % max == 0) {
				logger.info("assignInterestingSubspecies." + (count) + "." + entry.getLatinName());
			}
			assignInterestingSubspecies(entry);
		}
		logger.info("assignInterestingSubspecies < " + tree.size());
	}
	private Set<Integer> getEntryChildrenIds(TaxonNode entry) {
		Set<Integer> ids = new HashSet<Integer>();
		if (entry.getChildren() != null) {
			entry.getChildren().forEach(c -> ids.add(c.getTaxonId()));
		}
		return ids;
	}
	public void assignInterestingSubspecies(TaxonNode entry) {
		logger.debug("assignInterestingSubspecies." + entry.getLatinName());
		Set<Integer> childredIds = getEntryChildrenIds(entry);
		List<TaxonNode> entries = new ArrayList<>();
		Set<Integer> interesting = new HashSet<Integer>();
		entries.add(entry);
		int pass = 0;
		int tries = 0;
		while (!entries.isEmpty()) {
			entries = collectInterestingSubspecies(entries, interesting, pass);
			if (interesting.size() >= MAX_INTERESTING_CHILD_COUNT) {
				break;
			}
			if (pass == 2) {
				// if == 2 it means we can't do any more work
				break;
			}
			if (entries.isEmpty()) {
				entries.add(entry);
				pass++;
			}
			logger.debug("assignInterestingSubspecies.pass." + pass + "." + entry.getLatinName());
			logger.debug("assignInterestingSubspecies.entries." + entries.size());
			tries++;
			if (tries > 20) {
				logger.info("assignInterestingSubspecies.BAD." + pass + "." + entry.getLatinName());
//				throw new RuntimeException();
				// TODO - this broke at some point after the DB got corrupted, but I don't know how this works and I don't want to deal with it right now
				break;
			}
		}
		boolean assignIds = true;
		// make sure it's not just exactly the children
		if (!interesting.isEmpty() && interesting.size() == entry.getLoadedChildrenSize()) {
			if (interesting.containsAll(childredIds)) {
				assignIds = false;
			}
		}
		CrunchedIds ids = null;
		if (assignIds && !interesting.isEmpty()) {
			ids = EntryUtilities.CRUNCHER.build(interesting, null);
			logger.debug("assignInterestingSubspecies." + entry.getId() + "." + entry.getLatinName() + "." + ids);
		}
		String oldIds = null;
		if (entry.getInterestingCrunchedIds() != null) {
			oldIds = entry.getInterestingCrunchedIds().getCrunchedIds();
		}
		String newIds = null;
		if (ids != null) {
			newIds = ids.getCrunchedIds();
		}
		
		// can save a lot of performance if we check this first
		if (!StringUtils.equals(newIds, oldIds)) {
			entry.setInterestingCrunchedIds(ids);
			getTaxonService().updateInterestingCrunchedIds(entry);
		}
	}
	/**
	 * @return the next set of children to check
	 */
	private List<TaxonNode> collectInterestingSubspecies(
			List<TaxonNode> entries, Set<Integer> interesting, int pass) {
		List<TaxonNode> next = new ArrayList<>();
		for (TaxonNode entry: entries) {
			List<TaxonNode> some = collectInterestingSubspecies(entry, interesting, pass);
			if (interesting.size() >= MAX_INTERESTING_CHILD_COUNT) {
				break;
			}
			next.addAll(some);
		}
		return next;
	}
	private List<TaxonNode> collectInterestingSubspecies(
			TaxonNode entry, Set<Integer> interesting, int pass) {
		List<TaxonNode> next = new ArrayList<>();
		if (entry.hasChildren()) {
			for (TaxonNode child: entry.getChildren()) {
				if (isInteresting(child, pass)) {
					interesting.add(child.getTaxonId());
					if (interesting.size() >= MAX_INTERESTING_CHILD_COUNT) {
						break;
					}
				}
				next.add(child);
			}
		}
		return next;
	}
	private boolean isInteresting(TaxonNode entry, int pass) {
		if (pass == 0) {
			return entry.getImage().getFilePath() != null && entry.getCommonName() != null;
		} else if (pass == 1) {
			return entry.getImage().getFilePath() != null;
		} else {
			return entry.getCommonName() != null;
		}
	}
}
