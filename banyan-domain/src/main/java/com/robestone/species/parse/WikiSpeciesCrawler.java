package com.robestone.species.parse;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LogHelper;
import com.robestone.util.html.EntityMapper;

public class WikiSpeciesCrawler extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		if (args == null || args.length == 0) {
			args = new String[] { };
		}
		
		boolean crawlAllStoredLinks = true;
		//*
		args = new String[] {
				
			"ISSN 2176-7793",

				
		};
		crawlAllStoredLinks = false;
		//*/
		
		
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
//		String name = EntityMapper.convertToSymbolsText("[264]efpa[285]o", true);
		crawler.pushStoredLinks(crawlAllStoredLinks, args
		
//		"Artocarpus altilis"
		);// "Lethiscidae");//"Aïstopoda");//"Oligomyodi");
		crawler.crawl();
	}
	
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
		String page = WikiSpeciesCache.CACHE.readFile(ps.getLatinName());
		if (page == null) {
			return;
		}
		// save it
//		savePage(ps, page);
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
			link = EntryUtilities.urlDecode(link);
			link = WikiSpeciesParser.cleanCharacters(link);
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
		String type = getType(page);
		if (type != null) {
			LogHelper.speciesLogger.info("type." + link.getLatinName() + "." + type);
			link.setType(type);
			return;
		}
		boolean isDeleted = isDeleted(page);
		link.setDeleted(isDeleted);
		if (isDeleted) {
			LogHelper.speciesLogger.info("deleted." + link.getLatinName());
			return;
		}
		// parse it
		CompleteEntry results = parser.parse(link.getLatinName(), page);
		if (results == null) {
			visitUnparseablePage(link, page);
		} else {
			// checking for rank is a temp fix for over-zealous recursion on this
			while (results != null && results.getRank() != null) {
				parsed(results);
				results = results.getParent();
			}
		}
	}
	
	public void parsed(CompleteEntry entry) {
		boolean updated = speciesService.updateOrInsertEntryMaybe(entry);
		if (!updated) {
			return;
		}
		System.out.print("> updated." + (updatedCount++) + " > ");
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
		if (link.contains(":")) {
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
	private static Pattern[] authTypes = getAuthTypes();
	public static Pattern[] getAuthTypes() {
		String[] authTypes = {
				"Taxon_Authorities", 
//				"Entomologists", "Botanists", "Lichenologists",	"Palaeontologists", "Paleobotanists", "Ichthyologists",
				"[A-Za-z_]+ists",
				"ISSN"};
		Pattern[] patterns = new Pattern[authTypes.length];
		for (int i = 0; i < authTypes.length; i++) {
			patterns[i] = Pattern.compile("<a href=\"/wiki/Category\\:" + authTypes[i]);
		}
		return patterns;
	}
	public static String getType(String page) {
		for (Pattern authType: authTypes) {
			Matcher m = authType.matcher(page);
			if (m.find()) {
				return ParseStatus.AUTHORITY;
			}
		}
		String[] authHints = {
				"<span class=\"mw-headline\" id=\"Authored_taxa\">Authored taxa</span>"
		};
		for (String hint: authHints) {
			if (page.contains(hint)) {
				return ParseStatus.AUTHORITY;
			}
		}
		// CAN'T DO -- some good pages are also disambiguation
//		if (page.contains("<a href=\"/wiki/Category:Disambiguation_pages\"")) {
//			return true;
//		}
		return null;
	}

}
