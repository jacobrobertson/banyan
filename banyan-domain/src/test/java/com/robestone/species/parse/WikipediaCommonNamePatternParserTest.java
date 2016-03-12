package com.robestone.species.parse;

import junit.framework.TestCase;

public class WikipediaCommonNamePatternParserTest extends TestCase {

	private WikipediaCommonNamePatternParser parser = new WikipediaCommonNamePatternParser();

	/**
	 * TODO
	 * Acacia vestita
	 * The true soles are a family, '''Soleidae''',
	 */
	
	public void testNone() {
		doTest("Chiroteuthidae", null, "'''Chiroteuthidae'''... However, this common name is also applied to the [[Mastigoteuthidae]], which is itself sometimes treated as a subfamily (Mastigoteuthinae) of Chiroteuthidae.");
		doTest("Chiroteuthidae", null, "'''Chiroteuthids''' are deep-sea [[squid]] of the family '''Chiroteuthidae'''. They are generally small to medium in size, rather soft and gelatinous, and slow moving. They are found in most temperate and tropical oceans, but are known primarily from the [[Atlantic Ocean|North Atlantic]], [[Pacific Ocean|North Pacific]], and [[Indo-Pacific]]. The [[family (biology)|family]] is represented by approximately twelve [[species]] and four [[subspecies]] in four [[genus|genera]], two of which are [[monotypic]]. However, this common name is also applied to the [[Mastigoteuthidae]], which is itself sometimes treated as a subfamily (Mastigoteuthinae) of Chiroteuthidae.");
	}
	public void testApostraphes() {
		doTest("Paeonia brownii", "Brown's Peony", "'''''Paeonia brownii''''' ('''Brown's Peony''' or '''Native Peony'''), is a [[herbaceous]] [[perennial plant|perennial]] [[flowering plant]]");
		doTest("Lotus glaber", "Narrow-leaf Bird's-foot Trefoil", "'''''Lotus glaber''''' ('''Narrow-leaf Bird's-foot Trefoil''') is a flowering plant of the pea family [[Fabaceae]]");
	}
	public void testNoQuotes() {
		doTest("Megalopygidae", "crinkled flannel moths", "'''Megalopygidae''' commonly known as crinkled flannel moths.");
		doTest("Megalopygidae", "crinkled flannel moths", "'''Megalopygidae''' is the technical name of a group of insect species known generally as crinkled flannel moths, or simply '''Flannel Moths'''.");
	}
	public void testShortPhrases() {
		doTest("Leptoceridae", "long-horned caddisflies", "The [[family (biology)|family]] '''Leptoceridae''' are a family of [[Trichoptera|caddisflies]] often called \"long-horned caddisflies\".");
		doTest("Boarmiini", "Cleorini", "The '''Boarmiini''' (also often called '''Cleorini''') are a large [[tribe (biology)|tribe]] of [[geometer moth]]s in the [[Ennominae]] [[subfamily]].");
		doTest("Schizoglossa", "Paua slug", "'''''Schizoglossa''''', common name '''Paua slug''', is a [[genus]] of medium-sized to large [[predatory]], air-breathing,");
		doTest("Lampyris", "glowworms", "'''''Lampyris''''' is a [[genus]] of [[beetle]] in the [[firefly]] family (Lampyridae). In most of western [[Eurasia]], they are the predominant fireflies. They produce a continuous glow<ref name = stangerhalletal2007>Stanger-Hall ''et al.'' (2007)</ref>.; the [[larva]]e and [[larviform female]]s are among those organisms commonly called '''\"[[glowworm]]s\"'''.");
		doTest("Rhaphiolepis", "hawthorn", "'''''Rhaphiolepis''''' (},<ref>The first pronunciation is that expected for [[traditional English pronunciation of Latin|Anglo-Latin]]; the second is common in nurseries. ''Sunset Western Garden Book,'' 1995:606–607</ref> [[synonymy|syn.]] ''Raphiolepis'' [[Lindl.]]) is a genus of about 15 species of [[evergreen]] [[shrub]]s and small [[tree]]s in the family [[Rosaceae]], native to warm temperate and subtropical eastern and southeastern [[Asia]], from southern [[Japan]], southern [[Korea]] and southern [[China]] south to [[Thailand]] and [[Vietnam]]. The genus is closely related to ''[[Eriobotrya]]'' ([[loquat]]s). The common name ''hawthorn''");
		doTest("Naticidae", "moon snails", "'''Naticidae''', [[common name]] the '''moon snails''', is a [[family (biology)|family]] of minute to large-sized ");
		doTest("Potentilla gracilis", "Slender Cinquefoil", "'''''Potentilla gracilis''''', known as '''Slender Cinquefoil''', or '''Graceful Cinquefoil");
		doTest("Echinocystis", "wild cucumber", "'''Echinocystis''' is a [[genus]] in the gourd family [[Cucurbitaceae]] which includes the species ''Echinocystis lobata'', commonly called ''wild cucumber'' or ''prickly cucumber''.");
		doTest("Bostrichidae", "auger beetles", "The '''Bostrichidae''' are a family of beetles with more than 700 described species. They are commonly called '''auger beetles''', '''false powderpost beetles''' or '''horned powderpost beetles'''.");
		doTest("Gethyllis", "Kukumakranka", "'''''Gethyllis''''' (probably from [[Greek language|Greek]] ''\"gethyon\"'', bulb), commonly called '''Kukumakranka''', '''Koekemakranka''', or '''Kroekemakrank''',");
		doTest("Cethosia", "Lacewings", "'''''Cethosia''''', commonly called the '''Lacewings''',");
	}
	public void testGeneral() {
		doTest("Ascaris", "giant intestinal roundworms", "'''''Ascaris''''' is a genus of [[parasite|parasitic]] [[nematode]] worms known as the \"giant intestinal roundworms\". One species, ''[[Large roundworm of pigs|A. suum]]'', typically infects [[pigs]], while another, ''[[Ascaris lumbricoides|A. lumbricoides]]'', affects human populations, typically in sub-tropical and tropical areas with poor sanitation. ''A. lumbricoides'' is the largest intestinal roundworm and is the most common [[helminth]] infection of humans worldwide, an infection zzz ''[[ascariasis]]''.");
		doTest("Ascaris", "giant intestinal roundworms", "'''''Ascaris''''' is a genus of [[parasite|parasitic]] [[nematode]] worms known as the \"giant intestinal roundworms\". One species, ''[[Large roundworm of pigs|A. suum]]'', typically infects [[pigs]], while another, ''[[Ascaris lumbricoides|A. lumbricoides]]'', affects human populations, typically in sub-tropical and tropical areas with poor sanitation. ''A. lumbricoides'' is the largest intestinal roundworm and is the most common [[helminth]] infection of humans worldwide, an infection known as ''[[ascariasis]]''. ");
		doTest("Sorbus torminalis", "Chequer(s) Tree", "'''''Sorbus torminalis''''' ([[syn.]] ''Torminalis clusii'', '''Wild Service Tree'''), sometimes known as the '''Chequer(s) Tree''' or '''Checker(s) Tree''', is a species");
		doTest("Mutillidae", "velvet ant", "'''Mutillidae''' are a [[family (biology)|family]] of [[wasp]]s whose wingless [[female]]s resemble [[ant]]s. Their [[common name]] '''velvet ant''' refers to their dense hair which may be red, black, white, silver, or gold. They are known for their extremely painful sting, facetiously said to be strong enough to kill a [[cow]], hence the common name '''cow killer''' or '''cow ant''' is applied to some species.");
		doTest("Ulex gallii", "Western Gorse", "'''''Ulex gallii''''', '''Western Gorse'''");
		doTest("DOESNT MATTER", "Mohave rattlesnake", ":'''''Common names:''' Mohave rattlesnake,");
		doTest("Ustinaginales", "smut fungi", "''Ustinaginales'' is also known and classified as the \"smut fungi\".");
		doTest("Romaleidae", "lubber grasshoppers", "The '''Romaleidae''' or '''lubber grasshoppers''' are a family of [[grasshopper]]s.");
		doTest("Comptonia", "Sweetfern", "'''''Comptonia''''' is a [[monotypic]] genus (containing only '''''Comptonia peregrina''''') in the family [[Myricaceae]], order [[Fagales]]. It is native to eastern [[North America]], from southern [[Quebec]] south to the extreme north of [[Georgia (U.S. state)|Georgia]], and west to [[Minnesota]]. The common name is '''Sweetfern''' or '''Sweet-fern''', a confusing name as it is not a [[fern]].");
		doTest("Ailuroedus", "catbird", "'''''Ailuroedus''''' is a [[genus]] of [[bird]]s. The common name \"[[catbird]]\" refers to these species'\"wailing cat-like calls\".");
		doTest("Ailuroedus", "catbird", "'''''Ailuroedus''''' is a [[genus]] of [[bird]]s in the [[Ptilonorhynchidae]] family (bowerbirds). It contains the following species: * [[White-eared Catbird]] (''Ailuroedus buccoides'')* [[Green Catbird]] (''Ailuroedus crassirostris'')* [[Spotted Catbird]] (''Ailuroedus melanotis'') The common name \"[[catbird]]\" refers to these species'\"wailing cat-like calls\".");
		doTest("Chiroteuthidae", "whip-lash squid", "'''Chiroteuthids''' are deep-sea [[squid]] of the family '''Chiroteuthidae'''. They are generally small to medium in size, rather soft and gelatinous, and slow moving. They are found in most temperate and tropical oceans, but are known primarily from the [[Atlantic Ocean|North Atlantic]], [[Pacific Ocean|North Pacific]], and [[Indo-Pacific]]. The [[family (biology)|family]] is represented by approximately twelve [[species]] and four [[subspecies]] in four [[genus|genera]], two of which are [[monotypic]]. They are sometimes known collectively as '''whip-lash squid''': However, this common name is also applied to the [[Mastigoteuthidae]], which is itself sometimes treated as a subfamily (Mastigoteuthinae) of Chiroteuthidae.");
		doTest("Erpeton tentaculatum", "tentacled snake", "'''''Erpeton tentaculatum''''', or the \"tentacled snake\", is a [[rear-fanged]] aquatic [[snake]] native to South-East Asia.");
		doTest("Rhyzobius ventralis", "Black Lady Beetle", "'''''Rhyzobius ventralis''''', common names including '''Black Lady Beetle'''<ref name=\"ITIS_Rhyzobius ventralis_BinomialAuthorityandOtherNames\"/>, '''Gumtree Scale Ladybird'''<ref name=\"CSIRO_Ento_Rhyzobius_ventralis\">{{cite web|url=http://www.ento.csiro.au/aicn/system/c_753.htm|title=Rhyzobius ventralis ");
		doTest("Hoita macrostachya", "large leatherroot", "'''''Hoita macrostachya''''' is a species of [[Faboideae|legume]] known by the common name '''large leatherroot'''. It is native to [[California]] and [[Baja California]] where it can be found in moist areas of a number of habitat types.");
		doTest("Caltha leptosepala", "White Marsh Marigold", "'''''Caltha leptosepala''''' ('''White Marsh Marigold''', '''Twinflowered Marsh Marigold''', or '''Broadleaved Marsh Marigold''') is a species of flowering plant");
		doTest("Drymonia dodonaea", "Marbled Brown", "The '''Marbled Brown''' ''(Drymonia dodonaea)'' is a [[moth]] of the family [[Notodontidae]]. It is found in [[Europe]] and in the area surrounding the [[Caucasus]].");
		doTest("Achlya flavicornis", "Yellow Horned", "The '''Yellow Horned''' ''(Achlya flavicornis)'' is a [[moth]] of the family [[Drepanidae]]. It is found in [[Europe]].");
		doTest("Vibrio cholerae", "Kommabacillus", "'''''Vibrio cholerae''''' (also ''Kommabacillus'') is a minge [[gram negative]] curved-rod shaped [[bacterium]] with a polar [[flagellum]]");
		doTest("Speyeria", "Greater Fritillaries", "'''''Speyeria''''', commonly known as  '''Greater Fritillaries''', is the genus of [[butterflies]] in the family [[Nymphalidae]] commonly found in [[North America]] and [[Asia]].");
		doTest("Physocarpus capitatus", "Pacific ninebark", "'''''Physocarpus capitatus''''' ('''Pacific ninebark''' or '''tall ninebark''') is a species of ''[[Physocarpus]]'' native to western [[North America]] from southern [[Alaska]] east to [[Montana]] and [[Utah]], and south to southern [[California]].");
		doTest("Crataegus punctata", "dotted hawthorn", "'''''Crataegus punctata''''' is a species of [[Crataegus|hawthorn]] known by the common names '''dotted hawthorn''' or '''white haw'''.");
		doTest("Acacia confusa", "Acacia Petit Feuille", "'''''Acacia confusa''''' is a perennial tree native to Asia. Some common names for it are '''Acacia Petit Feuille''',");
		doTest("Perrunichthys perruno", "leopard catfish", "'''''Perrunichthys perruno''''' is the only [[species]] of [[catfish]] ([[order (biology)|order]] Siluriformes) of the [[monotypic]] [[genus]] '''''Perrunichthys''''' of the [[family (biology)|family]] [[Pimelodidae]].<ref>{{ITIS|ID=681763|taxon=''Perrunichthys perruno''|year=2007|date=May 26}}</ref> This species reaches about 60&nbsp;[[centimetre]]s (24&nbsp;[[inch|in]]) [[fish measurement|TL]] and originates from the [[Lake Maracaibo]] basin.<ref name=fishbase>{{FishBase species|genus=Perrunichthys|species=perruno|year=2007|month=May}}</ref> This fish is occasionally known as the '''leopard catfish'''.<ref name=fishbase/>");
		doTest("Diacrisia sannio", "Clouded Buff", "The '''Clouded Buff''' ''(Diacrisia sannio)'' is a [[moth]] of the family [[Arctiidae]].");
		doTest("Acontias", "lance skinks", "'''''Acontias''''', the '''lance skinks''', is a [[genus]] of limbless [[skink]]s");
		doTest("Geomys", "eastern pocket gophers", "The [[genus]] '''''Geomys''''' contains nine [[species]] of [[pocket gophers]] often collectively referred to as the '''eastern pocket gophers'''.");
		doTest("Acacia mearnsii", "Black Wattle", "'''''Acacia mearnsii''''' is a fast-growing [[legume|leguminous]] tree native to [[Australia]]. Common names for it include '''Black Wattle''', '''''Acácia-negra''''' ([[Portuguese language|Portuguese]]), '''Australian Acacia''', '''''Australische Akazie''''' ([[German language|German]]),");
		doTest("Atriplex nummularia", "old man saltbush", "'''''Atriplex nummularia''''' is a species of [[Atriplex|saltbush]] known by the common names '''old man saltbush''', '''bluegreen saltbush''', and '''giant saltbush'''.");
		doTest("Cryptoprocta ferox", "fossa", "The '''fossa''' (''Cryptoprocta ferox'') ({{pron-en|}}<ref>{{cite web | author = Hartley, Karen | url = http://www.pbs.org/wgbh/nova/madagascar/expedit");
		doTest("Polihierax", "pygmy-falcons", "The '''pygmy-falcons''', '''''Polihierax''''',");
		doTest("Formica sanguinea", "slavemaker ant", "'''''Formica sanguinea''''', the slavemaker ant,");
		doTest("Icterus pectoralis", "Spot-breasted Oriole", "The '''Spot-breasted Oriole''' ('''''Icterus pectoralis''''')");
		doTest("Apioceridae", "flower-loving flies", "The '''Apioceridae''', or '''flower-loving flies''',");
		doTest("Equisetum variegatum", "variegated horsetail", "'''''Equisetum variegatum''''' (commonly known as '''variegated horsetail''' or '''variegated scouring rush''')");
		doTest("Prunus emarginata", "Bitter cherry", "'''Oregon cherry''' or '''Bitter cherry''' ('''''Prunus emarginata''''') is a species of ''[[Prunus]]''");
		doTest("Faunis", "fauns", "'''''Faunis''''' is a [[genus]] of Asian [[butterfly|butterflies]] in the family [[Nymphalidae]]. They are among the butterflies commonly known as '''\"fauns\"''' or '''\"duffers\"'''.");
		doTest("Micromacronus", "miniature-babblers", "'''''Micromacronus''''' is a [[bird]] [[genus]] in the [[family (biology)|faily]] [[Timaliidae]] [[Endemism|endemic]] to the [[Philippines]]. Long considered to be [[monotypic]], its members are known as '''miniature-babblers''' or '''miniature tit-babblers'''.");
		doTest("Diaspididae", "armoured scale insects", "'''Diaspididae''' is the largest family of [[scale insect]]s with over 2650 described species in around 400 genera. As with all scale insects, the female produces a waxy protective scale beneath which it feeds on its host plant. Diaspidid scales are far more substantial than those of most other families: Incorporating the [[exuvia]]e from the first two [[nymph (biology)|nymphal]] [[instar]]s and sometimes [[faeces|faecal]] matter and fragments of the host plant, these can be complex and extremely waterproof structures rather resembling a suit of [[armour]]. For this reason these insects are commonly referred to as '''armoured scale insects'''.");
		doTest("Crataegus pubescens", "Mexican Hawthorn", "'''''Crataegus pubescens''''' is commonly used as the scientific name for '''Mexican Hawthorn''' or '''Tejocote''', a species of [[Crataegus|hawthorn]] native to [[Mexico]]. However, the name ''C. pubescens'' Steud. is illegitimate");
		doTest("Garrya", "silktassel", "'''''Garrya''''' is a genus of about 18 species of [[flowering plant]]s in the family [[Garryaceae]], native to [[North America|North]] and [[Central America]] and the [[Caribbean]]. Common names include '''silktassel''', and '''tassel bush'''.");
		doTest("Caenidae", "Small Squaregill Mayflies", "'''Caenidae''', or the Small Squaregill Mayflies,");
		doTest("Ramsayornis fasciatus", "Bar-breasted Honeyeater", "The '''Bar-breasted Honeyeater''' ('''''Ramsayornis fasciatus''''') is a species of [[bird]] in the [[Meliphagidae]] family. It is [[Endemism|endemic]] to [[Australia]].");
	}
	private void doTest(String latinName, String expect, String page) {
		page = "}} foo bar " + page + " ==>> funk";
		String found = parser.parse(latinName, page);
		assertEquals(expect, found);
	}
	
}
