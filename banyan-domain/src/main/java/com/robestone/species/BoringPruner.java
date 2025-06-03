package com.robestone.species;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.robestone.species.parse.ImagesCreater;

/**
 * Create a new pruner each time.
 * 
 * This class isolates code for pruning an in-memory tree.
 * 
 * After calling "prune" on a tree, there's still quite a bit of work to do.
 * - look over all detached entries, and update them in the db to be boring, and have no interesting parent id
 * - for the rest of the tree, each entry needs to be updated to ensure the correct 
 * 	-- interesting parent id
 *  -- interesting child count
 *  -- boring flag (of 0)
 * 
 * @author Jacob Robertson
 */
public class BoringPruner {

	public Logger logger = LogHelper.speciesLogger;
	
	private Set<CompleteEntry> detached = new HashSet<CompleteEntry>();
	private Set<CompleteEntry> entries = new HashSet<CompleteEntry>();
	private Tree tree;

	public Set<CompleteEntry> getBoring() {
		return detached;
	}
	public Set<CompleteEntry> getInteresting() {
		return entries;
	}
	
	/**
	 * Does not hit the database - after calling this method, the tree will need to
	 * be persisted.
	 */
	public void prune(Tree tree) {
		this.tree = tree;
		this.entries = EntryUtilities.getEntries(tree.getRoot());
		
		// some analysis if needed
		List<CompleteEntry> list = new ArrayList<CompleteEntry>(entries);
		Collections.sort(list, new EntryComparator());
//		for (CompleteEntry e : list) {
//			logger.debug("pruneBoringLeaves.boring." + e.getId() + "." + e.getLatinName() + " / " + e.getCommonName());
//			if (e.getLatinNameClean().startsWith("TESTUDO")) {		
//				logger.debug("pruneBoringLeaves.boring." + e.getId() + "." + e.getLatinName() + " / " + e.getCommonName());
//			}
//		}
		prepareEntries();
		prune();
		setAttributes();
	}

	private void setAttributes() {
		// interesting count
		// interesting parent id
		// "boring" - no way to set this attribute - it's not in the domain model
	}
	
	/**
	 * Perform any pre-processing prior to actual pruning.
	 */
	private void prepareEntries() {
		EntryUtilities.cleanEntries(entries);
		promoteCleanNames();
		removeBoringCommonNames();
		normalizeImageNames();
	}
	
	/**
	 * For convenience, move clean names to regular names.
	 */
	private void promoteCleanNames() {
		for (CompleteEntry e: entries) {
			e.setCommonName(e.getCommonNameClean());
			e.setLatinName(e.getLatinNameClean());
		}
	}
	
	private void prune() {
		logger.info("prune >");
		int pass = 0;
		int totalChanges = 0;
		while (true) {
			int passChanges = 0;
			while (true) {
				int changes = pruneBoringLeaves();
				if (changes == 0) {
					break;
				}
				passChanges += changes;
			}
			while (true) {
				int changes = pruneBoringParents();
				if (changes == 0) {
					break;
				}
				passChanges += changes;
			}
			
			pass++;
			logger.info("prune.pass." + pass + " - changes." + passChanges);
			if (passChanges == 0) {
				break;
			}
			totalChanges += passChanges;
		}
		logger.info("prune.totalChanges." + totalChanges);
	}
	
	/**
	 * When pruning is done, the common name will not be updated, so what I'm doing here is
	 * temporarily saying "there's a common name here, but I've decided it's boring anyways".
	 * 
	 * @param tree
	 */
	private void removeBoringCommonNames() {
		logger.debug("removeBoringCommonNames > " + entries.size());
		int count = 0;
		for (CompleteEntry e: entries) {
			if (e.getCommonName() != null) {
//				logger.debug("removeBoringCommonNames ? " + e.getId());
				if (isCommonNameBoring(e)) {
					logger.debug("removeBoringCommonNames." + e.getCommonName() + " (" + e.getLatinName() + ")");
					e.setCommonName(null);
					count++;
				}
			}
		}
		logger.debug("removeBoringCommonNames < " + count);
	}
	private boolean isCommonNameBoring(CompleteEntry e) {
		if (CommonNameSimilarityChecker.isCommonNameCleanBoring(e.getCommonName(), e.getLatinName())) {
			return true;
		}
		return false;
	}

	/**
	 * There are many cases of images that have different urls, but go back to the same root 
	 * wikimedia image file.
	 * 
	 * Since the image name won't be persisted back to the db, there's no harm at all in "fixing"
	 * these here.
	 * 
	 * (Full disclosure: I'm no longer sure this ever is an issue, but there is great potential
	 * 	for this to come up)
	 */
	private void normalizeImageNames() {
		logger.debug("normalizeImageNames > " + entries.size());
		for (CompleteEntry e: entries) {
			if (e.getImageLink() != null) {
				String name = ImagesCreater.parseFileName(e.getImageLink());
				if (!name.equals(e.getImageLink())) {
					e.setImageLink(name);
				}
			}
		}
		logger.debug("normalizeImageNames < " + entries.size());
	}
	
