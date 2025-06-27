package com.robestone.species.parse;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.Entry;
import com.robestone.species.LogHelper;

public class SiblingsWithSameCommonNamesAnalyzer extends AbstractWorker {

	public static void main(String[] args) {
		new SiblingsWithSameCommonNamesAnalyzer().run();
	}
	public void run() {
		speciesService.updateCommonNamesSharedWithSiblingsFalse();
		int i = 0;
		Set<String> found = new HashSet<String>();
		Collection<Entry> all = 
			speciesService.findInterestingTreeFromPersistence().getEntries();
		for (Entry e: all) {
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
	private String getKey(Entry e) {
		return e.getParentId() + "/" + e.getCommonName();
	}
	private int hasSiblingWithSameCommonName(Entry entry) {
		if (entry.getCommonName() == null) {
			return 0;
		}
		if (entry.getParent() == null) {
			return 0;
		}
		int count = 0;
		List<Entry> children = entry.getParent().getChildren();
		for (Entry e: children) {
			if (e == entry) {
				continue;
			}
			boolean shares = 
				e.getCommonName() != null && 
				e.getCommonName().equals(entry.getCommonName());
			e.setCommonNameSharedWithSiblings(shares);
			if (shares) {
				count++;
				speciesService.updateCommonNameSharedWithSiblings(e);
			}
		}
		boolean shares = (count > 0);
		entry.setCommonNameSharedWithSiblings(shares);
		if (shares) {
			speciesService.updateCommonNameSharedWithSiblings(entry);
		}
		return count;
	}
	
}
