package com.robestone.species.parse;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.LogHelper;

/**
 * Changes things that say they're done back to an undone status.
 * @author Jacob Robertson
 */
public class ParseDoneChanger extends AbstractWorker {

	
	public static void main(String[] args) throws Exception {
		ParseDoneChanger pc = new ParseDoneChanger();
//		pc.setAllAsDone();
//		pc.createForAllSpecies();
//		pc.findBadStatuses();
		pc.changeForNoSpeciesFound();
		
//		new WikiSpeciesCrawler().crawlStoredLinks();
//		new RecentChangesUpdater().runAll();
	}
	
	public void findBadStatuses() {
		List<ParseStatus> statuses = this.parseStatusService.findAllDoneNonAuth();
		Collection<String> entries = this.speciesService.findAllLatinNames();
		Set<String> set = new HashSet<String>(entries);
		int count = 0;
		for (ParseStatus status: statuses) {
			if (!set.contains(status.getLatinName())) {
				LogHelper.speciesLogger.info((count++) + ": " + status.getLatinName());
			}
		}
	}

	public void setAllAsDone() {
		parseStatusService.setAllAsDone();
	}
	public void changeForNoSpeciesFound() {
		int count = 0;
		boolean persist = true;
		List<ParseStatus> statuses = parseStatusService.findAllDoneNonAuth();
		Set<String> names = new HashSet<String>(speciesService.findAllLatinNames());
		LogHelper.speciesLogger.info("changeForNoSpeciesFound.all." + statuses.size() + "/" + names.size());
		for (ParseStatus one: statuses) {
			if (one.getType() != null) {
				continue;
			}
			if (!names.contains(one.getLatinName())) {
				LogHelper.speciesLogger.info("changeForNoSpeciesFound." + (count++) + "." + one.getLatinName());
				one.setStatus(ParseStatus.FOUND);
				if (persist) {
					parseStatusService.updateStatus(one);
				}
			}
		}
		LogHelper.speciesLogger.info("changeForNoSpeciesFound<" + count);
	}
	/**
	 * This will create many many new parses for scientists, etc...
	 */
	public void createForAllSpecies() {
		Collection<String> all = speciesService.findAllLatinNames();
		for (String latinName: all) {
			ParseStatus parsed = parseStatusService.findForLatinName(latinName);
			if (parsed == null) {
				LogHelper.speciesLogger.info("createForAllSpecies." + latinName);
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
		LogHelper.speciesLogger.info("changeForUnmatchedParents." + names.size());
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
				LogHelper.speciesLogger.info("inserted." + (inserted) + "." + name);
			} else {
				updated++;
				LogHelper.speciesLogger.info("updated." + (updated) + "." + name);
			}
		}
		LogHelper.speciesLogger.info("inserted." + inserted);
		LogHelper.speciesLogger.info("updated." + updated);
	}
}
