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

import com.robestone.species.Entry;
import com.robestone.species.LogHelper;
import com.robestone.species.Rank;
import com.robestone.species.UpdateType;
import com.robestone.util.html.EntityMapper;

public class WikiSpeciesCrawler extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		boolean forceNewDownloadForCache = true;
		boolean crawlAllStoredLinks = false;
		boolean argIsParentTree = !true;
		boolean downstreamOnly = false;
		boolean crawlOne = true; // to just "crawl" one only
		int distance = 2;
		//*
		args =  
//		 new String[] { "Pinnipediformes" };
		StringUtils.split(CRAWL_LIST, "\n\r"); // paste the RTRIM(latin_name) results from any search
		//*/
		
		WikiSpeciesCrawler crawler = new WikiSpeciesCrawler();
		crawler.setForceNewDownloadForCache(forceNewDownloadForCache);
		
		if (crawlOne) {
			for (String url : args) {
				ParseStatus ps = new ParseStatus();
				ps.setUrl(url);
				crawler.crawlOne(ps, false);
			}
		} else {
			if (argIsParentTree) {
				crawler.pushTree(args[0], distance, downstreamOnly);
			} else {
				crawler.pushOnlyTheseNames(new HashSet<String>(Arrays.asList(args)));
			}
			if (crawlAllStoredLinks) {
				crawler.pushAllFoundLinks();
			}
			crawler.crawl();
		}
	}
	
	private boolean forceNewDownloadForCache = false;
	private Stack<ParseStatus> nextStack = new Stack<ParseStatus>();
	private Stack<ParseStatus> currentStack = new Stack<ParseStatus>();
	private Set<ParseStatus> found = new HashSet<ParseStatus>();
	private WikiSpeciesParser parser = new WikiSpeciesParser();
	private RedirectPageParser redirectPageParser = new RedirectPageParser();
	private int updatedCount = 0;
	
	
	public void pushTree(String rootLatinName, int distance, boolean downstreamOnly) {
		Set<String> names = speciesService.findLatinNamesInTree(rootLatinName, distance, downstreamOnly);
		pushOnlyTheseNames(names);
	}
	
	/**
	 * Pushes any links in the Crawl table in status FOUND
	 */
	public void pushAllFoundLinks() throws Exception {
		List<ParseStatus> found = parseStatusService.findAllStatus();
		System.out.println("pushAllFoundLinks" + found.size());
		pushLinks(found);
	}
	/**
	 * Pushes any links in the Crawl table regardless of status
	 * - used only for full recrawling.
	 */
	public void pushAllStatus() throws Exception {
		List<ParseStatus> found = parseStatusService.findAllStatus();
		System.out.println("pushAllStatus" + found.size());
		pushLinks(found);
	}
	private void markAllDoneLinks() throws Exception {
		List<ParseStatus> found = parseStatusService.findAllStatus();
		for (ParseStatus one: found) {
			if (one.isDone()) {
				this.found.add(one);
			}
		}
	}
	private void pushLinks(List<ParseStatus> status) throws Exception {
		for (ParseStatus s: status) {
			s.setUrl(s.getLatinName().trim()); // corner case if this gets in the DB
			push(s);
		}
	}
	private Set<String> fixEntities(Set<String> names) {
		Set<String> fixed = new HashSet<String>();
		for (String name: names) {
			name = name.trim();
			try {
				name = EntityMapper.convertToSymbolsText(name, false);
			} catch (Exception e) {
				// we can't do much... either skip or add as-is
			}
			fixed.add(name);
		}
		return fixed;
	}
	public void pushOnlyTheseNames(Set<String> namesToForce) {
		namesToForce = fixEntities(namesToForce);
		// add all names to force that weren't already in the DB
		for (String latin: namesToForce) {
			ParseStatus i = new ParseStatus();
			i.setUrl(latin);
			i.setStatus(ParseStatus.FOUND);
			currentStack.push(i);
			found.add(i);
		}
	}
	private void push(ParseStatus link) {
		boolean existed = found.add(link);
		// avoid pushing twice
		if (!existed && !link.isDone()) {
			currentStack.push(link);
		}
	}
	
	public void crawl() throws Exception {
		crawl(true);
	}
	public void crawl(boolean recurseStack) throws Exception {
		markAllDoneLinks();
		while (!currentStack.empty()) {
			// loop for all found links
			while (!currentStack.empty()) {
				ParseStatus status = currentStack.pop();
	//			LogHelper.speciesLogger.info(found);
				if (status.getType() != null) {
					continue;
				}
				LogHelper.speciesLogger.info(
						"crawlOne." + currentStack.size() + " < " + found.size() + 
						"." + status.getLatinName() + "." + status.getStatus() + "." + status.getType());
				crawlOne(status, recurseStack);
			}
			if (recurseStack) {
				currentStack = nextStack;
				nextStack = new Stack<ParseStatus>();
			} else {
				break;
			}
		}
	}
	public void crawlOne(ParseStatus ps) throws Exception {
		crawlOne(ps, true);
	}
	public Entry crawlOne(ParseStatus ps, boolean parseLinks) throws Exception {
		// get the contents of the page
		String page = WikiSpeciesCache.CACHE.readFile(ps.getLatinName(), forceNewDownloadForCache);
		if (page == null) {
			return null;
		}
		// visit the link before getting more links
		Entry results = visitPage(ps, page);
		if (parseLinks) {
			// search for the right patterns, ie <a href="/wiki/Biciliata"
			Set<String> links = parseLinks(page);
			for (String link: links) {
				ParseStatus status = new ParseStatus();
				status.setUrl(link);
				status.setStatus(ParseStatus.FOUND);
				saveLink(status);
			}
		}
		// now that we've finished it, mark it as complete
		ps.setDate(new Date());
		ps.setStatus(ParseStatus.DONE);
		parseStatusService.updateStatus(ps);
		return results;
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
			link = WikiSpeciesParser.cleanCharacters(link);
			link = link.trim();
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
	
	public Entry visitPage(ParseStatus link, String page) {
		// cannot tell a redirect page from auth page, so have to check for redirect first
		String redirect = redirectPageParser.getRedirectTo(page);
		if (redirect != null) {
			// we won't return from the method here, because it's okay to log both the entry and the redirect
			speciesService.updateRedirect(link.getLatinName(), redirect);
		} else {
			boolean isAuth = AuthorityUtilities.isAuthorityPage(link.getLatinName(), page);
			if (isAuth) {
				LogHelper.speciesLogger.info("type." + link.getLatinName() + ".AUTH");
				link.setType(ParseStatus.AUTHORITY);
				return null;
			}
		}
		boolean isDeleted = isDeleted(page);
		link.setDeleted(isDeleted);
		if (isDeleted) {
			LogHelper.speciesLogger.info("deleted." + link.getLatinName());
			return null;
		}
		// parse it
		Entry results = parsePage(link, page);
		Entry firstResults = results;
		if (results == null) {
//			visitUnparseablePage(link, page);
			// Nothing to do here...
			// Couldn't figure out what this page was...
			if (redirect == null) {
				LogHelper.speciesLogger.error(">>> Could Not Parse >>> " + link.getLatinName());
			}
		} else {
			// for the page I just crawled, do the real update
			udpateOrInsert(results, false);
			
			// for any parent/gparent, we will consider inserting if it doesn't already exist
			while ((results = results.getParent()) != null) {
				udpateOrInsert(results, true);
			}
		}
		return firstResults;
	}
	private Entry parsePage(ParseStatus link, String page) {
		String name = link.getLatinName();
		Entry results = parser.parse(name, page);
		if (isEntryParsedOkay(results)) {
			return results;
		}
		// try the redirect "from" name(s)
		List<String> froms = speciesService.findRedirectFrom(name);
		for (String from: froms) {
			results = parser.parse(name, from, page, true);
			if (results != null) {
				return results;
			}
		}
		return null;
	}
	/**
	 * Can't rely on other code to determine if this was parsed or not.
	 */
	private boolean isEntryParsedOkay(Entry e) {
		if (e == null) {
			return false;
		} else if (e.getRank() != null && e.getRank() != Rank.Error) {
			// if rank is null, then this didn't parse - that is a requirement
			return true;
		} else {
			return false;
		}
	}
	private void udpateOrInsert(Entry entry, boolean onlyInsert) {
		
		UpdateType updated;
		if (onlyInsert) {
			updated = speciesService.insertEntryMaybe(entry);
		} else {
			updated = speciesService.updateOrInsertEntryMaybe(entry);
		}
		if (updated == UpdateType.NoChange) {
			return;
		}
		System.out.print("> " + updated + "." + (updatedCount++) + " > ");
		if (entry.getCommonName() != null) {
			System.out.print(entry.getCommonName());
		} else {
			System.out.print("--");
		}
		System.out.print(" | ");
		System.out.print(entry.getLatinName());
		if (entry.getImageLink() != null) {
			System.out.print(" | ");
			System.out.print(entry.getImageLink());
		}
		System.out.println();
	}
	
	public static boolean isSkippableLink(String link) {
		if (link.length() > 300) {
			return true;
		}
		// avoid things like "Template:" but parse the virus groups like "Group I: ..."
		if (link.contains(":") && !link.startsWith("Group ")) {
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
		return page.contains(AbstractSiteFileCache.DELETED_PAGE);
	}
	public void setForceNewDownloadForCache(boolean forceNewDownloadForCache) {
		this.forceNewDownloadForCache = forceNewDownloadForCache;
	}
	
	public static final String CRAWL_LIST = 
  "Abies beshanzuensis\r\n"
  + "Acaciella\r\n"
  + "Acianthus pusillus\r\n"
  + "Aglaiocercus berlepschi\r\n"
  + "Ammonastes pelzelni\r\n"
  + "Ampelornis griseiceps\r\n"
  + "Anabazenops dorsalis\r\n"
  + "Asthenes ayacuchensis\r\n"
  + "Asthenes berlepschi\r\n"
  + "Asthenes coryi\r\n"
  + "Asthenes harterti\r\n"
  + "Asthenes vilcabambae\r\n"
  + "Atriplex sturtii\r\n"
  + "Atronanus fuliginosus\r\n"
  + "Automolus cervinigularis\r\n"
  + "Bourreria tomentosa\r\n"
  + "Brachysola halganiacea\r\n"
  + "Cacicus koepckeae\r\n"
  + "Campylopterus duidae\r\n"
  + "Campylorhamphus multostriatus\r\n"
  + "Campylorhamphus probatus\r\n"
  + "Chloanthes glandulosa\r\n"
  + "Chlorostilbon olivaresi\r\n"
  + "Cichlocolaptes mazarbarnetti\r\n"
  + "Colaptes aeruginosus\r\n"
  + "Conopophaga ardesiaca\r\n"
  + "Conostylis crassinerva subsp. crassinerva\r\n"
  + "Cranioleuca henricae\r\n"
  + "Cyanostegia corifolia\r\n"
  + "Cyanostegia cyanocalyx\r\n"
  + "Cyanostegia microphylla\r\n"
  + "Dendrocolaptes sanctithomae punctipectus\r\n"
  + "Diglossa duidae\r\n"
  + "Diglossa venezuelensis\r\n"
  + "Eriocnemis isabellae\r\n"
  + "Eriocnemis mirabilis\r\n"
  + "Eucalyptus sporadica\r\n"
  + "Euchrepomis sharpei\r\n"
  + "Frederickena fulva\r\n"
  + "Frederickena unduliger\r\n"
  + "Glaucidium parkeri\r\n"
  + "Goldmania bella\r\n"
  + "Grallaria albigula\r\n"
  + "Grallaria atuensis\r\n"
  + "Grallaria ayacuchensis\r\n"
  + "Grallaria cajamarcae\r\n"
  + "Grallaria chthonia\r\n"
  + "Grallaria cochabambae\r\n"
  + "Grallaria eludens\r\n"
  + "Grallaria erythroleuca\r\n"
  + "Grallaria excelsa\r\n"
  + "Grallaria gravesi\r\n"
  + "Grallaria griseonucha\r\n"
  + "Grallaria occabambae\r\n"
  + "Grallaria sinaensis\r\n"
  + "Grallaricula cumanensis\r\n"
  + "Grallaricula loricata\r\n"
  + "Grusonia aggeria\r\n"
  + "Havardia albicans\r\n"
  + "Herpsilochmus roraimae\r\n"
  + "Hypsipetes parvirostris\r\n"
  + "Knipolegus aterrimus heterogyna\r\n"
  + "Margarornis bellulus\r\n"
  + "Mariosousa\r\n"
  + "Molothrus armenti\r\n"
  + "Monosolenium tenerum\r\n"
  + "Mustelirallus colombianus\r\n"
  + "Myrmelastes brunneiceps\r\n"
  + "Myrmelastes caurensis\r\n"
  + "Myrmelastes saturatus\r\n"
  + "Myrmothera subcanescens\r\n"
  + "Myrmotherula ambigua\r\n"
  + "Myrmotherula grisea\r\n"
  + "Myrmotherula sunensis\r\n"
  + "Nasikabatrachus bhupathi\r\n"
  + "Nesospiza wilkinsi\r\n"
  + "Oceanites pincoyae\r\n"
  + "Odontophorus dialeucos\r\n"
  + "Oxystophyllum\r\n"
  + "Percnostola arenarum\r\n"
  + "Pheugopedius schulenbergi\r\n"
  + "Phlegopsis borbae\r\n"
  + "Phragmipedium [215] talamancanum\r\n"
  + "Pilularia americana\r\n"
  + "Pithys castaneus\r\n"
  + "Pityrodia brynesii\r\n"
  + "Polioptila clementsi\r\n"
  + "Polioptila facilis\r\n"
  + "Polioptila guianensis\r\n"
  + "Polioptila paraensis\r\n"
  + "Premnoplex tatei\r\n"
  + "Pyriglena atra\r\n"
  + "Pyriglena similis\r\n"
  + "Pyrrhura hoematotis\r\n"
  + "Quercus laceyi\r\n"
  + "Quercus mohriana\r\n"
  + "Quercus pungens\r\n"
  + "Quoya paniculata\r\n"
  + "Ramphocaenus sticturus\r\n"
  + "Ramphocinclus sanctaeluciae\r\n"
  + "Rhaphidospora bonneyana\r\n"
  + "Sakesphorus canadensis pulchellus\r\n"
  + "Sciaphylax castanea\r\n"
  + "Setopagis heterura\r\n"
  + "Setopagis maculosa\r\n"
  + "Stigmatodactylus aegeridantennatus\r\n"
  + "Stigmatodactylus aquamarinus\r\n"
  + "Stigmatodactylus bracteatus\r\n"
  + "Stigmatodactylus confusus\r\n"
  + "Stigmatodactylus corniculatus\r\n"
  + "Stigmatodactylus croftianus\r\n"
  + "Stigmatodactylus cymbalariifolius\r\n"
  + "Stigmatodactylus elegans\r\n"
  + "Stigmatodactylus gibbsiae\r\n"
  + "Stigmatodactylus grandiflorus\r\n"
  + "Stigmatodactylus halleanus\r\n"
  + "Stigmatodactylus heptadactylus\r\n"
  + "Stigmatodactylus lamrii\r\n"
  + "Stigmatodactylus macroglossus\r\n"
  + "Stigmatodactylus oxyglossus\r\n"
  + "Stigmatodactylus paradoxus\r\n"
  + "Stigmatodactylus richardianus\r\n"
  + "Stigmatodactylus serratus\r\n"
  + "Stigmatodactylus sikokianus\r\n"
  + "Stigmatodactylus tenuilabris\r\n"
  + "Stigmatodactylus variegatus\r\n"
  + "Stigmatodactylus veillonis\r\n"
  + "Stigmatodactylus vulcanicus\r\n"
  + "Streptoprocne phelpsi\r\n"
  + "Synallaxis castanea\r\n"
  + "Synallaxis moesta\r\n"
  + "Syndactyla striata\r\n"
  + "Thamnistes anabatinus aequatorialis\r\n"
  + "Thamnistes rufescens\r\n"
  + "Thamnophilus aroyae\r\n"
  + "Thamnophilus bernardi shumbae\r\n"
  + "Turdus daguae\r\n"
  + "Turdus haplochrous\r\n"
  + "Turdus murinus\r\n"
  + "Turdus ravidus\r\n"
  + "Vireo latimeri\r\n"
  + "Vireo masteri\r\n"
  + "Westringia cremnophila\r\n"
  + "Westringia grandifolia\r\n"
  + "Xenodacnis parina";
}
