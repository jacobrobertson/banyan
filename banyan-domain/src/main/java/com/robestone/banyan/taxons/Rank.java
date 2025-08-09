package com.robestone.banyan.taxons;

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

	Tree(null, 1), 

	/*
	 * Cladus/Unknown: Cladus is the Latin term for a clade. A clade is a group of
	 * organisms that are monophyletic, meaning they consist of a common ancestor
	 * and all its descendants. The English equivalent, clade, is more commonly used
	 * in scientific discourse today, particularly in phylogenetic systematics,
	 * which focuses on evolutionary relationships.
	 */
	Cladus(null, 10, "*Branch", "Clade", "Cladi"), 
	
	Taxon(null, 20, "*Taxon", "Taxa"),
	Subtaxon(null, 25),
	
	// In taxonomy, alliance is an informal grouping used in biological taxonomy,
	// not a formal rank. It is often used for a group of species, genera, or tribes
	// provisionally considered closely related.
	// historically some 19th century botanical authors used alliance to denote groups that would now be considered orders, but this usage is now obsolete.
	// TODO move to "InformalGroups"?? - or is an alliance usually for certain taxons
	Alliance(null, 15),
	
	// I'm using this casually as "null" but that's not really great either
	Unranked(null, 30, "(Unranked)"), // Virus only
	
	// Regio/Unknown: In the context of biological taxonomy, Regio was proposed as the Latin form for Domain, the highest taxonomic rank. Domain is now widely used.
	// TODO make this an alternate name for Domain - need to fix the Databases though
	Regio(null, RankType.Domain, 100),
	
	// Imperium/Unknown: In the context of classification, Imperium appears in older
	// systems like Linnaeus's classification, where he established three kingdoms:
	// Regnum Animale, Regnum Vegetabile, and Regnum Lapideum. It is not a modern
	// taxonomic rank.
	Imperium(null, RankType.Kingdom, 110), // above kingdoms like "super-super" kingdom - so this is okay
	
	Superkingdom(null, 120, "Superregnum"), // TODO same as "domain"
	Kingdom(Superkingdom, 130, "Regnum"),
	Subkingdom(Kingdom, 140, "Subregnum"), 
	Infrakingdom(Subkingdom, 145, "Infraregnum"),
	Parvkingdom(Infrakingdom, 143), // TODO numbering is out of order 

	Superdomain(null, 147), // TODO this numbering is out of order, so if I do a renumber I will want to fix this
	Domain(Superdomain, 146),
	Subdomain(Domain, 148),
	
	Superphylum(null, 150, "*Super-division"),
	
	// Phylum Primarily used for: Animals, as well as archaea, bacteria, protists, and fungi.
	Phylum(Superphylum, 160, "*Division", "Divisio (Phylum)"),
	Subphylum(Phylum, 170, "*Sub-division", "Subphyla"),
	Infraphylum(Subphylum, 180, "*Sub-division"),
	
	Superdivision(null, 260, "Superdivisio"), // out of order now...
	
	// Division Traditionally used for: Plants, algae, and fungi
	Division(Superdivision, 190, "Divisio"),
	Subdivision(Division, 200, "Subdivisio", "Subdivisione"),
	Infradivision(Subdivision, 202),
	
	Megaclass(null, 218, "Megaclassis"),
	Superclass(Megaclass, 220, "*Super-class", "Superclasses", "Superclassis"),
	Class(Superclass, 230, "Classis", "Classes"),
	Subclass(Class, 240, "*Sub-class", "Subclasses", "Subclassis", "Sub-classis"),
	
	// MorphologicalGroup/Unknown: This term describes groupings of organisms based
	// on shared physical characteristics. While useful in some contexts (like
	// vegetation mapping), it is not a formal taxonomic rank and may not
	// necessarily reflect evolutionary relationships.
	// TODO might go along with other arbitrary rankings
	MorphologicalGroup(null, 231, "*Morphological group", "Morphological group (\u2248Classis)"),
	
	// TODO do I really want to map the common name like this?
	Infraclass(null, 245, "*Sub-class", "Infraclasses", "Infraclassis"),
	
	Parvclass(null, 250, "*Sub-class", "Parvclasses", "Parvclassis"),
	Subterclass(null, 255),
	
	// In botany, sectio (section) is a taxonomic rank below the genus and above the species, used to organize large genera. It is a valid taxonomic rank.
	Supersection(null, 490, "*Super-section", "Supersectio"), // out of order - I moved these around
	Section(Supersection, 290, "Sectio", "Sections", "Sectiones", "Sectione"),
	Subsection(Section, 510, "Sub-section", "Subsectio", "Subsect", "Subsectiones", "Subsections"),
	
	// --
	Supergroup(null, 295, "*Super-group"),
	Group(Supergroup, 296),
	Subgroup(Group, 297, "*Sub-group"),
	InformalGroup(Subgroup, 298, "+Informal group", "Informal Group"),
	
	
	// In zoology, legion is a non-obligatory rank between class and cohort
	Superlegion(null, 299),
	Legion(Superlegion, 300, "+Legion", "Legions", "Legio"),
	Infralegion(Legion, 301),
	Sublegion(Infralegion, 302),
	
	// Cohors was used as a higher rank in botanical classification in the 19th
	// century, before family was assigned to its current rank. In zoology, cohort
	// is a non-obligatory rank below legion and above superfamily. Megacohors,
	// supercohors, subcohors, and infracohors are further subdivisions within this
	// non-obligatory hierarchy, primarily used in zoology.
	Megacohors(null, 305, "+Megacohort"),
	Supercohors(Megacohors, 310, "Supercohort"),
	Cohors(Supercohors, 320, "+Cohort"),
	Subcohors(Cohors, 330, "*Sub-cohort", "Subcohort"),
	Infracohors(Subcohors, 331, "+Infracohort"),
	
	Stem(null, 335),
	
	Magnorder(null, 337, "Magnordo"),
	Superorder(Magnorder, 340, "Super-order", "Superordo"),
	Grandorder(Superorder, 345, "Grandordo"),
	Mirorder(Grandorder, 347, "Mirordo"),
	Order(Mirorder, 350, "Ordo", "Ordines", "Ordine"),
	Hyperorder(Order, 360, "Hyperordo", "Hypordo", "Hyporder"),
	Suborder(Hyperorder, 370, "Sub-order", "Subordines", "Subordo", "Subordine"),
	Infraorder(Suborder, 380, "Sub-order", "Infraordines", "Infraordo"),
	
	// -- do not know the order of these
	Parvorder(null, 381, "*Sub-order", "Parvordo"),
	Subinfraorder(null, 382, "*Sub-order", "Subinfraordo"),
	Nanorder(null, 383), // supposed to be right under parvordo
	Microorder(null, 385, "*Sub-order", "Microordo"),
	
	// TODO get rid of this, or figure it out
	Falanx(null, 390),
	
	Superfamily(null, 400, "*Super-family", "Superfamilia", "Superfamilae", "Superfamiliae", 
			"Superfamlia" // pretty sure this is a misspelling, but it's pretty common
			),
	Suprafamily(null, 405, "Suprafamilia"),
	Epifamily(null, 407, "Epifamilia"),
	Family(null, 410, "Familia", "Familiae", "Famila", "Familae"),
	Parafamily(null, 415, "Parafamilia"),
	Subfamily(null, 420, "Subfamilia", "Subfamiliae", "Subfamilae", "Subfamily", "Subfamilie", "Subfamila", "Subfamillia", "Subfamilila"),
	
	// The taxonomic rank of tribe (Latin: tribus) fits within the standard Linnaean
	// hierarchy between the Family and the Genus. It is considered an optional
	// rank, meaning it is not always used in all classifications, but it serves a
	// useful purpose in organizing large families.
	Supertribe(null, 430, "*Super-tribe", "Supertribus", "Supertribes"),
	Tribe(Supertribe, 440, "Tribus", "Tribu", "Tribes"),
	Supersubtribe(Tribe, 445, "Supersubtribus"), // TODO surely this isn't a thing?
	Subtribe(Supersubtribe, 450, "*Sub-tribe", "Subtribu", "Subtribus", "Subtribes", "Subtribi"),
	Infratribe(Subtribe, 452, "Infratribus"),
	
	GenusGroup(null, RankType.Genus, 485, "+Genus Group"),
	Supergenus(GenusGroup, 460, "*Super-genus"),
	Genus(Supergenus, 470, "Genua", "Generus", "Genre", "Genera"),
	Subgenus(Genus, 480, "*Sub-genus", "Subenus", "Subgenera"),
	
	// FormGenus: Definition: An artificial taxonomic category established for organisms, especially fossils or imperfect fungi, where the true relationships are obscure due to incomplete knowledge of their structure, development, or life history.
	// Explanation: Form genera are based on morphological similarities rather than on phylogenetic relationships.
	FormGenus(Subgenus, 488, "Form genus", "Form genera"),
	
	Series(null, 520, "Serie"),
	Subseries(null, 535, "Subserie"),
	
	SpeciesGroup(null, RankType.Species, 565, "+Species Group", "Species group"),
	Superspecies(SpeciesGroup, 530, "*Super-species"),
	Species(Superspecies, 540, "*Species", "Specie", "species", "Spcies", "Specoes"),
	Subspecies(Species, 560, "*Sub-species", "Subspeies", "Subpecies", "Subspecie", "Susbspecies", "Sub-Species", "subspecies"), 
		// TODO fix "Susbspecies" and "Sudspecies" and "species" in wikispecies
		//		the only reason I haven't is that on some of these there's so many pages or templates
	SpeciesAggregate(null, 566, "+Species Aggregate"),
	
	// Klepton refers to a type of hybrid, particularly in amphibians, that
	// reproduces by stealing genetic material from another species. It is a
	// specialized term for hybrid complexes.
	Klepton(null, 550),
	Hybrid(null, 570),
	
	// Convarietas (convariety) was previously used to describe a grouping of
	// cultivars in horticulture but has been replaced by "Group".
	// TODO fix this - it's no longer used, so should be an alternate name at best
	Convarietas(null, 580),
	
	Supervarietas(null, 590, "*Super-variety", "Supervariety"),
	
	// In botany, varietas (variety) is a taxonomic rank below subspecies and above form. It is a valid rank.
	Varietas(null, 600, "+Variety", "Varieties", "Varieta",
			"Variant" // used only twice, and I didn't want to edit wikispecies...
			),
	Subvarietas(null, 610, "*Sub-variety", "Subvariety"),
	
	// TODO - unknown
	Natio(null, 620),
	
	Superforma(null, 630, "*Super-form", "Superform", "Superformae"),
	Forma(null, 640, "+Form", "Formae"),
	Subforma(null, 650, "*Sub-form", "Subform", "Subformae"),
	FormaSpecialis(null, 655, "Forma specialis"),
	
	Aberratio(null, 660),
	Lusus(null, 670),
	
	// Viruses (and bacteria?)
	/*
	 * Species: The foundational taxonomic unit. 
	 * 
	 * Strain: A group of organisms within a species distinguished by particular characteristics. 
	 * 
	 * Biovar, Pathovar, Serovar/Serotype: These are specific ways to classify and differentiate
	 * strains based on different criteria: 
	 * 		A strain can belong to a particular biovar, based on its biochemical properties. 
	 * 		A strain can belong to a particular pathovar, based on the diseases it causes. 
	 * 		A strain can belong to a particular serovar/serotype, based on its antigenic characteristics. 
	 * 
	 * A single strain might be described using combinations of these terms, such as a
	 * particular E. coli strain being identified by its serotype (e.g., O157:H7)
	 * and its pathotype (e.g., associated with bloody diarrhea and hemolytic uremic
	 * syndrome). Therefore, these terms represent different lenses through which to
	 * examine and categorize variation below the species level, with strain serving
	 * as the foundational concept for these more specific distinctions.
	 */
	Realm(Tree, RankType.Type, 900), // EXCEPT WD says Virus "itself" is a Domain
	Virus(Realm, RankType.Type, 679), // "Virus" isn't a rank?
	Strain(Virus, RankType.Type, 675),
	Pathotype(Strain, 672, "Pathovar"),
	Biotype(Strain, 673, "Biovar"),
	Serotype(Strain, 678),
	Subtype(null, 676),
	
	Synonym(null, 680, "?Synonym", "Synonym?"),
	InvalidCombination(null, 681, "+Invalid combination", "Invalid combinations"),
	FossilSpecies(null, 682, "+Fossil species"),
	TypeSpecies(null, 683, "+Type species"),
	
	Nothosectio(null, 699),
	Nothogenus(null, 700),
	Nothospecies(null, 701),
	Nothosubspecies(null, 702),
	
	/*
		Chemovar: A strain or group of strains distinguished by differences in their chemical characteristics, often related to specific metabolites or chemical reactions. Oregon State University mentions the term "chemoform" as a synonym.
		Morphovar: A strain or group of strains distinguished by morphological differences that are not significant enough to warrant a separate subspecies or species designation.
		Phagovar: A strain or group of strains distinguished by their susceptibility to different bacteriophages (viruses that infect bacteria). the National Institutes of Health (NIH) uses the example Staphylococcus aureus phagovar 81.
		Phase: Used to describe different stages in the life cycle or phenotypic variations within a strain.
		State: Similar to phase, used to denote a particular condition or form of a bacterial strain.  
	 */
