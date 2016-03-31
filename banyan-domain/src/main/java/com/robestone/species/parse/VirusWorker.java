package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.CompleteEntry;

public class VirusWorker extends AbstractWorker {

	public static void main(String[] args) {
		new VirusWorker().makeVirusesInteresting();
	}
	
	public void makeVirusesInteresting() {
		CompleteEntry entry = speciesService.findEntryByLatinName("Virus");
		List<CompleteEntry> entries = new ArrayList<CompleteEntry>();
		Set<Integer> ids = new HashSet<Integer>();
		findChildren(entry.getId(), ids, entries);
		for (CompleteEntry e: entries) {
			if (e.getCommonName() == null) {
				System.out.println("virus." + e.getLatinName());
				e.setCommonName(e.getLatinName());
				speciesService.updateCommonName(e);
			}
		}
	}
	private void findChildren(Integer id, Set<Integer> ids, List<CompleteEntry> entries) {
		List<CompleteEntry> children = speciesService.findChildren(id);
		for (CompleteEntry e: children) {
			boolean added = ids.add(e.getId());
			if (added) {
				entries.add(e);
				findChildren(e.getId(), ids, entries);
			}
		}
	}
	
}
