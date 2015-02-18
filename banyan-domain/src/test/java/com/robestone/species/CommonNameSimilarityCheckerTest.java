package com.robestone.species;

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
	
}