//	Candidatus(null, null, 706), // the classification and naming of uncultivated or poorly characterized bacteria and archaea
	CultivarGroup(null, RankType.Cultivar, 707, "+Cultivar group"),
	Cultivar(null, 705), // found in between Alliance and Nothogenera
	Nothovarietas(null, 710),
	
	// extinct species - fossils
	Ichnogenus(null, 820),
	Ichnospecies(null, 840),
	FossilTaxon(null, 860, "+Fossil taxon"), // this is a catch-all that is usually an error, but is common enough
	
	Error(null, -1, false), // used to represent rank not found
	Empty(null, 0, false); // i.e. no real class in this position for this species - not sure I need this at all?
	
	
	private enum RankField {
		Zoology, Botany, Microbiology, General
	}
	
	public enum RankType {
		
	Tree(RankField.General, null),
		
		Domain(RankField.General, RankType.Tree),
			Kingdom(RankField.General, RankType.Domain),
				Phylum_Division(RankField.General, RankType.Tree), // TODO I should split this out, and have "botanical" as another thing I can add
					Class(RankField.General, RankType.Tree),
					
						Order(RankField.General, RankType.Class),
						Legion(RankField.Zoology, RankType.Class),
							
							Tribe(RankField.General, RankType.Order),
						
							Cohors(RankField.Botany, RankType.Order), // TODO this is above Family (only for botany), so how do I indicate that? also not sure on the parent
							Family(RankField.General, RankType.Tribe),
							
								Genus(RankField.General, RankType.Family),
									
									// intermediate types for certain fields
									Section(RankField.Botany, RankType.Genus),
									
										// TODO although this is below Genus, the other intermediates are above it, so how do I represent that?
										Species(RankField.General, RankType.Genus),
										
											// Co-equal, as cultivar is a cultivated variety as opposed to naturally occurring variety
											Varietas(RankField.Botany, RankType.Species),
											Cultivar(RankField.Botany, RankType.Species),
											
												Forma(RankField.Botany, RankType.Varietas),
						
		Type(RankField.Microbiology, RankType.Tree),
		
		
		Other(RankField.General, RankType.Tree),
		Unknown(RankField.General, RankType.Tree);
		
		// I use this to "enforce" that I know how I want these ranked
//		private RankField rankField;
//		private RankType parent;
		
		private RankType(RankField rankField, RankType parent) {
//			this.parent = parent;
//			this.rankField = rankField;
		}

		static RankType toRankType(Rank rank) {
			RankType found = Unknown;
			for (RankType rt : RankType.values()) {
				String[] tests = rt.name().toUpperCase().split("_");
				for (String test : tests) {
					if (rank.name().toUpperCase().endsWith(test)) {
						found = rt;
						break;
					}
				}
			}
			return found;
		}
		
	}
	
	private int rankIndex;
	private Set<String> names;
	private String commonName;
	private boolean isValidRank;
	private RankType rankType;
