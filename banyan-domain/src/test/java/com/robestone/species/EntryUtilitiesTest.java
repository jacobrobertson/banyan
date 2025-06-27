package com.robestone.species;

import java.util.ArrayList;
import java.util.List;

import com.robestone.util.html.EntityMapper;

import junit.framework.TestCase;

public class EntryUtilitiesTest extends TestCase {

	public void testCleanWithSymbols() {
		doTestCleanWithSymbols("funk[465]y", "FUNKOY", false);
		doTestCleanWithSymbols("funk [465] y", "FUNKOY", true);
		doTestCleanWithSymbols("funk [465]y", "FUNK OY", false);
		doTestCleanWithSymbols("funk [4655]y", "FUNK Y", false);
	}
	private void doTestCleanWithSymbols(String toTest, String expect, boolean cleanest) {
		String latin = EntityMapper.convertToSymbolsText(toTest, true);
		String clean = EntryUtilities.getClean(latin, cleanest);
		assertEquals(expect, clean);
	}
	public void testTreeReport() {
		
		Tree t = createMockTree();
		
		List<Tree> trees = EntryUtilities.findDisconnectedTrees(t);
		assertEquals(4, trees.size());
		
		assertEquals(7, trees.get(0).getRoot().getId().intValue());
		assertEquals(0, trees.get(1).getRoot().getId().intValue());
		assertEquals(9, trees.get(2).getRoot().getId().intValue());
		assertEquals(5, trees.get(3).getRoot().getId().intValue());
		
	}
	private Tree createMockTree() {
		List<Entry> list = new ArrayList<Entry>();
		
		addEntry(list, 0, 100);
		addEntry(list, 1, 0);
		addEntry(list, 2, 0);
		addEntry(list, 3, 1);
		addEntry(list, 4, 2);

		addEntry(list, 5, 800);
		addEntry(list, 6, 5);
		
		addEntry(list, 7, 801);
		addEntry(list, 8, 7);
		addEntry(list, 80, 7);
		addEntry(list, 81, 8);
		addEntry(list, 82, 8);
		addEntry(list, 83, 80);
		
		addEntry(list, 9, 802);
		addEntry(list, 10, 9);
		addEntry(list, 11, 10);
		
		return EntryUtilities.buildTree(list);
	}
	private Entry addEntry(List<Entry> list, int c, int p) {
		Entry e = new Entry();
		e.setId(c);
		e.setLatinName("latinName" + c);
		e.setParentId(p);
		e.setParentLatinName("latinName" + p);
		list.add(e);
		return e;
	}
	
	public void testCollapseListToOne() {
		List<Entry> list = createList("L0", null, "L1", null, "L2", null, "L3", null, "L4", null);
		
		Entry e = EntryUtilities.collapseListToOne(list);
		assertEquals(4, e.getCollapsedCount());
		
		assertEquals("L0", e.getLatinName());
		
		((Entry)list.get(3)).setCommonName("L3");
		e = EntryUtilities.collapseListToOne(list);
		assertEquals("L3", e.getLatinName());
		
		((Entry)list.get(4)).setCommonName("L4");
		e = EntryUtilities.collapseListToOne(list);
		assertEquals("L3", e.getLatinName());
		
		((Entry)list.get(4)).setCommonName("z L 4 a");
		e = EntryUtilities.collapseListToOne(list);
		assertEquals("L4", e.getLatinName());
	}
	
	public void testCollapseList() {
		List<Entry> list = createList("L0", null, "L1", null, "L2", null, "L3", null, "L4", null);
		List<Entry> collapsed = EntryUtilities.collapseList(list);
		assertCollapsedList(collapsed, 0, 2, 0);
	}
	
	private void assertCollapsedList(List<Entry> list, int... collapsedCount) {
		assertEquals(collapsedCount.length, list.size());
		for (int i = 0; i < collapsedCount.length; i++) {
			assertEquals(collapsedCount[i], list.get(i).getCollapsedCount());
		}
	}
	private List<Entry> createList(String... names) {
		List<Entry> entries = new ArrayList<Entry>();
		for (int i = 0; i < names.length; i += 2) {
			String latinName = names[i];
			String commonName = names[i + 1];
			Entry e = new Entry();
			e.setCommonName(commonName);
			e.setLatinName(latinName);
			entries.add(e);
		}
		
		return entries;
	}
	
}
