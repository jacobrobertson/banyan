package com.robestone.species.js;

import java.util.ArrayList;
import java.util.List;

import com.robestone.species.Entry;
import com.robestone.species.js.SearchIndexBuilder.CandidateEntry;
import com.robestone.species.js.SearchIndexBuilder.CandidateName;
import com.robestone.species.js.SearchIndexBuilder.KeyEntry;

import junit.framework.TestCase;

public class SearchIndexBuilderTest extends TestCase {

	public void testSimplest() throws Exception {
		SearchIndexBuilder b = new SearchIndexBuilder(5, 3, 6, false);
		
		List<CandidateEntry> candidates = new ArrayList<>();
		toCandidateEntry("Prince of Peec", candidates);
		toCandidateEntry("Helper", candidates);
		CandidateEntry test2 = toCandidateEntry("Funny", candidates);
		toCandidateEntry("Howitzer", candidates);
		toCandidateEntry("Prize Munny", candidates);
		toCandidateEntry("Junnyper", candidates);
		toCandidateEntry("Unnsterz", candidates);
		toCandidateEntry("MeMunn", candidates);
		toCandidateEntry("Trunnk", candidates);
		toCandidateEntry("No MorePrizeMunny", candidates);
		
		CandidateEntry test = testCandidateEntry("The United States of America", candidates, "UNN", 0);
		testCandidateEntry(test, "ATES", 20_332);
		testCandidateEntry(test, "STATES", 20_000_500);
		testCandidateEntry(test, "THE", 20_000_250);
		testCandidateEntry(test, "OFS", 10_250);
		
		testCandidateEntry(test2, "FUNNY", 200_002_000);
		testCandidateEntry(test2, "FUN", 2_001_200);
		
		b.setCandidates(candidates);
		b.iterateOverKeys();
	}

	private CandidateEntry toCandidateEntry(String name, List<CandidateEntry> candidates) {
		return testCandidateEntry(name, candidates, null, -1);
	}
	
