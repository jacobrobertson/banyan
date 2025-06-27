package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.robestone.species.Entry;

public class DisambiguationWorker extends AbstractWorker {

	public void runParseStatusLinksForAuth() throws Exception {
		// get all links not in DONE
		
		// sort out by auth or non-auth
		
		// for each non-auth look for a disambiguation
		
			// assign the status as AUTH
	}
	public void runEntries() throws Exception {
		
		// get list of all entries with no parent id
		Collection<Entry> entries = speciesService.findEntriesWithInvalidParent();
		
		// for each of those, get the parent page
		for (Entry entry: entries) {
			String parent = entry.getParentLatinName();
			String child = entry.getLatinName();
			String page = null; // WikiSpeciesCache...
			List<String> names = parseDisambiguationPage(page);
			if (names != null) {
				List<String> matchingPages = new ArrayList<String>();
				for (String name: names) {
					page = null; // WikiSpeciesCache...
					if (page != null) { // what to do if it's null? means it's redirect?
						List<String> links = parseLinksOnParentPage(page);
						Set<String> set = new HashSet<String>(links);
						if (set.contains(child)) {
							matchingPages.add(name);
						}
					} else {
						// we don't want to try and process this one if not all matches are searchable
						matchingPages.clear();
						break;
					}
				}
				// we only allow the disambiguation has succeeded if exactly one matches
				if (matchingPages.size() == 1) {
					assignDisambiguationFix(child, matchingPages.get(0));
				}
			}
		}
		
	}
	private void assignDisambiguationFix(String child, String newParent) {
		
	}
	
	private Pattern listItemPattern = Pattern.compile("<i>.*?\"/wiki/(.*?)\".*?</i>");
	public List<String> parseDisambiguationPage(String text) {
		
		// confirm
		int pos = text.indexOf("This is a disambiguation page");
		if (pos < 0) {
			return null;
		}
		
		// get the part of the page we need - to avoid getting any extra links
		text = text.substring(0, pos);
		pos = text.indexOf("<ul>");
		text = text.substring(pos);
		
		List<String> names = parseNames(text, listItemPattern);
		return names;
	}
	
	private Pattern linkPattern = Pattern.compile("\"/wiki/(.*?)\"");
	public List<String> parseLinksOnParentPage(String page) {
		List<String> names = parseNames(page, linkPattern);
		return names;
	}
	private List<String> parseNames(String text, Pattern pattern) {
		List<String> names = new ArrayList<String>();
		Matcher m = pattern.matcher(text);
		while (m.find()) {
			String name = m.group(1);
			name = StringUtils.replace(name, "_", " ");
			names.add(name);
		}
		return names;
	}
	
}
