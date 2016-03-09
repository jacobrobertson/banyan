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
		assertEquals(3, trees.size());
		
		assertEquals(7, trees.get(0).getRoot().getId().intValue());
		assertEquals(9, trees.get(1).getRoot().getId().intValue());
		assertEquals(5, trees.get(2).getRoot().getId().intValue());
		
	}
	private Tree createMockTree() {
		List<CompleteEntry> list = new ArrayList<CompleteEntry>();
		
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
	private CompleteEntry addEntry(List<CompleteEntry> list, int c, int p) {
		CompleteEntry e = new CompleteEntry();
		e.setId(c);
		e.setParentId(p);
		list.add(e);
		return e;
	}
}