	private int pruneBoringLeaves() {
		int changed = 0;
		Collection<CompleteEntry> leaves = EntryUtilities.getLeaves(tree.getRoot());
		logger.debug("pruneBoringLeaves > " + leaves.size());
		for (CompleteEntry e: leaves) {
			boolean boring = isLeafBoring(e);
//			if (e.getLatinNameClean().startsWith("TESTUDO")) {
//				logger.debug("pruneBoringLeaves.boring." + e.getId() + "." + e.getLatinName() + " / " + e.getCommonName());
//			}
			if (boring) {
				detach(e);
				changed++;
			}
		}
		logger.debug("pruneBoringLeaves < ");
		entries.removeAll(detached);
		
		return changed;
	}

	/**
	 * "prune" isn't a great analogy anymore, because what this means is to cut out
	 * parents that are more boring than their children, and thereby promote the child.
	 */
	private int pruneBoringParents() {
		int count = 0;
		logger.debug("pruneBoringParents > " + entries.size());
		for (CompleteEntry e: entries) {
			// we only care about entries that have exactly one child.
			if (e.getLoadedChildrenSize() != 1) {
				continue;
			}
			// see if the child is more interesting than the parent
			CompleteEntry child = e.getCompleteEntryChildren().get(0);
			boolean boring = isFirstSubsetOfSecond(e, child);
			if (boring) {
				rewireToGrandparent(child);
//				logger.debug("pruneBoringParents." + e.getLatinName() + "/" + e.getCommonName());
				count++;
			}
		}
		entries.removeAll(detached);
		
		logger.debug("pruneBoringParents <");
		return count;
	}
	
	private void rewireToGrandparent(CompleteEntry child) {
		CompleteEntry gparent = child.getParent().getParent();
		detach(child.getParent());
		child.setParent(gparent);
		child.setParentId(gparent.getId());
		gparent.getCompleteEntryChildren().add(child);
	}
	
	private boolean isLeafBoring(CompleteEntry e) {
		
		if (isLeafBoring_Simple(e)) {
			return true;
		}
		if (e.getParentId() == null) {
			// obviously a corner case...
			return false;
		}
		if (isLeafBoring_ComparedToParent(e)) {
//			logger.debug("isLeafBoring_ComparedToParent." + e.getLatinName() + "/" + e.getCommonName());
			return true;
		}
		if (isLeafBoring_ComparedToSiblings(e)) {
//			logger.debug("isLeafBoring_ComparedToSiblings." + e.getLatinName() + "/" + e.getCommonName());
			return true;
		}
		
		return false;
	}
	
	private boolean isLeafBoring_ComparedToParent(CompleteEntry e) {
		return isFirstSubsetOfSecond(e, e.getParent());
	}
	private boolean isLeafBoring_ComparedToSiblings(CompleteEntry e) {
		// cannot be boring if it has children (for the current pass...)
		if (e.hasChildren()) {
			return false;
		}
		List<CompleteEntry> siblings = e.getParent().getCompleteEntryChildren();
		for (CompleteEntry sibling: siblings) {
			if (sibling == e) {
				continue;
			}
			if (isFirstSubsetOfSecond(e, sibling)) {
				return true;
			}
		}
		return false;
	}
	private boolean isLeafBoring_Simple(CompleteEntry e) {
		return (e.getImageLink() == null && e.getCommonName() == null);
	}
	
	/**
	 * The first might not be boring on its own, but this method
	 * checks if it is merely a "subset" of the second.
	 */
	private boolean isFirstSubsetOfSecond(CompleteEntry first, CompleteEntry second) {
		// TODO "Mountain Beaver" > "Mountain [b]eaver[s]"
		
		String firstCommonName = first.getCommonName();
		String secondCommonName = second.getCommonName();
		
		boolean commonNameSubset = 
			CommonNameSimilarityChecker.isFirstBoringNextToSecond(firstCommonName, secondCommonName);
		
		// if it's not a subset, then the image doesn't matter
		if (!commonNameSubset) {
			return false;
		}
		
		// we now know that the common name is a subset, so we can check the image
		
		if (first.getImageLink() == second.getImageLink()) {
			return true;
		}
		if (first.getImageLink() == null) {
			return true;
		}
		if (second.getImageLink() == null) {
			return false;
		}
		
		// neither is null, so compare
		if (first.getImageLink().equals(second.getImageLink())) {
			return true;
		}
		
		return false;
	}
	
	private void detach(CompleteEntry e) {
		detached.add(e);
		if (e.getParentId() != null) {
			e.getParent().getChildren().remove(e);
			e.setInterestingParentId(null);
			e.setParent(null);
			e.setParentId(null);
		}
	}
	
}
