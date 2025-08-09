package com.robestone.banyan.workers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.banyan.taxons.Taxon;
import com.robestone.banyan.taxons.TaxonNode;
import com.robestone.banyan.util.LogHelper;

public class VirusWorker extends AbstractWorker {

	public static void main(String[] args) {
		new VirusWorker().makeVirusesInteresting();
	}
	
	public void makeVirusesInteresting() {
		LogHelper.speciesLogger.info("makeVirusesInteresting");
		Taxon entry = getTaxonService().findEntryByLatinName("Virus");
		List<Taxon> entries = new ArrayList<>();
		Set<Integer> ids = new HashSet<Integer>();
		findChildren(entry.getTaxonId(), ids, entries);
		for (Taxon e: entries) {
			if (e.getCommonName() == null) {
				System.out.println("virus." + e.getLatinName());
				e.setCommonName(e.getLatinName());
				getTaxonService().updateCommonName(e);
			}
		}
	}
	private void findChildren(Integer id, Set<Integer> ids, List<Taxon> entries) {
		LogHelper.speciesLogger.info("makeVirusesInteresting.findChildren." + id);
		List<TaxonNode> children = getTaxonService().findChildren(id);
		for (Taxon e: children) {
			boolean added = ids.add(e.getTaxonId());
			if (added) {
				entries.add(e);
				findChildren(e.getTaxonId(), ids, entries);
			}
		}
	}
	
}
