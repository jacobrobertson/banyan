package com.robestone.species.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LuceneSearcher;
import com.robestone.species.LuceneSearcher.SearchResult;

import junit.framework.TestCase;

/**
 * Same as other test, but use the actual DbIndex (don't create a new one or add to existing)
 * @author jacob
 */
public class Lucene7xDbIndexTest extends TestCase {

	private LuceneSearcher searcher;

	@Override
	protected void setUp() throws Exception {
		String indexDir = "/home/private/banyan-lucene";
		List<Entry> entries = new ArrayList<>();
		searcher = new LuceneSearcher(entries, indexDir);
	}
	
	public void testIdSearch() throws Exception {
		String name = "83948";
		SearchResult result = searcher.searchForDocument(name, null);
		assertNotNull(result);
		assertEquals(name, String.valueOf(result.getId()));
	}
	
	public void testSmoke() throws Exception {
		// just see if I can find one thing
		String name = "animals";
		SearchResult result = searcher.searchForDocument(name, null);
		assertNotNull(result);
	}
	public void testSimluateMicroservice() throws Exception {
		doTestSimluateMicroservice("lepidoptera/.157dm_362o0OFnX9-1381714_7G-bdc.1Iw0Lb1zY19J.1111_WT__11Hl0lDh");
		doTestSimluateMicroservice("lilly/.15_3F06ePL9uMfy-12_Zy-3d_lV_2qE.4.3OQavL19b-2j-1i.f5y.2.BQ1.ur.acU.Z");
	}
	private void doTestSimluateMicroservice(String url) throws Exception {
		int pos = url.indexOf("/");
		String query = url.substring(0,  pos);
		String cids = url.substring(pos + 1);
		List<Integer> ids = EntryUtilities.CRUNCHER.toList(cids);
		System.out.println("service.query: " + query);
		System.out.println("service.cids: " + cids);
		System.out.println("service.existing: " + ids);
		SearchResult result = searcher.searchForDocument(query, ids);
		List<Integer> rids = EntryUtilities.CRUNCHER.toList(result.getCrunchedAncestorIds());
		System.out.println("service.result.id: " + result.getId());
		System.out.println("service.result.cids: " + rids);
	}
	
	/**
	 * This scenario is what I don't understand when I use directly from the Sring Boot JSON service.
	 */
	public void testIdScenario() throws Exception {
		// this should be able to find indefinite results
		doTestExistingIds("lepidoptera", 5);
	}
	public void testExistingIds() throws Exception {
		doTestExistingIds("fink", 4);
	}
	private void doTestExistingIds(String name, int tries) throws Exception {
		// loop through x times and see it gets a new result each time
		Set<Integer> foundIds = new HashSet<>();
		for (int i = 0; i < tries; i++) {
			SearchResult result = searcher.searchForDocument(name, foundIds);
			assertNotNull(result);
			assertTrue(result.getId() != -1);
			assertTrue(!foundIds.contains(result.getId()));
			List<Integer> cids = EntryUtilities.CRUNCHER.toList(result.getCrunchedAncestorIds());
			List<Integer> fids = new ArrayList<>(foundIds);
			Collections.sort(fids);
			System.out.println(
					name + ":" + result.getId() + ","
							+ result.getCrunchedAncestorIds() + ":" + cids + ", foundIds:" + fids);
			foundIds.addAll(cids);
		}
	}
	
	
}
