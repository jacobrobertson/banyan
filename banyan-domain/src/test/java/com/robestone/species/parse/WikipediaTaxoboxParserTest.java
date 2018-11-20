package com.robestone.species.parse;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.robestone.species.Rank;

import junit.framework.TestCase;

public class WikipediaTaxoboxParserTest extends TestCase {
	
	private WikipediaTaxoboxParser parser = new WikipediaTaxoboxParser();

	public void testCommonNameFromImage() {
		doTestCommonNameFromImage("Stactolaema leucotis", "White-eared Barbet", "White-eared Barbet (Stactolaema leucotis) eating fruit.jpg");
		doTestCommonNameFromImage("Forsterygion flavonigrum", "Yellow & black triplefin", "Forsterygion flavonigrum (Yellow & black triplefin).jpg");
	}
	private void doTestCommonNameFromImage(String latin, String expectCommon, String imageLine) {
		Taxobox box = new Taxobox();
		box.setImage(imageLine);
		box.setLatinName(latin);
		String foundCommon = parser.getCommonNameFromImageName(box);
		assertEquals(expectCommon, foundCommon);
	}

	public void testGhostSlug() {
		doTest("Ghost slug", "Ghost slug", "Selenochlamys ysbryda", "Ghost_Slug_adult.jpg", null, Rank.Species, "Selenochlamys", "Rowson & Symondson, 2008<ref>Rowson B. & Symondson O. C.: ''Selenochlamys ysbryda sp. nov. from Wales, UK: a Testacella-like slug new to western Europe (Stylommatophora: Trigonochlamydidae)''. Journal of Conchology, June 2008, Vol. 39, part 5,  537-552. ([http://www.conchsoc.org/resources/show-abstract-39.php?id=%20%20%20%20%20%20%20%20%20%20%20%20151 abstract])</ref>");
	}
	public void testBananaSlug() {
		doTest("Banana slug", "Banana slug", "Ariolimax", "Ariolimax_columbianus_9428.JPG", "Ariolimax columbianus", Rank.Genus, "Ariolimacinae", "[[Otto Andreas Lowson Mörch|Mörch]], 1859");
	}
	public void testVeronicellidae() {
		doTest("Veronicellidae", "leatherleaf slugs", "Veronicellidae", "Laevecaulis-2.jpg", "Laevicaulis alte", Rank.Familia, "Veronicelloidea", "[[Gray]], 1840");
	}
	public void testTrigonochlamydidae() {
		doTest("Trigonochlamydidae", null, "Trigonochlamydidae", "Ghost Slug adult.jpg", "Selenochlamys ysbryda", Rank.Familia, "Parmacelloidea", "");
	}
	public void testFrog() {
		doTest("Frog", "Frogs", "Anura", "Caerulea3 crop.jpg", "Litoria caerulea", Rank.Ordo, "Amphibia", "[[Blasius Merrem|Merrem]], 1820");
	}
	public void testVampire_Squid() {
		doTest("Vampire_Squid", "Vampire Squid", "Vampyroteuthis infernalis", "vampyroteuthis illustration.jpg", null, Rank.Species, "Vampyroteuthis", "[[Carl Chun|Chun]], 1903");
	}
	public void testAfricanBushElephant() {
		doTest("African Bush Elephant", "African Bush Elephant", "Loxodonta africana", "Elephant near ndutu.jpg", null, Rank.Species, "Loxodonta", "([[Johann Friedrich Blumenbach|Blumenbach]], 1797)");
	}
	public void testVaginulus() {
		doTest("Vaginulus", null, "Vaginulus", "Vaginulus occidentalis 002.jpg", "Vaginulus occidentalis", Rank.Genus, "Veronicellidae", "");
	}
	public void testArionfuscus() {
		doTest("Arion fuscus", null, "Arion fuscus", "Arion subfuscus.jpg", "Arion fuscus", Rank.Species, "Arion", "([[Otto Friedrich Müller|O. F. Müller]], [[1774]])");
	}
	public void testMeghimatium() {
		doTest("Meghimatium", null, "Meghimatium", "Meghimatium fruhstorferi.JPG", "Meghimatium fruhstorferi", Rank.Genus, "Philomycidae", "");
	}
	public void testEriocranioidea() {
		doTest("Eriocranioidea", null, "Eriocranioidea", "Eriocrania semipurpurella01.jpg", "Eriocrania semipurpurella", Rank.Familia, "Eriocranioidea", null);
	}
	public void testEmpidonax() {
		doTest("Empidonax", null, "Empidonax", "Empidonax traillii.jpg", "Empidonax traillii extimus", Rank.Genus, "Tyrannidae", "[[Jean Cabanis|Cabanis]] 1855</small>");
	}
	public void testSerpophaga() {
		doTest("Serpophaga", null, "Serpophaga", "TIQUITUIQUI DE BAÑADO Serpophaga nigricans.jpg", "Serpophaga nigricans", Rank.Genus, "Tyrannidae", null);
	}
	public void testCureti_siva() {
		doTest("Curetis siva", "Shiva's Sunbeam", "Curetis siva", null, null, Rank.Species, "Curetis", "Evans, 1954.");
	}
	// Rhinochimaeridae - | image = Harriotta raleighana (Narrownose chimaera).gif
	// Serinus scotops - Forest Canary (Serinus scotops) facing left, side view.jpg

