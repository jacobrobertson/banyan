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

	private Map<String, String> replacedBy = new HashMap<String, String>(); {
		replacedBy.put("Eutheria", "Placentalia");
		replacedBy.put("Parazoa", "Porifera"); // will probably make it a self-reference, which gets weeded out
	}
	
//	private String[] boring = {
////		"Eutheria"	 
//	};
	
	private Map<String, String> assignParent = new HashMap<String, String>(); {
		assignParent.put("Aves", "Avialae");
	}
	
	private SpeciesService speciesService;
	
	public WikiSpeciesTreeFixer(SpeciesService speciesService) {
		this.speciesService = speciesService;
	}

	public void run() {
		fixReplacedBy();
		fixByMakingBoring();
		fixAssignParent();
	}
	
	/**
	 * These types of fixes will take all species that have a parent of X and simply replace that
	 * parent with Y.
	 * 
	 * TODO where in the workflow should this take place?  it will change the parent latin name only.
	 * 		the rest (including parent id) all need to be updated by the workflow.
	 */
	public void fixReplacedBy() {
		for (String toReplace: replacedBy.keySet()) {
			String replaceWith = replacedBy.get(toReplace);
			fixReplacedBy(toReplace, replaceWith);
		}
	}
	public void fixReplacedBy(String toReplace, String replaceWith) {
		// this is a very inefficient way of doing this, but it's a very low-volume operation
		Entry parent = speciesService.findEntryByLatinName(toReplace, true);
		List<CompleteEntry> children = speciesService.findChildren(parent.getId());
		for (CompleteEntry child: children) {
			LogHelper.speciesLogger.info("fixReplacedBy." + 
					child.getLatinName() + "(" + child.getId() + ")." +
					child.getParentLatinName() + " => " + replaceWith);
			// does not change parent latin id - has to be handled elsewhere
			child.setParentLatinName(replaceWith);
			speciesService.updateParentLatinName(child);
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
		entry.setParentLatinName(newParent);
		LogHelper.speciesLogger.info("fixAssignParent." + 
				entry.getLatinName() + "(" + entry.getId() + ")." +
				entry.getParentLatinName() + " => " + newParent);
		speciesService.updateParentLatinName(entry);
	}
	
	/**
	 * These are species that I don't know exactly what to do with, so I just override
	 * their boring flag.
	 */
	public void fixByMakingBoring() {
//		for (String one: boring) {
//			
//		}
	}
	
}
