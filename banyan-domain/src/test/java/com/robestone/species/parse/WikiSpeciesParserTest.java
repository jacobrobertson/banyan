package com.robestone.species.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.Rank;

public class WikiSpeciesParserTest extends TestCase {
	WikiSpeciesParser parser = new WikiSpeciesParser();

	public void testSanity() {
		String s = "\\p{L}+";
		Pattern p = Pattern.compile(s);
		assertTrue(p.matcher("Aïstopoda").matches());
	}
	public void testParentPattern() {
		String w = "[\\p{L}\\(\\)_ ']+";
		doTestPattern(w, "Unassigned_(Gammaridea)");
		doTestPattern(w, "Unassigned (Gammaridea)");

		doTestPattern(
				"</a>(?:</i>)?(?:<br />|[`\\s]|</p>|<dd>|</dd>|</dl>|<dl>|<p>)*Familia:",
				"</a></dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>````</dl>``</dd>``</dl>``</dd>``</dl>``<p>Familia:");
		
		String text = ": <a href=\"/wiki/Unassigned_(Gammaridea)\" title=\"Unassigned (Gammaridea)\">Unassigned</a></dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>``</dl>``</dd>````</dl>``</dd>``</dl>``</dd>``</dl>``<p>Familia:";
		String pattern =
				":\\s*(?:<i>)?\\s*<a href=\"/wiki/(" + w + ")\" title=\"(" + w + ")\">(" + w + ")</a>(?:</i>)?(?:<br />|[`\\s]|</p>|<dd>|</dd>|<dl>|</dl>|<p>)*" + 
				"Familia:";
		doTestPattern(pattern, text);
	}
	
	private void doTestPattern(String pattern, String match) {
		Pattern cPattern = Pattern.compile(pattern);
		assertTrue(cPattern.matcher(match).matches());
	}
	
	public void testPattern() {
		Pattern p = Pattern.compile("A(\\p{L})B\\$1C");
		assertTrue(p.matcher("AEB$1C").matches());
	}
	
	// failing
	public void ztestPoxviridae() throws IOException {
		doTest("Poxviridae", null, "Scorpaenidae", null, Rank.Genus);
	}
	