	public void testRhinochimaeridae() {
		doTest("Rhinochimaeridae", "Chimaeridae", "Rhinochimaeridae", "Harriotta raleighana (Narrownose chimaera).gif", "Harriotta raleighana", Rank.Familia, "Chimaeriformes", "");
	}
	public void testSerinus_scotops() {
		doTest("Serinus scotops", "Forest Canary", "Serinus scotops", "Forest Canary (Serinus scotops) facing left, side view.jpg", null, Rank.Species, "Serinus", "([[Carl Jakob Sundevall|Sundevall]], 1850)");
	}
	public void testEchinocystis() {
		doTest("Echinocystis", "wild cucumber", "Echinocystis", "Echinocystis lobata.jpg", null, Rank.Genus, "Cyclantherinae", null);
	}
	
	public void tests() {
		doTest("Naticidae", "moon snails", "Naticidae", "hebraeus2.jpg", "Naticarius hebraeus", Rank.Familia, "Naticoidea", "[[Guilding]], 1834");
		doTest("Forsterygion", null, "Forsterygion", "Fosterygion flavonigrum (Yellow & black triplefin).jpg", "Forsterygion flavonigrum", Rank.Genus, "Tripterygiidae", "Whitley & Phillipps , 1939");
		doTest("Lampyris", "glowworms", "Lampyris", "Lampyris_noctiluca%2C_Nordisk_familjebok.png", "Lampyris noctiluca", Rank.Genus, "Lampyrini");
		doTest("Ascaris", "giant intestinal roundworms", "Ascaris", "Ascaris lumbricoides.jpeg", null/*depict*/, Rank.Genus, "Ascarididae");
//		doTest("filename", "common", "latin", "image", null/*depict*/, Rank.Genus, "parentLatin");
	}
	
	private void doTest(String name, 
			String commonName, String latinName, String image, String imageSpeciesDepicted,
			Rank rank, String parentLatinName) {
		doTest(name, commonName, latinName, image, imageSpeciesDepicted, rank, parentLatinName, null);
	}
	private void doTest(String name, 
			String commonName, String latinName, String image, String imageSpeciesDepicted,
			Rank rank, String parentLatinName, String binomialAuthorityRaw) {
		String page = getPage(name);
		Taxobox parsed = parser.parseHtmlPage(name, page);
		assertNotNull(parsed);
		assertEquals(commonName, parsed.getCommonName());
		assertEquals(latinName, parsed.getLatinName());
		assertEquals(image, parsed.getImage());
		assertEquals(imageSpeciesDepicted, parsed.getImageSpeciesDepicted());
		assertEquals(rank, parsed.getRank());
		assertEquals(parentLatinName, parsed.getParentLatinName());
		// no reason to test this - I'm not using it
//		assertEquals(binomialAuthorityRaw, parsed.getBinomialAuthorityRaw());
	}
	
	public static String getPage(String name) {
		InputStream in = WikiSpeciesParserTest.class.getResourceAsStream(
				"/com/robestone/species/wikipedia/" + name + ".html");
		String page;
		try {
			page = IOUtils.toString(in, "UTF-8");
		} catch (IOException e) {
			page = null;
		}
		return page;
	}
	
