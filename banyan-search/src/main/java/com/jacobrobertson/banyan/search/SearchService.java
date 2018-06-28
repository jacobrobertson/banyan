package com.jacobrobertson.banyan.search;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LuceneSearcher;

@RestController
@RequestMapping(value = "/", method = RequestMethod.GET)
public class SearchService {

	@Value("${banyan.lucene.dir}")
	private String luceneDir;
	
	@Value("${spring.resources.static-locations}")
	private String locations;
	
	private LuceneSearcher searcher;
	
	@PostConstruct
	public void init() {
		
		System.out.println(">>>--->>>" + locations + "<<<===<<<");
		
		List<CompleteEntry> entries = new ArrayList<CompleteEntry>();
		
//		int id = 1;
//		addEntry("Wolves", "Lupin", id++, entries);
//		addEntry("Banana", "Barga", id++, entries);
//		addEntry(null, "Vespus", id++, entries);
//		addEntry(null, "Vespusi", id++, entries);
//		addEntry(null, "Vespusii", id++, entries);
		
		searcher = new LuceneSearcher(entries, luceneDir);
	}

	@RequestMapping(value = "/search-test/{query}")
	public Entry search(@PathVariable String query) {
		return search(query, null);
	}

	@RequestMapping(value = "/search/{query}/{existingIds}")
	public Entry search(@PathVariable String query, @PathVariable String existingIds) {
		List<Integer> ids;
		if (existingIds == null) {
			ids = new ArrayList<Integer>();
		} else {
			ids = EntryUtilities.CRUNCHER.toList(existingIds);
		}
		int id = searcher.search(query, ids);
		Entry entry = new Entry();
		entry.setId(id);
		return entry;
	}

	@RequestMapping("/error")
	public Entry error() {
		return new Entry(-666);
	}
	
	void addEntry(String cname, String lname, int id, List<CompleteEntry> list) {
		CompleteEntry e = new CompleteEntry();
		e.setCommonName(cname);
		e.setLatinName(lname);
		e.setId(id);
		list.add(e);
	}
	
}