	public void testGentianaAngustifolia() throws Exception {
		List<CandidateEntry> candidates = new ArrayList<>();

		int id = 0;
		testCandidateEntry2("Restrepia antennifera", "Antennae-carrying Restrepia", candidates, null, id++);
		testCandidateEntry2("Agapeta angelana", candidates, null, id++);
		testCandidateEntry2("Gentiana angustifolia", candidates, null, id++);
		testCandidateEntry2("Jacoona anasuja", candidates, null, id++);
		testCandidateEntry2("Gentiana andrewsii", "Closed bottle Gentian", candidates, null, id++);
		testCandidateEntry2("Anemone caroliniana", "Carolina anemone", candidates, null, id++);
		testCandidateEntry2("Roldana angulifolia", candidates, null, id++);
		testCandidateEntry2("Amana anhuiensis", candidates, null, id++);
		testCandidateEntry2("Dacalana anysides", candidates, null, id++);
		testCandidateEntry2("Jacoona anasuja irmina", candidates, null, id++);
		testCandidateEntry2("Jacoona anasuja jusana", candidates, null, id++);
		testCandidateEntry2("Chrysozephyrus souleana angustimargo", candidates, null, id++);
		testCandidateEntry2("Dacalana anysis", candidates, null, id++);
		testCandidateEntry2("Dacalana anysis anysis", candidates, null, id++);
		testCandidateEntry2("Dendrocincla anabatina", "Tawny-winged Woodcreeper", candidates, null, id++);
		testCandidateEntry2("Buchanania (Anacardiaceae)", candidates, null, id++);
		testCandidateEntry2("Euphorbia anacampseros", candidates, null, id++);
		testCandidateEntry2("Baronia (Anacardiaceae)", candidates, null, id++);
		testCandidateEntry2("Rhodosphaera (Anacardiaceae)", candidates, null, id++);
		testCandidateEntry2("Euphorbia anachoreta", candidates, null, id++);
		testCandidateEntry2("Ehretia anacua", "Knockaway", candidates, null, id++);
		testCandidateEntry2("Anadia pamplonensis", "Pamplona Anadia", candidates, null, id++);
		testCandidateEntry2("Anadia bogotensis", "Bogota Anadia", candidates, null, id++);
		testCandidateEntry2("Taeniophyllum reijnvaanae", candidates, null, id++);
		testCandidateEntry2("Torenia anagallis", candidates, null, id++);
		testCandidateEntry2("Catasticta anaitis", candidates, null, id++);
		testCandidateEntry2("Salvelinus malma anaktavukensis", "Angayukaksurak char", candidates, null, id++);
		testCandidateEntry2("Jacobaea analoga", candidates, null, id++);
		testCandidateEntry2("Diomedea antipodensis", "Antipodean Albatross", candidates, null, id++);
		testCandidateEntry2("Edolisoma anale", "New Caledonian Cuckooshrike", candidates, null, id++);
		testCandidateEntry2("Vespa analis", candidates, null, id++);
		testCandidateEntry2("Johnstonella angustifolia", "Bristlelobe cryptantha, Narrowleaf pick-me-not, Panamint catseye, Narrow-leaved popcorn flower", candidates, null, id++);
		testCandidateEntry2("Arhopala anamuta", candidates, null, id++);
		testCandidateEntry2("Ceropegia anantii", candidates, null, id++);
		testCandidateEntry2("Antennaria anaphaloides", "Pearly Pussytoes", candidates, null, id++);
		testCandidateEntry2("Arhopala anarte auzea", candidates, null, id++);
		testCandidateEntry2("Amblypodia narada andersonii", candidates, null, id++);
		testCandidateEntry2("Amblypodia anita naradoides", candidates, null, id++);
		testCandidateEntry2("Curcuma angustifolia", "East Indian Arrowroot", candidates, null, id++);
		testCandidateEntry2("Teia anartoides", "Painted apple moth", candidates, null, id++);
		testCandidateEntry2("Gaultheria anastomosans", candidates, null, id++);
		testCandidateEntry2("Amnihyla angiana", "Angiana Treefrog", candidates, null, id++);
		testCandidateEntry2("Lingula anatina", "Lamp shell", candidates, null, id++);
		testCandidateEntry2("Apis mellifera anatoliaca", "Anatolian honey bee", candidates, null, id++);
		testCandidateEntry2("Anatololacerta anatolica", "Anatolian Rock Lizard", candidates, null, id++);
		testCandidateEntry2("Aphonopelma anax", "Kingsville Bronze Brown", candidates, null, id++);
		testCandidateEntry2("Yushania anceps", candidates, null, id++);
		testCandidateEntry2("Cassia brewsteri", "Brewster\u2019s Cassia, Leichhardt Bean, Cassia pea and Bean Tree", candidates, null, id++);
		testCandidateEntry2("Acacia anceps", "Port Lincoln wattle, Two edged wattle", candidates, null, id++);
		testCandidateEntry2("Sabatia angularis", "Rose pink, American centaury", candidates, null, id++);
		testCandidateEntry2("Neoromicia anchietae", "Anchieta's Pipistrelle", candidates, null, id++);
		testCandidateEntry2("Ptychadena anchietae", "Benguella Grassland Frog", candidates, null, id++);
		testCandidateEntry2("Eumorpha anchemolus", "Anchemolus Sphinx", candidates, null, id++);
		testCandidateEntry2("Aldama anchusifolia", candidates, null, id++);
		testCandidateEntry2("Scaevola anchusifolia", candidates, null, id++);
		testCandidateEntry2("Lindelofia anchusoides", candidates, null, id++);
		testCandidateEntry2("Acacia ancistrocarpa", "Fitzroy wattle, Fish hook wattle, Pindan wattle", candidates, null, id++);
		testCandidateEntry2("Acacia ancistrophylla", candidates, null, id++);
		testCandidateEntry2("Telicota ancilla", "Dark Palm Dart", candidates, null, id++);
		testCandidateEntry2("Gigartina ancistroclada", candidates, null, id++);
		testCandidateEntry2("Euphyllia ancora", candidates, null, id++);
		testCandidateEntry2("Euphyllia paraancora", candidates, null, id++);
		testCandidateEntry2("Phoracantha ancoralis", candidates, null, id++);
		testCandidateEntry2("Catopyrops ancyra ancyra", candidates, null, id++);
		testCandidateEntry2("Caladenia ancylosa", "Genoa spider orchid", candidates, null, id++);
		testCandidateEntry2("Janusia anisandra", candidates, null, id++);
		testCandidateEntry2("Crocidura andamanensis", "Andaman Shrew", candidates, null, id++);
		testCandidateEntry2("Ovabunda andamanensis", candidates, null, id++);
		testCandidateEntry2("Allamanda angustifolia", candidates, null, id++);
		testCandidateEntry2("Zoothera andromedae", "Sunda Thrush", candidates, null, id++);
		testCandidateEntry2("Fejervarya andamanensis", "Andaman Wart Frog", candidates, null, id++);
		testCandidateEntry2("Dinochloa andamanica", candidates, null, id++);
		testCandidateEntry2("Eulophia andamanensis", candidates, null, id++);
		testCandidateEntry2("Pinanga andamanensis", candidates, null, id++);
		testCandidateEntry2("Gastrotheca andaquiensis", "Andes Marsupial Frog", candidates, null, id++);
		testCandidateEntry2("Vachellia leucophloea", "Distiller's Acacia, Katu Andara, Maha Andara, Nimbar, Reru, Safed Kikar, Velvalayam, Velvel, White Babul", candidates, null, id++);
		testCandidateEntry2("Bouteloua", "Grama and Buffalo Grasses", candidates, null, id++);
		testCandidateEntry2("Anthozoa", "Sea anemones and corals", candidates, null, id++);
		testCandidateEntry2("Phrynopus oblivius", "Tarma Andes Frog", candidates, null, id++);
		testCandidateEntry2("Euphorbia antisyphilitica", "Candelilla, Wax Euphorbia", candidates, null, id++);
		testCandidateEntry2("Candelariella antennaria", "Pussytoes eggyolk lichen", candidates, null, id++);
		testCandidateEntry2("Lavandula angustifolia", "Common lavender, Lavender, True lavender, English lavender, Garden lavender, Narrow-leaved lavender", candidates, null, id++);
		testCandidateEntry2("Drosera andersoniana", "Sturdy Sundew", candidates, null, id++);
		testCandidateEntry2("Vernonia angustifolia", "Carolina sandhill ironweed, Carolina slender ironweed", candidates, null, id++);
		testCandidateEntry2("Dermanura anderseni", "Andersen's Fruit-eating Bat", candidates, null, id++);
		testCandidateEntry2("Dobsonia anderseni", "Andersen's Naked-backed Fruit Bat", candidates, null, id++);
		testCandidateEntry2("Helicophagus waandersii", "Rat-faced pangasiid", candidates, null, id++);
		testCandidateEntry2("Eurema andersoni", "One-spot Grass Yellow", candidates, null, id++);
		testCandidateEntry2("Nienburgia andersoniana", candidates, null, id++);
		testCandidateEntry2("Elymnias esaca andersonii", candidates, null, id++);
		testCandidateEntry2("Alsophila andersonii", candidates, null, id++);
		testCandidateEntry2("Cyaniriodes libna andersonii", candidates, null, id++);
		testCandidateEntry2("Marmosa andersoni", "Anderson's mouse opossum, Heavy-browed mouse opossum", candidates, null, id++);
		testCandidateEntry2("Arctoa anderssonii", "Andersson's Arctoa Moss", candidates, null, id++);
		testCandidateEntry2("Catocala andromedae", "Gloomy Underwing", candidates, null, id++);
		testCandidateEntry2("Erica andevalensis", candidates, null, id++);
		testCandidateEntry2("Muhlenbergia andina", "Foxtail muhly", candidates, null, id++);
		testCandidateEntry2("Cristaria andicola", candidates, null, id++);
		testCandidateEntry2("Monticalia andicola", candidates, null, id++);
		testCandidateEntry2("Vicia andicola", candidates, null, id++);
		testCandidateEntry2("Grallaria andicolus", "Stripe-headed antpitta", candidates, null, id++);
		testCandidateEntry2("Neoholmgrenia andina", "Blackfoot River Evening Primrose", candidates, null, id++);
		testCandidateEntry2("Andira anthelmia", candidates, null, id++);
		testCandidateEntry2("Araliaceae", "Aralia and Ivy family", candidates, null, id++);
		testCandidateEntry2("Saussurea andoana", candidates, null, id++);
		testCandidateEntry2("Ptyodactylus rivapadiali", "Riva and Padial\u2019s Fan-footed Gecko", candidates, null, id++);
		testCandidateEntry2("Channa andrao", candidates, null, id++);
		testCandidateEntry2("Saurauia andreana", candidates, null, id++);
		testCandidateEntry2("Bomarea andreana", candidates, null, id++);
		testCandidateEntry2("Adenomera andreae", "Lowland Tropical Bullfrog", candidates, null, id++);
		testCandidateEntry2("Atlantolacerta andreanskyi", "Andreansky's Rock Lizard", candidates, null, id++);
		testCandidateEntry2("Fregata andrewsi", "Christmas frigatebird", candidates, null, id++);
		testCandidateEntry2("Clintonia andrewsiana", "Andrew's clintonia", candidates, null, id++);
		testCandidateEntry2("Ampelita andriamamonjyi", candidates, null, id++);
		testCandidateEntry2("Entandrophragma angolense", candidates, null, id++);
		testCandidateEntry2("Trichromia androconiata", candidates, null, id++);
		testCandidateEntry2("Justicia andrographioides", candidates, null, id++);
		testCandidateEntry2("Ochrolechia androgyna", "Powdery saucer lichen, Crabseye lichen", candidates, null, id++);
		testCandidateEntry2("Catocala andromache", candidates, null, id++);
		testCandidateEntry2("Acraea andromacha", candidates, null, id++);
		testCandidateEntry2("Pellaea andromedifolia", "Coffee cliffbrake, Coffee fern", candidates, null, id++);
		testCandidateEntry2("Halgania andromedifolia", candidates, null, id++);
		testCandidateEntry2("Puccinia andropogonis", candidates, null, id++);
		testCandidateEntry2("Miconia androsaemifolia", candidates, null, id++);
		testCandidateEntry2("Dianthera androsaemifolia", candidates, null, id++);
		testCandidateEntry2("Eschscholzia androuxii", candidates, null, id++);
		testCandidateEntry2("Jacobaea andrzejowskyi", candidates, null, id++);
		testCandidateEntry2("Manduca andicola", candidates, null, id++);
		testCandidateEntry2("Lavandula antineae", candidates, null, id++);
		testCandidateEntry2("Tacca ankaranensis", candidates, null, id++);
		testCandidateEntry2("Emoia aneityumensis", "Anatom Emo Skink", candidates, null, id++);
		testCandidateEntry2("Arhopala anella", candidates, null, id++);
		testCandidateEntry2("Phacelia anelsonii", "Aven Nelson's phacelia", candidates, null, id++);
		testCandidateEntry2("Gentianella antarctica", candidates, null, id++);
		testCandidateEntry2("Gentianella anisodonta", candidates, null, id++);
		testCandidateEntry2("Paraquilegia anemonoides", candidates, null, id++);
		testCandidateEntry2("Acraea anemosa", candidates, null, id++);
		testCandidateEntry2("Casinycteris campomaanensis", "Campo-Ma'an Fruit Bat", candidates, null, id++);
		testCandidateEntry2("Monsonia angustifolia", "Crane\u2019s bill", candidates, null, id++);
		testCandidateEntry2("Vanessa annabella", "West Coast Lady", candidates, null, id++);
		testCandidateEntry2("Acacia aneura", "Mulga", candidates, null, id++);
		testCandidateEntry2("Stanhopea anfracta", candidates, null, id++);
		testCandidateEntry2("Masdevallia anfracta", candidates, null, id++);
		testCandidateEntry2("Alstonia angustiloba", candidates, null, id++);
		testCandidateEntry2("Mirounga angustirostris", "Northern Elephant Seal", candidates, null, id++);
		testCandidateEntry2("Boloria angarensis", candidates, null, id++);
		testCandidateEntry2("Cyrtodactylus laangensis", "Phnom Laang Bent-toed Gecko", candidates, null, id++);
		testCandidateEntry2("Dipterocarpus turbinatus", "Yaang daeng", candidates, null, id++);
		testCandidateEntry2("Hydrangea anomala subsp. anomala", "Climbing Hydrangea", candidates, null, id++);
		testCandidateEntry2("Hydrangea anomala subsp. petiolaris", candidates, null, id++);
		testCandidateEntry2("Ruta angustifolia", "Narrow-leaved fringed rue", candidates, null, id++);
		testCandidateEntry2("Nacaduba angelae", candidates, null, id++);
		testCandidateEntry2("Soehrensia angelesiae", candidates, null, id++);
		testCandidateEntry2("Helgicirrha angelicae", candidates, null, id++);
		testCandidateEntry2("Hellinsia angela", candidates, null, id++);
		testCandidateEntry2("Clionidae", "Sea Angels", candidates, null, id++);
		testCandidateEntry2("Cattleya angereri", candidates, null, id++);
		testCandidateEntry2("Hydrangea anomala", "Climbing Hydrangea", candidates, null, id++);
		testCandidateEntry2("Litoria angiana", "Angiana Treefrog", candidates, null, id++);
		testCandidateEntry2("Westringia angustifolia", "Scabrous westringi", candidates, null, id++);
		testCandidateEntry2("Sphaeralcea angustifolia var. oblongifolia", candidates, null, id++);
		testCandidateEntry2("Genista anglica", "Petty Whin, Needle Furze, Needle Whin", candidates, null, id++);
		testCandidateEntry2("Cologania angustifolia", "Longleaf cologania", candidates, null, id++);
		testCandidateEntry2("Cochlearia anglica", "English scurvy-grass, Long-leaved scurvy grass", candidates, null, id++);
		testCandidateEntry2("Lobelia anceps", "Angled lobelia", candidates, null, id++);
		testCandidateEntry2("Cryptocarya nova-anglica", "Mountain laurel", candidates, null, id++);
		testCandidateEntry2("Blommersia angolafa", candidates, null, id++);
		testCandidateEntry2("Dracaena angolensis", candidates, null, id++);
		testCandidateEntry2("Tylosema angolense", candidates, null, id++);
		testCandidateEntry2("Eulophia angolensis", candidates, null, id++);
		testCandidateEntry2("Maerua angolensis subsp. angolensis", "Bead-bean Tree", candidates, null, id++);
		testCandidateEntry2("Cousinia angrenii", candidates, null, id++);
		testCandidateEntry2("Porlieria angustifolia", "Texas Guaiacum, Texas Lignum-vitae", candidates, null, id++);
		testCandidateEntry2("Aristolochia anguicida", "Harlequin dutchman's pipe", candidates, null, id++);
		testCandidateEntry2("Aetea anguina", candidates, null, id++);
		testCandidateEntry2("Scaevola angulata", candidates, null, id++);
		testCandidateEntry2("Begonia angularis", candidates, null, id++);
		testCandidateEntry2("Odontoptilum angulata angulata", "Chestnut Angle", candidates, null, id++);
		testCandidateEntry2("Adrastia angulifrons", candidates, null, id++);
		testCandidateEntry2("Masdevallia angulifera", candidates, null, id++);
		testCandidateEntry2("Hellinsia angulofuscus", candidates, null, id++);
		testCandidateEntry2("Dracaena angustifolia", candidates, null, id++);
		testCandidateEntry2("Castilleja angustifolia", "Northwestern paintbrush, Desert Indian paintbrush", candidates, null, id++);
		testCandidateEntry2("Catocala angusi", candidates, null, id++);
		testCandidateEntry2("Saussurea angustifolia", "Narrowleaf saw-wort", candidates, null, id++);
		testCandidateEntry2("Scaevola angustata", candidates, null, id++);
		testCandidateEntry2("Balduina angustifolia", "Coastal plain honeycombhead, Coastalplain honeycombhead", candidates, null, id++);
		testCandidateEntry2("Platanthera angustata var. angustata", candidates, null, id++);
		testCandidateEntry2("Arisaema angustatum", candidates, null, id++);
		testCandidateEntry2("Catuna angustatum", candidates, null, id++);
		testCandidateEntry2("Physostegia angustifolia", "Narrowleaf false dragonhead", candidates, null, id++);
		testCandidateEntry2("Cyanostegia angustifolia", candidates, null, id++);
		testCandidateEntry2("Freycinetia angustifolia", candidates, null, id++);
		testCandidateEntry2("Goeppertia angustifolia", candidates, null, id++);
		testCandidateEntry2("Gehyra angusticaudata", "Narrowhead Dtella", candidates, null, id++);
		testCandidateEntry2("Ambulyx japonica angustifasciata", candidates, null, id++);
		testCandidateEntry2("Euphorbia angustiflora", candidates, null, id++);
		testCandidateEntry2("Diplolaena angustifolia", candidates, null, id++);
		testCandidateEntry2("Macrolearia angustifolia", candidates, null, id++);
		testCandidateEntry2("Haworthia angustifolia", candidates, null, id++);
		testCandidateEntry2("Kalmia angustifolia subsp. angustifolia", candidates, null, id++);
		testCandidateEntry2("Alstroemeria angustifolia", candidates, null, id++);
		testCandidateEntry2("Sphaeralcea angustifolia var. angustifolia", "Copper globemallow", candidates, null, id++);
		testCandidateEntry2("Pomatocalpa angustifolium", candidates, null, id++);
		testCandidateEntry2("Gastrotheca angustifrons", "Pacific Marsupial Frog", candidates, null, id++);
		testCandidateEntry2("Chaetoptila angustipluma", "Kioea", candidates, null, id++);
		testCandidateEntry2("Tarentola angustimentalis", "East Canary gecko", candidates, null, id++);
		testCandidateEntry2("Chamaedorea angustisecta", candidates, null, id++);
		testCandidateEntry2("Dichaea angustisegmenta", candidates, null, id++);
		testCandidateEntry2("Senna angustisiliqua", candidates, null, id++);
		testCandidateEntry2("Pseudobrickellia angustissima", candidates, null, id++);
		testCandidateEntry2("Antizoma angustifolia", candidates, null, id++);
		testCandidateEntry2("Heliconia angusta", "Christmas Heliconia", candidates, null, id++);
		testCandidateEntry2("Aristobia angustifrons", candidates, null, id++);
		testCandidateEntry2("Stanhopea annulata", candidates, null, id++);
		testCandidateEntry2("Crocidura anhuiensis", "Anhui White-toothed Shrew", candidates, null, id++);
		testCandidateEntry2("Coleataenia anceps", "Redtop panic grass", candidates, null, id++);
		testCandidateEntry2("Dysphania anthelmintica", "American wormseed, Wormseed", candidates, null, id++);
		testCandidateEntry2("Haaniella erringtoniae", "Errington\u2019s Haaniella", candidates, null, id++);
		testCandidateEntry2("Euselasia anica", candidates, null, id++);
		testCandidateEntry2("Haaniella muelleri", "M\u00fcller's Haaniella", candidates, null, id++);
		testCandidateEntry2("Jordaaniella cuprea", candidates, null, id++);
		testCandidateEntry2("Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella aculeata", "Acutely spined Haaniella", candidates, null, id++);
		testCandidateEntry2("Jordaaniella clavifolia", candidates, null, id++);
		testCandidateEntry2("Haaniella dehaanii", "De Haan's Haaniella", candidates, null, id++);
		testCandidateEntry2("Jordaaniella dubia", candidates, null, id++);
		testCandidateEntry2("Haaniella echinata", "Prickly Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella gintingi", "Ginting\u2019s Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella glaber", "Smooth-bodied Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella gorochovi", "Gorochov\u2019s Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella grayii", "Gray's Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella jacobsoni", "Jacobson\u2019s Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella kerincia", "Spiny Mt. Kerinci Stick-insect", candidates, null, id++);
		testCandidateEntry2("Haaniella mecheli", "Mechel\u2019s Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella macroptera", "Long-winged Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella parva", candidates, null, id++);
		testCandidateEntry2("Haaniella rosenbergii", "Rosenberg\u2019s Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella scabra", "Small Haaniella", candidates, null, id++);
		testCandidateEntry2("Haaniella saussurei", "Saussure's Haaniella", candidates, null, id++);
		testCandidateEntry2("Jordaaniella spongiosa", candidates, null, id++);
		testCandidateEntry2("Alectoria (Animalia)", candidates, null, id++);
		testCandidateEntry2("Heterotoma (Animalia)", candidates, null, id++);
		testCandidateEntry2("Sclerolaena anisacanthoides", "Yellow Burr", candidates, null, id++);
		testCandidateEntry2("Clausena anisata", candidates, null, id++);
		testCandidateEntry2("Illicium floridanum", "Florida anise, Purple anise", candidates, null, id++);
		testCandidateEntry2("Attalea anisitsiana", candidates, null, id++);
		testCandidateEntry2("Habenaria anisitsii", candidates, null, id++);
		testCandidateEntry2("Primula anisodora", candidates, null, id++);
		testCandidateEntry2("Nemesia anisocarpa", candidates, null, id++);
		testCandidateEntry2("Anisodontea anomala", candidates, null, id++);
		testCandidateEntry2("Xylaria anisopleura", candidates, null, id++);
		testCandidateEntry2("Pera anisotricha", candidates, null, id++);
		testCandidateEntry2("Aphonopelma anitahoffmannae", candidates, null, id++);
		testCandidateEntry2("Amblypodia anita", candidates, null, id++);
		testCandidateEntry2("Amblypodia anita anita", candidates, null, id++);
		testCandidateEntry2("Entomobrya aniwaniwaensis", candidates, null, id++);
		testCandidateEntry2("Crassula ankaratrensis", candidates, null, id++);
		testCandidateEntry2("Microkayla ankohuma", "Cooco Andes Frog", candidates, null, id++);
		testCandidateEntry2("Tscherskia collina", "Ningshaan Long-tailed Hamster", candidates, null, id++);
		testCandidateEntry2("Wrightia annamensis", candidates, null, id++);
		testCandidateEntry2("Nanohyla annamensis", "Vietnam Rice Frog", candidates, null, id++);
		testCandidateEntry2("Euphorbia annamarieae", candidates, null, id++);
		testCandidateEntry2("Renanthera annamensis", candidates, null, id++);
		testCandidateEntry2("Crocidura annamitensis", "Annamite White-toothed Shrew", candidates, null, id++);
		testCandidateEntry2("Nanorana annandalii", "Annandale's Paa Frog", candidates, null, id++);
		testCandidateEntry2("Amblypodia annetta faisina", candidates, null, id++);
		testCandidateEntry2("Agandecca annectens", candidates, null, id++);
		testCandidateEntry2("Caenurgina annexa", candidates, null, id++);
		testCandidateEntry2("Mammillaria anniana", candidates, null, id++);
		testCandidateEntry2("Hermannia angularis", candidates, null, id++);
		testCandidateEntry2("Flos anniella anniella", candidates, null, id++);
		testCandidateEntry2("Annona (Annonaceae)", candidates, null, id++);
		testCandidateEntry2("Begonia annulata", candidates, null, id++);
		testCandidateEntry2("Clitoria annua", candidates, null, id++);
		testCandidateEntry2("Iva annua", "Annual marsh elder, Pelocote, Rough marsh-elder, Sumpweed", candidates, null, id++);
		testCandidateEntry2("Lunaria annua", "Annual Honesty", candidates, null, id++);
		testCandidateEntry2("Poa annua", "Annual Meadow Grass", candidates, null, id++);
		testCandidateEntry2("Arhopala annulata", candidates, null, id++);
		testCandidateEntry2("Tarentola annularis", "White-spotted Gecko", candidates, null, id++);
		testCandidateEntry2("Culiseta annulata", candidates, null, id++);
		testCandidateEntry2("Annulata annulifera", candidates, null, id++);
		testCandidateEntry2("Achthophora annulicornis", candidates, null, id++);
		testCandidateEntry2("Euborellia annulipes", candidates, null, id++);
		testCandidateEntry2("Mylia anomala", candidates, null, id++);
		testCandidateEntry2("Anoda thurberi", "Arizona anoda", candidates, null, id++);
		testCandidateEntry2("Hopkinsia anoectocolea", candidates, null, id++);
		testCandidateEntry2("Leptosema anomalum", candidates, null, id++);
		testCandidateEntry2("Aegiphila anomala", candidates, null, id++);
		testCandidateEntry2("Blakea anomala", candidates, null, id++);
		testCandidateEntry2("Paeonia anomala subsp. veitchii", candidates, null, id++);
		testCandidateEntry2("Raukaua anomalus", candidates, null, id++);
		testCandidateEntry2("Margaritaria anomala", candidates, null, id++);
		testCandidateEntry2("Torrenticola anoplopalpa", candidates, null, id++);
		testCandidateEntry2("Xenorhina anorbis", "Fly River Snouted Frog", candidates, null, id++);
		testCandidateEntry2("Euthalia anosia anosia", candidates, null, id++);
		testCandidateEntry2("Oenothera anomala", candidates, null, id++);
		testCandidateEntry2("Spigelia anthelmia", "West Indian pinkroot", candidates, null, id++);
		testCandidateEntry2("Podospora anserina", candidates, null, id++);
		testCandidateEntry2("Crocidura ansellorum", "Ansell's Shrew", candidates, null, id++);
		testCandidateEntry2("Crassula anso-lerouxiae", candidates, null, id++);
		testCandidateEntry2("Ptychadena ansorgii", "Angola Grassland Frog, Ansorge's ridged frog", candidates, null, id++);
		testCandidateEntry2("Cranioleuca antisiensis baroni", "Baron's Spinetail", candidates, null, id++);
		testCandidateEntry2("Coelodonta antiquitatis", "Woolly rhinoceros", candidates, null, id++);
		testCandidateEntry2("Lyciasalamandra antalyana", "Anatolia Lycian Salamander", candidates, null, id++);
		testCandidateEntry2("Discothyrea antarctica", candidates, null, id++);
		testCandidateEntry2("Caulleriella antarctica", candidates, null, id++);
		testCandidateEntry2("Dicksonia antarctica", "Soft Tree Fern", candidates, null, id++);
		testCandidateEntry2("Lymantria antennata", candidates, null, id++);
		testCandidateEntry2("Thelymitra antennifera", "Rabbit-eared sun orchid", candidates, null, id++);
		testCandidateEntry2("Bonatea antennifera", "Green wood orchid", candidates, null, id++);
		testCandidateEntry2("Aphelochaeta antelonga", candidates, null, id++);
		testCandidateEntry2("Wrightia antidysenterica", candidates, null, id++);
		testCandidateEntry2("Aricia anteros", candidates, null, id++);
		testCandidateEntry2("Erinacea anthyllis", "Hedgehog-broom", candidates, null, id++);
		testCandidateEntry2("Arhopala antharita", candidates, null, id++);
		testCandidateEntry2("Arhopala anthelus sotades", candidates, null, id++);
		testCandidateEntry2("Khaya anthotheca", candidates, null, id++);
		testCandidateEntry2("Anthelia (Antheliaceae)", candidates, null, id++);
		testCandidateEntry2("Arhopala anthelus anunda", candidates, null, id++);
		testCandidateEntry2("Arhopala anthelus grahami", candidates, null, id++);
		testCandidateEntry2("Arhopala anthelus marinduquensis", candidates, null, id++);
		testCandidateEntry2("Arhopala anthelus majestatis", candidates, null, id++);
		testCandidateEntry2("Ursinia anthemoides", candidates, null, id++);
		testCandidateEntry2("Soliva anthemifolia", "Button burrweed", candidates, null, id++);
		testCandidateEntry2("Geniostoma antherotrichum", candidates, null, id++);
		testCandidateEntry2("Agapanthia annularis", candidates, null, id++);
		testCandidateEntry2("Dudleya anthonyi", candidates, null, id++);
		testCandidateEntry2("Acacia anthochaera", "Kimberly's Wattle", candidates, null, id++);
		testCandidateEntry2("Chlerogella anthonoma", candidates, null, id++);
		testCandidateEntry2("Acanthoscurria antillensis", candidates, null, id++);
		testCandidateEntry2("Chamaesphecia anthraciformis", candidates, null, id++);
		testCandidateEntry2("Bolitoglossa anthracina", "Coal Black Mushroom-tongue Salamander", candidates, null, id++);
		testCandidateEntry2("Zygaena anthyllidis", candidates, null, id++);
		testCandidateEntry2("Gnidia anthylloides", candidates, null, id++);
		testCandidateEntry2("Pringlea antiscorbutica", "Kerguelen cabbage", candidates, null, id++);
		testCandidateEntry2("Eupithecia anticaria", candidates, null, id++);
		testCandidateEntry2("Justicia antirrhina", candidates, null, id++);
		testCandidateEntry2("Picramnia antidesma", candidates, null, id++);
		testCandidateEntry2("Hypolycaena antifaunus", candidates, null, id++);
		testCandidateEntry2("Glossophaga antillarum", "Jamaican Long-tongued Bat", candidates, null, id++);
		testCandidateEntry2("Diadema antillarum", "Black Long-Spined Sea Urchin", candidates, null, id++);
		testCandidateEntry2("Sternula antillarum", "Least Tern", candidates, null, id++);
		testCandidateEntry2("Arhopala antimuta", candidates, null, id++);
		testCandidateEntry2("Catocala antinympha", candidates, null, id++);
		testCandidateEntry2("Dieffenbachia antioquensis", candidates, null, id++);
		testCandidateEntry2("Passiflora antioquiensis", "Banana passionfruit, Red banana passionflower", candidates, null, id++);
		testCandidateEntry2("Gaultheria antipoda", candidates, null, id++);
		testCandidateEntry2("Euphorbia antiquorum", candidates, null, id++);
		testCandidateEntry2("Scutellaria antirrhinoides", "Nose skullcap", candidates, null, id++);
		testCandidateEntry2("Gastrotheca antoniiochoai", candidates, null, id++);
		testCandidateEntry2("Grallaria saltuensis", "Perija Antpitta", candidates, null, id++);
		testCandidateEntry2("Euphorbia antso", candidates, null, id++);
		testCandidateEntry2("Vigna antunesii", candidates, null, id++);
		testCandidateEntry2("Albizia antunesiana", candidates, null, id++);
		testCandidateEntry2("Campanula andrewsii", candidates, null, id++);
		testCandidateEntry2("Hasora anura anura", candidates, null, id++);
		testCandidateEntry2("Hasora anura", candidates, null, id++);
		testCandidateEntry2("Primula anvilensis", "Boreal primrose", candidates, null, id++);
		testCandidateEntry2("Lerista anyara", "Olkola Slider Skink", candidates, null, id++);
		testCandidateEntry2("Limnonectes maanyanorum", "Maanyan Creek Frog", candidates, null, id++);
		
		SearchIndexBuilder b = new SearchIndexBuilder(15, 3, 20, false, candidates);
		b.iterateOverKeys();
	}
	