	public void testIsoptera_incertae_sedis() throws IOException {
		doTest("Isoptera incertae sedis", null, "Isoptera", null, Rank.Cladus);
	}
	public void testMastotermitidae() throws IOException {
		doTest("Mastotermitidae", null, "Isoptera incertae sedis", null, Rank.Familia);
	}
	public void testMastotermes() throws IOException {
		doTest("Mastotermes", "Giant Northern Termite", "Mastotermitidae", "thumb/a/a3/Mastotermes_darwiniensis.jpg/250px-Mastotermes_darwiniensis.jpg", Rank.Genus);
	}
	public void testMastotermes_darwiniensis() throws IOException {
		doTest("Mastotermes darwiniensis", "Giant Northern Termite", "Mastotermes", "thumb/a/a3/Mastotermes_darwiniensis.jpg/250px-Mastotermes_darwiniensis.jpg", Rank.Species);
	}
	public void testStrigiformes() throws IOException {
		doTest("Strigiformes", "Owls", "Neognathae", "thumb/d/d6/Long_ear_owl_shandong.jpg/220px-Long_ear_owl_shandong.jpg", Rank.Ordo, false, null, "Asio otus");
	}
	public void testNerodia_fasciata() throws IOException {
		doTest("Nerodia fasciata", "Southern Water Snakes", "Nerodia", "thumb/0/04/Nerodia_fasciata_CDC.png/250px-Nerodia_fasciata_CDC.png", Rank.Species, false);
	}
	public void testHylobates_pileatus() throws IOException {
		doTest("Hylobates pileatus", "Pileated Gibbon", "Hylobates", "thumb/7/7d/Pileated_Gibbon_%28Hylobates_pileatus%29.jpg/220px-Pileated_Gibbon_%28Hylobates_pileatus%29.jpg", Rank.Species, false);
	}
	public void testGalagogranti() throws IOException {
		doTest("Galago granti", "Grant's Bushbaby", "Galago", null, Rank.Species, false);
	}
	public void testAcantharchus() throws IOException {
		doTest("Acantharchus", "Mud sunfish", "Centrarchidae", "thumb/9/93/Acantharchus_pomotis.jpg/250px-Acantharchus_pomotis.jpg", Rank.Genus, false, null, "Acantharchus pomotis");
	}
	public void testDelphinoidea() throws IOException {
		doTest("Delphinoidea", null, "Odontoceti", null, Rank.Superfamilia, false);
	}
	// I'm changing my strategy for "redirected"
	public void ztestAvialae() throws IOException {
		doTest("Avialae", null, null, null, null);
	}
	public void testDismorphia_theucharila() throws IOException {
		doTest("Dismorphia theucharila", null, "Dismorphia", null, Rank.Species);
	}
	public void testAlnus_glutinosa_subsp_barbata() throws IOException {
		doTest("Alnus glutinosa subsp. barbata", null, "Alnus glutinosa", "thumb/b/b8/Sakall%C4%B1_k%C4%B1z%C4%B1la%C4%9Fa%C3%A7-yaprak.JPG/245px-Sakall%C4%B1_k%C4%B1z%C4%B1la%C4%9Fa%C3%A7-yaprak.JPG", Rank.Subspecies);
	}
	public void testMastigoteuthis_flammea() throws IOException {
		doTest("Mastigoteuthis flammea", null, "Mastigoteuthis", "thumb/e/e4/Mastigoteuthis_flammea.jpg/250px-Mastigoteuthis_flammea.jpg", Rank.Species);
	}
	public void testCucurbita_argyrosperma_subsp_argyrosperma_var_mixta() throws IOException {
		doTest("Cucurbita argyrosperma subsp. argyrosperma var. mixta", null, "Cucurbita argyrosperma subsp. argyrosperma", null, Rank.Varietas);
	}
	public void testAcropolitis_magnana() throws IOException {
		doTest("Acropolitis magnana", null, "Acropolitis", null, Rank.Species);
	}
	public void testSurniinae() throws IOException {
		doTest("Surniinae", null, "Strigidae", "thumb/1/17/Aegolius-funereus-001.jpg/220px-Aegolius-funereus-001.jpg", Rank.Subfamilia, false, null, "Aegolius funereus");
	}
	public void testPelecanus() throws IOException {
		doTest("Pelecanus", "Pelicans", "Pelecanidae", "thumb/6/64/Lebski-pelikan.JPG/250px-Lebski-pelikan.JPG", Rank.Genus);
	}
	public void testDendrochirus() throws IOException {
		doTest("Dendrochirus", null, "Scorpaenidae", null, Rank.Genus);
	}
	public void testCarabus_Tachypus() throws IOException {
		doTest("Carabus (Tachypus)", null, "Carabus subdiv. Latitarsi", "thumb/9/95/Carabus_auratus_with_prey.jpg/225px-Carabus_auratus_with_prey.jpg", Rank.Subgenus, false, null, "Carabus");
	}
	public void testMastigoteuthidae() throws IOException {
		doTest("Mastigoteuthidae", "Whip-lash Squid", "Oegopsina", null, Rank.Familia, false);
	}
	public void testLumbrineridae() throws IOException {
		doTest("Lumbrineridae", null, "Eunicida", null, Rank.Familia, false);
	}
	public void testMotacillagrandis()  throws IOException {
		doTest("Motacilla grandis", "Japanese Wagtail", "Motacilla", "thumb/1/1b/Segurosekirei_06f7982v.jpg/250px-Segurosekirei_06f7982v.jpg", Rank.Species, false);
	}
	public void testParagleneafortunei() throws IOException {
		doTest("Paraglenea fortunei", null, "Paraglenea", null, Rank.Species, false);
	}
	public void testSelkirkiacolumbia() throws IOException {
		doTest("Selkirkia columbia", null, "Selkirkia", null, Rank.Species, true);
	}
	public void testValettidae() throws IOException {
		doTest("Valettidae", null, "Unassigned (Gammaridea)", null, Rank.Familia);
	}
	public void testBeaver() throws IOException {
		doTest("Aplodontia rufa", "Mountain Beaver", "Aplodontia", "3/3c/Aplodontia.jpg", Rank.Species);
	}
	public void testCoelurosauria() throws IOException {
		doTest("Coelurosauria", null, "Avatherapoda", null, Rank.Taxon, true);
	}
	public void testStertomys() throws IOException {
		doTest("Stertomys", null, "Glirinae", null, Rank.Genus, true);
	}
	public void testEurybiasaxicastellii() throws IOException {
		doTest("Eurybia saxicastellii", null, "Eurybia (Asteraceae)", null, Rank.Species, false);
	}
	public void testTheria() throws IOException {
		doTest("Theria", null, "Mammalia", "thumb/7/7b/Lagothrix_lagotricha.jpg/250px-Lagothrix_lagotricha.jpg", Rank.Subclassis, false, null, "Lagothrix lagotricha");
	}
	public void testSus_cebifrons() throws IOException {
		doTest("Sus cebifrons", "Visayan Warty Pig", "Sus", null, Rank.Species);
	}
	public void testScoloura() throws IOException {
		doTest("Scoloura", null, "Incertae sedis (Paratanoidea)", null, Rank.Genus);
	}
	public void testAnisoptera() throws IOException {
		doTest("Anisoptera", "Dragonfly", "Odonata", "thumb/0/03/Sympetrum_flaveolum_-_side_%28aka%29.jpg/250px-Sympetrum_flaveolum_-_side_%28aka%29.jpg", Rank.Subordo, false, null, "Sympetrum flaveolum");
	}
	public void testNothobranchius() throws IOException {
		doTest("Nothobranchius", null, "Nothobranchiidae", "7/72/Nothobranchius_rachovii_male.jpg", Rank.Genus, false, null, "Nothobranchius rachovii");
	}
	public void testCtenophora() throws IOException {
		doTest("Ctenophora", "Comb jellies", "Eumetazoa (incertae sedis)", "thumb/2/2f/Mertensia_ovum.jpg/250px-Mertensia_ovum.jpg", Rank.Phylum, false, null, "Mertensia ovum");
	}
	public void testGracilentulus() throws IOException {
		doTest("Gracilentulus", null, "Berberentulinae", null, Rank.Genus, false, Rank.Subfamilia, null);
	}
	public void testCuspirostrisornis() throws IOException {
		doTest("Cuspirostrisornis", null, "Cuspirostrisornithidae", null, Rank.Genus, true);
	}
	public void testXenoturbellidae() throws IOException {
		doTest("Xenoturbellidae", null, "Xenoturbellida", null, Rank.Familia);
	}
	public void testSceloglaux() throws IOException {
		doTest("Sceloglaux", null, "Surniinae", null, Rank.Genus);
	}
	public void testCapsicum() throws IOException {
		doTest("Capsicum", null, "Solanoideae", "thumb/9/95/Capsicum_annuum_Bluete.jpg/250px-Capsicum_annuum_Bluete.jpg", Rank.Genus, false, null, "Capsicum annuum");
	}
	public void testCentrosaurus() throws IOException {
		doTest("Centrosaurus", "Pointed lizard", "Centrosaurini", "thumb/5/52/ROM-Dinosaur-CentrosaurusApertusSkull.png/250px-ROM-Dinosaur-CentrosaurusApertusSkull.png", Rank.Genus);
	}
	public void testBrachiopoda() throws IOException {
		doTest("Brachiopoda", "Brachiopods", "Lophotrochozoa", "thumb/8/86/Onniella.jpg/250px-Onniella.jpg", Rank.Phylum, false, null, "Onniella");
	}
	public void testCepaea_nemoralis() throws IOException {
		doTest("Cepaea nemoralis", "Grove snail", "Cepaea", "thumb/6/66/Schneckesnail1.jpg/220px-Schneckesnail1.jpg", Rank.Species, false, null, null /* not a link - should be null "Cepaea nemoralis" */);
	}
	public void testGrobben() throws IOException {
		doTest("Grobben", null, null, null, null);
	}
	public void testSeleucidis_melanoleucus() throws IOException {
		doTest("Seleucidis melanoleucus", "Twelve-wired Bird of Paradise", "Seleucidis", "thumb/3/3c/BxZ_Seleucidis_melanoleuca_00a.jpg/220px-BxZ_Seleucidis_melanoleuca_00a.jpg", Rank.Species);
	}
	public void testEndopterygota() throws IOException {
		doTest("Endopterygota", "Complete Metamorphosis Insects", "Eumetabola", "thumb/7/7c/BeetleBrazil_068.jpg/250px-BeetleBrazil_068.jpg", Rank.Cladus);
	}
	public void testLethiscidae() throws IOException {
		doTest("Lethiscidae", null, "Aïstopoda", null, Rank.Familia);
	}
	public void testArchaeognatha() throws IOException {
		doTest("Archaeognatha", "Bristletails, jumping bristletails", "Basal Insecta", null, Rank.Ordo);
	}
	public void testBasal_Insecta() throws IOException {
		doTest("Basal Insecta", null, "Insecta", null, Rank.Cladus);
	}
	public void testPilosa() throws IOException {
		doTest("Pilosa", null, "Xenarthra", "thumb/1/18/Bradypus.jpg/250px-Bradypus.jpg", Rank.Ordo, false, null, "Bradypus variegatus infuscatus");
	}
	public void testEumolpinae() throws IOException {
		doTest("Eumolpinae", "Oval Leaf Beetles", "Chrysomelidae", "2/22/Eumolpus.asclepiadeus.-.calwer.45.06.jpg", Rank.Subfamilia, false, null, "Eumolpus asclepiadeus");
	}
	public void testHoangus_venustus() throws IOException {
		doTest("Hoangus venustus", "Flax ladybird", "Hoangus", "thumb/b/be/Cassiculus_venustus_%28card_mount%29.jpg/250px-Cassiculus_venustus_%28card_mount%29.jpg", Rank.Species, false, null, null);
	}
	