	public void testGetTaxoBoxString() {
		doTestGetTaxoBoxString(
				"abc {{Taxobox {{abc}} def {{xyz}} qq}} bb", 
				"{{abc}} def {{xyz}} qq");
		doTestGetTaxoBoxString(
				"abc {{Taxobox {{ {{abc}} def {{xyz}} qq}} bb }} ww", 
				"{{ {{abc}} def {{xyz}} qq}} bb");
		doTestGetTaxoBoxString(
				"{{Automatic Taxobox\r\n" + 
				"| name = Protoceratids\r\n" + 
				"| fossil_range = {{Fossil range|Middle Eocene|Early Pliocene|ref=<ref name=Prothero98>{{cite book |last=Prothero |first=D.R. |coauthors= |editor=Janis, C.M. |editor2=Scott, K.M. |editor3=Jacobs, L.L. |title=Evolution of Tertiary mammals of North America |edition= |year=1998 |publisher=Cambridge University Press |location=Cambridge |isbn=0-521-35519-2 |pages=431–438 |chapter=Protoceratidae }}</ref>}}\r\n" + 
				"| image = Synthetoceras_BW.jpg\r\n" + 
				"| image_caption = ''[[Synthetoceras]]''\r\n" + 
				"| image_upright = 0.7\r\n" + 
				"| taxon = Protoceratidae\r\n" + 
				"| authority =\r\n" + 
				"| subdivision_ranks = Subfamilies and Genera\r\n" + 
				"| subdivision =\r\n" + 
				"†Leptotragulinae (same as Protoceratid)\r\n" + 
				"* †''[[Heteromeryx]]''\r\n" + 
				"* †''[[Leptoreodon]]''\r\n" + 
				"* †''[[Leptotragulus]]''\r\n" + 
				"* †''[[Poabromylus]]''\r\n" + 
				"* †''[[Toromeryx]]''\r\n" + 
				"* †''[[Trigenicus]]''\r\n" + 
				"†[[Protoceratinae]]\r\n" + 
				"* †''[[Paratoceras]]''\r\n" + 
				"* †''[[Protoceras]]''\r\n" + 
				"* †''[[Pseudoprotoceras]]''\r\n" + 
				"†[[Synthetoceratinae]]\r\n" + 
				"* †''[[Kyptoceras]]''\r\n" + 
				"*''†[[Lambdoceras]]''\r\n" + 
				"* †''[[Prosynthetoceras]]''\r\n" + 
				"* †''[[Synthetoceras]]''\r\n" + 
				"* †''[[Syndyoceras]]''\r\n" + 
				"|range_map = Protoceratidae range.png\r\n" + 
				"|range_map_caption = Range of Protoceratidae based on fossil record.\r\n" + 
				"}}", 
				"| name = Protoceratids\r\n" + 
				"| fossil_range = {{Fossil range|Middle Eocene|Early Pliocene|ref=<ref name=Prothero98>{{cite book |last=Prothero |first=D.R. |coauthors= |editor=Janis, C.M. |editor2=Scott, K.M. |editor3=Jacobs, L.L. |title=Evolution of Tertiary mammals of North America |edition= |year=1998 |publisher=Cambridge University Press |location=Cambridge |isbn=0-521-35519-2 |pages=431–438 |chapter=Protoceratidae }}</ref>}}\r\n" + 
				"| image = Synthetoceras_BW.jpg\r\n" + 
				"| image_caption = ''[[Synthetoceras]]''\r\n" + 
				"| image_upright = 0.7\r\n" + 
				"| taxon = Protoceratidae\r\n" + 
				"| authority =\r\n" + 
				"| subdivision_ranks = Subfamilies and Genera\r\n" + 
				"| subdivision =\r\n" + 
				"†Leptotragulinae (same as Protoceratid)\r\n" + 
				"* †''[[Heteromeryx]]''\r\n" + 
				"* †''[[Leptoreodon]]''\r\n" + 
				"* †''[[Leptotragulus]]''\r\n" + 
				"* †''[[Poabromylus]]''\r\n" + 
				"* †''[[Toromeryx]]''\r\n" + 
				"* †''[[Trigenicus]]''\r\n" + 
				"†[[Protoceratinae]]\r\n" + 
				"* †''[[Paratoceras]]''\r\n" + 
				"* †''[[Protoceras]]''\r\n" + 
				"* †''[[Pseudoprotoceras]]''\r\n" + 
				"†[[Synthetoceratinae]]\r\n" + 
				"* †''[[Kyptoceras]]''\r\n" + 
				"*''†[[Lambdoceras]]''\r\n" + 
				"* †''[[Prosynthetoceras]]''\r\n" + 
				"* †''[[Synthetoceras]]''\r\n" + 
				"* †''[[Syndyoceras]]''\r\n" + 
				"|range_map = Protoceratidae range.png\r\n" + 
				"|range_map_caption = Range of Protoceratidae based on fossil record.");
	}
	private void doTestGetTaxoBoxString(String edit, String expectString) {
		String found = WikipediaTaxoboxParser.getTaxoboxString(edit);
		assertEquals(expectString, found);
	}

}
