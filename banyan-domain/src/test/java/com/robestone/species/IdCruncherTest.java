package com.robestone.species;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.math.RandomUtils;

public class IdCruncherTest extends TestCase {

	private IdCruncher ic4 = 
		new IdCruncher("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", 4);

	private IdCruncher ic = IdCruncher.R62_3;

	public void testLongNumber() {
		assertCrunch(40179, "0as3");
		assertCrunch(235136, "0Zaw");
		assertCrunch(243792, "11q8");
		assertCrunch(2434792, "adoQ");
		assertCrunch(23513643, "1AEYj");
	}
	private void assertCrunch(int num, String crunch) {
		assertEquals(crunch, ic4.toString(num));
		assertEquals(num, ic4.toInt(crunch));
	}
	public void testList() {
		doTestList("3bg3bh3bn3bo3cz3R54uicWIiL6laYlTqoWZpmVq3BsGwuMnvR1vYQwc6wguwhzwiLwlkNsY", 24, 12230, 12231, 12237, 12238, 12311, 14823, 17254, 49768, 72112, 81404, 84160, 95913, 97521, 100167, 110268, 118319, 122451, 122936, 123758, 124030, 124097, 124171, 124330, 190152);
		doTestList("3bg3bh3bn3bo3czcWIpmVuMnwc6wguwlk", 11, 12230, 12231, 12237, 12238, 12311, 49768, 97521, 118319, 123758, 124030, 124330);
		doTestList("nC8nCunEdnEsnEGnFanFSnH5EtFEtG", 10, 90776, 90798, 90905, 90920, 90934, 90964, 91008, 91083, 155599, 155600);
		doTestList("012", 1, 64);
		doTestList("012as3", 2, 64, 40179);
		doTestList("012as3Zaw", 3, 64, 40179, 235136);
		doTestList("012as3Zaw_11q8adoQ", 5, 64, 40179, 235136, 243792, 2434792);
		doTestList("012as3Zaw_adoQ_1AEYj", 5, 64, 40179, 235136, 2434792, 23513643);
		doTestList("012as3Zaw_11q8adoQ_1AEYj", 6, 64, 40179, 235136, 243792, 2434792, 23513643);
		doTestList("_adoQ", 1, 2434792);
		doTestList("bA9KVr", 2, 44525, 180385);
	}
	private void doTestList(String s, int expectedLength, int... expectedVals) {
		List<Integer> l = ic.toList(s);
//		System.out.println(l);
		assertEquals(expectedLength, l.size());
		String s2 = ic.toString(l);
		List<Integer> l2 = ic.toList(s2);
		assertEquals(s, s2);
		assertEquals(l, l2);
		assertEquals(expectedLength, expectedVals.length);
		for (int i = 0; i < expectedVals.length; i++) {
			assertEquals(expectedVals[i], l2.get(i).intValue());
		}
		int delimPos = s.indexOf('-');
		assertEquals(-1, delimPos);
	}
	
	public void testAll() {
		int max = 200000;
		for (int i = 0; i < max; i++) {
			String s = ic.toString(i);
			int i2 = ic.toInt(s);
			String s2 = ic.toString(i2);
			assertEquals(s, s2);
			assertEquals(i, i2);
//			System.out.println(i + " > " + s);
		}
	}
	public void testToIntPadded() {
		doTestToIntPadded("001", 1);
		doTestToIntPadded("001", 1);
		doTestToIntPadded("001", 1);
		doTestToIntPadded("00B", 37);
		doTestToIntPadded("00B", 37);
		doTestToIntPadded("0xA", 2082);
		doTestToIntPadded("0xA", 2082);
		doTestToIntPadded("00xA", 2082);
	}
	private void doTestToIntPadded(String s, int expect) {
		int found = ic.toInt(s);
		assertEquals(found, expect);
		String reverse = ic.toString(found);
		assertTrue(s.endsWith(reverse));
	}
	public void testToString() {
		doTestToString(0, "000");
		doTestToString(1, "001");
		doTestToString(9, "009");
		doTestToString(10, "00a");
		doTestToString(35, "00z");
		doTestToString(36, "00A");
		doTestToString(38, "00C");
		doTestToString(61, "00Z");
		
		doTestToString(62, "010");
		doTestToString(232621, "YvX");
	}
	private void doTestToString(int n, String expect) {
		String found = ic.toString(n);
		assertEquals(expect, found);
		
		int reverse = ic.toInt(found);
		assertEquals(n, reverse);
	}
	public void testToChar() {
		doTestToChar(0, '0');
		doTestToChar(9, '9');
		doTestToChar(10, 'a');
		doTestToChar(35, 'z');
		doTestToChar(36, 'A');
		doTestToChar(38, 'C');
		doTestToChar(61, 'Z');
	}
	
