package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * In the DB, I see an entry with a null parent
 * Get the list of all children of that entry
 * Use the existing method that scrapes all page names from a given page, and exclude the names of all children
 * For each of those pages, see if they link to the page.  If exactly one of them does, it’s a likely hit.
 * TODO exclude AUTH pages also
 * TODO after getting the parent, I have to get the rank
 * TODO this will actually be most useful once I have the count, so I know if it's worth doing at all, because
 * 		maybe there's only 10 of these
 */
public class MissingParentFilesFinder {

	public static void main(String[] args) throws Exception {
		new MissingParentFilesFinder().runOne(
				"Paracobitis", 
					"Paracobitis hircanica",
					"Paracobitis persa",
					"Paracobitis smithi", 
					"Paracobitis malapterura", 
					"Paracobitis zabgawraensis", 
					"Paracobitis basharensis", 
					"Paracobitis longicauda", 
					"Paracobitis molavii", 
					"Paracobitis vignai",
					"Paracobitis rhadinae" 
					);
	}
	
	public void runOne(String name, String... children) throws Exception {
		
		List<String> childrenList = Arrays.asList(children);
		
		String contents = WikiSpeciesCache.CACHE.readFile(name, true);
		Set<String> links = WikiSpeciesCrawler.parseLinks(contents);
		// TODO is "_" a problem?
		links.removeAll(childrenList);
		links.remove(name);
		List<String> matches = new ArrayList<String>();
		for (String link: links) {
			String page = WikiSpeciesCache.CACHE.readFile(link, false);
			Set<String> pageLinks = WikiSpeciesCrawler.parseLinks(page);
			if (pageLinks.contains(name)) {
				matches.add(link);
			}
		}
		System.out.println(name + " is child of " + matches);
	}

}
