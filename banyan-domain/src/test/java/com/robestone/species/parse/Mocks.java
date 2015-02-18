package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.robestone.species.CompleteEntry;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Rank;
import com.robestone.species.Tree;

public class Mocks {

	private void addIds(CompleteEntry root, Set<Integer> used) {
		int id = 0;
		while (!used.add(id)) {
			id = new Random().nextInt(50000);
		}
		root.setId(id);
		if (root.getChildren() != null) {
			for (CompleteEntry child: root.getCompleteEntryChildren()) {
				addIds(child, used);
			}
		}
	}

	public Entry getEntryForName(String name) {
		Entry root = getRoot();
		Entry found = findForName(name, root);
		if (found == null) {
			found = root;
		}
		return found;
	}
	private Entry findForName(String n, Entry e) {
		if (e.getLatinName().equals(n)) {
			return e;
		}
		if (n.equals(e.getCommonName())) {
			return e;
		}
		if (e.hasChildren()) {
			for (Entry c: e.getChildren()) {
				Entry f = findForName(n, c);
				if (f != null) {
					return f;
				}
			}
		}
		return null;
	}
	
	private CompleteEntry root;
	private Tree tree;
	public Tree getTree() {
		if (tree == null) {
			tree = EntryUtilities.buildTree(getRoot());
		}
		return tree;
	}
	public CompleteEntry getRoot() {
		if (root == null) {
			root = doGetRoot();
		}
		return root;
	}
	private CompleteEntry doGetRoot() {
		CompleteEntry root = new CompleteEntry(Rank.Cladus, "Tree of Life", "");
		CompleteEntry top = root;
		CompleteEntry next = root;
		next = addChild(next, Rank.Cladus, "eukaryotes", "Eukaryota");
		next = addChild(next, Rank.Supergroup, "Opisthokonta", "Opisthokonta");
		next = addChild(next, Rank.Regnum, "animals", "Animalia");
		next = addChild(next, Rank.Subregnum, "eumetazoans", "Eumetazoa");
		next = addChild(next, Rank.Cladus, "bilaterians", "Bilateria");
		next = addChild(next, Rank.Cladus, "Deuterostome", "Deuterostomia");
		next = addChild(next, Rank.Phylum, "Chordates", "Chordata");
		next = addChild(next, Rank.Subphylum, "vertebrates", "Vertebrata");
		next = addChild(next, Rank.Infraphylum, "Gnathostomata", "Gnathostomata");
		
		// We don't want the whole chain right now...
		root = next;

		next = addChild(next, Rank.Superclassis, "Tetrapods", "Tetrapoda");

		addChild(next, getMammals());
		addChild(next, getReptiles());
		addChild(next, getSearchCases());
		addChild(next, getGigantopithecus());
		addChild(next, getContrivedForTesting());
		
		addIds(top, new HashSet<Integer>());
		
		return top;
	}

