package com.robestone.species.parse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.util.html.EntityMapper;

public class WikiSpeciesCrawler extends AbstractWorker {

	public static void main(String[] args) {
		
		if (args == null || args.length == 0) {
			args = new String[] { "Ciliophrys" };
		}
		
		boolean crawlAllStoredLinks = true;
		/*
		args = new String[] {
				
			"Eutheria",

				
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
	
	public void crawlStoredLinks() {
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
	
	public void crawl() {
		while (!currentStack.empty()) {
			// loop for all found links
			while (!currentStack.empty()) {
				ParseStatus status = currentStack.pop();
	//			System.out.println(found);
				if (status.getType() != null) {
					continue;
				}
				System.out.println("crawlOne." + currentStack.size() + "." + status);
				crawlOne(status);
			}
			currentStack = nextStack;
			nextStack = new Stack<ParseStatus>();
		}
	}
	public void crawlOne(ParseStatus ps) {
		// get the contents of the page
		String page = getPage(ps.getLatinName());
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
			System.out.println("foundNewLink." + link.getLatinName());
			nextStack.push(link);
			parseStatusService.updateStatus(link);
		}
	}
	
	public void visitPage(ParseStatus link, String page) {
		String type = getType(page);
		if (type != null) {
			System.out.println("type." + link.getLatinName() + "." + type);
			link.setType(type);
			return;
		}
		boolean isDeleted = isDeleted(page);
		link.setDeleted(isDeleted);
		if (isDeleted) {
			System.out.println("deleted." + link.getLatinName());
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
	static void savePage(ParseStatus link, String page) {
		try {
			IOUtils.write(page, new FileOutputStream("E:\\WikiSpeciesCache\\" + link.getLatinName()));
		} catch (IOException ioe) {
			ioe.printStackTrace();
//			throw new RuntimeException(ioe);
		}
	}
	
	private static String getPage(String link) {
		link = StringUtils.replace(link, " ", "_");
		link = "https://species.wikimedia.org/wiki/" + link + "?redirect=no";
		return getPageForUrl(link);
	}
	public static String getPageForUrl(String link) {
		return getPageForUrl(link, 3);
	}
	public static String getPageForUrl(String link, int maxRetries) {
		try {
			URL url = new URL(link);
			URLConnection con = url.openConnection();
			HttpURLConnection hconn = (HttpURLConnection) con;
			con.setConnectTimeout(15000); // not sure what a good number is here?
			hconn.addRequestProperty("Content-Type", "text/html;charset=UTF-8");
			hconn.setUseCaches(false);
			int status = hconn.getResponseCode();
			if (status == HttpURLConnection.HTTP_NOT_FOUND) {
				// 401 means it was deleted
				// this is a pretty big workaround, but it should work...
				System.out.println("status." + status + "." + link);
				return DELETED_PAGE;
			}
			InputStream in = con.getInputStream();
			String contentEncoding = con.getHeaderField("Content-Encoding");
	        if ("gzip".equalsIgnoreCase(contentEncoding)) {
				in = new GZIPInputStream(in);
	        }
			String page = IOUtils.toString(in, "UTF-8");
			// I do this to avoid spamming wikispecies too hard
			// not sure how long a sleep I need
			Thread.sleep(250);
			return page;
		} catch (IOException ioe) {
			System.out.println("getPageForUrl.IOException." + ioe.getMessage() +  "(" + maxRetries + ")." + link);
			if (maxRetries == 0) {
				return null;
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				return getPageForUrl(link, maxRetries - 1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
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
	private static String DELETED_PAGE = "This page has been deleted.";
	private static boolean isDeleted(String page) {
		return page.contains(DELETED_PAGE);
	}
	public static String getType(String page) {
		String[] authTypes = {
				"Entomologists", "Taxon_Authorities", "Botanists", "Lichenologists",
				"Palaeontologists", "Paleobotanists"};
		for (String authType: authTypes) {
			if (page.contains("<a href=\"/wiki/Category:" + authType)) {
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
