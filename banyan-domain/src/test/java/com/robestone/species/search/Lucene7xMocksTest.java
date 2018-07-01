package com.robestone.species.search;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.Entry;
import com.robestone.species.LuceneSearcher;
import com.robestone.species.LuceneSearcher.SearchResult;
import com.robestone.species.parse.Mocks;

import junit.framework.TestCase;

/**
 * Test things after upgrading to 7.x.  All the tests have broken, need to regroup.
 * @author jacob
 */
public class Lucene7xMocksTest extends TestCase {

	private LuceneSearcher mockSearcher;
	private Mocks mocks = new Mocks();

	@Override
	protected void setUp() throws Exception {
		String indexDir = "./target/Lucene7xTest";
		File file = new File(indexDir);
		List<Entry> entries = new ArrayList<>();
		if (!file.exists()) {
			file.mkdirs();
			entries.addAll(mocks.getEntries());
		}
		mockSearcher = new LuceneSearcher(entries, indexDir);
	}

	public void testId() throws Exception {
		String name = "animals";
		SearchResult r1 = mockSearcher.searchForDocument(name, null);
		SearchResult r2 = mockSearcher.searchForDocument(String.valueOf(r1.getId()), null);
		assertEquals(r1.getId(), r2.getId());
	}
	
	public void testSmoke() throws Exception {
		// just see if I can find one thing
		String name = "animals";
		Entry e = mocks.getEntryForName(name);
		SearchResult result = mockSearcher.searchForDocument(name, null);
		assertNotNull(result);
		assertEquals(e.getId().intValue(), result.getId());
		
		// test that we find the exact same thing if we search again
		result = mockSearcher.searchForDocument(name, null);
		assertEquals(e.getId().intValue(), result.getId());
	}
	
	public void testExistingIds() throws Exception {
		// I want these two to bring back the same exact results
		// currently they're not, which is probably okay
		doTestExistingIds("fink", 4);
		doTestExistingIds("Fink", 4);
	}
	private void doTestExistingIds(String name, int tries) throws Exception {
		// loop through x times and see it gets a new result each time
		Set<Integer> foundIds = new HashSet<>();
		for (int i = 0; i < tries; i++) {
			SearchResult result = mockSearcher.searchForDocument(name, foundIds);
			assertNotNull(result);
			assertTrue(result.getId() != -1);
			assertTrue(!foundIds.contains(result.getId()));
			foundIds.add(result.getId());
			Entry mock = mocks.getEntryForId(result.getId());
			System.out.println(name + ", found:" + result.getId() + ", mocks:" + mock.getId() + ":" + mock.getLatinName() + "/" + mock.getCommonName());
		}
	}
	
	
}
