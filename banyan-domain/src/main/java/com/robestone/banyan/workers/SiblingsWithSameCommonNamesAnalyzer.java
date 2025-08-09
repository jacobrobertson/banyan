package com.robestone.banyan.workers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.banyan.taxons.TaxonNode;
import com.robestone.banyan.util.LogHelper;

public class SiblingsWithSameCommonNamesAnalyzer extends AbstractWorker {

	public static void main(String[] args) {
		new SiblingsWithSameCommonNamesAnalyzer().run();
	}
	public void run() {
		getWikiSpeciesService().updateCommonNamesSharedWithSiblingsFalse();
		int i = 0;
		Set<String> found = new HashSet<String>();
		Collection<TaxonNode> all = 
				getTaxonService().findInterestingTreeFromPersistence().getNodesList();
		for (TaxonNode e: all) {
			String key = getKey(e);
			if (found.contains(key)) {
				continue;
			}
			int count = hasSiblingWithSameCommonName(e);
			if (count > 0) {
				found.add(key);
				i++;
				LogHelper.speciesLogger.info("siblings." + i + ". " + key + " (" + count + ")");
			}
		}
	}
	private String getKey(TaxonNode e) {
		return e.getParentId() + "/" + e.getCommonName();
	}
	private int hasSiblingWithSameCommonName(TaxonNode entry) {
		if (entry.getCommonName() == null) {
			return 0;
		}
		if (entry.getParent() == null) {
			return 0;
		}
		int count = 0;
		List<TaxonNode> children = entry.getParent().getChildren();
		for (TaxonNode e: children) {
			if (e == entry) {
				continue;
			}
			boolean shares = 
				e.getCommonName() != null && 
				e.getCommonName().equals(entry.getCommonName());
			// TODO
//			e.setCommonNameSharedWithSiblings(shares);
			if (shares) {
				count++;
				// TODO
//				getTaxonService().updateCommonNameSharedWithSiblings(e);
			}
		}
		boolean shares = (count > 0);
		// TODO
//		entry.setCommonNameSharedWithSiblings(shares);
		if (shares) {
			// TODO
//			getTaxonService().updateCommonNameSharedWithSiblings(entry);
		}
		return count;
	}
	
}
