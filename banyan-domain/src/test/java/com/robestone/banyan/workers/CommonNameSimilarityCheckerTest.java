package com.robestone.banyan.workers;

import com.robestone.banyan.wikispecies.EntryUtilities;

import junit.framework.TestCase;

public class CommonNameSimilarityCheckerTest extends TestCase {

	public void testMoreSpecificPairs() {
		doTestPair("Myomorpha", "Myodonta");
		doTestPair("Geomyoidea", "Geomorpha");
		doTestPair("Sciuromorpha", "Sciurognathi");
		doTestPair("Theria", "Theriiformes");
		doTestPair("Didelphimorphia", "Didelphidae");
		doTestPair("Macropodinae", "Macropus");
		doTestPair("Velociraptor", "Velociraptorinae");
		doTestPair("Hoolock gibbon", "Hoolock");
	}
	public void testGeneralPairs() {
		doTestPair("Animalia", "Animals");
		doTestPair("Grylloblattodea", "Grylloblattidae");
		doTestPair("Eriocranioidea", "Eriocraniidae");
		doTestPair("Actinomycetales", "Actinomycineae");
		doTestPair("Cyttariales", "Cyttariaceae");
		doTestPair("Enantiornithines", "Enantiornithes");
		doTestPair("Enantiornithines", "Enantiornithes");
		doTestPair("Metamonad", "Metamonada");
		doTestPair("Uropygid", "Uropygi");
		doTestPair("Excavate", "Excavata");
		doTestPair("Mesonychid", "Mesonychia");
		doTestPair("Tetrapods", "Tetrapoda");
		doTestPair("Condylarth", "Condylarthra");
		doTestPair("Eumetazoans", "Eumetazoa");
		doTestPair("Cynodont", "Cynodontia");
		doTestPair("Deuterostome", "Deuterostomia");
		doTestPair("Mammals", "Mammalia");
		doTestPair("Chordates", "Chordata");
		doTestPair("Kinetoplastid", "Kinetoplastea");
		doTestPair("Laurasiatheres", "Laurasiatheria");
		doTestPair("Parabasalid", "Parabasalia");
		doTestPair("Galago", "Galagidae");
		doTestPair("Kollikodon", "Kollikodontidae"); 	
		doTestPair("Kinetoplastid", "Kinetoplastea");
		doTestPair("Alligators", "Alligatorinae");
		doTestPair("Dichaea", "Dichaeinae");
	}
	private void doTestPair(String c, String l) {
		
		boolean boring = CommonNameSimilarityChecker.isCommonNameCleanBoring(
				EntryUtilities.getClean(c, false),
				EntryUtilities.getClean(l, false)
				);
		assertTrue(boring);
	}
	
	public void testPlural() {
		 doTestPlural("smoke", "test", false);
		 doTestPlural("test", "test", false);
		 doTestPlural("tests", "test", true);
		 doTestPlural("testess", "test", false);
		 doTestPlural("testies", "test", false);
		 doTestPlural("testies", "testy", true);
		 doTestPlural("testesses", "testess", true);
	}
	
	private void doTestPlural(String first, String second, boolean expect) {
		boolean found = CommonNameSimilarityChecker.isFirstPluralOfSecond(first, second);
		assertTrue(first + "/" + second + "/" + expect, found == expect);
	}
	
	public void testMorphaComprehension() {
		doTestMorphaComprehension("lionesses", "lioness");
		doTestMorphaComprehension("lionesser", "lionesser");
		doTestMorphaComprehension("lion", "lion");
		doTestMorphaComprehension("lions", "lion");
		doTestMorphaComprehension("flies", "fly");
	}
	private void doTestMorphaComprehension(String pluralWord, String stemmed) {
		String found = edu.washington.cs.knowitall.morpha.MorphaStemmer.stem(pluralWord);
		assertEquals(stemmed, found);
	}
	
}
