package com.robestone.species;

import java.util.List;

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

	private String[][] replacedBy = 
	{
			{"Eutheria", "Placentalia"},	
	};
	
	private String[] boring = {
//		"Eutheria"	
	};
	
	private SpeciesService speciesService;
	
	public WikiSpeciesTreeFixer(SpeciesService speciesService) {
		this.speciesService = speciesService;
	}

	/**
	 * These types of fixes will take all species that have a parent of X and simply replace that
	 * parent with Y.
	 * 
	 * TODO where in the workflow should this take place?  it will change the parent latin name only.
	 * 		the rest (including parent id) all need to be updated by the workflow.
	 */
	public void fixReplacedBy() {
		for (String[] replacement: replacedBy) {
			String toReplace = replacement[0];
			String replaceWith = replacement[1];
			fixReplacedBy(toReplace, replaceWith);
		}
	}
	public void fixReplacedBy(String toReplace, String replaceWith) {
		// this is a very inefficient way of doing this, but it's a very low-volume operation
		Entry parent = speciesService.findEntryByLatinName(toReplace, true);
		List<CompleteEntry> children = speciesService.findChildren(parent.getId());
		for (CompleteEntry child: children) {
			System.out.println("fixReplacedBy." + 
					child.getLatinName() + "(" + child.getId() + ")." +
					child.getParentLatinName() + " => " + replaceWith);
			// does not change parent latin id - has to be handled elsewhere
			child.setParentLatinName(replaceWith);
			speciesService.updateParentLatinName(child);
		}
	}
	/**
	 * These are species that I don't know exactly what to do with, so I just override
	 * their boring flag.
	 */
	public void fixByMakingBoring() {
		for (String one: boring) {
			
		}
	}
	
}
