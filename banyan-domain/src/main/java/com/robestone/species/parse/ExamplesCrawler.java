package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.Entry;
import com.robestone.species.LogHelper;

public class ExamplesCrawler extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new ExamplesCrawler().crawlExamplesTerms();
	}
	
	/**
	 * For new installs just to help jump start things.
	 * @throws Exception 
	 */
	public void crawlExamplesTerms() throws Exception {
		
		Set<String> allLinks = new HashSet<String>();
		Set<String> crawlLinks = new HashSet<String>();
		
//		crawlLinks.add("Mammaliaformes");
		crawlLinks.addAll(getTerms());
		
		boolean forceDownload = false; // set to true if the local files should be re-doownloaded
		
		AbstractSiteFileCache.DEFAULT_MAX_RETRIES = 0;
		
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(forceDownload);
		
		while (!crawlLinks.isEmpty()) {
			LogHelper.speciesLogger.info("crawlExamplesTerms.found." + crawlLinks.size());
			Set<String> newLinks = new HashSet<String>();
			for (String name : crawlLinks) {
				ParseStatus ps = new ParseStatus();
				ps.setUrl(name);
				Entry one = crawler.crawlOne(ps, false);
				if (one != null) {
					Entry fresh = speciesService.findEntryByLatinName(name, true);
					LogHelper.speciesLogger.info("crawlExamplesTerms." + name + "." + fresh.getId() + ">" + fresh.getParentLatinName());
					if (fresh.getParentLatinName() == null) {
						LogHelper.speciesLogger.info("crawlExamplesTerms." + name + "." + fresh.getId() + ">null");
					} else {
						newLinks.add(fresh.getParentLatinName());
					}
				} else {
					LogHelper.speciesLogger.info("crawlExamplesTerms." + name + ".NotFound");
				}
			}
			newLinks.removeAll(allLinks);
			allLinks.addAll(newLinks);
			crawlLinks = newLinks;
		}
	}
	
	public List<String> getTerms() {
		List<String> list = new ArrayList<String>();
		for (String raw : rawTerms) {
			String[] some = raw.replace("!", "").replace("$", "").split(",");
			list.addAll(Arrays.asList(some));
		}
		return list;
	}
	
	private String[] rawTerms = {

			
			"Proboscidea,$Panthera tigris,Suidae,!Pholidota,!Chiroptera,Delphinidae",
			"!Coleoptera,$Orthoptera,!Blattodea,Anisoptera,Vespoidea,Tuberolachnus salignus",
			"Malus domestica,$Nymphaeaceae,Cactaceae,Cucumis sativus",
			"$Mobula,Stegostoma fasciatum,Rhynchobatus djiddensis,!Squatiniformes",

			//-- OTHER_FAMILIES
			"Petaurus breviceps,Phascolarctos cinereus,$Macropus,Didelphis virginiana",
			"!Tubulidentata,$Elephantidae,Hyracoidea,Sirenia,Tenrecomorpha",
			"Theraphosidae,Panulirus longipes,$Chilopoda,!Scorpiones,!Acariformes,!Orthoptera",
			"$Filoviridae,Rabies lyssavirus,Parvoviridae",

			//-- HAVE_YOU_HEARD_OF
			"Ochotona,$Sylvilagus,Lepus,!Brachylagus",
			"$Microcaecilia,Lumbricus terrestris,Scaphiophryne,Bolitoglossa mexicana",
			"Phyllopteryx taeniolatus,$Solenostomidae,Corythoichthys schultzi,Hippocampus guttulatus",
			"$Okapia,Giraffa camelopardalis tippelskirchi,Giraffa camelopardalis reticulata,Giraffa camelopardalis giraffa",

			//-- YOU_MIGHT_NOT_KNOW (i.e. which is closer ...)
			"$Hippopotamidae,Rhinocerotidae,Suidae",
			"Felis,Canis familiaris,$Procyonidae,Bassariscus,!Nasua,Bassaricyon gabbii,!Potos",
			"Nicotiana sylvestris,Cucumis,$Lycopersicum esculentum",
			"Vespidae,$Formicidae,Termitoidae",

			//-- main page default tree
			"!Chondrichthyes,Canidae,!Amphibia,!Chelicerata,Crustacea,!Chlorophyta,Ananas comosus",
	};
	
}