	public void testSimpleIterateKeys() throws Exception {
		List<CandidateEntry> candidates = new ArrayList<>();
		
		testCandidateEntry("abc", null, candidates, null, 11);
		testCandidateEntry("abcd", null, candidates, null, 222);
		
		SearchIndexBuilder b = new SearchIndexBuilder(5, 2, 3, false, candidates);
		b.iterateOverKeys();
	}
	
	public void testAntsPlants() throws Exception {
		// "1685" : { "ids" : ".1_1vpF", "latin" : "Plantae", "common" : "Plants" },
		CandidateEntry e = new CandidateEntry();
		CandidateName pname = e.addName("PLANTS", "Plants", "PLANTS", false, false);
		KeyEntry ke = new KeyEntry("ANTS");
		int pscore = SearchIndexBuilder.score(pname, ke);
		assertEquals(21_332, pscore);
		
		// "25150" : { "ids" : ".1_1vno-161h49_2G-K1_4u1s012a1o7X1r-137.3hP22a.1_rD01", "latin" : "Formicidae", "common" : "Ants" },
		CandidateName aname = e.addName("ANTS", "Ants", "ANTS", false, false);
		int ascore = SearchIndexBuilder.score(aname, ke);
		assertEquals(200_002_000, ascore);
		
		CandidateName wname = e.addName("WASPSBEESANDANTS", "Ants", "WASPS BEES AND ANTS", false, false);
		int wscore = SearchIndexBuilder.score(wname, ke);
		assertEquals(20_000_500, wscore);
		
		List<CandidateEntry> candidates = new ArrayList<>();
		testCandidateEntry("Gephyromantis zavona", "Antsahamarana Madagascar Frog", candidates, "ANTS", 2_000_296);
		testCandidateEntry("Gephyromantis zavona", "Antsahamarana Madagascar Frog", candidates, "ANT", 2_010_379);
		testCandidateEntry("Gephyronothin zavona", "Antsahamarana Madagascar Frog", candidates, "ANTS", 2_000_296);
	}
	
