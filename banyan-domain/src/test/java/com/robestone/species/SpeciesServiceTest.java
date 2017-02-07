package com.robestone.species;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpeciesServiceTest extends TestCase {

	private SpeciesService service;
	private ExamplesService examplesService;
	
	@Override
	protected void setUp() throws Exception {
		setUpSpeciesService();
	}
	private void setUpSpeciesService() {
		String path = "com/robestone/species/parse/SpeciesServices.spring.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		service = (SpeciesService) context.getBean("SpeciesService");
		service.setSearcher(new SqlSearcher(service));
		
		examplesService = (ExamplesService) context.getBean("ExamplesService");
	}
	
	public void testExampleService() throws Exception {
		// this is a smoke test, to make sure I have the SQL right
		List<ExampleGroup> groups = examplesService.findExampleGroups();
		for (ExampleGroup group: groups) {
			List<Example> examples = group.getExamples();
			for (Example example: examples) {
				String cids = example.getCrunchedIds();
				Set<Integer> ids = EntryUtilities.CRUNCHER.toSet(cids);
				List<CompleteEntry> entries = service.findEntries(ids);
				assertTrue(!entries.isEmpty());
				for (CompleteEntry entry: entries) {
					assertTrue(entry.getLatinName() != null);
					System.out.println("testExampleService." + entry.getId() + ", " + entry.getLatinName() + ", " + entry.getCommonName());
				}
			}
		}
	}
	
	public void testEntityMapper() {
		String latin = "Varanus (Philippinosaurus) olivaceus";
		char code = 8217;
		Entry e = service.findEntryByLatinName(latin);
		String common = e.getCommonName();
		int pos = common.indexOf(code);
		assertEquals(4, pos);
	}
	/**
	 * Shows that after X number of searches, each new id is still unique.
	 */
	public void testSearchWhereNotIn() {
		Set<Integer> ids = new HashSet<Integer>();
		int count = 10;
		for (int i = 0; i < count; i++) {
			int id = service.findBestId("owl", ids);
			boolean added = ids.add(id);
			assertTrue(added);
		}
	}
	public void testCache() {
		int id = service.findBestId("owl", new HashSet<Integer>());
		CompleteEntry found1 = service.findEntry(id);
		CompleteEntry found2 = service.findEntry(id);
		
		EntryProperties p1 = found1.getEntryProperties();
		EntryProperties p2 = found2.getEntryProperties();
		
		assertSame(p1, p2);
		
		service.clearCache();
		CompleteEntry found3 = service.findEntry(id);
		EntryProperties p3 = found3.getEntryProperties();
		assertTrue(p3 != p1);
	}
	public void testFindOwls() {
		int id = service.findBestId("owl", new HashSet<Integer>());
		Entry found = service.findEntry(id);
		assertEquals("Owl", found.getCommonName());
	}
	public void test45293Fails() {
		CompleteEntry entry = service.findEntry(45293);
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
		Entry root = service.findTreeForNodes(ids);
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

	public void testCucurbitoideae() {
		String name = "Cucurbitoideae";
		Entry found = service.findEntryByLatinName(name);
		assertEquals(name, found.getLatinName());
		
		int id = service.findBestId(name, new HashSet<Integer>());
		assertEquals(id, found.getId().intValue());
	}
	
	/**
	 * Keeps failing due to changing data...
	 */
	public void ztestCucurbitales() {
		String name = "Cucurbitales";
		Entry found = service.findEntryByLatinName(name);
		
		int persistedChildren = 6;
		int loadedChildren = 3;
		
		assertEquals(persistedChildren, found.getPersistedChildCount());

		Collection<Integer> cids = service.findChildrenIds(found.getId());
		assertEquals(loadedChildren, cids.size());
		
		Set<Integer> ids = new HashSet<Integer>(cids);
		CompleteEntry root = service.findTreeForNodes(ids);
		Tree tree = EntryUtilities.buildTree(root);
		CompleteEntry cuc = tree.get(found.getId());
		assertEquals(loadedChildren, cuc.getChildren().size());
	}
	public void zzztestCucumbers() {
		assertEquals("Cucumerinae", "Cucumerinae");
		String[] names = 
			{
				"Plantae",
				"Magnoliophyta", 
				"Magnoliopsida", 
				"Cucurbitales", 
				"Cucurbitaceae", 
				"Cucurbitoideae",
				"Melothrieae", 
				"Cucumerinae", 
				"Cucumis",
				"Cucumis sativus",
				};

		String name = names[names.length - 1];
		int id = service.findBestId(name, new HashSet<Integer>());
		Set<Integer> ids = new HashSet<Integer>();
		ids.add(id);
		CompleteEntry entry = service.findTreeForNodes(ids);
		
		Tree tree = EntryUtilities.buildTree(entry);
		
		CompleteEntry found = tree.get(id);
		
		assertEquals(id, found.getId().intValue());
		
		// check the parents are what I expect
		for (int i = names.length - 1; i >= 0; i--) {
			assertEquals(names[i], found.getLatinName());
			found = found.getParent();
			System.out.println(names[i]);
		}
	}
	
	public void testFindBestId() {
		doTestFindBestId("canidae", true);
		doTestFindBestId("fox", true);
		doTestFindBestId("Dormouse", true);
		doTestFindBestId("rat", true);
		doTestFindBestId("fluffy", true);
	}
	private void doTestFindBestId(String search, boolean expect) {
		int found = service.findBestId(search, new HashSet<Integer>());
		assertEquals(expect, found > 0);
	}
	
}
