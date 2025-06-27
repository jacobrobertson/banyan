package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.robestone.species.Entry;
import com.robestone.species.LogHelper;

/**
 * In the DB, I see an entry with a null parent
 * Get the list of all children of that entry
 * Use the existing method that scrapes all page names from a given page, and exclude the names of all children
 * For each of those pages, see if they link to the page.  If exactly one of them does, it�s a likely hit.
 * TODO exclude AUTH pages also
 * TODO after getting the parent, I have to get the rank
 * TODO this will actually be most useful once I have the count, so I know if it's worth doing at all, because
 * 		maybe there's only 10 of these
 */
public class MissingParentFilesFinder extends AbstractWorker {

	public static void main(String[] args) throws Exception {
//		new MissingParentFilesFinder().searchNotParsed();
		new MissingParentFilesFinder().searchFilesForParentIdNull();
	}
	
	private Set<String> authNames;
	private WikiSpeciesParser parser = new WikiSpeciesParser();
	
	public MissingParentFilesFinder() throws Exception {
		List<ParseStatus> auth = this.parseStatusService.findAllAuth();
		authNames = new HashSet<String>();
		for (ParseStatus one: auth) {
			authNames.add(one.getLatinName());
		}
	}
	
	
	public void searchFilesForParentIdNull() throws Exception {
		LogHelper.speciesLogger.debug("searchFilesForParentIdNull>");
		Set<String> parentsChecked = new HashSet<String>();
		Collection<Entry> entries = speciesService.findEntriesWithInvalidParent();
		LogHelper.speciesLogger.debug("searchFilesForParentIdNull>>");
		Map<String, List<String>> reasons = new HashMap<String, List<String>>();
		for (Entry e: entries) {
			// check if the child exists - it might have been a red link
			String cname = e.getLatinName();
			boolean exists = WikiSpeciesCache.CACHE.isFilePresent(cname, false);
			if (exists) {
				continue;
			}
			
			if (e.getParentLatinName() == null) {
				// very rare case - recursive self-links
				continue;
			}
			if (parentsChecked.contains(e.getParentLatinName())) {
				continue;
			}
			parentsChecked.add(e.getParentLatinName());
			String reason = parseParentFileToDetermineFailure(e);
			if (reason != null) {
				addReason(reason, e, reasons);
			}
		}
	}
	public String parseParentFileToDetermineFailure(Entry entry) throws Exception {
		String pname = entry.getParentLatinName();
//		LogHelper.speciesLogger.debug("parseParentFileToDetermineFailure>" + pname + " < " + entry.getLatinName());
		String page = WikiSpeciesCache.CACHE.readFile(pname, false);
//		LogHelper.speciesLogger.debug("parseParentFileToDetermineFailure>1>");
		if (page == null) {
			return "FileNotFound";
		}
//		LogHelper.speciesLogger.debug("parseParentFileToDetermineFailure>2>");
		Entry parsed = parser.parse(pname, page);
//		LogHelper.speciesLogger.debug("parseParentFileToDetermineFailure>3>");
		if (parsed == null) {
			return "ParseFailed";
		}
		return "Okay";
	}
	private void addReason(String reason, Entry entry, Map<String, List<String>> reasons) {
		LogHelper.speciesLogger.debug("parseReason." + reason + "." + entry.getParentLatinName() + " < " + entry.getLatinName());
		List<String> names = reasons.get(reason);
		if (names == null) {
			names = new ArrayList<String>();
			reasons.put(reason, names);
		}
		names.add(entry.getParentLatinName());
	}
	public void searchParentIdNull() throws Exception {
		Collection<Entry> entries = speciesService.findEntriesWithInvalidParent();
		List<Integer> ids = new ArrayList<Integer>();
		for (Entry e: entries) {
			ids.add(e.getId());
		}
		int count = countParentIdNullSubTree(ids);
		LogHelper.speciesLogger.debug("searchParentIdNull.ids." + ids.size());
		LogHelper.speciesLogger.debug("searchParentIdNull.tree." + count);
	}
	public int countParentIdNullSubTree(List<Integer> ids) throws Exception {
		
		int count = ids.size();
		int max = 100;
		for (int i = 0; i < ids.size(); i+=max) {
			List<Integer> sub;
			if (i + max >= ids.size()) {
				sub = ids.subList(i, ids.size());
			} else {
				sub = ids.subList(i, i + max);
			}
			List<Integer> childIds = speciesService.findIdsForParentIds(sub);
			count += countParentIdNullSubTree(childIds);
		}
		
		return count;
	}
	
	public void searchNotParsed() throws Exception {
		List<ParseStatus> statuses = this.parseStatusService.findAllNonAuth();
		Collection<String> entries = this.speciesService.findAllLatinNames();
		Set<String> entriesSet = new HashSet<String>(entries);
//		Collection<String> redirectList = this.speciesService.findAllRedirectFroms();
//		Set<String> redirectSet = new HashSet<String>(redirectList);
		LogHelper.speciesLogger.info("MissingParentFilesFinder.runAll." + statuses.size());
		for (ParseStatus status: statuses) {
			String name = status.getLatinName();
			// only looking for entries we couldn't parse
			if (entriesSet.contains(name)) {
				continue;
			}
			List<String> childNames = speciesService.findChildNamesByParentLatinName(name);
			if (childNames.isEmpty()) {
				continue;
			}
			runOne(name, childNames);
		}
	}
	
	public void runOne(String name, List<String> childrenList) throws Exception {
		String contents = WikiSpeciesCache.CACHE.readFile(name, true);
		Set<String> links = WikiSpeciesCrawler.parseLinks(contents);
		// TODO is "_" a problem?
		links.removeAll(childrenList);
		links.remove(name);
		links.removeAll(authNames);
		List<String> matches = new ArrayList<String>();
		for (String link: links) {
			String page = WikiSpeciesCache.CACHE.readFile(link, false);
			Set<String> pageLinks = WikiSpeciesCrawler.parseLinks(page);
			if (pageLinks.contains(name)) {
				matches.add(link);
			}
		}
		if (matches.size() == 1) {
			System.out.println(name + " is child of " + matches);
		}
	}

}
