package com.robestone.species;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO there is a problem with me trying to use numbers in an order for
 * the rank because cladus and taxon can go anywhere.  It might not matter
 * because I could just use the tree to determine "numbers" dynamically.
 * I just need to be careful that I don't try and match numbers directly.
 * 
 * TODO another problem is with things like sectio, divisio, etc where they
 * are in different places in the ranking depending on whether we're talking
 * about plants or animals.  This may not matter since I really don't use
 * the numbers for anything...
 * 
 * @author jacob
 *
 */
public enum Rank {

	Tree(1), 

	// these two are general place-holders that can go anywhere
	Cladus(10, "*Branch", "Clade", "Cladi"), 
	Taxon(20, "*Taxon", "Taxa"),
	Subtaxon(25),
	
	// similar... usually means "no good parent"
	Alliance(15),
	Unranked(30, "(Unranked)"), // Virus only
	
	Regio(100),
	Imperium(110),
	Superregnum(120, "*Super-kingdom", "Superkingdom"),
	Regnum(130, "*Kingdom"),
	Subregnum(140, "*Sub-kingdom", "Subkingdom"),
	Infraregnum(145, "*Sub-kingdom", "Infrakingdom"), // TODO need to merge these or something - what does wikidata say?

	Superdomain(147), // TODO this numbering is out of order, so if I do a renumber I will want to fix this
	Domain(146),
	Subdomain(148),
	
	Superphylum(150, "*Super-division"),
	Phylum(160, "*Division", "Divisio (Phylum)"),
	Subphylum(170, "*Sub-division", "Subphyla"),
	Infraphylum(180, "*Sub-division"),
	
	Superdivisio(260, "*Super-division", "Superdivision"), // out of order now...
	Divisio(190, "+Division"),
	Subdivisio(200, "*Sub-division", "Subdivision", "Subdivisione"),
	
	Megaclassis(218),
	Superclassis(220, "*Super-class", "Superclasses", "Superclass"),
	Classis(230, "+Class", "Classes"),
	Subclassis(240, "*Sub-class", "Subclasses", "Subclass", "Sub-classis"),
	
	MorphologicalGroup(231, "*Morphological group", "Morphological group (\u2248Classis)"),
	
	// --
	Infraclassis(245, "*Sub-class", "Infraclasses", "Infraclass"),
	
	Parvclassis(250, "*Sub-class", "Parvclasses"),
	Subterclass(255),
	
//	Superdivisio(260), -- moved it
//	Divisio(270),
//	Subdivisio(280),
	
	Supersectio(490, "*Super-section", "Supersection"), // out of order - I moved these around
	Sectio(290, "+Section", "Sections", "Sectiones", "Sectione"),
	Subsectio(510, "*Sub-section", "Subsection", "Subsect", "Subsectiones", "Subsections"),
	
	// --
	Supergroup(295, "*Super-group"),
	Group(296),
	Subgroup(297, "*Sub-group"),
	InformalGroup(298, "+Informal group", "Informal Group"),
	
	Legio(300, "+Legion", "Legions"),
	Megacohors(305, "+Megacohort"),
	Supercohors(310, "Supercohort"),
	Cohors(320, "+Cohort"),
	Subcohors(330, "*Sub-cohort", "Subcohort"),
	Infracohors(331, "+Infracohort"),
	
	Stem(335),
	
	Magnordo(337, "Magnorder"),
	Superordo(340, "*Super-order", "Superorder"),
	Grandordo(345, "Grandorder"),
	Mirordo(347, "+Mirorder"),
	Ordo(350, "+Order", "Ordines", "Ordine"),
	Hyperordo(360, "Hyperorder", "Hypordo", "Hyporder"),
	Subordo(370, "*Sub-order", "Subordines", "Suborder", "Subordine"),
	Infraordo(380, "*Sub-order", "Infraordines", "Infraorder"),
	
	// -- do not know the order of these
	Parvordo(381, "*Sub-order", "Parvorder"),
	Subinfraordo(382, "*Sub-order"),
	Microordo(385, "*Sub-order"),
	
	Falanx(390),
	Superfamilia(400, "*Super-family", "Superfamily", "Superfamilae", "Superfamiliae", 
			"Superfamlia" // pretty sure this is a misspelling, but it's pretty common
			),
	Suprafamilia(405),
	Epifamilia(407),
	Familia(410, "+Family", "Familiae", "Famila", "Familae"),
	Parafamilia(415),
	Subfamilia(420, "*Sub-family", "Subfamiliae", "Subfamilae", "Subfamily", "Subfamilie", "Subfamila", "Subfamillia", "Subfamilila"),
	
	Supertribus(430, "*Super-tribe", "Supertribe", "Supertribes"),
	Tribus(440, "+Tribe", "Tribu", "Tribes"),
	Supersubtribe(445),
	Subtribus(450, "*Sub-tribe", "Subtribu", "Subtribe", "Subtribes", "Subtribi"),
	Infratribe(452),
	