	public void testChordata_Craniata() throws IOException {
		doTest("Chordata Craniata", null, "Chordata", null, Rank.Cladus, false, null, null);
	}
	public void testCordylus_giganteus() throws IOException {
		doTest("Cordylus giganteus", "Giant girdled lizard, Giant sungazer lizard", "Cordylus", "thumb/7/7b/Cordylus_giganteus.jpg/250px-Cordylus_giganteus.jpg", Rank.Species, false, null, null);
	}
	public void testAves() throws IOException {
		doTest("Aves", "Birds", "Avialae", "thumb/f/f7/Pieni_2_0622.jpg/300px-Pieni_2_0622.jpg", Rank.Classis);
	}
	
	// tests proper parsing of thumbs with two versions
	// thumb/c/c6/Rapanea.gif/375px-Rapanea.gif 1.5x, //upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Rapanea.gif/500px-Rapanea.gif 2x
	public void testMyrsinoideae() throws IOException {
		doTest("Myrsinoideae", null, "Primulaceae", "thumb/c/c6/Rapanea.gif/250px-Rapanea.gif", Rank.Subfamilia);
	}
	/*
https://upload.wikimedia.org/wikipedia/commons/thumb/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv/640px--Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv.jpg	
                                               thumb/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv/640px--Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv.jpg[34] controls=[34][34] preload=[34]none[34] autoplay=[34][34] style=[34]width:640px;height:480px[34] class=[34]kskin[34] data-durationhint=[34]29.833333333333[34] data-startoffset=[34]0[34] data-mwtitle=[34]Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv[34] data-mwprovider=[34]wikimediacommons[34][62][60]source src=[34]//upload.wikimedia.org/wikipedia/commons/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv[34] type=[34]video/ogg; codecs=[38]quot;theora[38]quot;[34] data-title=[34]Original Ogg file, 640 [215] 480 (582 kbps)[34] data-shorttitle=[34]Ogg source[34] data-width=[34]640[34] data-height=[34]480[34] data-bandwidth=[34]581957[34] data-framerate=[34]24[34] /[62][60]source src=[34]//upload.wikimedia.org/wikipedia/commons/transcoded/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv.480p.ogv[34] type=[34]video/ogg; codecs=[38]quot;theora, vorbis[38]quot;[34] transcodekey=[34]480p.ogv[34] data-title=[34]Web streamable Ogg video (480P)[34] data-shorttitle=[34]Ogg 480P[34] data-width=[34]640[34] data-height=[34]480[34] data-bandwidth=[34]705560[34] data-framerate=[34]24[34] /[62][60]source src=[34]//upload.wikimedia.org/wikipedia/commons/transcoded/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv.480p.webm[34] type=[34]video/webm; codecs=[38]quot;vp8, vorbis[38]quot;[34] transcodekey=[34]480p.webm[34] data-title=[34]Web streamable WebM (480P)[34] data-shorttitle=[34]WebM 480P[34] data-width=[34]640[34] data-height=[34]480[34] data-bandwidth=[34]1005936[34] data-framerate=[34]24[34] /[62][60]source src=[34]//upload.wikimedia.org/wikipedia/commons/transcoded/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv.160p.ogv[34] type=[34]video/ogg; codecs=[38]quot;theora, vorbis[38]quot;[34] transcodekey=[34]160p.ogv[34] data-title=[34]Low bandwidth Ogg video (160P)[34] data-shorttitle=[34]Ogg 160P[34] data-width=[34]214[34] data-height=[34]160[34] data-bandwidth=[34]121160[34] data-framerate=[34]15[34] /[62][60]source src=[34]//upload.wikimedia.org/wikipedia/commons/transcoded/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv.360p.ogv[34] type=[34]video/ogg; codecs=[38]quot;theora, vorbis[38]quot;[34] transcodekey=[34]360p.ogv[34] data-title=[34]Web streamable Ogg video (360P)[34] data-shorttitle=[34]Ogg 360P[34] data-width=[34]480[34] data-height=[34]360[34] data-bandwidth=[34]459976[34] data-framerate=[34]24[34] /[62][60]source src=[34]//upload.wikimedia.org/wikipedia/commons/transcoded/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv.360p.webm[34] type=[34]video/webm; codecs=[38]quot;vp8, vorbis[38]quot;[34] transcodekey=[34]360p.webm[34] data-title=[34]Web streamable WebM (360P)[34] data-shorttitle=[34]WebM 360P[34] data-width=[34]480[34] data-height=[34]360[34] data-bandwidth=[34]502944[34] data-framerate=[34]24[34] /[62]Sorry, your browser either has JavaScript disabled or does not have any supported player.[60]br /[62] You can [60]a href=[34]//upload.wikimedia.org/wikipedia/commons/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv[34][62]download the clip[60]/a[62] or [60]a href=[34]https://www.mediawiki.org/wiki/Special:MyLanguage/Extension:TimedMediaHandler/Client_download[34][62]download a player[60]/a[62] to play the clip in your browser.[60]/video[62][60]/div[62]
 src="//upload.wikimedia.org/wikipedia/commons/thumb/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv/220px--Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv.jpg"	
	*/
	public void testAtelopus_franciscus() throws IOException {
		doTest("Atelopus franciscus", "Central Coast Stubfoot Toad", "Atelopus", "thumb/7/7a/Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv/220px--Magnetic_resonance_imaging_of_Atelopus_franciscus_head_-_pone.0022080.s004.ogv.jpg", Rank.Species);
	}
	public void testPhiomorpha() throws IOException {
		doTest("Phiomorpha", null, "Hystricognathi", "thumb/0/02/Nacktmull.jpg/220px-Nacktmull.jpg", Rank.Infraordo);
	}
	public void testCiliophrys() throws IOException {
		doTest("Ciliophrys", null, "Pedinellaceae", null, Rank.Genus);
	}
	public void testChloranthales() throws IOException {
		doTest("Chloranthales", null, "Magnoliopsida", "thumb/6/66/Chloranthus_serratus_%28200705%29.jpg/250px-Chloranthus_serratus_%28200705%29.jpg", Rank.Ordo);
	}
	public void testRubus_racemosus() throws IOException {
		doTest("Rubus racemosus", null, "Rubus subg. Idaeobatus", null, Rank.Species);
	}
	public void testRubus_subg_Idaeobatus() throws IOException {
		doTest("Rubus subg. Idaeobatus", null, "Rubus", null, Rank.Subgenus);
	}
	public void testDimares() throws IOException {
		doTest("Dimares", null, "Dimarinae", null, Rank.Genus);
	}
	public void testTrachymedusae() throws IOException {
		doTest("Trachymedusae", null, "Trachylinae", null, Rank.Ordo);
	}
	public void testSordarialesincertaesedis() throws IOException {
		doTest("Sordariales incertae sedis", null, "Sordariales", null, Rank.Familia);
	}
	public void testCentropogonCampanulaceae() throws IOException {
		doTest("Centropogon (Campanulaceae)", null, "Lobelioideae", "thumb/a/a5/Centropogon_ferrugineus_1.jpg/250px-Centropogon_ferrugineus_1.jpg", Rank.Genus);
	}
	public void testStenocorusStenocorus() throws IOException {
		doTest("Stenocorus (Stenocorus)", null, "Stenocorus", null, Rank.Subgenera);
	}
	/* still can't parse it even with new rank
	public void testHyphomycetes() throws IOException {
		doTest("Hyphomycetes", null, "Anamorphic fungi", null, Rank.Subgenera);
	}
	*/
	
