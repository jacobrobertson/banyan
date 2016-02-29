package com.robestone.species.parse;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.Entry;

/**
 * Changes things that say they're done back to an undone status.
 * @author Jacob Robertson
 */
public class ParseDoneChanger extends AbstractWorker {

	
	public static void main(String[] args) {
		ParseDoneChanger pc = new ParseDoneChanger();
//		pc.setAllAsDone();
//		pc.createForAllSpecies();
//		pc.findBadStatuses();
		pc.changeForUnmatchedParents();
		
		new WikiSpeciesCrawler().crawlStoredLinks();
	}
	
	public void findBadStatuses() {
		List<ParseStatus> statuses = this.parseStatusService.findAllDoneNonAuth();
		Collection<String> entries = this.speciesService.findAllLatinNames();
		Set<String> set = new HashSet<String>(entries);
		int count = 0;
		for (ParseStatus status: statuses) {
			if (!set.contains(status.getLatinName())) {
				System.out.println((count++) + ": " + status.getLatinName());
			}
		}
	}

	public void setAllAsDone() {
		parseStatusService.setAllAsDone();
	}
	/**
	 * This will create many many new parses for scientists, etc...
	 */
	public void changeForNoSpeciesFound() {
		List<ParseStatus> all = parseStatusService.findAllStatus();
		for (ParseStatus one: all) {
			if (one.getType() != null) {
				continue;
			}
			Entry e = speciesService.findEntryByLatinName(one.getLatinName());
			if (e == null) {
				System.out.println("changeForNoSpeciesFound." + one.getLatinName());
				one.setStatus(ParseStatus.FOUND);
				parseStatusService.updateStatus(one);
			}
		}
	}
	/**
	 * This will create many many new parses for scientists, etc...
	 */
	public void createForAllSpecies() {
		Collection<String> all = speciesService.findAllLatinNames();
		for (String latinName: all) {
			ParseStatus parsed = parseStatusService.findForLatinName(latinName);
			if (parsed == null) {
				System.out.println("createForAllSpecies." + latinName);
				parsed = new ParseStatus();
				parsed.setUrl(latinName);
				parsed.setStatus(ParseStatus.DONE);
				parseStatusService.updateStatus(parsed);
			}
		}
	}
	public void changeForUnmatchedParents() {
		// get all parent names that have no matching number
		Collection<String> names = speciesService.findAllUnmatchedParentNames();
		// update the parse status to FOUND
		int updated = 0;
		int inserted = 0;
		System.out.println("changeForUnmatchedParents." + names.size());
		for (String name: names) {
			if (name == null) {
				continue;
			}
			ParseStatus status = new ParseStatus();
			status.setUrl(name);
			status.setDate(new Date());
			status.setStatus("FOUND");
			int changed = parseStatusService.updateStatus(status);
			if (changed == 0) {
				status.setDate(null);
				parseStatusService.updateStatus(status);
				inserted++;
				System.out.println("inserted." + (inserted) + "." + name);
			} else {
				updated++;
				System.out.println("updated." + (updated) + "." + name);
			}
		}
		System.out.println("inserted." + inserted);
		System.out.println("updated." + updated);
	}
}