	Supergenus(460, "*Super-genus"),
	Genus(470, "Genua", "Generus", "Genre", "Genera"),
	Subgenus(480, "*Sub-genus", "Subenus", "Subgenera"),
	GenusGroup(485, "+Genus Group"),
	FormGenus(488, "Form genus", "Form genera"),
	
	Series(520, "Serie"),
	Subseries(535, "Subserie"),
	
	Superspecies(530, "*Super-species"),
	Species(540, "*Species", "Specie", "species", "Spcies", "Specoes"),
	Subspecies(560, "*Sub-species", "Subspeies", "Subpecies", "Subspecie", "Susbspecies", "Sub-Species", "subspecies"), 
		// TODO fix "Susbspecies" and "Sudspecies" and "species" in wikispecies
		//		the only reason I haven't is that on some of these there's so many pages or templates
	SpeciesGroup(565, "+Species Group", "Species group"),
	SpeciesAggregate(566, "+Species Aggregate"),
	
	Klepton(550),
	Hybrid(570),
	Convarietas(580),
	
	Supervarietas(590, "*Super-variety", "Supervariety"),
	Varietas(600, "+Variety", "Varieties", "Varieta",
			"Variant" // used only twice, and I didn't want to edit wikispecies...
			),
	Subvarietas(610, "*Sub-variety", "Subvariety"),
	
	Natio(620),
	Superforma(630, "*Super-form", "Superform", "Superformae"),
	Forma(640, "+Form", "Formae"),
	Subforma(650, "*Sub-form", "Subform", "Subformae"),
	FormaSpecialis(655, "Forma specialis"),
	
	Aberratio(660),
	Lusus(670),
	
	// Viruses (and bacteria?)
	Pathovar(672),
	Strain(675),
	Subtype(676),
	Serotype(678),
	Virus(679),
	
	Synonym(680, "?Synonym", "Synonym?"),
	InvalidCombination(681, "+Invalid combination", "Invalid combinations"),
	FossilSpecies(682, "+Fossil species"),
	TypeSpecies(683, "+Type species"),
	
	Nothosectio(699),
	Nothogenus(700),
	Nothospecies(701),
	Nothosubspecies(702),
	Cultivar(705), // found in between Alliance and Nothogenera
	CultivarGroup(707, "+Cultivar group"),
	Nothovarietas(710),
	
	// extinct species - fossils
	Ichnogenus(820),
	Ichnospecies(840),
	FossilTaxon(860, "+Fossil taxon"), // this is a catch-all that is usually an error, but is common enough
	
	// virus only?
	Realm(900),
	
	Error(-1, false), // used to represent rank not found
	Empty(0, false); // i.e. no real class in this position for this species - not sure I need this at all?
	
	private int rankIndex;
	private Set<String> names;
	private String commonName;
	private boolean isValidRank;
	private String qID;
	
	static {
		// execute after other init
		Set<Integer> rankIds = new HashSet<Integer>();
		for (Rank rank: values()) {
			if (!rankIds.add(rank.rankIndex)) {
				throw new IllegalArgumentException("Rank " + rank + " has duplicate index");
			}
		}
	}

	static {
		Familia.qID = "Q35409";
	}
	
	Rank(int rankIndex, String... alternateNames) {
		this(rankIndex, true, alternateNames);
	}
	/**
	 * @param alternateNames Often due to using the plural instead of the singular
	 */
	Rank(int rankIndex, boolean isValidRank, String... alternateNames) {
		this.isValidRank = isValidRank;
		this.commonName = toString();
		Set<String> set = new HashSet<String>();
		for (String name: alternateNames) {
			if (name.startsWith("*")) {
				this.commonName = name.substring(1);
			} else if (name.startsWith("+")) {
				this.commonName = name.substring(1);
				set.add(commonName);
			} else {
				set.add(name);
			}
		}
		set.add(toString());
		this.rankIndex = rankIndex;
		this.names = Collections.unmodifiableSet(set);
	}
	public boolean isValidRank() {
		return isValidRank;
	}
	public String getCommonName() {
		return commonName;
	}
	public Set<String> getNames() {
		return names;
	}
	public int getRankIndex() {
		return rankIndex;
	}
	public String getQid() {
		return qID;
	}
	public static Rank valueOfWithAlternates(String s) {
		if (s == null) {
			throw new IllegalArgumentException("Rank value cannot be null");
		}
		for (Rank r: values()) {
			if (r.toString().equalsIgnoreCase(s)) {
				return r;
			}
			if (r.commonName.equalsIgnoreCase(s)) {
				return r;
			}
			for (String a: r.names) {
				if (a.equalsIgnoreCase(s)) {
					return r;
				}
			}
		}
		throw new IllegalArgumentException("Rank value (" + s + ") not valid");
	}
	public static Rank valueOf(int i) {
		for (Rank r: values()) {
			if (r.rankIndex == i) {
				return r;
			}
		}
		throw new IllegalArgumentException("Rank value (" + i + ") not valid");
	}
	
}