	public void testTuojiangosaurus_multispinus() throws IOException {
		doTest("Tuojiangosaurus multispinus", null, "Tuojiangosaurus", null, Rank.Species);
	}
	public void testDictyococcites() throws IOException {
		doTest("Dictyococcites", null, "Prymnesiophyceae incertae sedis", null, Rank.Genus);
	}
	public void testButschliella() throws IOException {
		doTest("Butschliella", null, "Cryptophyta incertae sedis", null, Rank.Genus);
	}
	public void testLycaena_tityrus() throws IOException {
		doTest("Lycaena tityrus", "Sooty Copper", "Lycaena", "thumb/8/8f/Lycaena_tityrus_1%28loz%29.jpg/250px-Lycaena_tityrus_1%28loz%29.jpg", Rank.Species);
	}
	public void testPuperita_pupa() throws IOException {
		doTest("Puperita pupa", null, "Puperita", "thumb/c/ca/Puperita_pupa%2C_Salinas_de_Araya%2C_Sucre_-_Venezuela_001.jpg/250px-Puperita_pupa%2C_Salinas_de_Araya%2C_Sucre_-_Venezuela_001.jpg", Rank.Species);
	}
	public void testCamptochaeta_mixta() throws IOException {
		doTest("Camptochaeta mixta", null, "Camptochaeta", null, Rank.Species);
	}
	public void testChlorobium() throws IOException {
		doTest("Chlorobium", null, "Chlorobium/Pelodictyon group", null, Rank.Genus);
	}
	public void testPerigraphacinctumslovenica() throws IOException {
		doTest("Perigrapha i-cinctum slovenica", null, "Perigrapha i-cinctum", null, Rank.Subspecies);
	}
	public void testAbantiadesalbofasciatus() throws IOException {
		doTest("Abantiades albofasciatus", null, "Abantiades (Herrich-Schäffer)", null, Rank.Species);
	}
	public void testActinobalanusactinomorphus() throws IOException {
		doTest("Actinobalanus actinomorphus", null, "Actinobalanus", null, Rank.Species);
	}
	public void testMicrodytescameroni() throws IOException {
		doTest("Microdytes cameroni", null, "Microdytes Balfour-Browne", null, Rank.Species);
	}
	public void testOurateamembranacea() throws IOException {
		doTest("Ouratea membranacea", null, "Ouratea", null, Rank.Species);
	}
	public void testMoeritherium() throws IOException {
		doTest("Moeritherium", null, "Moeritheriidae", "thumb/9/97/Moeritherium.jpg/250px-Moeritherium.jpg", Rank.Genus);
	}
	
