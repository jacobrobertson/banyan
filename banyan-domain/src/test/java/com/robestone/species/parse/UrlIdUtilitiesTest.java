package com.robestone.species.parse;

import com.robestone.species.Entry;
import com.robestone.species.UrlIdUtilities;

import junit.framework.TestCase;

public class UrlIdUtilitiesTest extends TestCase {

	public void testToUrlId() {
		doTestToId("a", "b", "a_b_1");
		doTestToId(null, "b", "b_1");
		doTestToId("a b", "c d", "a_b_c_d_1");
		doTestToId("a, b", "c � d", "a_b_c_d_1");
		doTestToId("a, b", "c %56 d", "a_b_c_56_d_1");
		doTestToId("a", "b (c) d", "a_b_c_d_1");
	}
	private void doTestToId(String common, String latin, String expectedUrlId) {
		Entry entry = new Entry(null, common, latin);
		entry.setId(1);
		String id = UrlIdUtilities.getUrlId(entry);
		assertEquals(expectedUrlId, id);
	}
	public void testToId() {
		doTestToId("1");
		doTestToId("_1");
		doTestToId("a_b_1");
		doTestToId("a_1");
		doTestToId("c_56_1");
		doTestToId("a_33_b_c_56_d_1");
	}
	private void doTestToId(String id) {
		Integer got = UrlIdUtilities.getIdFromUrlId(id);
		assertEquals(new Integer(1), got);
	}
	
}
