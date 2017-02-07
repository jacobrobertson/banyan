package com.robestone.species.search;

import java.util.Arrays;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.robestone.species.CompleteEntry;
import com.robestone.species.LuceneSearcher;
import com.robestone.species.SpeciesService;

public class DbSearcherTest extends TestCase {
	
	private SpeciesService speciesService;
	
	@Override
	protected void setUp() throws Exception {
		String path = "com/robestone/species/parse/SpeciesServices.spring.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		speciesService = (SpeciesService) context.getBean("SpeciesService");
	}
	public void testLuceneSearcherAndDatabase() throws Exception {
		LuceneSearcher searcher = new LuceneSearcher(speciesService);
		// "jacob" is too common, and this isn't a stable test
//		Integer id1 = doTestLuceneSearcherAndDatabase(searcher, "jacob", 38903, 43083);
//		Integer id2 = doTestLuceneSearcherAndDatabase(searcher, "jacob", 157878, id1);
//		Integer id3 = doTestLuceneSearcherAndDatabase(searcher,"jacob", 43540, id1, id2);
//		doTestLuceneSearcherAndDatabase(searcher,"jacob", 107208, id1, id2, id3);
		
		doTestLuceneSearcherAndDatabase(searcher, "wedgefish", "African Wedgefish"); // "Bowmouth Guitarfish, Bowmouth Wedgefish, Shark Ray"); // 84095);
		doTestLuceneSearcherAndDatabase(searcher, "langurs", "Gray Langurs"); // 91067);
		doTestLuceneSearcherAndDatabase(searcher, "Arctic Whale", "Arctic Whale"); // , 44769); Bowhead Whale, Bowhead, Arctic Whale, Greenland Right Whale
		doTestLuceneSearcherAndDatabase(searcher, "Pongo pygmaeus", "Orangutan");
		doTestLuceneSearcherAndDatabase(searcher, "rats", "Rat");
		doTestLuceneSearcherAndDatabase(searcher, "Rattus", "Rat");
		doTestLuceneSearcherAndDatabase(searcher, "abcdefghijklmnop", null); // , -1);
	}
	private Integer doTestLuceneSearcherAndDatabase(LuceneSearcher searcher, String queryString, Object expected, Integer... ids) throws Exception {
		Integer foundId = searcher.search(queryString, Arrays.asList(ids));
		if (foundId == null || foundId == -1) {
			assertNull(expected);
		} else {
			CompleteEntry e = speciesService.findEntry(foundId);
			if (expected instanceof String) {
				String found = e.getCommonName();
				assertTrue("Expected " + expected + ", but found " + found, found.indexOf((String) expected) >= 0);
			} else {
				Integer found = e.getId();
				assertEquals(expected, found);
			}
		}
		return foundId;
	}
	
}