	private void doTest(String latin, String common, String parent, String image,
			Rank rank) throws IOException {
		doTest(latin, common, parent, image, rank, false, null, null);
	}
	private void doTest(String latin, String common, String parent, String image,
			Rank rank, boolean extinct) throws IOException {
		doTest(latin, common, parent, image, rank, extinct, null, null);
	}
	public static String getPage(String latinName) throws IOException {
		InputStream in = WikiSpeciesParserTest.class.getResourceAsStream(latinName + ".html");
		String page = IOUtils.toString(in, "UTF-8");
		return page;
	}
	private void doTest(String latin, String common, String parent, String image,
			Rank rank, boolean extinct, Rank parentRank, String depicted) throws IOException {
		String page = getPage(latin);
		CompleteEntry results = parser.parse(latin, page);
		
		if (rank == null) {
			assertNull(results);
		} else {
			assertNotNull("Wan't able to parse page - results were null", results);
			assertEquals(latin, results.getLatinName());
			assertEquals(parent, results.getParentLatinName());
			assertEquals(common, results.getCommonName());
			assertEquals(image, results.getImageLink());
			assertEquals(rank, results.getRank());
			assertEquals(results.isExtinct(), extinct);
			assertEquals(depicted, results.getDepictedLatinName());
		}
		if (parent != null) {
			assertNotNull(results.getParent());
			assertEquals(parent, results.getParent().getLatinName());
			assertEquals(parentRank, results.getParent().getRank());
		}
	}
	