	private CompleteEntry getSearchCases() {
		CompleteEntry searchCases = new CompleteEntry(Rank.Classis, "", "Search Cases");

		CompleteEntry next = searchCases;
		
		// if someone searches for "Friend" we probably want "F"
		next = addChild(next, Rank.Infraclassis, "Friend mean", "M");
		next = addChild(next, Rank.Subclassis, "Friends", "F");
		next = addChild(next, Rank.Infraclassis, "Friend nice", "N");

		next = addChild(next, Rank.Subclassis, "Beaver", "Castor");
		next = addChild(next, Rank.Infraclassis, "Nestor (genus)", "Nestor");

		next = addChild(next, Rank.Infraclassis, "Praire Dog", "p1");

		next = addChild(next, Rank.Infraclassis, "Jacob's dotted lichen", "Bacidia jacobi");
		next = addChild(next, Rank.Infraclassis, "Abscess Root, Spreading Jacob's Ladder, False Jacob's Ladder, American Greek Valerian, Blue bells, Sweatroot", "Polemonium reptans");
		
		next = addChild(next, Rank.Infraclassis, "Jacky Winter", "Microeca fascinans");
		next = addChild(next, Rank.Infraclassis, "Jacobin (hummingbird)", "Florisuga");
		
		next = addChild(next, Rank.Infraclassis, "Needlefish", "Belonidae");
		next = addChild(next, Rank.Infraclassis, "Bowmouth Guitarfish, Bowmouth Wedgefish, Shark Ray", "Rhina");
		
		next = addChild(next, Rank.Infraclassis, "Tengmalm's Owl, Boreal Owl", "Aegolius funereus");
		next = addChild(next, Rank.Infraclassis, "Boreal Toad", "Anaxyrus boreas boreas");

		next = addChild(next, Rank.Cladus, "Raccoons", "Procyonidae");
		next = addChild(next, Rank.Cladus, "Raccoons", "Procyon");
		next = addChild(next, Rank.Cladus, "Common Raccoon", "Procyon lotor");
		next = addChild(next, Rank.Cladus, "Baboon", "Papio");
		next = addChild(next, Rank.Cladus, "Siberian Salamanders", "Ranodon");
		next = addChild(next, Rank.Cladus, "Semirechensk Salamander", "Ranodon sibiricus");
		next = addChild(next, Rank.Cladus, "", "Lacon punctatus");
		
		next = addChild(next, Rank.Cladus, "Gray Langurs", "Semnopithecus");
		next = addChild(next, Rank.Cladus, "Francois' Langur, Francois' Leaf Monkey", "Trachypithecus francoisi");
		next = addChild(next, Rank.Cladus, "Gee's Golden Langur, Golden Langur", "Trachypithecus geei");
		next = addChild(next, Rank.Cladus, "", "Lanius");

		next = addChild(next, Rank.Cladus, "", "Arctocephalus");
		next = addChild(next, Rank.Cladus, "Bowhead, Bowhead Whale, Arctic Whale, Greenland Right Whale", "Balaena");
		next = addChild(next, Rank.Cladus, "Bowhead, Bowhead Whale, Arctic Whale, Greenland Right Whale", "Balaena mysticetus");
		
		next = addChild(next, Rank.Cladus, "Lion", "Panthera Leo");
		next = addChild(next, Rank.Cladus, "Chestnut Long-tongued Bat", "Lionycteris");
		
		next = addChild(next, Rank.Cladus, "Rabbit's Foot Clover", "Trifolium arvense");
		next = addChild(next, Rank.Cladus, "Amami Rabbit", "Pentalagus");
		next = addChild(next, Rank.Cladus, "Rabbits and Hares", "Leporidae");

		return searchCases;
	}
	private CompleteEntry getReptiles() {
		CompleteEntry reptiles = new CompleteEntry(Rank.Classis, "", "Reptilia");

		CompleteEntry next = reptiles;
		
		next = addChild(next, Rank.Subclassis, "Diapsid", "Diapsida");
		next = addChild(next, Rank.Infraclassis, "", "Lepidosauromorpha");
		next = addChild(next, Rank.Superordo, "", "Lepidosauria");
		next = addChild(next, Rank.Ordo, "Scaled Reptiles", "Squamata");
		next = addChild(next, Rank.Subordo, "Snakes", "Serpentes");
		CompleteEntry snakes = next;
		
		next = addChild(next, Rank.Superfamilia, "", "Colubroidea");
		next = addChild(next, Rank.Familia, "Colubrid", "Colubridae");
		next = addChild(next, Rank.Subfamilia, "", "Natricinae");
		next = addChild(next, Rank.Genus, "", "Natrix");
		next = addChild(next, Rank.Species, "Grass Snake", "Natrix natrix");
		
		next = addChild(snakes, Rank.Superfamilia, "", "Booidea");
		next = addChild(next, Rank.Familia, "Boas", "Boidae");
		next = addChild(next, Rank.Subfamilia, "True Boas", "Boinae");
		next = addChild(next, Rank.Genus, "Anacondas", "Eunectes");
		next = addChild(next, Rank.Species, "Bolevian Fakething", "Eunectes fakisis");
		next = addChild(next, Rank.Species, "Bolivian Anaconda", "Eunectes beniensis");
		next = addChild(next, Rank.Species, "French Aniconda", "Eunectes francois");
		next = addChild(next, Rank.Species, "Bolivian Boa", "Boinae beniensis");
		
		return reptiles;
	}

