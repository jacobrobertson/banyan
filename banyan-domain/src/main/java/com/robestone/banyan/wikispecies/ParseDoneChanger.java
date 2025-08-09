package com.robestone.banyan.wikispecies;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.robestone.banyan.util.LogHelper;
import com.robestone.banyan.workers.AbstractWorker;

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
		pc.fixDuplicates();
	}
	public void fixDuplicates() throws Exception {
		LogHelper.speciesLogger.info("fixDuplicates>");
		Map<String, ParseStatus> map = new HashMap<String, ParseStatus>();
		List<ParseStatus> statuses = this.parseStatusService.findAllStatusDuplicatesOkay();
		LogHelper.speciesLogger.info("fixDuplicates." + statuses.size());
		int count = 0;
		for (ParseStatus s: statuses) {
			String key = s.getLatinName();
			if (map.containsKey(key)) {
				ParseStatus previous = map.get(key);
				ParseStatus toDelete;
				if (previous.isDone()) {
					toDelete = s;
				} else {
					toDelete = previous;
				}
				count += parseStatusService.deleteStatus(toDelete);
				LogHelper.speciesLogger.info(
						"fixDuplicates.delete." + toDelete.getLatinName() + "." + 
								s.getCrawlId() + "/" + previous.getCrawlId());
				System.out.println("deleted." + count);
			} else {
				map.put(key,  s);
			}
		}
	}
	public void reCrawlAllUnparsed() throws Exception {
		checkSpeciesNeedingWork(true);
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.pushAllFoundLinks();
		crawler.crawl();
	}
	
	public void checkSpeciesNeedingWork(boolean persist) {
		LogHelper.speciesLogger.info("checkSpeciesNeedingWork>");
		List<ParseStatus> statuses = this.parseStatusService.findAllNonAuth();
		Collections.sort(statuses, new NameComp());
		
		Collection<String> namesToIgnore = this.getWikiSpeciesService().findLatinNamesForParseDoneChangerToIgnore();
		Set<String> namesToIgnoreSet = new HashSet<String>(namesToIgnore);
		Collection<String> redirectList = this.getWikiSpeciesService().findAllRedirectFroms();
		Set<String> redirectSet = new HashSet<String>(redirectList);
		int count = 0;
		LogHelper.speciesLogger.info("checkSpeciesNeedingWork." + statuses.size());
		for (ParseStatus status: statuses) {
			String key = status.getLatinName();
			if (!namesToIgnoreSet.contains(key) && !redirectSet.contains(key)) {
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
		Collection<String> all = getWikiSpeciesService().findAllLatinNames();
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
		Collection<String> names = getWikiSpeciesService().findAllUnmatchedParentNames();
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
