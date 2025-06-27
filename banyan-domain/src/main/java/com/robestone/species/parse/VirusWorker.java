package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.Entry;
import com.robestone.species.LogHelper;

public class VirusWorker extends AbstractWorker {

	public static void main(String[] args) {
		new VirusWorker().makeVirusesInteresting();
	}
	
	public void makeVirusesInteresting() {
		LogHelper.speciesLogger.info("makeVirusesInteresting");
		Entry entry = speciesService.findEntryByLatinName("Virus");
		List<Entry> entries = new ArrayList<Entry>();
		Set<Integer> ids = new HashSet<Integer>();
		findChildren(entry.getId(), ids, entries);
		for (Entry e: entries) {
			if (e.getCommonName() == null) {
				System.out.println("virus." + e.getLatinName());
				e.setCommonName(e.getLatinName());
				speciesService.updateCommonName(e);
			}
		}
	}
	private void findChildren(Integer id, Set<Integer> ids, List<Entry> entries) {
		LogHelper.speciesLogger.info("makeVirusesInteresting.findChildren." + id);
		List<Entry> children = speciesService.findChildren(id);
		for (Entry e: children) {
			boolean added = ids.add(e.getId());
			if (added) {
				entries.add(e);
				findChildren(e.getId(), ids, entries);
			}
		}
	}
	
}
