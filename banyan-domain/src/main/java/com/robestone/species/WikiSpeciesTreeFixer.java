package com.robestone.species;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wiki Species has many problems with tree inconsistencies.  Many of these I'm just
 * not brave enough to try and edit those pages.
 * 
 * The purpose of this fixer is to try and address at least some of the issues in my own database.
 * My initial strategy is to look for the most egregious knots and untie them.
 * A great example is the wikispecies use of "Eutheria (=Placentalia)" which is fine for a wiki,
 * but for my tree it makes things pretty messed up.
 * 
 * @author jacob
 */
public class WikiSpeciesTreeFixer {

	/**
	 * Reassigns all parent latin names.
	 */
	private Map<String, String> replacedParentLatinNames = new HashMap<String, String>(); {
		replacedParentLatinNames.put("Eutheria", "Placentalia");
		replacedParentLatinNames.put("Parazoa", "Porifera"); // will probably make it a self-reference, which gets weeded out
	}
	
	/**
	 * Reassigns the parent latin names for the given child latin names.
	 */
	private Map<String, String> assignParent = new HashMap<String, String>(); {
		assignParent.put("Placentalia", "Theria");
		assignParent.put("Aves", "Avialae");
//		assignParent.put("Virus", null); // this didn't work anyways, and I've fixed virus parsing
	}
	
	private SpeciesService speciesService;
	
	public WikiSpeciesTreeFixer(SpeciesService speciesService) {
		this.speciesService = speciesService;
	}

	public void run() {
		// have to assign parent first, at least for Placentalia
		fixAssignParent();
		fixReplaceAllParentLatinNames();
		fixExtinct();
	}
	
	public void fixExtinct() {
		speciesService.assignExtinctToSpecies();
	}
	
	/**
	 * These types of fixes will take all species that have a parent of X and simply replace that
	 * parent with Y.
	 * 
	 * TODO where in the workflow should this take place?  it will change the parent latin name only.
	 * 		the rest (including parent id) all need to be updated by the workflow.
	 */
	public void fixReplaceAllParentLatinNames() {
		for (String toReplace: replacedParentLatinNames.keySet()) {
			String replaceWith = replacedParentLatinNames.get(toReplace);
			fixReplaceAllParentLatinNames(toReplace, replaceWith);
		}
	}
	public void fixReplaceAllParentLatinNames(String toReplace, String replaceWith) {
		List<CompleteEntry> entries = speciesService.findEntriesByParentLatinName(toReplace);
		for (CompleteEntry entry: entries) {
			entry.setParentLatinName(replaceWith);
			LogHelper.speciesLogger.info("fixReplaceAllParentLatinNames." + 
					entry.getLatinName() + "(" + entry.getId() + ")." +
					entry.getParentLatinName() + " => " + replaceWith);
			speciesService.updateParentLatinName(entry);
		}
	}
	public void fixAssignParent() {
		for (String child: assignParent.keySet()) {
			String newParent = assignParent.get(child);
			fixAssignParent(child, newParent);
		}
	}
	public void fixAssignParent(String child, String newParent) {
		CompleteEntry entry = speciesService.findEntryByLatinName(child, true);
		if (!newParent.equals(entry.getParentLatinName())) {
			entry.setParentLatinName(newParent);
			LogHelper.speciesLogger.info("fixAssignParent." + 
					entry.getLatinName() + "(" + entry.getId() + ")." +
					entry.getParentLatinName() + " => " + newParent);
			speciesService.updateParentLatinName(entry);
		}
	}
	
}