	// This is failing
	// Genus: <i><a href="/wiki/Rubus" title="Rubus">Rubus</a></i><br />
	// Subgenus: <strong class="selflink"><i>R.</i> subg. <i>Idaeobatus</i></strong><br />
	// vs this is okay
	// Subgenus: <i><a href="/wiki/Aedes_(Aedimorphus)" title="Aedes (Aedimorphus)">Aedes (Aedimorphus)</a></i><br />
	// Species: <i><strong class="selflink">Aedes vexans</strong></i></p>
	/*
	public void testGetParent() {
		// it's failing because of the "R."
		doTestGetParent("Rubus subg. Idaeobatus", "Subgenus", "Rubus", "Genus: <i><a href=\"/wiki/Rubus\" title=\"Rubus\">Rubus</a></i><br />\r\nSubgenus: <strong class=\"selflink\"><i>R. subg. Idaeobatus</strong><br />");
		// make sure it still works with other patterns
		doTestGetParent("Aedes vexans", "Species", "Aedes (Aedimorphus)", "Subgenus: <i><a href=\"/wiki/Aedes_(Aedimorphus)\" title=\"Aedes (Aedimorphus)\">Aedes (Aedimorphus)</a></i><br />\r\nSpecies: <i><strong class=\"selflink\">Aedes vexans</strong></i></p>");
		doTestGetParent("R. subg. Idaeobatus", "Subgenus", "Rubus", "Genus: <i><a href=\"/wiki/Rubus\" title=\"Rubus\">Rubus</a></i><br />\r\nSubgenus: <strong class=\"selflink\"><i>R. subg. Idaeobatus</strong><br />");
		doTestGetParent("R. subg. Idaeobatus", "Subgenus", "Rubus", "Rubeae</a><br />  Genus: <a href=\"/wiki/Rubus\" title=\"Rubus\">Rubus</a><br />  Subgenus: <strong class=\"selflink\">R. subg. Idaeobatus</strong><br />  Species: ");

		doTestGetParent("Trachymedusae", "Ordo", "Trachylinae", "Sub-classis: <a href=\"/wiki/Trachylinae\" title=\"Trachylinae\">Trachylinae</a><br />\r\nOrdo: <strong class=\"selflink\">Trachymedusae</strong><br />\r\n");
	}
	private void doTestGetParent(String latinName, String rank, String expectParent, String text) {
		WikiSpeciesParser parser = new WikiSpeciesParser();
		CompleteEntry parent = parser.getParent(text, latinName, rank);
		assertNotNull("Could not parse text", parent);
		assertEquals(expectParent, parent.getLatinName());
	}
	*/
	public void testLatinAbbreviations() {
		doTestLatinAbbreviation("Anything", "null");
		doTestLatinAbbreviation("Anything goes", "[A. goes]");
		doTestLatinAbbreviation("Anything will do", "[A. will do, A. w. do]");
		doTestLatinAbbreviation("This (one) too", "[T. (one) too, T. (o.) too]");
	}
	public void doTestLatinAbbreviation(String latin, String expect) {
		Object found = WikiSpeciesParser.getLatinAbbreviations(latin);
		assertEquals(expect, String.valueOf(found));
	}
	public void testAuth() throws Exception {
		doTestAuth("ISSN 0021-1311");
		doTestAuth("Wan-Peng_Chen");
		doTestAuth("Fedor Bogdanovich Schmidt");
		doTestAuth("Richard Van der Laan");
	}
	// TODO this "unit test" relies on downloding the file from URL or being cached - better to put into resources!!
	public void doTestAuth(String name) throws Exception {
		String page = WikiSpeciesCache.CACHE.readFile(name, false);
		String type = WikiSpeciesCrawler.getType(name, page);
		assertEquals(ParseStatus.AUTHORITY, type);
	}
	public void testRedirectSelfLinks() throws Exception {
		String page = getPage("Euphaedra kakamegae");
		int pos = page.indexOf("<strong class=\"selflink\">");
		assertEquals(-1, pos);
		page = WikiSpeciesParser.preProcessRedirectSelfLinks(page);
		pos = page.indexOf("<strong class=\"selflink\">");
		assertTrue(pos > 0);
	}
	
}
