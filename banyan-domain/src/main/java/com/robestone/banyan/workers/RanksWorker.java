package com.robestone.banyan.workers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.robestone.banyan.taxons.Rank;
import com.robestone.banyan.taxons.Rank.RankType;
import com.robestone.banyan.util.DerbyDataSource;
import com.robestone.banyan.util.LogHelper;
import com.robestone.banyan.wikidata.WdTaxon;
import com.robestone.banyan.wikidata.WikiDataService;
import com.robestone.banyan.wikispecies.Entry;

public class RanksWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new RanksWorker().countRanksUsage();
	}
	
	/**
	 * Represents one child-rank > parent-rank combination.
	 * How often is it used?
	 * 
	 * There is no parent RankNode, as that has no meaning.
	 */
	private static class RankNode implements Comparable<RankNode> {
		Rank rank;
		Rank parentRank;
		String key; // TODO remove, unless I print it or validate it somewhere

		float rankEntriesCountPercent;
		float parentRankEntriesCountPercent;
		
		public RankNode(Rank rank, Rank parentRank, String key) {
			this.rank = rank;
			this.parentRank = parentRank;
			this.key = key;
		}
		
		@Override
		public int compareTo(RankNode o) {
			return o.key.compareTo(this.key);
		}
	}
	
	private Map<String, RankNode> nodesByKey = new HashMap<String, RankNode>();
	
	// TODO do I need both these, or just the parent one for crawling afterwards
	private Map<Rank, Set<RankNode>> nodesByParentRank = new HashMap<>();

	private Map<Rank, Integer> entryCountByRank = new HashMap<>();
	private Map<Rank, Integer> entryCountByParentRank = new HashMap<>();
	private Map<String, Integer> entryCountByKey = new HashMap<>();
	
	void buildEmpricalTaxonRankStrengths() throws Exception {
		List<Entry> entries = findAllWikiDataEntries();
		buildEmpricalTaxonRanks(entries);
	}

	void countRanksUsage() throws Exception {
		
		Map<Rank, Integer> ranksWs = new HashMap<>();
		Map<Rank, Integer> ranksWd = new HashMap<>();

		WikiDataService wdService = getWikiDataService();
		Map<String, WdTaxon> taxons = wdService.findAllTaxons();
		for (WdTaxon taxon : taxons.values()) {
			increment(taxon.getRank(), ranksWd);
		}
		
		Collection<String> latinWs = getWikiSpeciesService().findAllLatinNames();
		for (String latin : latinWs) {
			Entry e = getWikiSpeciesService().findEntryByLatinName(latin);
			if (e != null) {
				increment(e.getRank(), ranksWs);
			}
		}
		
		for (Rank rank : Rank.values()) {
			Integer ws = ranksWs.get(rank);
			Integer wd = ranksWd.get(rank);
			System.out.println(rank.name() + ", " + ws + ", " + wd);
		}
	}

	void findWsRanksNotUsedByWd() throws Exception {
		
		// this could be done by a join/group probably but I only need to run this for research and this lets me dig in
		Map<String, Rank> wdLatinToRank = new HashMap<>();
		
		WikiDataService wdService = getWikiDataService();
		Map<String, WdTaxon> taxons = wdService.findAllTaxons();
		for (String qid : taxons.keySet()) {
			WdTaxon taxon = taxons.get(qid);
			wdLatinToRank.put(taxon.getLatinName(), taxon.getRank());
		}
		taxons = null;
		
		Map<String, Integer> ranksDiffCount = new HashMap<>();
		for (String latin : wdLatinToRank.keySet()) {
			
			latin = StringUtils.removePattern(latin, "\\[[a-zA-z]+\\]");
			
			Rank wdRank = wdLatinToRank.get(latin);
			
			try {
				Entry entry = getWikiSpeciesService().findEntryByLatinName(latin);
				if (entry != null) {
					Rank wsRank = entry.getRank();
					
					if (wsRank != wdRank) {
						String key = wsRank + ", " + wdRank;
						increment(key, ranksDiffCount);
					}
				}
			} catch (Exception e) {
				System.out.println("Exception for: " + latin);
				e.printStackTrace();
			}
		}
		
		List<String> keys = new ArrayList<>(ranksDiffCount.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			Integer count = ranksDiffCount.get(key);
			System.out.println(key + ", " + count);
		}
	}
	
	void countRanks() throws Exception {
		List<Entry> entries = findAllWikiDataEntries();
		
		Map<Rank, Integer> ranksCount = new HashMap<>();
		Map<RankType, Integer> rankTypesCount = new HashMap<>();
		
		for (Entry entry : entries) {
			increment(entry.getRank(), ranksCount);
			if (entry.getRank() != null) {
				increment(entry.getRank().getRankType(), rankTypesCount);
			}
		}
		
		for (Rank rank : ranksCount.keySet()) {
			if (rank != null) {
				Integer rcount = ranksCount.get(rank);
				Integer tcount = rankTypesCount.get(rank.getRankType());
				System.out.println(rank + ", " + rcount + ", " + rank.getRankType() + ", " + tcount);
			}
		}
		
	}
	
	private <T> Integer increment(T key, Map<T, Integer> map) {
		Integer count = map.get(key);
		if (count == null) {
			count = 0;
		}
		count++;
		map.put(key, count);
		return count;
	}
	
	void buildEmpricalTaxonRankStrengthsPaths() throws Exception {
		List<Entry> entries = findAllWikiDataEntries();
		Map<String, Integer> paths = new HashMap<>();
		List<Entry> leaves = getLeaves(entries);
		
		for (Entry leaf : leaves) {
			StringBuilder buf = new StringBuilder();
			Set<Integer> recursiveCheck = new HashSet<>();
			RankType lastRankType = null;
			while (leaf != null) {
				boolean okay = recursiveCheck.add(leaf.getId());
				if (!okay) {
					break;
				}
				RankType leafRankType = null;
				if (leaf.getRank() != null) {
					leafRankType = leaf.getRank().getRankType();
				}
				if (leafRankType != lastRankType) {
					buf.append(leafRankType);
					buf.append(", ");
					lastRankType = leafRankType;
				}
				leaf = leaf.getParent();
			}
			String path = buf.toString();
			increment(path, paths);
		}
		
		for (String path : paths.keySet()) {
			System.out.println(paths.get(path) + ", " + path);
		}
		
	}
	private List<Entry> getLeaves(List<Entry> entries) {
		List<Entry> leaves = new ArrayList<>();

		// collect all ids that are parents to any entry
		Set<Integer> parentIds = new HashSet<>();
		for (Entry e : entries) {
			Integer pid = e.getParentId();
			if (pid != null) {
				parentIds.add(pid);
			}
		}

		// any entry that is not a parent is a leaf
		for (Entry e : entries) {
			if (!parentIds.contains(e.getId())) {
				leaves.add(e);
			}
		}
		
		return leaves;
	}
	
	void buildEmpricalTaxonRankStrengthsTable() throws Exception {
		buildEmpricalTaxonRankStrengths();
		List<Rank> ranks = new ArrayList<>(entryCountByRank.keySet());
		
		List<Rank> rows = new ArrayList<>();
		for (Rank rank : ranks) {
			if (rank != null) {
				rows.add(rank);
			}
		}
		Collections.sort(rows);
		rows.add(null);
		List<Rank> cols = new ArrayList<>(rows);
		System.out.print("Ranks, ");
		for (Rank col : cols) {
			System.out.print(col);
			System.out.print(", ");
		}
		System.out.println();
		boolean topLeft;
		for (Rank row : rows) {
			topLeft = true;
			System.out.print(row);
			System.out.print(", ");
			for (Rank col : cols) {
				String key;
				if (topLeft) {
					key = toKey(row, col);
				} else {
					key = toKey(col, row);
				}
				RankNode node = nodesByKey.get(key);
				
				float percent;
				if (node == null) {
					percent = 0;
				} else if (topLeft) {
					percent = node.parentRankEntriesCountPercent;
				} else {
					percent = node.rankEntriesCountPercent;
				}
				
				System.out.printf("%.4f", percent);
				System.out.print(", ");
				
				if (col == row) {
					topLeft = false;
				}
			}
			System.out.println();
		}
	}
	
	public void buildEmpricalTaxonRanks() throws Exception {
		buildEmpricalTaxonRankStrengths();
		outputAfterBuildingHierarchy();
	}
	private void buildEmpricalTaxonRanks(List<Entry> entries) throws Exception {
		System.out.println("buildEmpricalTaxonRanks: " + entries.size());
		Map<String, Entry> wsEntries = new HashMap<String, Entry>();
		entryCountByRank.put(null, 0);
		
		// build each node
		System.out.println("buildEmpricalTaxonRanks.addRankNodes");
		for (Entry entry : entries) {
			wsEntries.put(entry.getLatinName(), entry);
			RankNode node = addRankNode(entry);
			
			// track the number of entries using the rank
			Integer count = entryCountByRank.get(node.rank);
			if (count == null) {
				count = 0;
			}
			count++;
			entryCountByRank.put(node.rank, count);
			
			increment(node.parentRank, entryCountByParentRank);
			
			// the RankNode is being reused, we increment the number of entries using that key
			increment(node.key, entryCountByKey);
			
		}
		
		// TODO add in the WD nodes - compare to wsEntries
		
		// add up the counts and percents
		System.out.println("buildEmpricalTaxonRanks.nodesByRank");
		for (RankNode node : nodesByKey.values()) {
			Integer keyEntriesCount = entryCountByKey.get(node.key);
			
			// for the "left" part of the key, what percent of entries with that rank use this key
			Integer rankCount = entryCountByRank.get(node.rank);
			if (rankCount == null) {
				rankCount = 0;
			}
			node.rankEntriesCountPercent = ((float) keyEntriesCount / (float) rankCount);
			
			// for the "right" part of the key, what percent of entries with that rank use this key
			Integer parentRankCount = entryCountByRank.get(node.parentRank);
			if (parentRankCount == null) {
				parentRankCount = 0;
			}
			node.parentRankEntriesCountPercent = ((float) keyEntriesCount / (float) parentRankCount);

			Integer testParentRankCount = entryCountByRank.get(node.parentRank);
			
			if (node.rankEntriesCountPercent > 1 || node.parentRankEntriesCountPercent > 1) {
				System.out.println(
						"buildEmpricalTaxonRanks.nodesByRank: " + node.key + 
						", kc%=" + keyEntriesCount + ", tc%=" + testParentRankCount + 
						", ec%=" + rankCount + ", pc%=" + parentRankCount +
						", ep%=" + node.rankEntriesCountPercent + ", pp%=" + node.parentRankEntriesCountPercent
						);
			}
		}
	}
	private void outputAfterBuildingHierarchy() {
		RankNode root = new RankNode(Rank.Unranked, null, "null-Root");
		Set<String> usedKeys = new HashSet<>();
		output(root, 0, usedKeys);
	}
	private void output(RankNode node, int depth, Set<String> usedKeys) {
		String pad = "                                                                                              ".substring(0, depth * 3);
		System.out.println(pad + node.key + ", e%=" + node.rankEntriesCountPercent + ", p%=" + node.parentRankEntriesCountPercent);
		Set<RankNode> childNodes = nodesByParentRank.get(node.rank);
		usedKeys.add(node.key);
		if (childNodes == null) {
			return;
		}
		List<RankNode> children = new ArrayList<RanksWorker.RankNode>(childNodes);
		Collections.sort(children);
		
		for (RankNode child : children) {
			// don't recurse if it will make a loop
			if (!usedKeys.contains(child.key)) {
				float strengthPercentMin = .5f;
				float strengthPercent = child.parentRankEntriesCountPercent + child.rankEntriesCountPercent;
				if (strengthPercent >= strengthPercentMin) {
					output(child, depth + 1, usedKeys);
				}
			}
		}
	}
	public static String toKey(Object rank, Object parentRank) {
		return String.valueOf(rank) + " -> " + String.valueOf(parentRank);
	}
	private RankNode addRankNode(Entry entry) {
		Entry parent = entry.getParent();
		Rank rank = entry.getRank();
		Rank parentRank = (parent != null) ? parent.getRank() : Rank.Unranked;
		String key = toKey(rank, parentRank);
		RankNode node = nodesByKey.get(key);
		if (node == null) {
			node = new RankNode(rank, parentRank, key);
			nodesByKey.put(key, node);
			
			Set<RankNode> nodesByParentRankSet = nodesByParentRank.get(parentRank);
			if (nodesByParentRankSet == null) {
				nodesByParentRankSet = new HashSet<RankNode>();
				nodesByParentRank.put(parentRank, nodesByParentRankSet);
			}
			nodesByParentRankSet.add(node);
		}
		
		return node;
	}
	
	private WikiDataService getWikiDataService() {
		WikiDataService wdService = new WikiDataService();
		wdService.setDataSource(DerbyDataSource.getDataSource());
		return wdService;
	}
	private List<Entry> findAllWikiDataEntries() {
		List<Entry> entries = new ArrayList<Entry>();
		
		WikiDataService wdService = getWikiDataService();
		
		Map<String, WdTaxon> taxons = wdService.findAllTaxons();
		
		// there's no ID in the DB so we adapt it this way because it doesn't matter
		int entryId = 1;
		
		// first pass create the entries
		Map<String, Entry> qidToEntry = new HashMap<>();
		for (String qid : taxons.keySet()) {
			Entry entry = new Entry();
			WdTaxon child = taxons.get(qid);
			entry.setRank(child.getRank());
			entry.setLatinName(child.getLatinName());
			entry.setId(entryId++);

			qidToEntry.put(qid, entry);
			entries.add(entry);
		}
		
		// second pass connect the parents
		for (String qid : taxons.keySet()) {
			Entry entry = qidToEntry.get(qid);
			WdTaxon taxon = taxons.get(qid);
			if (taxon.getParentQid() != null) {
				Entry parent = qidToEntry.get(taxon.getParentQid());
				// these can be null because I didn't finish parsing all or there's a parsing error
				if (parent != null) {
					entry.setParent(parent);
					entry.setParentId(parent.getId());
				}
			}
		}
		
		return entries;
	}
	
	public void run() {
		Collection<Rank> ranks = getWikiSpeciesService().findUsedRanks();
		List<Rank> list = new ArrayList<Rank>(ranks);
		Collections.sort(list);
		for (Rank rank: list) {
			LogHelper.speciesLogger.info(rank.getRankIndex() + " " + rank + "/" + rank.getCommonName());
		}
	}
	
}