	public void testAntsPlantsFull() throws Exception {
		List<CandidateEntry> candidates = new ArrayList<>();
		testCandidateEntry("Hymenoptera", "Wasps, Bees and Ants", candidates, "ANTS", 20_000_500);
		testCandidateEntry("Formicidae", "Ants", candidates, "ANTS", 200_002_000);
		testCandidateEntry("Gephyromantis zavona", "Antsahamarana Madagascar Frog", candidates, "ANTS", 2_000_296);
		SearchIndexBuilder b = new SearchIndexBuilder(3, 3, 4, false, candidates);
		b.iterateOverKeys();
	}
	
	private CandidateEntry testCandidateEntry(String name, List<CandidateEntry> candidates, String key, int expectedScore) {
		return testCandidateEntry("BADLATIN", name, candidates, key, expectedScore);
	}
	private CandidateEntry testCandidateEntry2(String latinName, List<CandidateEntry> candidates, String key, int expectedScore) {
		return testCandidateEntry(latinName, null, candidates, key, expectedScore);
	}
	private CandidateEntry testCandidateEntry2(String latinName, String commonName, List<CandidateEntry> candidates, String key, int expectedScore) {
		return testCandidateEntry(latinName, commonName, candidates, key, expectedScore);
	}
	private CandidateEntry testCandidateEntry(String latinName, String commonName, List<CandidateEntry> candidates, String key, int expectedScore) {
		Entry entry = new Entry();
		entry.setLatinName(latinName);
		entry.setCommonName(commonName);
		entry.setId(Integer.valueOf(expectedScore));
		
		CandidateEntry candidate = new SearchIndexBuilder.CandidateEntry(entry, null);
		candidates.add(candidate);
		
		if (key != null) {
			key = key.toUpperCase();
			testCandidateEntry(candidate, key, expectedScore);
		}
		
		return candidate;
	}
	private void testCandidateEntry(CandidateEntry candidate, String key, int expectedScore) {
		// we care about the common name - use 1, it will work
		int score = SearchIndexBuilder.score(candidate, new KeyEntry(key));
		if (expectedScore >= 0) {
			assertEquals("Score not correct", expectedScore, score);
		}
	}
	
}
