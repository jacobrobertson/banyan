package com.robestone.species.search;

import java.util.Arrays;

import junit.framework.TestCase;

import com.robestone.species.CompleteEntry;
import com.robestone.species.LuceneSearcher;
import com.robestone.species.SpeciesService;
import com.robestone.species.SpeciesServiceTest;

public class DbSearcherTest extends TestCase {
	
	private SpeciesService speciesService;
	
	@Override
	protected void setUp() throws Exception {
		speciesService = SpeciesServiceTest.setUpSpeciesService();
	}
	public void testLuceneSearcherAndDatabase() throws Exception {
		LuceneSearcher searcher = new LuceneSearcher(speciesService);
		// "jacob" is too common, and this isn't a stable test
//		Integer id1 = doTestLuceneSearcherAndDatabase(searcher, "jacob", 38903, 43083);
//		Integer id2 = doTestLuceneSearcherAndDatabase(searcher, "jacob", 157878, id1);
//		Integer id3 = doTestLuceneSearcherAndDatabase(searcher,"jacob", 43540, id1, id2);
//		doTestLuceneSearcherAndDatabase(searcher,"jacob", 107208, id1, id2, id3);
		
		doTestLuceneSearcherAndDatabase(searcher, "wedgefish", "Bowmouth Guitarfish, Bowmouth Wedgefish, Shark Ray"); // 84095);
		doTestLuceneSearcherAndDatabase(searcher, "langurs", "Gray Langurs"); // 91067);
		doTestLuceneSearcherAndDatabase(searcher, "Arctic Whale", 44769); // , 44769); Bowhead Whale, Bowhead, Arctic Whale, Greenland Right Whale
//		doTestLuceneSearcherAndDatabase(searcher, "Gigantopithecus", 119818);
		doTestLuceneSearcherAndDatabase(searcher, "abcdefghijklmnop", null); // , -1);
	}
	private Integer doTestLuceneSearcherAndDatabase(LuceneSearcher searcher, String queryString, Object expected, Integer... ids) throws Exception {
		Integer foundId = searcher.search(queryString, Arrays.asList(ids));
		if (foundId == null || foundId == -1) {
			assertNull(expected);
		} else {
			CompleteEntry e = speciesService.findEntry(foundId);
			Object found;
			if (expected instanceof String) {
				found = e.getCommonName();
			} else {
				found = e.getId();
			}
			assertEquals(expected, found);
		}
		return foundId;
	}
	
}
