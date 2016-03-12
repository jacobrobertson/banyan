package com.robestone.species;

import java.util.List;

import junit.framework.TestCase;

public class CommonNameSplitterTest extends TestCase {

	private CommonNameSplitter splitter = new CommonNameSplitter();
	@Override
	protected void setUp() throws Exception {
		splitter.setMaxKeepLength(9);
	}
	public void testNormalizeShortSplit() {
		CommonNameSplitter splitter = new CommonNameSplitter();
		CompleteEntry e = new CompleteEntry();
		e.setCommonName("funk; junk");
		e.setLatinName("Any old name");
		splitter.assignCommonNames(e);
		assertEquals("Funk, Junk", e.getCommonName());
	}

	public void testSplitCommonName() {
		doTest("funk");
		doTest("funk, junk", "Funk", "Junk");
		doTest("funk; junk", "Funk", "Junk");
		doTest("red or blue tick");
		doTest("funk thing or junk", "Funk thing", "Junk");
		doTest("funk, or junk", "Funk", "Junk");
		doTest("funk, and junk", "Funk", "Junk");
		doTest("funk and junk");
		doTest("funk, junk; hunk / trunk man or sunk", "Funk", "Junk", "Hunk", "Trunk man", "Sunk");
		doTest("Indian beet., Old maid's bonnets, and Sundial lupine", "Indian beet", "Old maid's bonnets", "Sundial lupine");
	}
	public void testGetCommonNames() {
		doTestEntry("Monotremes/Egg-laying mammals", "Monotremata", "Egg-laying mammals");
		doTestEntry("Tooth-nosed Snout Beetles / Rhynchitids", "Rhynchitinae", "Tooth-nosed Snout Beetles");
		doTestEntry("Solifuges, Camel spiders, Sun spiders, Wind scorpions", "Solifugae", "Camel spiders", "Sun spiders", "Wind scorpions"); 
	}
	
	private void doTest(String toSplit, String... splits) {
		doTestEntry(toSplit, "test-name", splits);
	}
	private void doTestEntry(String commonName, String latinName, String... splits) {
		CompleteEntry entry = new CompleteEntry();
		entry.setCommonName(commonName);
		entry.setLatinName(latinName);
		doTest(entry, splits);
	}
	
	private void doTest(Entry entry, String... splits) {
		List<String> list = splitter.splitCommonName(entry);
		if (splits.length == 0) {
			assertNull(list);
		} else {
			assertNotNull(list);
			assertEquals(splits.length, list.size());
			for (int i = 0; i < splits.length; i++) {
				String found = list.get(i);
				assertEquals(splits[i], found);
			}
		}
	}
	
}
