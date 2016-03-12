package com.robestone.species.parse;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
//		pc.checkSpeciesNeedingWork(true);
		pc.reCrawlAllUnparsed();
	}
	public void reCrawlAllUnparsed() throws Exception {
		checkSpeciesNeedingWork(true);
		new WikiSpeciesCrawler().crawlStoredLinks();
	}
	
	public void checkSpeciesNeedingWork(boolean persist) {
		LogHelper.speciesLogger.info("checkSpeciesNeedingWork>");
		List<ParseStatus> statuses = this.parseStatusService.findAllNonAuth();
		Collections.sort(statuses, new NameComp());
		
		Collection<String> entries = this.speciesService.findAllLatinNames();
		Set<String> entriesSet = new HashSet<String>(entries);
		Collection<String> redirectList = this.speciesService.findAllRedirectFroms();
		Set<String> redirectSet = new HashSet<String>(redirectList);
		int count = 0;
		LogHelper.speciesLogger.info("checkSpeciesNeedingWork." + statuses.size());
		for (ParseStatus status: statuses) {
			String key = status.getLatinName();
			if (!entriesSet.contains(key) && !redirectSet.contains(key)) {
				LogHelper.speciesLogger.info("checkSpeciesNeedingWork." + (count++) + "." + status.getLatinName());
				if (persist) {
					status.setStatus(ParseStatus.FOUND);
					parseStatusService.updateStatus(status);
				}
			}
		}
	}
	private class NameComp implements Comparator<ParseStatus> {
		@Override
		public int compare(ParseStatus o1, ParseStatus o2) {
			return o1.getLatinName().compareTo(o2.getLatinName());
		}
	}

	public void setAllAsDone() {
		parseStatusService.setAllAsDone();
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
