package com.robestone.species.parse;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.LogHelper;
import com.robestone.species.Rank;
import com.robestone.species.UpdateType;
import com.robestone.util.html.EntityMapper;

public class WikiSpeciesCrawler extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		boolean forceNewDownloadForCache = true;
		boolean crawlAllStoredLinks = false;
		//*
		args = new String[] {
				"Lucianovenator",
		};
		crawlAllStoredLinks = false;
		//*/
		
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(forceNewDownloadForCache);
		crawler.pushOnlyTheseNames(new HashSet<String>(Arrays.asList(args)));
		if (crawlAllStoredLinks) {
			crawler.pushAllFoundLinks();
		}
		crawler.crawl();
	}
	
	private boolean forceNewDownloadForCache = false;
	private Stack<ParseStatus> nextStack = new Stack<ParseStatus>();
	private Stack<ParseStatus> currentStack = new Stack<ParseStatus>();
	private Set<ParseStatus> found = new HashSet<ParseStatus>();
	private WikiSpeciesParser parser = new WikiSpeciesParser();
	private RedirectPageParser redirectPageParser = new RedirectPageParser();
	private int updatedCount = 0;
	
	/**
	 * Pushes any links in the Crawl table in status FOUND
	 */
	public void pushAllFoundLinks() throws Exception {
		List<ParseStatus> found = parseStatusService.findAllStatus();
		System.out.println("pushAllFoundLinks" + found.size());
		pushLinks(found);
	}
	/**
	 * Pushes any links in the Crawl table regardless of status
	 * - used only for full recrawling.
	 */
	public void pushAllStatus() throws Exception {
		List<ParseStatus> found = parseStatusService.findAllStatus();
		System.out.println("pushAllStatus" + found.size());
		pushLinks(found);
	}
	private void markAllDoneLinks() throws Exception {
		List<ParseStatus> found = parseStatusService.findAllStatus();
		for (ParseStatus one: found) {
			if (one.isDone()) {
				this.found.add(one);
			}
		}
	}
	private void pushLinks(List<ParseStatus> status) throws Exception {
		for (ParseStatus s: status) {
			s.setUrl(s.getLatinName().trim()); // corner case if this gets in the DB
			push(s);
		}
	}
	private Set<String> fixEntities(Set<String> names) {
		Set<String> fixed = new HashSet<String>();
		for (String name: names) {
			name = name.trim();
			try {
				name = EntityMapper.convertToSymbolsText(name, false);
			} catch (Exception e) {
				// we can't do much... either skip or add as-is
			}
			fixed.add(name);
		}
		return fixed;
	}
	public void pushOnlyTheseNames(Set<String> namesToForce) {
		namesToForce = fixEntities(namesToForce);
		// add all names to force that weren't already in the DB
		for (String latin: namesToForce) {
			ParseStatus i = new ParseStatus();
			i.setUrl(latin);
			i.setStatus(ParseStatus.FOUND);
			currentStack.push(i);
			found.add(i);
		}
	}
	private void push(ParseStatus link) {
		boolean existed = found.add(link);
		// avoid pushing twice
		if (!existed && !link.isDone()) {
			currentStack.push(link);
		}
	}
	
	public void crawl() throws Exception {
		markAllDoneLinks();
		while (!currentStack.empty()) {
			// loop for all found links
			while (!currentStack.empty()) {
				ParseStatus status = currentStack.pop();
	//			LogHelper.speciesLogger.info(found);
				if (status.getType() != null) {
					continue;
				}
				LogHelper.speciesLogger.info(
						"crawlOne." + currentStack.size() + " < " + found.size() + 
						"." + status.getLatinName() + "." + status.getStatus() + "." + status.getType());
				crawlOne(status);
			}
			currentStack = nextStack;
			nextStack = new Stack<ParseStatus>();
		}
	}
	public void crawlOne(ParseStatus ps) throws Exception {
		// get the contents of the page
		String page = WikiSpeciesCache.CACHE.readFile(ps.getLatinName(), forceNewDownloadForCache);
		if (page == null) {
			return;
		}
		// visit the link before getting more links
		visitPage(ps, page);
		// search for the right patterns, ie <a href="/wiki/Biciliata"
		Set<String> links = parseLinks(page);
		for (String link: links) {
			ParseStatus status = new ParseStatus();
			status.setUrl(link);
			status.setStatus(ParseStatus.FOUND);
			saveLink(status);
		}
		// now that we've finished it, mark it as complete
		ps.setDate(new Date());
		ps.setStatus(ParseStatus.DONE);
		parseStatusService.updateStatus(ps);
	}

	public static Set<String> parseLinks(String page) {
		page = StringUtils.replace(page, "\n", "`"); // TODO why do I need to do this? (again..)
		page = StringUtils.replace(page, "\r", "`"); // TODO why do I need to do this?
		Set<String> links = new HashSet<String>();
		Pattern linksPattern = Pattern.compile("href=\"/wiki/(.*?)\"");
		Matcher matcher = linksPattern.matcher(page);
		while (matcher.find()) {
			// save the links
			String link = matcher.group(1);
			link = StringUtils.replace(link, "_", " ");
			link = WikiSpeciesParser.cleanCharacters(link);
			link = link.trim();
			if (!isSkippableLink(link)) {
				links.add(link);
			}
		}
		return links;
	}

	public void saveLink(ParseStatus link) {
		// check if we've already checked this link, and how long ago
		boolean added = found.add(link);
		if (added) {
			// record the status of the link
			// push to the stack
			LogHelper.speciesLogger.info("foundNewLink." + link.getLatinName());
			nextStack.push(link);
			parseStatusService.updateStatus(link);
		}
	}
	
	public void visitPage(ParseStatus link, String page) {
		// cannot tell a redirect page from auth page, so have to check for redirect first
		String redirect = redirectPageParser.getRedirectTo(page);
		if (redirect != null) {
			// we won't return from the method here, because it's okay to log both the entry and the redirect
			speciesService.updateRedirect(link.getLatinName(), redirect);
		} else {
			boolean isAuth = AuthorityUtilities.isAuthorityPage(link.getLatinName(), page);
			if (isAuth) {
				LogHelper.speciesLogger.info("type." + link.getLatinName() + ".AUTH");
				link.setType(ParseStatus.AUTHORITY);
				return;
			}
		}
		boolean isDeleted = isDeleted(page);
		link.setDeleted(isDeleted);
		if (isDeleted) {
			LogHelper.speciesLogger.info("deleted." + link.getLatinName());
			return;
		}
		// parse it
		CompleteEntry results = parsePage(link, page);
		if (results == null) {
//			visitUnparseablePage(link, page);
			// Nothing to do here...
			// Couldn't figure out what this page was...
			if (redirect == null) {
				LogHelper.speciesLogger.error(">>> Could Not Parse >>> " + link.getLatinName());
			}
		} else {
			// for the page I just crawled, do the real update
			udpateOrInsert(results, false);
			
			// for any parent/gparent, we will consider inserting if it doesn't already exist
			while ((results = results.getParent()) != null) {
				udpateOrInsert(results, true);
			}
		}
	}
	private CompleteEntry parsePage(ParseStatus link, String page) {
		String name = link.getLatinName();
		CompleteEntry results = parser.parse(name, page);
		if (isEntryParsedOkay(results)) {
			return results;
		}
		// try the redirect "from" name(s)
		List<String> froms = speciesService.findRedirectFrom(name);
		for (String from: froms) {
			results = parser.parse(name, from, page, true);
			if (results != null) {
				return results;
			}
		}
		return null;
	}
	/**
	 * Can't rely on other code to determine if this was parsed or not.
	 */
	private boolean isEntryParsedOkay(CompleteEntry e) {
		if (e == null) {
			return false;
		} else if (e.getRank() != null && e.getRank() != Rank.Error) {
			// if rank is null, then this didn't parse - that is a requirement
			return true;
		} else {
			return false;
		}
	}
	private void udpateOrInsert(CompleteEntry entry, boolean onlyInsert) {
		
		UpdateType updated;
		if (onlyInsert) {
			updated = speciesService.insertEntryMaybe(entry);
		} else {
			updated = speciesService.updateOrInsertEntryMaybe(entry);
		}
		if (updated == UpdateType.NoChange) {
			return;
		}
		System.out.print("> " + updated + "." + (updatedCount++) + " > ");
		if (entry.getCommonName() != null) {
			System.out.print(entry.getCommonName());
		} else {
			System.out.print("--");
		}
		System.out.print("/");
		System.out.print(entry.getLatinName());
		if (entry.getImageLink() != null) {
			System.out.print("/");
			System.out.print(entry.getImageLink());
		}
		System.out.println();
	}
	
	public static boolean isSkippableLink(String link) {
		if (link.length() > 300) {
			return true;
		}
		// avoid things like "Template:" but parse the virus groups like "Group I: ..."
		if (link.contains(":") && !link.startsWith("Group ")) {
			return true;
		}
		if (link.contains("#")) {
			return true;
		}
		if (link.contains("?")) {
			return true;
		}
		// check for chinese
		if (!chinese.matcher(link).matches()) {
			return true;
		}
		if (isForeign(link)) {
			return true;
		}
		return false;
	}
	private static boolean isForeign(String t) {
		// see how many %AA we have - there can't be very many if it's english
		int count = StringUtils.countMatches(t, "%");
		return count >= 5; // 3 might be better, but this hasn't really actually been a problem
	}
	private static Pattern chinese = Pattern.compile(".*[a-zA-Z]{2,}.*");
	private static boolean isDeleted(String page) {
		return page.contains(WikiSpeciesCache.DELETED_PAGE);
	}
	public void setForceNewDownloadForCache(boolean forceNewDownloadForCache) {
		this.forceNewDownloadForCache = forceNewDownloadForCache;
	}
}
