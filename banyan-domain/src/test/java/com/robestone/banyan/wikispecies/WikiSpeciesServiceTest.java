package com.robestone.banyan.wikispecies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.banyan.taxons.AnalyzableTreeNode;
import com.robestone.banyan.taxons.Example;
import com.robestone.banyan.taxons.ExampleGroup;
import com.robestone.banyan.taxons.ExamplesService;
import com.robestone.banyan.workers.AbstractWorker;

import junit.framework.TestCase;

public class WikiSpeciesServiceTest extends TestCase {

	private WikiSpeciesService service;
	private ExamplesService examplesService;
	
	@Override
	protected void setUp() throws Exception {
		setUpSpeciesService();
	}
	private class Helper extends AbstractWorker {
		public WikiSpeciesService getWikiSpeciesService() {
			return super.getWikiSpeciesService();
		}
	}
	private void setUpSpeciesService() {
		Helper helper = new Helper();
		this.service = helper.getWikiSpeciesService();
		this.examplesService = helper.examplesService;
	}
	
	public void testExampleService() throws Exception {
		// this is a smoke test, to make sure I have the SQL right
		List<ExampleGroup> groups = examplesService.findExampleGroups();
		for (ExampleGroup group: groups) {
			List<Example> examples = group.getExamples();
			for (Example example: examples) {
				String cids = example.getCrunchedIds();
				Set<Integer> ids = EntryUtilities.CRUNCHER.toSet(cids);
				List<Entry> entries = service.findEntries(ids);
				assertTrue(!entries.isEmpty());
				for (Entry entry: entries) {
					assertTrue(entry.getLatinName() != null);
					System.out.println("testExampleService." + entry.getId() + ", " + entry.getLatinName() + ", " + entry.getCommonName());
				}
			}
		}
	}
	
	public void testEntityMapper() {
		String latin = "Varanus olivaceus";
		char code = 8217;
		Entry e = service.findEntryByLatinName(latin);
		String common = e.getCommonName();
		int pos = common.indexOf(code);
		assertEquals(4, pos);
	}
	public void test45293Fails() {
		Entry entry = service.findEntry(45293);
		String commonName = getShortenedRenderableCommonName(entry);
		assertEquals(entry.getCommonName(), commonName);
	}
//	public void test12629Filas() {
//		CompleteEntry entry = service.findEntry(12629);
//		
//	}
	/**
	 * Copied literally from tapestry project to test it.
	 */
	public static String getShortenedRenderableCommonName(Entry entry) {
		String commonName = entry.getCommonName();
		if (commonName == null) {
			return null;
		}
		if (entry.getCommonNames() != null) {
			commonName = entry.getCommonNames().get(0);
			if (entry.getCommonNames().size() > 1) {
				commonName = commonName + "...";
			}
		}
		return commonName;
	}

	
	public void testFindTree() {
		Set<Integer> ids = getMockIds();
		AnalyzableTreeNode root = service.findTreeForNodes(ids);
		assertNotNull(root);
		// TODO assert something
	}
	public Set<Integer> getMockIds() {
		Set<Integer> ids = new HashSet<Integer>();
		ids.add(15940);
		ids.add(15422);
		ids.add(12620);
		ids.add(12621);
		ids.add(12623);
		ids.add(12627);
		ids.add(12628);
//		ids.add(12629);
		ids.add(12630);
		ids.add(12631);
		ids.add(12632);
		ids.add(12650);
		ids.add(12677);
		ids.add(21825);
		return ids;
	}

}
