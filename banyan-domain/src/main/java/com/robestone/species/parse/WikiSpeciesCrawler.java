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
import com.robestone.species.UpdateType;
import com.robestone.util.html.EntityMapper;

public class WikiSpeciesCrawler extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		boolean forceNewDownloadForCache = true;
		boolean crawlAllStoredLinks = true;
		//*
		args = new String[] {
				"Micranthemum umbrosum",
		};
		crawlAllStoredLinks = false;
		//*/
		
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(forceNewDownloadForCache);
		crawler.pushStoredLinks(crawlAllStoredLinks, args);
		crawler.crawl();
	}
	
	private boolean forceNewDownloadForCache = false;
	private Stack<ParseStatus> nextStack = new Stack<ParseStatus>();
	private Stack<ParseStatus> currentStack = new Stack<ParseStatus>();
	private Set<ParseStatus> found = new HashSet<ParseStatus>();
	private WikiSpeciesParser parser = new WikiSpeciesParser();
	private RedirectPageParser redirectPageParser = new RedirectPageParser();
	private int updatedCount = 0;
	
	public void crawlStoredLinks() throws Exception {
		pushStoredLinks(true);
		crawl();
	}
	
	public void pushStoredLinks(String... actualLinks) {
		pushStoredLinks(true, actualLinks);
	}
	public void pushStoredLinks(boolean findAll, String... actualLinks) {
		Set<String> actualSet = new HashSet<String>();
		actualSet.addAll(Arrays.asList(actualLinks));
		actualSet = fixEntities(actualSet);
		pushStoredLinks(actualSet, findAll);
	}
	private Set<String> fixEntities(Set<String> names) {
		Set<String> fixed = new HashSet<String>();
		for (String name: names) {
			name = name.trim();
			name = EntityMapper.convertToSymbolsText(name);
			fixed.add(name);
		}
		return fixed;
	}
	/**
	 * @param namesToForce Use these if we're doing some for a special reason
	 * 	these are guaranteed to be crawled regardless of their status.
	 */
	public void pushStoredLinks(Set<String> namesToForce) {
		pushStoredLinks(namesToForce, true);
	}
	public void pushStoredLinks(Set<String> namesToForce, boolean findAll) {
		List<ParseStatus> all = parseStatusService.findAllStatus();
		if (findAll) {
			// add all that aren't done, 
			// plus any that are done that we want to force
			for (ParseStatus s: all) {
				found.add(s);
				s.setUrl(s.getLatinName().trim()); // corner case if this gets in the DB
				if (!ParseStatus.DONE.equals(s.getStatus())
						|| namesToForce.contains(s.getLatinName())) {
					currentStack.push(s);
					// don't want to add twice
					namesToForce.remove(s.getLatinName());
				}
			}
		} else {
			found.addAll(all);
		}
		// add all names to force that weren't already in the DB
		for (String latin: namesToForce) {
			ParseStatus i = new ParseStatus();
			i.setUrl(latin);
			i.setStatus(ParseStatus.FOUND);
			currentStack.push(i);
			found.add(i);
		}
	}
	
	public void crawl() throws Exception {
		while (!currentStack.empty()) {
			// loop for all found links
			while (!currentStack.empty()) {
				ParseStatus status = currentStack.pop();
	//			LogHelper.speciesLogger.info(found);
				if (status.getType() != null) {
					continue;
				}
				LogHelper.speciesLogger.info("crawlOne." + currentStack.size() + "." + status);
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

	public void visitUnparseablePage(ParseStatus ps, String page) {
		String redirectTo = redirectPageParser.getRedirectTo(page);
		if (redirectTo != null) {
			speciesService.updateRedirect(ps.getLatinName(), redirectTo);
		}
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
		boolean isAuth = AuthorityUtilities.isAuthorityPage(link.getLatinName(), page);
		if (isAuth) {
			LogHelper.speciesLogger.info("type." + link.getLatinName() + ".AUTH");
			link.setType(ParseStatus.AUTHORITY);
			return;
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
			visitUnparseablePage(link, page);
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
		if (results != null) {
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