//	private Rank parent;
	
	static {
		// execute after other init
		Set<Integer> rankIds = new HashSet<Integer>();
		for (Rank rank: values()) {
			if (!rankIds.add(rank.rankIndex)) {
				throw new IllegalArgumentException("Rank " + rank + " has duplicate index");
			}
		}
	}

	Rank(Rank parent, int rankIndex, String... alternateNames) {
		this(parent, null, rankIndex, alternateNames);
	}
	Rank(Rank parent, RankType rankType, int rankIndex, String... alternateNames) {
		this(parent, rankType, rankIndex, true, alternateNames);
	}
	/**
	 * @param alternateNames Often due to using the plural instead of the singular
	 */
	Rank(Rank parent, int rankIndex, boolean isValidRank, String... alternateNames) {
		this(parent, null, rankIndex, isValidRank, alternateNames);
	}
	Rank(Rank parent, RankType rankType, int rankIndex, boolean isValidRank, String... alternateNames) {
//		this.parent = parent;
		this.isValidRank = isValidRank;
		this.commonName = toString();
		Set<String> set = new HashSet<String>();
		for (String name: alternateNames) {
			// TODO once I get this straightened out, I shouldn't need this logic, because the "Name" is the correct thing and all else is for parsing
			if (name.startsWith("*")) {
				// * = common name = yes, alternate name = no
				this.commonName = name.substring(1);
			} else if (name.startsWith("+")) {
				// + = common name = yes, alternate name = yes
				this.commonName = name.substring(1);
				set.add(commonName);
			} else {
				set.add(name);
			}
		}
		set.add(toString());
		this.rankIndex = rankIndex;
		this.names = Collections.unmodifiableSet(set);
		if (rankType == null) {
			this.rankType = RankType.toRankType(this);
		} else {
			this.rankType = rankType;
		}
	}
	public RankType getRankType() {
		return rankType;
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
	
	public static void main(String[] args) {
		for (Rank r : Rank.values()) {
			if (r.rankType == RankType.Unknown) {
				System.out.println(r);
			}
		}
	}
	
}
