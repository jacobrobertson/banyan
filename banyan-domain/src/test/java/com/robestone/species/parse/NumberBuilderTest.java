package com.robestone.species.parse;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.robestone.species.EntryUtilities;

import junit.framework.TestCase;

public class NumberBuilderTest extends TestCase {

	private Random rand = new Random();
	
	public void testSubtractionCompression() {
		NumberListCruncher cruncher = new NumberListSubtractionCruncher(3);
		doTestSubtractionCompression(10,				99, cruncher);
		doTestSubtractionCompression(100,				999, cruncher);
		doTestSubtractionCompression(1000,				9999, cruncher);
		doTestSubtractionCompression(10000,				99999, cruncher);
		doTestSubtractionCompression(100000,			999999, cruncher);
		doTestSubtractionCompression(1000000,			9999999, cruncher);
		doTestSubtractionCompression(10000000,			99999999, cruncher);
		doTestSubtractionCompression(100000000,			999999999, cruncher);
		doTestSubtractionCompression(1000,				999999999, cruncher);
	}
	private void doTestSubtractionCompression(int min, int max, NumberListCruncher cruncher) {
		doTestSubtractionCompression2(0, max, cruncher);
		doTestSubtractionCompression2(min, max, cruncher);
	}
	private void doTestSubtractionCompression2(int min, int max, NumberListCruncher cruncher) {
		doTestSubtractionCompression(min, max, 5, cruncher);
		doTestSubtractionCompression(min, max, 10, cruncher);
		doTestSubtractionCompression(min, max, 20, cruncher);
		doTestSubtractionCompression(min, max, 40, cruncher);
		System.out.println("----");
	}
	// would prefer to do with biginteger, but for now getting a feeling for whether it's worth it to try longer numbers
	private void doTestSubtractionCompression(int min, int max, int listSize, NumberListCruncher cruncher) {
		int iterations = 10000;
		
		int rawLength = String.valueOf(max).length() * listSize;
		float totalCrunchedLength = 0;
		
		for (int i = 0; i < iterations; i++) {
			List<Integer> list = getList(min, max, listSize);
			String crunched = cruncher.toString(list);
			int crunchedLength = crunched.length();
			totalCrunchedLength += crunchedLength;
		}
		
		float averageCrunchedLength = totalCrunchedLength / iterations;
		float compressionPercent = (averageCrunchedLength / rawLength) * 100f;
		System.out.printf("Size: %d, Min: %d, Max: %d, Raw: %d, Ave: %.0f, Compression: %.2f%%", 
				listSize, min, max, rawLength, averageCrunchedLength, compressionPercent);
		System.out.println();
	}
	
	public void testNumberListCompoundCruncher() {
		doTestNumberListCompoundCruncher(400000);
//		doTestNumberListCompoundCruncher(Integer.MAX_VALUE);
	}
	private List<Integer> getList(int min, int max, int size) {
		int randMax = max - min;
		List<Integer> numbers = new ArrayList<Integer>();
		for (int j = 0; j < size; j++) {
			numbers.add(rand.nextInt(randMax) + min);
		}
		return numbers;
	}
	public void doTestNumberListCompoundCruncher(int maxNum) {
		NumberListCompoundCruncher cruncher = new NumberListCompoundCruncher();
		
		for (int i = 0; i < 100000; i++) {
			List<Integer> numbers = new ArrayList<Integer>();
			int numCount = rand.nextInt(20) + 1;
			for (int j = 0; j < numCount; j++) {
				numbers.add(rand.nextInt(maxNum));
			}
			
			String crunched = cruncher.toString(numbers);
			List<Integer> after = cruncher.toList(crunched);
			
			Collections.sort(numbers);
			Collections.sort(after);
			assertEquals(numbers.size(), after.size());
			for (int j = 0; j < numbers.size(); j++) {
				assertEquals(numbers.get(j), after.get(j));
			}
			String currentCruncherResults = EntryUtilities.CRUNCHER.toString(numbers);
			if (currentCruncherResults.length() > crunched.length() + 3) {
				System.out.println(crunched);
				System.out.println(currentCruncherResults);
				System.out.println(numbers);
				System.out.println("================================================================");
			}
		}
	}
	
	public void testNumberListSubtractionCruncher() {
		for (int q = 1; q <= 10; q++) {
			NumberListSubtractionCruncher cruncher = new NumberListSubtractionCruncher(q);
			Random rand = new Random();
			
			for (int i = 0; i < 10000; i++) {
				List<Integer> numbers = new ArrayList<Integer>();
				int numCount = rand.nextInt(20);
				for (int j = 0; j < numCount; j++) {
					numbers.add(rand.nextInt(Integer.MAX_VALUE));
				}
				
				String crunched = cruncher.toString(numbers);
				List<Integer> after = cruncher.toList(crunched);
				
				Collections.sort(numbers);
				Collections.sort(after);
				assertEquals(numbers.size(), after.size());
				for (int j = 0; j < numbers.size(); j++) {
					assertEquals(numbers.get(j), after.get(j));
				}
//				System.out.println(crunched + " > " + numbers);
			}
		}
	}
	
	public void testBigIntegerComprehension() {
		for (int i = 0; i < 10000; i++) {
			doTestBigIntegerComprehension();
		}
	}
	private void doTestBigIntegerComprehension() {
		Random rand = new Random();
		BigInteger b1 = BigInteger.valueOf(rand.nextLong());
		BigInteger b2 = BigInteger.valueOf(rand.nextLong());
		
		BigInteger[] two = b1.divideAndRemainder(b2);
		BigInteger top = b1.divide(b2);
		assertEquals(two[0].toString(), top.toString());
	}
	
	public void testWorking() {
		assertWorking("1");
		assertWorking("17");
		assertWorking("817");
		assertWorking("2123");
		assertWorking("43123");
		assertWorking("943123");
		assertWorking("6943123");
		assertWorking("56943123");
		assertWorking("956943123");
		assertWorking("1956943123");
		assertWorking("84989347534");
		assertWorking("184989347534");
		assertWorking("1084989347534");
		assertWorking("31084989347534");
		assertWorking("831084989347534");
		assertWorking("5831084989347534");
		assertWorking("80831084989347534");
		assertWorking("380831084989347534");
		assertWorking("21080831084989347534");
		assertWorking("231080831084989347534");
		assertWorking("2341080831084989347534");
		assertWorking("52341080831084989347534");
		assertWorking("852341080831084989347534");
		assertWorking("852341080831084989347534852341080831084989347534852341080831084989347534852341080831084989347534");
	}
	
	private void assertWorking(String num) {
		BigInteger bi = new BigInteger(num);
		String b62 = NumberBuilder.R62.toString(bi);
		BigInteger found = NumberBuilder.R62.toBigInteger(b62);
		assertEquals(bi.toString(), found.toString());
//		System.out.println(num + " > " + b62);
	}
	
}