	private CompleteEntry getContrivedForTesting() {
		CompleteEntry root = new CompleteEntry(Rank.Classis, "Testing", "Testerificus");

		CompleteEntry next = root;
		addChild(next, Rank.Species, "Finky", "T1");
		addChild(next, Rank.Species, "T2", "Finky");
		addChild(next, Rank.Species, "Finkies", "T3");
		addChild(next, Rank.Species, "T4", "Finkies");
		
		addChild(next, Rank.Species, "Thing Finky", "T5");
		addChild(next, Rank.Species, "T6", "Thing Finky");
		addChild(next, Rank.Species, "Things Finkies", "T7");
		addChild(next, Rank.Species, "T8", "Things Finkies");

		addChild(next, Rank.Species, "Finkyt", "T1S");
		addChild(next, Rank.Species, "T2S", "Finkyt");
		addChild(next, Rank.Species, "Finkiest", "T3S");
		addChild(next, Rank.Species, "T4S", "Finkiest");
		
		addChild(next, Rank.Species, "Thing Finkyt", "T5S");
		addChild(next, Rank.Species, "T6S", "Thing Finkyt");
		addChild(next, Rank.Species, "Things Finkiest", "T7S");
		addChild(next, Rank.Species, "T8S", "Things Finkiest");

		addChild(next, Rank.Species, "tFinkyt", "T1C");
		addChild(next, Rank.Species, "T2C", "tFinkyt");
		addChild(next, Rank.Species, "tFinkiest", "T3C");
		addChild(next, Rank.Species, "T4C", "tFinkiest");

		addChild(next, Rank.Species, "fimky", "F1");
		addChild(next, Rank.Species, "finksie", "F2");

		addChild(next, Rank.Species, "phunky phrase", "P1");
		addChild(next, Rank.Species, "the phunky phrase", "P2");
		addChild(next, Rank.Species, "the phunky phrase", "P3");
		addChild(next, Rank.Species, "the phunky phrase master", "P4");
		addChild(next, Rank.Species, "the phunky little phrase master", "P5");
		addChild(next, Rank.Species, "the little phrase mixster phunky master", "P6");

		addChild(next, Rank.Species, "longerphase", "F3");
		addChild(next, Rank.Species, "long erphase", "F4");

		addChild(next, Rank.Species, "venus fly trap", "V1");

		return root;
	}

	private CompleteEntry getGigantopithecus() {
		CompleteEntry root = new CompleteEntry(Rank.Classis, "Monkeys and Apes", "Monkeynusses");

		CompleteEntry next = root;
		next = addChild(next, Rank.Species, "", "Gigantopithecus");
		next = addChild(next, Rank.Species, "Lion tamarin", "Leontopithecus");
		return root;
	}
	
	private CompleteEntry getMammals() {
		CompleteEntry mammals = new CompleteEntry(Rank.Classis, "Mammals", "Mammalia");

		CompleteEntry next = mammals;
		next = addChild(next, Rank.Subclassis, "", "Theria");
		next = addChild(next, Rank.Infraclassis, "Placental mammals", "Placentalia");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Ordo, "Rodents", "Rodentia");
		CompleteEntry rodents = next;
		
		next = addChild(next, Rank.Subordo, "Squirrel-like rodents", "Sciuromorpha");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Familia, "Mountain Beaver", "Aplodontiidae");
		next = addChild(next, Rank.Subfamilia, "Mountain Beaver", "Aplodontiinae");
		next = addChild(next, Rank.Genus, "Mountain Beaver", "Aplodontia");
		next = addChild(next, Rank.Species, "Mountain Beaver", "Aplodontia rufa");
		
		next = addChild(rodents, Rank.Subordo, "", "Castorimorpha");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Familia, "", "Heteromyidae");
		next = addChild(next, Rank.Subfamilia, "", "Perognathinae");
		next = addChild(next, Rank.Genus, "", "Chaetodipus");
		next = addChild(next, Rank.Species, "Lined Pocket Gopher", "Chaetodipus lineatus");
	
		next = addChild(mammals, Rank.Subclassis, "", "Prototheria");
//		next = addChild(next, Rank.Empty, "", "");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Ordo, "Monotremes/Egg-laying mammals", "Monotremata");
//		next = addChild(next, Rank.Empty, "", "");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Familia, "Echidna", "Tachyglossidae");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Genus, "", "Zaglossus");
		next = addChild(next, Rank.Species, "Western Long-beaked Echidna", "Zaglossus bruijni");
		
		return mammals;
	}

	private CompleteEntry addChild(CompleteEntry parent, Rank level, String common, String latin) {
		// this is a temp measure instead of writing more complex logic elsewhere
		if (level == Rank.Empty) {
			return parent;
		}
		CompleteEntry entry = new CompleteEntry();
		entry.setCommonName(common);
		entry.setLatinName(latin);
		entry.setRank(level);
		return addChild(parent, entry);
	}
	private CompleteEntry addChild(CompleteEntry parent, CompleteEntry child) {
		child.setParent(parent);
		List<CompleteEntry> children = parent.getCompleteEntryChildren();
		if (children == null) {
			children = new ArrayList<CompleteEntry>();
			parent.setChildren(children);
		}
		children.add(child);
		return child;
	}
	
}
