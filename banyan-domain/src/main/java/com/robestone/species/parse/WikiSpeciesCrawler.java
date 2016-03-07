package com.robestone.species.parse;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LogHelper;
import com.robestone.util.html.EntityMapper;

public class WikiSpeciesCrawler extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		if (args == null || args.length == 0) {
			args = new String[] { };
		}
		boolean forceNewDownloadForCache = true;
		boolean crawlAllStoredLinks = true;
		//*
		args = new String[] {
				
				"	Aaroniella badonneli	",
				"	Abitagua	",
				"	Ablerus bidentatus	",
				"	Ablerus biguttatibiae	",
				"	Ablerus diana	",
				"	Ablerus emersoni	",
				"	Ablerus grotiusi	",
				"	Ablerus hastatus	",
				"	Ablerus howardii	",
				"	Ablerus hyalinus	",
				"	Ablerus impunctatipennis	",
				"	Ablerus longfellowi	",
				"	Ablerus marchali	",
				"	Ablerus miricilia	",
				"	Ablerus nelsoni	",
				"	Ablerus novicornis	",
				"	Ablerus nympha	",
				"	Ablerus pan	",
				"	Ablerus piceipes	",
				"	Ablerus plinii	",
				"	Ablerus poincarei	",
				"	Ablerus pullicornis	",
				"	Ablerus punctatus	",
				"	Ablerus rhea	",
				"	Ablerus romae	",
				"	Ablerus saintpierrei	",
				"	Ablerus semifuscipennis	",
				"	Ablerus sidneyi	",
				"	Ablerus socratis	",
				"	Ablerus socrus	",
				"	Ablerus venustulus	",
				"	Acanthocephalus (Echinorhynchidae)	",
				"	Acantholybas	",
				"	Acanthorrhynchium	",
				"	Acaraptera dimorpha	",
				"	Acaraptera minuta	",
				"	Acentrogobius cyanomos	",
				"	Acentrogobius nebulosus	",
				"	Aceratoneura	",
				"	Aceratoneura splendida	",
				"	Aceratoneuromyia	",
				"	Acerophagoides	",
				"	Acrias	",
				"	Acridotarsa	",
				"	Acrobeloides	",
				"	Acrochaetiaceae	",
				"	Acronicta orientalis	",
				"	Acrostiba	",
				"	Acrotritia comteae	",
				"	Actenobius	",
				"	Actinolaimidae	",
				"	Adeixis	",
				"	Adelognathinae	",
				"	Adelognathus	",
				"	Adeonoidea	",
				"	Adesmia muricata (ICZN)	",
				"	Aechmia	",
				"	Aedeomyiini	",
				"	Aedes (Finlaya)	",
				"	Aethriostoma	",
				"	Aforedon	",
				"	Agaon	",
				"	Agathiphagoidea	",
				"	Aiteta	",
				"	Alectoria (Animalia)	",
				"	Aleyrodes	",
				"	Alletelura hilli	",
				"	Alliphis	",
				"	Allodrepa subcylindricum	",
				"	Alloeorhynchus	",
				"	Allogromiidae	",
				"	Allopauropus maoriorum	",
				"	Allopauropus muscicolus	",
				"	Alulatettix	",
				"	Amasa darwini	",
				"	Amastrini	",
				"	Amaurobioides maritima	",
				"	Amaurobioides minor	",
				"	Amaurobioides pallida	",
				"	Amaurobioides picuna	",
				"	Amaurobioides pleta	",
				"	Amaurobioides pohara	",
				"	Amauroderma	",
				"	Amigdoscalpellinae	",
				"	Ammoscalaria	",
				"	Amneris	",
				"	Ampeliscoidea	",
				"	Amphitritinae	",
				"	Amphiuridae	",
				"	Anacanthorus	",
				"	Anadiasa	",
				"	Anaplecta	",
				"	Anaspididae	",
				"	Anelastidini	",
				"	Anisochaeta macleayi	",
				"	Anthogonidae	",
				"	Anthrenocerus australis	",
				"	Aonidiella	",
				"	Apatochernes antarcticus	",
				"	Apatochernes chathamensis	",
				"	Apatochernes cheliferoides	",
				"	Apatochernes cruciatus	",
				"	Apatochernes curtulus	",
				"	Apatochernes gallinaceus	",
				"	Apatochernes insolitus	",
				"	Apatochernes kuscheli	",
				"	Apatochernes maoricus	",
				"	Apatochernes nestoris	",
				"	Apatochernes obrieni	",
				"	Apatochernes proximus	",
				"	Apatochernes solitarius	",
				"	Apatochernes vastus	",
				"	Apatochernes wisei	",
				"	Apatopygidae	",
				"	Apeira	",
				"	Aphantes	",
				"	Aphiura breviceps	",
				"	Aploparaksis	",
				"	Aplota	",
				"	Apocheiridium validissimum	",
				"	Apocheiridium validum	",
				"	Apocheiridium zealandicum	",
				"	Apodida	",
				"	Aporrectodea longa	",
				"	Aporrectodea rosea	",
				"	Aporrectodea trapezoides	",
				"	Aporrectodea tuberculata	",
				"	Appendicularia thymifolia	",
				"	Apsil	",
				"	Aragua	",
				"	Arauco	",
				"	Archoophora	",
				"	Arcturidae	",
				"	Ardaris	",
				"	Argidava	",
				"	Argoctenus igneus	",
				"	Argoctenus pictus	",
				"	Ariola	",
				"	Ariolica	",
				"	Arrhapa	",
				"	Artiora	",
				"	Ascuta (Forster)	",
				"	Asinduma	",
				"	Asovia	",
				"	Asperococcus	",
				"	Aspidioides	",
				"	Aspidiotus	",
				"	Astiini	",
				"	Asychis amphiglyptus	",
				"	Athyridida	",
				"	Auratiopycnidiella	",
				"	Australiatelura tasmanica	",
				"	Australoplana	",
				"	Austroleucon	",
				"	Austrophthiracarus konwerskii	",
				"	Azuayia	",
				"	Bacillus megaterium	",
				"	Baetopus	",
				"	Balta papua	",
				"	Bannatettix	",
				"	Barchatus indicus	",
				"	Bargylia	",
				"	Bathyllus	",
				"	Bathysquillidae	",
				"	Beckerina polysticha	",
				"	Betylobraconini	",
				"	Bezu	",
				"	Blepharocosta	",
				"	Bothrometopus sulcatus	",
				"	Brachypauropoides praestans	",
				"	Bromeliaemiris	",
				"	Brundinia	",
				"	Bruxneria	",
				"	Bruxneria lamingtoniana	",
				"	Bupalus	",
				"	Busckia	",
				"	Cacostola	",
				"	Cadurca	",
				"	Caenanthura	",
				"	Caenis	",
				"	Cales spenceri	",
				"	Callianideidae	",
				"	Calochromus	",
				"	Calopsocidae	",
				"	Calymera	",
				"	Cambarus (Hiaticambarus)	",
				"	Capasa	",
				"	Captorhinidae	",
				"	Cardamine enneaphyllos	",
				"	Caripeta	",
				"	Carpha alpina	",
				"	Carpophilus davidsoni	",
				"	Carpophilus gaveni	",
				"	Carsia	",
				"	Cassyma	",
				"	Casuarina cristata	",
				"	Cateristis	",
				"	Caudiferidae	",
				"	Caviria	",
				"	Celleporoidea	",
				"	Cephalonomia	",
				"	Cepphis	",
				"	Cerastium eriophorum	",
				"	Chaetosiphon	",
				"	Chandica	",
				"	Chirodiscidae	",
				"	Choisya	",
				"	Chromadoridae	",
				"	Ciampa	",
				"	Cingilia	",
				"	Cissidium scutellare	",
				"	Cladogynia	",
				"	Cladoxycanus	",
				"	Clasmatocolea verrucosa	",
				"	Cleobora mellyi	",
				"	Clusiota	",
				"	Coccophagus aethiopis	",
				"	Coccophagus auricaput	",
				"	Coccophagus biguttatus	",
				"	Coccophagus crucigerus	",
				"	Coccophagus emersoni	",
				"	Coccophagus exiguiventris	",
				"	Coccophagus funeralis	",
				"	Coccophagus gregarius	",
				"	Coccophagus mixtus	",
				"	Coccophagus nympha	",
				"	Coccophagus perhispidus	",
				"	Coccophagus pulcher	",
				"	Coccophagus redini	",
				"	Coccophagus signus	",
				"	Coccophagus vegai	",
				"	Cochliarion victoriense	",
				"	Coenina	",
				"	Cololejeunea floccosa	",
				"	Coloradoa	",
				"	Conosara	",
				"	Copidita	",
				"	Copris diversus	",
				"	Copris elphenor	",
				"	Copris fallaciosus	",
				"	Corethrella	",
				"	Corula	",
				"	Corymica	",
				"	Cosmopterix attenuatella	",
				"	Cosmozosteria subzonata	",
				"	Cossedia	",
				"	Cothornobata	",
				"	Cotylea	",
				"	Craspedophorus	",
				"	Creagra	",
				"	Crepis mollis	",
				"	Cribrilinidae	",
				"	Crinoniscidae	",
				"	Crompus	",
				"	Crorema	",
				"	Cryptotympanini	",
				"	Ctimene	",
				"	Cucullanidae	",
				"	Curbia	",
				"	Curdiea balthazar	",
				"	Cyanus segetum	",
				"	Cyclophoridae	",
				"	Cyclopinidae	",
				"	Cystidia	",
				"	Cytisus albus	",
				"	Cytisus austriacus	",
				"	Dahana	",
				"	Dalma	",
				"	Danala	",
				"	Dargeia	",
				"	Dasypsylla	",
				"	Dendrodrilus rubidus	",
				"	Destutia	",
				"	Detounda	",
				"	Diastylopsis	",
				"	Dichomeris litoxyla	",
				"	Dichomeris rasilella	",
				"	Dichomeris tostella	",
				"	Dichomeris ustalella	",
				"	Dictyothyrium	",
				"	Dicycla	",
				"	Didigua	",
				"	Didugua	",
				"	Dinizia	",
				"	Dinodriloides beddardi	",
				"	Dioptis	",
				"	Diplotrema fallax	",
				"	Diplotrema haplocystis	",
				"	Diplotrema micros	",
				"	Diplotrema montana	",
				"	Diplotrema pallida	",
				"	Diplotrema paludosa	",
				"	Diplotrema parva	",
				"	Diplotrema rossi	",
				"	Diporochaeta aquatica	",
				"	Diporochaeta brachysoma	",
				"	Diporochaeta chathamensis	",
				"	Diporochaeta duodecimalis	",
				"	Diporochaeta heterochaeta	",
				"	Diporochaeta intermedia	",
				"	Discinaceae	",
				"	Discophora (Stemonuraceae)	",
				"	Distichophora crassimana	",
				"	Ditrichophora flavitarsis	",
				"	Docirava	",
				"	Dolicholana	",
				"	Doris subaustralis	",
				"	Dorylaimoidea	",
				"	Draconinae	",
				"	Dreata	",
				"	Drobeta	",
				"	Drusus	",
				"	Drypta australis	",
				"	Dumatha	",
				"	Dura	",
				"	Dyscritobaeus aquaticus	",
				"	Dyscritobaeus armatus	",
				"	Dyscritobaeus flavicornis	",
				"	Dyscritobaeus fuscipes	",
				"	Dyscritobaeus nigricoxella	",
				"	Dyscritobaeus ocularis	",
				"	Dyscritobaeus orientalis	",
				"	Dyscritobaeus parvipennis	",
				"	Dyscritobaeus spinosus	",
				"	Dyscritobaeus splendidus	",
				"	Dyscritobaeus variocellus	",
				"	Echmepteryx brunnea	",
				"	Echmepteryx hartmeyeri	",
				"	Echmepteryx quadrilineata	",
				"	Ectemnorhinus drygalskii	",
				"	Ectopsocus vachoni	",
				"	Edessa	",
				"	Eisenia andrei	",
				"	Eiseniella tetraedra	",
				"	Elatobium	",
				"	Elenchus perkinsianus	",
				"	Elenchus varleyi	",
				"	Eligma	",
				"	Ellipsidion australe	",
				"	Ellipsidion laetum	",
				"	Ellipsidion marginiferum	",
				"	Ellipsidion ramosum	",
				"	Ellipsidion reticulatum	",
				"	Ellipsidion variegatum	",
				"	Elophos	",
				"	Empicoris aeneus	",
				"	Endevouridae	",
				"	Enterpia	",
				"	Entisberus	",
				"	Entomobrya duofascia	",
				"	Entomobrya egmontia	",
				"	Entomobrya ephippiaterga	",
				"	Entomobrya exalga	",
				"	Entomobrya exfoliata	",
				"	Entomobrya exoricarva	",
				"	Entomobrya fusca	",
				"	Entomobrya glaciata	",
				"	Entomobrya hurunuiensis	",
				"	Entomobrya intercolorata	",
				"	Entomobrya interfilixa	",
				"	Entomobrya lamingtonensis	",
				"	Entomobrya livida	",
				"	Entomobrya miniparva	",
				"	Entomobrya multifasciata	",
				"	Entomobrya nigranota	",
				"	Entomobrya nigraoculata	",
				"	Entomobrya nivalis	",
				"	Entomobrya obscuroculata	",
				"	Entomobrya opotikiensis	",
				"	Entomobrya penicillata	",
				"	Entomobrya proceraseta	",
				"	Entomobrya processa	",
				"	Entomobrya rubra	",
				"	Entomobrya salta	",
				"	Entomobrya saxatila	",
				"	Entomobrya totapunctata	",
				"	Entomobrya varia	",
				"	Entomobryoidea	",
				"	Enypia	",
				"	Eosentomon bornemisszai	",
				"	Eosentomon imadatei	",
				"	Eosentomon oceaniae	",
				"	Eosentomon swani	",
				"	Eosentomon westraliense	",
				"	Eosentomon womersleyi	",
				"	Ephedrus persicae	",
				"	Ephemeraceae	",
				"	Epholca	",
				"	Ephydatia fluviatilis	",
				"	Ephydrella aquaria	",
				"	Ephydrella assimilis	",
				"	Ephydrella spathulata	",
				"	Ephydrinae	",
				"	Epichrysus	",
				"	Epicyme	",
				"	Epimeriidae	",
				"	Epione	",
				"	Epyaxa	",
				"	Erebus	",
				"	Erieopteridae	",
				"	Erizada	",
				"	Erpodiaceae	",
				"	Erysiphales	",
				"	Espinosa	",
				"	Etanna	",
				"	Ethmostigmus	",
				"	Eucalyptococcus hakeae	",
				"	Eucypridinae	",
				"	Eudia	",
				"	Eudorellopsis	",
				"	Eudule	",
				"	Eukoenenia guzikae	",
				"	Eumedonidae	",
				"	Eunapius	",
				"	Eunapius fragilis	",
				"	Eurycyde	",
				"	Euryischia unfasciatipennis	",
				"	Euryischia unmaculata	",
				"	Euryischia vertexalis	",
				"	Eusarca	",
				"	Euschizaphis	",
				"	Eusyllinae	",
				"	Eutrichosomella albiclava	",
				"	Eutrichosomella albifemora	",
				"	Eutrichosomella blattophaga	",
				"	Eutrichosomella multifasciata	",
				"	Excidobates	",
				"	Exelis	",
				"	Fabiola	",
				"	Fala	",
				"	Fessisentidae	",
				"	Fisera	",
				"	Flabellidae	",
				"	Gabriola	",
				"	Galenara	",
				"	Galium rotundifolium	",
				"	Gamakia	",
				"	Garcia	",
				"	Gariga	",
				"	Gastrina	",
				"	Gastrobothrus abdominalis	",
				"	Gelasma	",
				"	Gellonia dejectaria	",
				"	Gellonia pannularia	",
				"	Genusa	",
				"	Geoscapheus castaneus	",
				"	Gigaspermataceae	",
				"	Gippius	",
				"	Glacies	",
				"	Glaucias amyoti	",
				"	Glaucocharis auriscriptella	",
				"	Glaucocharis bipunctella	",
				"	Glaucocharis elaina	",
				"	Glaucocharis epiphaea	",
				"	Glaucocharis harmonica	",
				"	Glaucocharis helioctypa	",
				"	Glaucocharis holanthes	",
				"	Glossobius	",
				"	Glossocephalus	",
				"	Glycyphagus	",
				"	Gnatholonche	",
				"	Godonela	",
				"	Goniodes colchici	",
				"	Goniodes dispar	",
				"	Goniodes dissimilis	",
				"	Goniodes ortygis	",
				"	Goniodes pavonis	",
				"	Goniodes retractus	",
				"	Goniodes stefani	",
				"	Gonodactylaceus	",
				"	Gonodactylaceus falcatus	",
				"	Gonodactylus	",
				"	Gonodes	",
				"	Gracilariales	",
				"	Graliophilus aucklandicus	",
				"	Grevillia	",
				"	Gubernatoriana	",
				"	Gueneria	",
				"	Haffneria	",
				"	Hakea archaeoides	",
				"	Hakea eriantha	",
				"	Hakea gibbosa	",
				"	Hapalips	",
				"	Haplothrips	",
				"	Haptosquilla	",
				"	Hecamede granifera	",
				"	Hecamedoides affinis	",
				"	Helioporidae	",
				"	Heliozela anantia	",
				"	Heliozela autogenes	",
				"	Heliozela catoptrias	",
				"	Heliozela crypsimetalla	",
				"	Heliozela eucarpa	",
				"	Heliozela isochroa	",
				"	Heliozela microphylla	",
				"	Heliozela nephelitis	",
				"	Heliozela prodela	",
				"	Heliozela rutilella	",
				"	Heliozela siderias	",
				"	Heliozela trisphaera	",
				"	Helophilus antipodus	",
				"	Helpis minitabunda	",
				"	Hemerodromiini	",
				"	Hemichela	",
				"	Hemiptarsenus varicornis	",
				"	Hemipyrellia ligurriens	",
				"	Hemisquillidae	",
				"	Hemnypia	",
				"	Heterocentron	",
				"	Heterochernes novaezealandiae	",
				"	Heterogaster urticae	",
				"	Heterosquilla laevis	",
				"	Heterosquilla tricarinata	",
				"	Hexapodidae	",
				"	Hildalgo	",
				"	Hinewaia	",
				"	Hoplochaetina durvilleana	",
				"	Hoplochaetina rossii	",
				"	Hoplochaetina rubra	",
				"	Hoplochaetina spirilla	",
				"	Hoplochaetina subtilis	",
				"	Hyadaphis	",
				"	Hydrichthyidae	",
				"	Hydrodynastes	",
				"	Hydroides	",
				"	Hylocomium splendens	",
				"	Hyolitha	",
				"	Hystrignathus	",
				"	Ibalia leucospoides	",
				"	Ichthybotidae	",
				"	Ideobisium peregrinum	",
				"	Idiocerus distinguendus	",
				"	Idiopterus	",
				"	Ileodictyon gracile	",
				"	Illinoia azaleae	",
				"	Ilytheini	",
				"	Incurvarites	",
				"	Indoapseudes	",
				"	Ironidae	",
				"	Iroponera	",
				"	Jekelius	",
				"	Katianna	",
				"	Kermarion	",
				"	Kobonga	",
				"	Kraussiinae	",
				"	Labanda	",
				"	Lambdina	",
				"	Lamprochernes savignyi	",
				"	Lanceoporidae	",
				"	Laneco	",
				"	Lathraea squamaria	",
				"	Leandra	",
				"	Leperina conspicua	",
				"	Leperina marmorata	",
				"	Leperina seposita	",
				"	Lepidastheniinae	",
				"	Lepidium seditiosum	",
				"	Lepidodexia	",
				"	Lepidonotinae	",
				"	Lepidoscia melanogramma	",
				"	Leponosandrus	",
				"	Leptasterias	",
				"	Leptecophylla juniperina	",
				"	Leptocheliinae	",
				"	Leptoiulus	",
				"	Lernaeopodidae	",
				"	Leucodrilus disparatus	",
				"	Leucodrilus robustus	",
				"	Liarea egea	",
				"	Liarea hochstetteri	",
				"	Liarea turriculata	",
				"	Lincolnia	",
				"	Liosomaphis	",
				"	Liothrips vaneeckei	",
				"	Lipaphis	",
				"	Lissaptera denticeps	",
				"	Lissonota	",
				"	Lithornis	",
				"	Lithotelestidae	",
				"	Lodiana	",
				"	Loisthodon	",
				"	Loweria	",
				"	Loxomiza	",
				"	Loxsoma	",
				"	Luciliini	",
				"	Lumbricus castaneus	",
				"	Lumbricus eiseni	",
				"	Lysiphlebus testaceipes	",
				"	Lyssacinosida	",
				"	Machilellus orientalis	",
				"	Macraulacini	",
				"	Macroclymenella stewartensis	",
				"	Macrogynoplax	",
				"	Macropanesthia kraussiana	",
				"	Macrostomida	",
				"	Magnolia amazonica	",
				"	Maorichernes vigil	",
				"	Maoridrilus alpinus	",
				"	Maoridrilus carnosus	",
				"	Maoridrilus dissimilis	",
				"	Maoridrilus fuscus	",
				"	Maoridrilus gravus	",
				"	Maoridrilus intermedius	",
				"	Maoridrilus mauiensis	",
				"	Maoridrilus megacystis	",
				"	Maoridrilus michaelseni	",
				"	Maoridrilus minor	",
				"	Maoridrilus modestus	",
				"	Maoridrilus montanus	",
				"	Maoridrilus nelsoni	",
				"	Maoridrilus pallidus	",
				"	Maoridrilus parkeri	",
				"	Maoridrilus plumbeus	",
				"	Maoridrilus purus	",
				"	Maoridrilus ruber	",
				"	Maoridrilus rubicundus	",
				"	Maoridrilus smithi	",
				"	Maoridrilus suteri	",
				"	Maoridrilus tetragonurus	",
				"	Maoridrilus thomsoni	",
				"	Maoridrilus transalpinus	",
				"	Maoridrilus uliginosus	",
				"	Maoridrilus ultimus	",
				"	Maoridrilus volutus	",
				"	Maoridrilus wilkini	",
				"	Maorimyia	",
				"	Margelopsidae	",
				"	Maurilia	",
				"	Megachile mystaceana	",
				"	Megalocoleus	",
				"	Megamerinidae	",
				"	Megascolides neglectus	",
				"	Megascolides reptans	",
				"	Megascolides unipapillatus	",
				"	Megaselia castanea	",
				"	Megaselia curtineura	",
				"	Megaselia dolichoptera	",
				"	Megaselia dupliciseta	",
				"	Megaselia longinqua	",
				"	Megaselia rufipes	",
				"	Melampyrum cristatum	",
				"	Melemaea	",
				"	Melobasina	",
				"	Mesaster	",
				"	Meschiidae	",
				"	Mesoplophora (Mesoplophora)	",
				"	Metacapnodiaceae	",
				"	Meteima	",
				"	Metopolophium	",
				"	Micromyzus	",
				"	Microscolex aucklandicus	",
				"	Microscolex campbellianus	",
				"	Microscolex dubius	",
				"	Microscolex phosphoreus	",
				"	Microsporidium	",
				"	Microterys aristotelea	",
				"	Microterys australicus	",
				"	Microterys garibaldia	",
				"	Microterys gilberti	",
				"	Microterys longifuniculus	",
				"	Microterys newcombi	",
				"	Microterys purpureiventris	",
				"	Microterys spinozai	",
				"	Microterys triguttatus	",
				"	Microveliini	",
				"	Mikado parva	",
				"	Mimomiza	",
				"	Mimonectidae	",
				"	Minetia	",
				"	Mirollia	",
				"	Missulena granulosa	",
				"	Missulena insignis	",
				"	Mitodon	",
				"	Modiola	",
				"	Moneta australis	",
				"	Moneta longicauda	",
				"	Monoliropus	",
				"	Monopharsus	",
				"	Muehlenbeckia australis	",
				"	Mummuciidae	",
				"	Munidopsis	",
				"	Munidopsis chacei	",
				"	Myriotrochidae	",
				"	Myrteta	",
				"	Mytilus californianus	",
				"	Mytilus trossulus	",
				"	Myzaphis	",
				"	Naegleria gruberi	",
				"	Naja (Naja)	",
				"	Naja atra	",
				"	Naja mandalayensis	",
				"	Naja philippinensis	",
				"	Naja sagittifera	",
				"	Naja siamensis	",
				"	Nargus	",
				"	Narraga	",
				"	Nemeris	",
				"	Neochaeta forsteri	",
				"	Neochaeta salmoni	",
				"	Neodrilus agilis	",
				"	Neodrilus campestris	",
				"	Neodrilus dissimilis	",
				"	Neodrilus edwardsi	",
				"	Neodrilus polycystis	",
				"	Neolycaena rufina	",
				"	Neophyllaphis brimblecombei	",
				"	Neophyllaphis gingerensis	",
				"	Neophyllaphis lanata	",
				"	Nepytia	",
				"	Nereis	",
				"	Neromia	",
				"	Nerthra femoralis	",
				"	Nerthra hirsuta	",
				"	Nerthra luteovaria	",
				"	Nerthra stali	",
				"	Nerthra tuberculata	",
				"	Nesidiochernes kuscheli	",
				"	Nesidiochernes scutulatus	",
				"	Nesidiochernes zealandicus	",
				"	Nesiotochernes stewartensis	",
				"	Nesochernes gracilis	",
				"	Nimbopsocus huttoni	",
				"	Nisaga	",
				"	Nisista	",
				"	Nomada abnormis	",
				"	Nomada bicellularis	",
				"	Norix	",
				"	Nosematidae	",
				"	Nothoceros giganteus	",
				"	Nothofagus moorei	",
				"	Notiphilini	",
				"	Notonemouridae	",
				"	Notophthiracarus abstemius	",
				"	Notophthiracarus admirabilis	",
				"	Notoscolex huttoni	",
				"	Notoscolex kirki	",
				"	Notoscolex maorica	",
				"	Notoscolex sapida	",
				"	Notoscolex suteri	",
				"	Notoscolex urewerae	",
				"	Nudaurelia dione	",
				"	Nuvol	",
				"	Nylanderia tasmaniensis	",
				"	Ocana	",
				"	Octochaetus antarcticus	",
				"	Octochaetus huttoni	",
				"	Octochaetus kapitiensis	",
				"	Octochaetus levis	",
				"	Octochaetus michaelseni	",
				"	Octochaetus microchaetus	",
				"	Octochaetus multiporus	",
				"	Octochaetus pelorus	",
				"	Octochaetus ravus	",
				"	Octochaetus thomasi	",
				"	Octolasion cyaneum	",
				"	Octolasion tyrtaeum	",
				"	Odezia	",
				"	Odontodactylidae	",
				"	Odontodactylus	",
				"	Odysseylana	",
				"	Oecobius annulipes	",
				"	Oithonidae	",
				"	Okadaiidae	",
				"	Ommatoleucon	",
				"	Oncholaimidae	",
				"	Onthophagus ferox	",
				"	Onychora	",
				"	Ophelimus	",
				"	Opisthocheiridae	",
				"	Opsochernes carbophilus	",
				"	Orbiniinae	",
				"	Oribotritia parachichijimensis	",
				"	Ornithoprion	",
				"	Orophia	",
				"	Orosius orientalis	",
				"	Orthopodomyiini	",
				"	Orthotylus josifovi	",
				"	Orthotylus meridionalis	",
				"	Ostertagia circumcincta	",
				"	Ovatus	",
				"	Oxalis exilis	",
				"	Oxalis perennans	",
				"	Oxalis rubens	",
				"	Oxalis thompsoniae	",
				"	Oxyini	",
				"	Pachyrhabda	",
				"	Palleopa	",
				"	Palyas	",
				"	Panesthia lata	",
				"	Papago	",
				"	Paradoxaphis aristoteliae	",
				"	Paradoxosomatidae	",
				"	Parahyadina lacustris	",
				"	Parakiefferiella	",
				"	Paralaea	",
				"	Paralobella	",
				"	Paranura	",
				"	Paraphiloscia fragilis	",
				"	Parectromoides varipes	",
				"	Paris incompleta	",
				"	Parthenopea	",
				"	Paschalococos	",
				"	Patalene	",
				"	Patna	",
				"	Pauropsylla	",
				"	Pauropus confines	",
				"	Pauropus dolosus	",
				"	Pauropus forsteri	",
				"	Pauropus furcifer	",
				"	Pauropus furcillatus	",
				"	Pauropus hirtus	",
				"	Pauropus huxleyi	",
				"	Pentapria	",
				"	Pepsinae	",
				"	Percolozoa	",
				"	Perieodrilus lateralis	",
				"	Perieodrilus montanus	",
				"	Perieodrilus plunketi	",
				"	Perieodrilus ricardi	",
				"	Perionychella helophila	",
				"	Perionychella perionychopsis	",
				"	Perionychella shoeana	",
				"	Perionyx excavatus	",
				"	Periplaneta brunnea	",
				"	Peripsocus fici	",
				"	Persectania	",
				"	Persparsia	",
				"	Petelia	",
				"	Pezothrips	",
				"	Phaenacantha australiae	",
				"	Phaeoura	",
				"	Phalangodidae	",
				"	Phaulochernes howdenensis	",
				"	Phaulochernes jenkinsi	",
				"	Phaulochernes kuscheli	",
				"	Phaulochernes maoricus	",
				"	Phaulochernes townsendi	",
				"	Phellopsylla trigutta	",
				"	Pherne	",
				"	Phidyle	",
				"	Philaenus	",
				"	Philedia	",
				"	Philobota chionoptera	",
				"	Philomaoria hispida	",
				"	Philomaoria pallipes	",
				"	Phintia	",
				"	Phycomyces	",
				"	Phyllidiella pustulosa	",
				"	Phyllonomaceae	",
				"	Physobryaxis	",
				"	Phytoptidae	",
				"	Pilipalpus	",
				"	Pimelea villosa	",
				"	Pironastrea	",
				"	Pityeja	",
				"	Plagiochaeta lineata	",
				"	Plagiochaeta stewartensis	",
				"	Plagiochaeta sylvestris	",
				"	Platycuma	",
				"	Pleonexes	",
				"	Pleonexes macrocornutus	",
				"	Plesiothrips	",
				"	Pliolampadidae	",
				"	Pneumolaelaps	",
				"	Podagra	",
				"	Poecilotheriinae	",
				"	Pogonogasterini	",
				"	Polycirrinae	",
				"	Polyodaspis	",
				"	Polyscia	",
				"	Polyzoniidae	",
				"	Pontania proxima	",
				"	Pontodrilus lacustris	",
				"	Pontodrilus litoralis	",
				"	Pramila	",
				"	Praon	",
				"	Prasinocyma semicrocea	",
				"	Prepotherium	",
				"	Preptos	",
				"	Prethura hutchingsae	",
				"	Primula elatior subsp. elatior	",
				"	Pristoderus bellus	",
				"	Pristoderus chloreus	",
				"	Pristoderus cornutus	",
				"	Pristoderus duvalensis	",
				"	Pristoderus monteithi	",
				"	Pristoderus occidentalis	",
				"	Pristoderus occultus	",
				"	Pristoderus spinosus	",
				"	Pristoderus tomentosus	",
				"	Probole	",
				"	Proconus	",
				"	Prolesophanta nelsonensis	",
				"	Proprepotherium	",
				"	Propsocus pallipes	",
				"	Prosopocoilus	",
				"	Prostoma eilhardi	",
				"	Prostoma graecense	",
				"	Proteodes	",
				"	Proteuxoa	",
				"	Protithona	",
				"	Protochelifer exiguus	",
				"	Protochelifer novaezealandiae	",
				"	Protohyale campbellica	",
				"	Protohyale grenfelli	",
				"	Protohyale maroubrae	",
				"	Protohyale rubra	",
				"	Protulophila	",
				"	Pseudacaudella	",
				"	Pseudocolus	",
				"	Pseudoeudesis	",
				"	Pseudonemadus australis	",
				"	Pseudonemadus compactus	",
				"	Pseudonemadus elegans	",
				"	Pseudonemadus exiguus	",
				"	Pseudonemadus integer	",
				"	Pseudonemadus irregularis	",
				"	Pseudonemadus pusillus	",
				"	Pseudonemadus sagittarius	",
				"	Pseudonemadus transvestitus	",
				"	Pseudopanax laetus	",
				"	Pseudoregma	",
				"	Pseudovermidae	",
				"	Psodos	",
				"	Psyllaephagus abyssus	",
				"	Psyllaephagus alienus	",
				"	Psyllaephagus aquilus	",
				"	Ptenidium fuscicorne	",
				"	Ptenidium laevigatum	",
				"	Ptyonota	",
				"	Pucciniastrum	",
				"	Pupinidae	",
				"	Pycnostigminae	",
				"	Pygmaena	",
				"	Pylorgus	",
				"	Queenslandina	",
				"	Ramitia	",
				"	Recordoxylon	",
				"	Regatarma	",
				"	Reischekia coracoides	",
				"	Reischekia exigua	",
				"	Resupinatus	",
				"	Rhododrilus agathis	",
				"	Rhododrilus aquaticus	",
				"	Rhododrilus besti	",
				"	Rhododrilus cockaynei	",
				"	Rhododrilus dobsoni	",
				"	Rhododrilus edulis	",
				"	Rhododrilus huttoni	",
				"	Rhododrilus kermadecensis	",
				"	Rhododrilus leptomerus	",
				"	Rhododrilus microgaster	",
				"	Rhododrilus minutus	",
				"	Rhododrilus monticola	",
				"	Rhododrilus rosae	",
				"	Rhododrilus similis	",
				"	Rhododrilus subtilis	",
				"	Rhopalosiphoninus	",
				"	Rhopalostylis	",
				"	Rhuma	",
				"	Rhytidoponera chalybaea	",
				"	Rhyzobius aurantii	",
				"	Rhyzobius discolor	",
				"	Rhyzobius nitidus	",
				"	Richardiinae	",
				"	Ringiculidae	",
				"	Rosbeeva pachyderma	",
				"	Rosenvingea	",
				"	Rucana	",
				"	Rugonectria	",
				"	Rugotruncana	",
				"	Rugotruncana circumnodifer	",
				"	Sacculina	",
				"	Sagmariasus	",
				"	Sannina	",
				"	Sarcosomataceae	",
				"	Sardia	",
				"	Scalpelloniscus	",
				"	Scaptia abdominalis	",
				"	Scaptia adrel	",
				"	Scaptia alpina	",
				"	Scaptia auranticula	",
				"	Scaptia aurata	",
				"	Scaptia aureohirta	",
				"	Scaptia aureovestita	",
				"	Scaptia aurinotum	",
				"	Scaptia auripleura	",
				"	Scaptia bancrofti	",
				"	Scaptia barbara	",
				"	Scaptia berylensis	",
				"	Scaptia bicolorata	",
				"	Scaptia binotata	",
				"	Scaptia brevirostris	",
				"	Scaptia calabyi	",
				"	Scaptia calliphora	",
				"	Scaptia cinerea	",
				"	Scaptia clavata	",
				"	Scaptia clelandi	",
				"	Scaptia divisa	",
				"	Scaptia fulgida	",
				"	Scaptia gemina	",
				"	Scaptia georgii	",
				"	Scaptia gibbula	",
				"	Scaptia guttata	",
				"	Scaptia guttipennis	",
				"	Scaptia ianthina	",
				"	Scaptia inopinata	",
				"	Scaptia jacksoniensis	",
				"	Scaptia jacksonii	",
				"	Scaptia lasiophthalma	",
				"	Scaptia limbithorax	",
				"	Scaptia media	",
				"	Scaptia minuscula	",
				"	Scaptia muscula	",
				"	Scaptia neoconcolor	",
				"	Scaptia neotricolor	",
				"	Scaptia nigerrima	",
				"	Scaptia nigroapicalis	",
				"	Scaptia nigrocincta	",
				"	Scaptia norrisi	",
				"	Scaptia orba	",
				"	Scaptia orientalis	",
				"	Scaptia patula	",
				"	Scaptia pictipennis	",
				"	Scaptia plana	",
				"	Scaptia pulchra	",
				"	Scaptia quadrimacula	",
				"	Scaptia regisgeorgii	",
				"	Scaptia roei	",
				"	Scaptia rufonigra	",
				"	Scaptia similis	",
				"	Scaptia singularis	",
				"	Scaptia subcana	",
				"	Scaptia subcinerea	",
				"	Scaptia subcontigua	",
				"	Scaptia testacea	",
				"	Scaptia testaceomaculata	",
				"	Scaptia vertebrata	",
				"	Scaptia vicina	",
				"	Scaptia violacea	",
				"	Scaptia walkeri	",
				"	Scaptia xanthopilis	",
				"	Schimia	",
				"	Schizymeniaceae	",
				"	Sciodrepoides	",
				"	Scleropauropus dugdalei	",
				"	Scolopendrellidae	",
				"	Scopelocheiridae	",
				"	Scymbalium	",
				"	Seiodidae	",
				"	Setomimini	",
				"	Severinia (Rutaceae)	",
				"	Shaka	",
				"	Sinkara	",
				"	Sinoconodon	",
				"	Siphoninus	",
				"	Siteroptes	",
				"	Sitobion	",
				"	Snowia	",
				"	Solenofigites	",
				"	Solenopsis (ICBN)	",
				"	Sphaerolaiminae	",
				"	Sphaerulariidae	",
				"	Spilopsocus colliensis	",
				"	Stathmopoda campylocha	",
				"	Stauralia	",
				"	Stiria	",
				"	Studeria	",
				"	Stylopauropoides zelandus	",
				"	Subacronicta	",
				"	Subantarctia	",
				"	Supella	",
				"	Syllis	",
				"	Sylvodrilus gravus	",
				"	Synsphyronus lineatus	",
				"	Synsphyronus melanochelatus	",
				"	Synthetonychiidae	",
				"	Systellochernes alacki	",
				"	Systellochernes zonatus	",
				"	Syzeton immaculatus	",
				"	Syzeton lateralis	",
				"	Syzeton semitestaceus	",
				"	Syzetonellus alpicola	",
				"	Tacparia	",
				"	Tanaissus	",
				"	Tanapseudinae	",
				"	Tarasco	",
				"	Tasmangarica	",
				"	Teratomyces	",
				"	Tethinosoma fulvifrons	",
				"	Tetriginae	",
				"	Thalaina	",
				"	Thalassochernes kermadecensis	",
				"	Thalassochernes taierensis	",
				"	Thecidellina	",
				"	Thelonectria	",
				"	Therapis	",
				"	Therioaphis	",
				"	Thripsaphis	",
				"	Thymaris	",
				"	Tingena	",
				"	Tinodon	",
				"	Tirista	",
				"	Titulcia	",
				"	Tococa	",
				"	Topobea	",
				"	Tornatellinops	",
				"	Tornos	",
				"	Toxoptera	",
				"	Trachycrusus	",
				"	Trachygamasus	",
				"	Triaenonychidae	",
				"	Trichobranchidae	",
				"	Trichogrammatidae	",
				"	Trichopsocidae	",
				"	Trichostrongylus	",
				"	Tricimba	",
				"	Trigonotylus	",
				"	Trioplognathus	",
				"	Trioxys	",
				"	Triphasia	",
				"	Tristemma	",
				"	Trombidiinae	",
				"	Trypetina	",
				"	Trypetocoris	",
				"	Tubulifera	",
				"	Turbicellepora	",
				"	Tylencholaimellus	",
				"	Typhlodromus (Typhlodromus)	",
				"	Typhloplanidae	",
				"	Udvardya	",
				"	Ungla	",
				"	Urostola	",
				"	Vanicela	",
				"	Venusticrus	",
				"	Vexillariidae	",
				"	Vinemina	",
				"	Virgulariidae	",
				"	Vitacea	",
				"	Vozzhennikovia	",
				"	Weinmannia racemosa	",
				"	Withius piger	",
				"	Woodwardiana	",
				"	Xenolpium pacificum	",
				"	Xiphidiopsis (Xiphidiopsis)	",
				"	Yaminuechelys gasparinii	",
				"	Yaminuechelys major	",
				"	Yermoia	",
				"	Zapyrastra calliphana	",
				"	Zelandiscus	",
				"	Zelotypia	",
				"	Zernyia	",
				"	Zeugodacus	",
				"	Zotheca	",
				
		};
		crawlAllStoredLinks = false;
		//*/
		
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(forceNewDownloadForCache);
		crawler.pushStoredLinks(crawlAllStoredLinks, args);
		crawler.crawl();
	}
	
	private boolean forceNewDownloadForCache = false;
	private Stack<ParseStatus> nextStack = new Stack<ParseStatus>();
	private Stack<ParseStatus> currentStack = new Stack<ParseStatus>();
	private Set<ParseStatus> found = new HashSet<ParseStatus>();
	private WikiSpeciesParser parser = new WikiSpeciesParser();
	private RedirectPageParser redirectPageParser = new RedirectPageParser();
	private int updatedCount = 0;
	
	public void crawlStoredLinks() throws Exception {
		pushStoredLinks(true);
		crawl();
	}
	
	public void pushStoredLinks(String... actualLinks) {
		pushStoredLinks(true, actualLinks);
	}
	public void pushStoredLinks(boolean findAll, String... actualLinks) {
		Set<String> actualSet = new HashSet<String>();
		actualSet.addAll(Arrays.asList(actualLinks));
		actualSet = fixEntities(actualSet);
		pushStoredLinks(actualSet, findAll);
	}
	private Set<String> fixEntities(Set<String> names) {
		Set<String> fixed = new HashSet<String>();
		for (String name: names) {
			name = name.trim();
			name = EntityMapper.convertToSymbolsText(name);
			fixed.add(name);
		}
		return fixed;
	}
	/**
	 * @param namesToForce Use these if we're doing some for a special reason
	 * 	these are guaranteed to be crawled regardless of their status.
	 */
	public void pushStoredLinks(Set<String> namesToForce) {
		pushStoredLinks(namesToForce, true);
	}
	public void pushStoredLinks(Set<String> namesToForce, boolean findAll) {
		List<ParseStatus> all = parseStatusService.findAllStatus();
		if (findAll) {
			// add all that aren't done, 
			// plus any that are done that we want to force
			for (ParseStatus s: all) {
				found.add(s);
				if (!ParseStatus.DONE.equals(s.getStatus())
						|| namesToForce.contains(s.getLatinName())) {
					currentStack.push(s);
					// don't want to add twice
					namesToForce.remove(s.getLatinName());
				}
			}
		} else {
			found.addAll(all);
		}
		// add all names to force that weren't already in the DB
		for (String latin: namesToForce) {
			ParseStatus i = new ParseStatus();
			i.setUrl(latin);
			i.setStatus(ParseStatus.FOUND);
			currentStack.push(i);
			found.add(i);
		}
	}
	
	public void crawl() throws Exception {
		while (!currentStack.empty()) {
			// loop for all found links
			while (!currentStack.empty()) {
				ParseStatus status = currentStack.pop();
	//			LogHelper.speciesLogger.info(found);
				if (status.getType() != null) {
					continue;
				}
				LogHelper.speciesLogger.info("crawlOne." + currentStack.size() + "." + status);
				crawlOne(status);
			}
			currentStack = nextStack;
			nextStack = new Stack<ParseStatus>();
		}
	}
	public void crawlOne(ParseStatus ps) throws Exception {
		// get the contents of the page
		String page = WikiSpeciesCache.CACHE.readFile(ps.getLatinName(), forceNewDownloadForCache);
		if (page == null) {
			return;
		}
		// save it
//		savePage(ps, page);
		// visit the link before getting more links
		visitPage(ps, page);
		// search for the right patterns, ie <a href="/wiki/Biciliata"
		Set<String> links = parseLinks(page);
		for (String link: links) {
			ParseStatus status = new ParseStatus();
			status.setUrl(link);
			status.setStatus(ParseStatus.FOUND);
			saveLink(status);
		}
		// now that we've finished it, mark it as complete
		ps.setDate(new Date());
		ps.setStatus(ParseStatus.DONE);
		parseStatusService.updateStatus(ps);
	}

	public void visitUnparseablePage(ParseStatus ps, String page) {
		String redirectTo = redirectPageParser.getRedirectTo(page);
		if (redirectTo != null) {
			speciesService.updateRedirect(ps.getLatinName(), redirectTo);
		}
	}
	
	public static Set<String> parseLinks(String page) {
		page = StringUtils.replace(page, "\n", "`"); // TODO why do I need to do this? (again..)
		page = StringUtils.replace(page, "\r", "`"); // TODO why do I need to do this?
		Set<String> links = new HashSet<String>();
		Pattern linksPattern = Pattern.compile("href=\"/wiki/(.*?)\"");
		Matcher matcher = linksPattern.matcher(page);
		while (matcher.find()) {
			// save the links
			String link = matcher.group(1);
			link = StringUtils.replace(link, "_", " ");
			link = EntryUtilities.urlDecode(link);
			link = WikiSpeciesParser.cleanCharacters(link);
			if (!isSkippableLink(link)) {
				links.add(link);
			}
		}
		return links;
	}

	public void saveLink(ParseStatus link) {
		// check if we've already checked this link, and how long ago
		boolean added = found.add(link);
		if (added) {
			// record the status of the link
			// push to the stack
			LogHelper.speciesLogger.info("foundNewLink." + link.getLatinName());
			nextStack.push(link);
			parseStatusService.updateStatus(link);
		}
	}
	
	public void visitPage(ParseStatus link, String page) {
		String type = getType(link.getLatinName(), page);
		if (type != null) {
			LogHelper.speciesLogger.info("type." + link.getLatinName() + "." + type);
			link.setType(type);
			return;
		}
		boolean isDeleted = isDeleted(page);
		link.setDeleted(isDeleted);
		if (isDeleted) {
			LogHelper.speciesLogger.info("deleted." + link.getLatinName());
			return;
		}
		// parse it
		CompleteEntry results = parsePage(link, page);
		if (results == null) {
			visitUnparseablePage(link, page);
		} else {
			// checking for rank is a temp fix for over-zealous recursion on this
			while (results != null && results.getRank() != null) {
				parsed(results);
				results = results.getParent();
			}
		}
	}
	private CompleteEntry parsePage(ParseStatus link, String page) {
		String name = link.getLatinName();
		CompleteEntry results = parser.parse(name, page);
		if (results != null) {
			return results;
		}
		// try the redirect "from" name(s)
		List<String> froms = speciesService.findRedirectFrom(name);
		for (String from: froms) {
			results = parser.parse(from, page);
			if (results != null) {
				return results;
			}
		}
		return null;
	}
	
	public void parsed(CompleteEntry entry) {
		boolean updated = speciesService.updateOrInsertEntryMaybe(entry);
		if (!updated) {
			return;
		}
		System.out.print("> updated." + (updatedCount++) + " > ");
		if (entry.getCommonName() != null) {
			System.out.print(entry.getCommonName());
		} else {
			System.out.print("--");
		}
		System.out.print("/");
		System.out.print(entry.getLatinName());
		if (entry.getImageLink() != null) {
			System.out.print("/");
			System.out.print(entry.getImageLink());
		}
		System.out.println();
	}
	
	public static boolean isSkippableLink(String link) {
		if (link.length() > 300) {
			return true;
		}
		if (link.contains(":")) {
			return true;
		}
		if (link.contains("#")) {
			return true;
		}
		if (link.contains("?")) {
			return true;
		}
		// check for chinese
		if (!chinese.matcher(link).matches()) {
			return true;
		}
		if (isForeign(link)) {
			return true;
		}
		return false;
	}
	private static boolean isForeign(String t) {
		// see how many %AA we have - there can't be very many if it's english
		int count = StringUtils.countMatches(t, "%");
		return count >= 5; // 3 might be better, but this hasn't really actually been a problem
	}
	private static Pattern chinese = Pattern.compile(".*[a-zA-Z]{2,}.*");
	private static boolean isDeleted(String page) {
		return page.contains(WikiSpeciesCache.DELETED_PAGE);
	}
	private static Pattern[] authTypes = getAuthTypes();
	public static Pattern[] getAuthTypes() {
		String[] authTypes = {
				"([A-Za-z]+_)?Taxon_Authorities", "Repositories",
//				"Entomologists", "Botanists", "Lichenologists",	"Palaeontologists", "Paleobotanists", "Ichthyologists",
				"[A-Za-z_]+ists",
				"ISSN"};
		Pattern[] patterns = new Pattern[authTypes.length];
		for (int i = 0; i < authTypes.length; i++) {
			patterns[i] = Pattern.compile("<a href=\"/wiki/Category\\:" + authTypes[i]);
		}
		return patterns;
	}
	public static String getType(String latinName, String page) {
		if (latinName.startsWith("ISSN")) {
			return ParseStatus.AUTHORITY;
		}
		for (Pattern authType: authTypes) {
			Matcher m = authType.matcher(page);
			if (m.find()) {
				return ParseStatus.AUTHORITY;
			}
		}
		String[] authHints = {
				"<span class=\"mw-headline\" id=\"Authored_taxa\">Authored taxa</span>",
				"<span class=\"mw-headline\" id=\"Described_taxa\">Described taxa</span>",
				"<span class=\"mw-headline\" id=\"works_include\">works include</span>",
				"<span class=\"mw-headline\" id=\"work_include\">works include</span>",
				"<span class=\"mw-headline\" id=\"works_including\">works including</span>",
		};
		for (String hint: authHints) {
			int find = StringUtils.indexOfIgnoreCase(page, hint);
			if (find > 0) {
				return ParseStatus.AUTHORITY;
			}
		}
		
		// because some hints might not be conclusive, we only check them if there is also no taxobox
		boolean hasTaxoBox = page.contains("id=\"Taxonavigation\">Taxonavigation");
		if (!hasTaxoBox) {
			String[] authHints2 = {
					"id=\"Publications\">Publications",
					"<li><b>Dates:</b>",
					"<li><b>Dates</b>", // <li><b>Dates</b> 1758-1759, 2 vols. [2: 825-1384]</li>
			};
			for (String hint: authHints2) {
				int find = StringUtils.indexOfIgnoreCase(page, hint);
				if (find > 0) {
					return ParseStatus.AUTHORITY;
				}
			}
		}
		
		
		// CAN'T DO -- some good pages are also disambiguation
//		if (page.contains("<a href=\"/wiki/Category:Disambiguation_pages\"")) {
//			return true;
//		}
		return null;
	}
	public void setForceNewDownloadForCache(boolean forceNewDownloadForCache) {
		this.forceNewDownloadForCache = forceNewDownloadForCache;
	}
}
