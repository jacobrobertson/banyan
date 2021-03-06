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
import com.robestone.species.LuceneSearcher.SearchResult;

@RestController
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
		searcher = new LuceneSearcher(entries, luceneDir);
	}

	@RequestMapping(value = "/search-test/{query}")
	public Entry search(@PathVariable String query) {
		return search(query, null);
	}

	@RequestMapping(
			value = "/search/{query}/{existingIds}/", 
			method = RequestMethod.GET, produces = "application/json")
	public Entry search(@PathVariable String query, @PathVariable String existingIds) {
		try {
			System.out.println("search." + query);
			List<Integer> ids;
			if (existingIds == null || existingIds.length() == 0 || "+".equals(existingIds)) {
				ids = new ArrayList<Integer>();
			} else {
				ids = EntryUtilities.CRUNCHER.toList(existingIds);
			}
			SearchResult result = searcher.searchForDocument(query, ids);
			Entry entry = new Entry();
			if (result == null) {
				entry.setId(-1);
			} else {
				entry.setId(result.getId());
				entry.setCids(result.getCrunchedAncestorIds());
			}
			System.out.println("search." + query + "=" + entry.getId());
			return entry;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

//	@RequestMapping("/error")
//	public Entry error() {
//		return new Entry(-666);
//	}
	
	void addEntry(String cname, String lname, int id, List<CompleteEntry> list) {
		CompleteEntry e = new CompleteEntry();
		e.setCommonName(cname);
		e.setLatinName(lname);
		e.setId(id);
		list.add(e);
	}
	
}