	private void doTestToChar(int n, char expect) {
		char found = ic.toChar(n);
		assertEquals(expect, found);
		
		int reverse = ic.toInt(found);
		assertEquals(n, reverse);
	}
	
	public void testSub() {
		doTestSub(2110, 14816);
		doTestSub(64, 40179, 235136, 243792, 2434792, 23513643);
		doTestSub(163690, 180007, 180025, 207281);
		doTestSub(206840, 238328, 247369, 290647);
		doTestSub(147326, 186003, 253165, 253227, 275019);
		doTestSub(241405, 251494, 254423, 254429);
		doTestSub(24003, 44474, 46011, 46444, 47951, 55930, 91456, 95842, 113612, 126727, 133867, 133929, 155821, 163690, 180007, 180025, 207281, 220028, 228738, 235223, 241405, 251494, 254423, 254429);
		doTestSub(239479);
		doTestSub(279969, 182554, 53821, 240049, 280395, 284417, 217802, 80794, 270136, 125361, 214726, 208421, 138630, 43759, 210579, 278931, 154281, 189253, 154936, 173355, 275610, 187109, 103644, 207019, 51471, 265072, 180372, 110466, 71124, 275160, 270775, 50167, 180754, 247842, 216005, 66859, 148103, 205394);
		doTestSub(90776, 90798, 90905, 90920, 90934, 90964, 91008, 91083, 155599, 155600);
		doTestSub(64, 40179, 235136, 243792, 2434792, 23513643);
		doTestSub(100, 101, 102, 105, 109, 150);
		doTestSub(215136, 215139, 215146, 215636, 218136, 235136);
		doTestSub(112141, 112199, 112650, 112707, 112783, 112872, 112958, 112966, 112969, 113227, 116100, 116552, 117014, 117196, 118931);
		doTestSub(112141, 112199, 112650, 112707, 112783, 112872, 112958, 112966, 112969, 113227, 116100, 116552, 117014, 117196, 118931, 119839, 122456, 122680, 122682, 122706, 123589, 123598, 123766, 124031, 124099, 136557, 136560, 136599, 159389, 159391, 159392, 159467);
		doTestSub(11710, 11748, 11974, 11991, 12083, 14823, 45166, 45167, 45293, 45934, 84160, 90692, 90757, 91247, 91248, 91361, 91400, 112141, 112199);
		doTestSub(11710, 11748, 11974, 11991, 12083, 14823, 45166, 45167, 45293, 45934, 84160, 90692, 90757, 91247, 91248, 91361, 91400, 112141, 112199, 112650, 112707, 112783, 112872, 112958, 112966, 112969, 113227, 116100, 116552, 117014, 117196, 118931, 119839, 122456, 122680, 122682, 122706, 123589, 123598, 123766, 124031, 124099, 136557, 136560, 136599, 159389, 159391, 159392, 159467);
		
		// without rebasing this one only saves one char because near the end we're back to padding them even though the numbers are so close
		doTestSub(45166, 45167, 45293, 45934, 84160, 90692, 90757, 91247, 91248, 91361, 91400, 112141, 112199, 112650, 112707, 112783, 112872, 112958, 112966, 112969, 113227, 116100, 116552, 117014, 117196, 118931, 119839, 122456, 122680, 122682, 122706, 123589, 123598, 123766, 124031, 124099, 136557, 136560, 136599, 159389, 159391, 159392, 159467);
		
		// this example will show the rebasing in action
		doTestSub(1, 2, 3, 4, 5, 101, 102, 103, 104, 105, 10001, 10002, 10003, 10004, 10005, 100001, 100002, 100003, 100004, 100005, 10000001, 10000002, 10000003, 10000004, 10000005, 100000001, 100000002, 100000003, 100000004, 100000005);
		
		// shows how the multiplier should help
		doTestSub(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000);
	}
	private void doTestSub(Integer... nums) {
		List<Integer> col = Arrays.asList(nums);
		doTestSub(col);
	}
	private Score doTestSub(List<Integer> col) {
//		System.out.println(col.size() + ":" + col);
		long start = System.currentTimeMillis();
		String old = new IdCruncher(IdCruncher.R62_CHARS, 3).toString(col);
//		System.out.println(old);

		IdCruncher ids = IdCruncher.withSubtraction(IdCruncher.R62_CHARS);
		
		// show new algorithm is backwards compatible
		Collection<Integer> uncrunchValidate = ids.toList(old);
		assertEquals(uncrunchValidate.size(), col.size());
		uncrunchValidate.removeAll(col);
		assertEquals(0, uncrunchValidate.size());
		
		// actual crunch
		String nc = ids.toString(col);
//		System.out.println(nc);
		
		// validate it worked
		Collection<Integer> uncrunch = ids.toList(nc);
		assertEquals(col.size(), uncrunch.size());
		uncrunch.removeAll(col);
		assertEquals(0, uncrunch.size());
		
		long end = System.currentTimeMillis();
		long diff = end - start;
		int saved = old.length() - nc.length();
		Collections.sort(col);
		if ((saved == 1 && !old.equals(nc)) || saved > 30) {
			System.out.println("Saved " + (saved) + " chars in " + diff + " ms " + "\n\t" + col + "\n\t" + old + "\n\t"  + nc);
			showDiffs(col);
		}
//		System.out.println("--------------------------------------------------------------------------------------");
		Score score = new Score();
		score.saved = saved;
		score.total = old.length();
		return score;
	}
	private void showDiffs(List<Integer> ints) {
		int last = 0;
		System.out.print("\t");
		for (Integer i: ints) {
			int diff = i - last;
			last = i;
			System.out.print(diff);
			System.out.print(" > ");
		}
		System.out.println();
	}
	public void testRandom() {
		int savedCount = 0;
		int total = 10000;
		float totalChars = 0;
		float totalSaved = 0;
		for (int i = 0; i < total; i++) {
			Score score = doTestRandom();
			if (score.saved > 0) {
				savedCount++;
			}
			totalChars += score.total;
			totalSaved += score.saved;
		}
		float averageImprovement = (totalSaved / totalChars) * 100;
		System.out.println(savedCount + "/" + total + " improved, " + (total- savedCount) + "/" + total + " not improved, average improvement " + averageImprovement + "%");
	}
	private Score doTestRandom() {
		int min = 30000;
		min = RandomUtils.nextInt(min);
		int max = 250000 - min;
		int count = RandomUtils.nextInt(50) + 1;
		List<Integer> ints = new ArrayList<Integer>();
		for (int i = 0; i < count; i++) {
			int num = RandomUtils.nextInt(max) + min;
			ints.add(num);
		}
		Collections.sort(ints);
//		System.out.println(ints);
		return doTestSub(ints);
	}
	private class Score {
		int saved;
		int total;
	}
}
