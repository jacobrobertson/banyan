package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Rank;
import com.robestone.species.Tree;

public class Mocks {

	public Mocks() {
		getRoot();
	}
	
	private void addIds(Entry root) {
//		addIds(root, new HashSet<Integer>());
		addIds(root, 0, new HashSet<Integer>());
	}
	
	int addIds(Entry root, int seed, Set<Integer> used) {
		boolean added = used.add(seed);
		if (!added) {
			throw new IllegalStateException("duplicate id " + seed);
		}
//		System.out.println("addIds." + seed + ":" + root.getLatinName() + "/" + root.getCommonName());
		root.setId(seed);
		int nextId = seed + 100;
		if (root.getChildren() != null) {
			for (Entry child: root.getCompleteEntryChildren()) {
				nextId = addIds(child, nextId, used);
				nextId = nextId + 1;
			}
		}
		return nextId;
	}
	void addIds(Entry root, Set<Integer> used) {
		int id = 10;
		while (!used.add(id)) {
			id = new Random().nextInt(50000);
		}
		root.setId(id);
		if (root.getChildren() != null) {
			for (Entry child: root.getCompleteEntryChildren()) {
				addIds(child, used);
			}
		}
	}

	public Entry getEntryForId(int id) {
		return EntryUtilities.findEntry(getRoot(), id);
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
	
	private Entry root;
	private Tree tree;
	public Set<Entry> getEntries() {
		return EntryUtilities.getEntries(getRoot());
	}
	public Tree getTree() {
		if (tree == null) {
			tree = EntryUtilities.buildTree(getRoot());
		}
		return tree;
	}
	public Entry getRoot() {
		if (root == null) {
			root = doGetRoot();
		}
		return root;
	}
	private Entry doGetRoot() {
		Entry root = new Entry(Rank.Cladus, "Tree of Life", "");
		Entry top = root;
		Entry next = root;
		next = addChild(next, Rank.Cladus, "eukaryotes", "Eukaryota");
		next = addChild(next, Rank.Supergroup, "Opisthokonta", "Opisthokonta");
		next = addChild(next, Rank.Kingdom, "animals", "Animalia");
		next = addChild(next, Rank.Subkingdom, "eumetazoans", "Eumetazoa");
		next = addChild(next, Rank.Cladus, "bilaterians", "Bilateria");
		next = addChild(next, Rank.Cladus, "Deuterostome", "Deuterostomia");
		next = addChild(next, Rank.Phylum, "Chordates", "Chordata");
		next = addChild(next, Rank.Subphylum, "vertebrates", "Vertebrata");
		next = addChild(next, Rank.Infraphylum, "Gnathostomata", "Gnathostomata");
		
		// We don't want the whole chain right now...
		root = next;

		next = addChild(next, Rank.Superclass, "Tetrapods", "Tetrapoda");

		addChild(next, getMammals());
		addChild(next, getReptiles());
		addChild(next, getSearchCases());
		addChild(next, getGigantopithecus());
		addChild(next, getContrivedForTesting());
		
		addIds(top);
		
		return top;
	}

	private Entry getSearchCases() {
		Entry searchCases = new Entry(Rank.Class, "", "Search Cases");

		Entry next = searchCases;
		
		// if someone searches for "Friend" we probably want "F"
		next = addChild(next, Rank.Infraclass, "Friend mean", "M");
		next = addChild(next, Rank.Subclass, "Friends", "F");
		next = addChild(next, Rank.Infraclass, "Friend nice", "N");

		next = addChild(next, Rank.Subclass, "Beaver", "Castor");
		next = addChild(next, Rank.Infraclass, "Nestor (genus)", "Nestor");

		next = addChild(next, Rank.Infraclass, "Praire Dog", "p1");

		next = addChild(next, Rank.Infraclass, "Jacob's dotted lichen", "Bacidia jacobi");
		next = addChild(next, Rank.Infraclass, "Abscess Root, Spreading Jacob's Ladder, False Jacob's Ladder, American Greek Valerian, Blue bells, Sweatroot", "Polemonium reptans");
		
		next = addChild(next, Rank.Infraclass, "Jacky Winter", "Microeca fascinans");
		next = addChild(next, Rank.Infraclass, "Jacobin (hummingbird)", "Florisuga");
		
		next = addChild(next, Rank.Infraclass, "Needlefish", "Belonidae");
		next = addChild(next, Rank.Infraclass, "Bowmouth Guitarfish, Bowmouth Wedgefish, Shark Ray", "Rhina");
		
		next = addChild(next, Rank.Infraclass, "Tengmalm's Owl, Boreal Owl", "Aegolius funereus");
		next = addChild(next, Rank.Infraclass, "Boreal Toad", "Anaxyrus boreas boreas");

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
	private Entry getReptiles() {
		Entry reptiles = new Entry(Rank.Class, "", "Reptilia");

		Entry next = reptiles;
		
		next = addChild(next, Rank.Subclass, "Diapsid", "Diapsida");
		next = addChild(next, Rank.Infraclass, "", "Lepidosauromorpha");
		next = addChild(next, Rank.Superorder, "", "Lepidosauria");
		next = addChild(next, Rank.Order, "Scaled Reptiles", "Squamata");
		next = addChild(next, Rank.Suborder, "Snakes", "Serpentes");
		Entry snakes = next;
		
		next = addChild(next, Rank.Superfamily, "", "Colubroidea");
		next = addChild(next, Rank.Family, "Colubrid", "Colubridae");
		next = addChild(next, Rank.Subfamily, "", "Natricinae");
		next = addChild(next, Rank.Genus, "", "Natrix");
		next = addChild(next, Rank.Species, "Grass Snake", "Natrix natrix");
		
		next = addChild(snakes, Rank.Superfamily, "", "Booidea");
		next = addChild(next, Rank.Family, "Boas", "Boidae");
		next = addChild(next, Rank.Subfamily, "True Boas", "Boinae");
		next = addChild(next, Rank.Genus, "Anacondas", "Eunectes");
		next = addChild(next, Rank.Species, "Bolevian Fakething", "Eunectes fakisis");
		next = addChild(next, Rank.Species, "Bolivian Anaconda", "Eunectes beniensis");
		next = addChild(next, Rank.Species, "French Aniconda", "Eunectes francois");
		next = addChild(next, Rank.Species, "Bolivian Boa", "Boinae beniensis");
		
		return reptiles;
	}

	private Entry getContrivedForTesting() {
		Entry root = new Entry(Rank.Class, "Testing", "Testerificus");

		Entry next = root;
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

	private Entry getGigantopithecus() {
		Entry root = new Entry(Rank.Class, "Monkeys and Apes", "Monkeynusses");

		Entry next = root;
		next = addChild(next, Rank.Species, "", "Gigantopithecus");
		next = addChild(next, Rank.Species, "Lion tamarin", "Leontopithecus");
		return root;
	}
	
	private Entry getMammals() {
		Entry mammals = new Entry(Rank.Class, "Mammals", "Mammalia");

		Entry next = mammals;
		next = addChild(next, Rank.Subclass, "", "Theria");
		next = addChild(next, Rank.Infraclass, "Placental mammals", "Placentalia");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Order, "Rodents", "Rodentia");
		Entry rodents = next;
		
		next = addChild(next, Rank.Suborder, "Squirrel-like rodents", "Sciuromorpha");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Family, "Mountain Beaver", "Aplodontiidae");
		next = addChild(next, Rank.Subfamily, "Mountain Beaver", "Aplodontiinae");
		next = addChild(next, Rank.Genus, "Mountain Beaver", "Aplodontia");
		next = addChild(next, Rank.Species, "Mountain Beaver", "Aplodontia rufa");
		
		next = addChild(rodents, Rank.Suborder, "", "Castorimorpha");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Family, "", "Heteromyidae");
		next = addChild(next, Rank.Subfamily, "", "Perognathinae");
		next = addChild(next, Rank.Genus, "", "Chaetodipus");
		next = addChild(next, Rank.Species, "Lined Pocket Gopher", "Chaetodipus lineatus");
	
		next = addChild(mammals, Rank.Subclass, "", "Prototheria");
//		next = addChild(next, Rank.Empty, "", "");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Order, "Monotremes/Egg-laying mammals", "Monotremata");
//		next = addChild(next, Rank.Empty, "", "");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Family, "Echidna", "Tachyglossidae");
//		next = addChild(next, Rank.Empty, "", "");
		next = addChild(next, Rank.Genus, "", "Zaglossus");
		next = addChild(next, Rank.Species, "Western Long-beaked Echidna", "Zaglossus bruijni");
		
		return mammals;
	}

	private Entry addChild(Entry parent, Rank level, String common, String latin) {
		// this is a temp measure instead of writing more complex logic elsewhere
		if (level == Rank.Empty) {
			return parent;
		}
		Entry entry = new Entry();
		entry.setCommonName(common);
		entry.setLatinName(latin);
		entry.setRank(level);
		return addChild(parent, entry);
	}
	private Entry addChild(Entry parent, Entry child) {
		child.setParent(parent);
		List<Entry> children = parent.getCompleteEntryChildren();
		if (children == null) {
			children = new ArrayList<Entry>();
			parent.setChildren(children);
		}
		children.add(child);
		return child;
	}
	
}
