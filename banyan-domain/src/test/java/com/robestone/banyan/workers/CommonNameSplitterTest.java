package com.robestone.banyan.workers;

import java.util.List;

import com.robestone.banyan.taxons.NameInfo;
import com.robestone.banyan.wikispecies.Entry;

import junit.framework.TestCase;

public class CommonNameSplitterTest extends TestCase {

	private int setMaxKeepLength = 9;
	public void testNormalizeShortSplit() {
		Entry e = new Entry();
		e.setCommonName("funk; junk");
		e.setLatinName("Any old name");
		NameInfo name = new NameInfo(e.getCommonName(), e.getLatinName(), setMaxKeepLength);

		assertEquals("Funk, Junk", name.getCommonNameFixed());
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
		doTest("funk � junk", "Funk", "Junk");
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
		Entry entry = new Entry();
		entry.setCommonName(commonName);
		entry.setLatinName(latinName);
		doTest(entry, splits);
	}
	
	private void doTest(Entry entry, String... splits) {
		NameInfo name = new NameInfo(entry.getCommonName(), entry.getLatinName(), setMaxKeepLength);
		List<String> list = name.getCommonNames();
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
