package com.robestone.species;

import static com.robestone.species.EntityMapperJdbcTemplate.getString;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class SpeciesService implements ParameterizedRowMapper<CompleteEntry>, ISpeciesService {

	private Logger logger = Logger.getLogger(SpeciesService.class);
	
	public static final Integer TREE_OF_LIFE_ID = 141817;
	public static final CompleteEntry TREE_OF_LIFE_ENTRY = new CompleteEntry(Rank.Cladus, "Tree of Life", "Arbor vitae"); {
		TREE_OF_LIFE_ENTRY.setId(TREE_OF_LIFE_ID);
	}
	
	private SimpleJdbcTemplate template;
	private boolean useInterestingAttributesForSearches = true;

	private Cache cache;
	private EntrySearcher searcher;
	private CommonNameSplitter commonNameSplitter = new CommonNameSplitter();
	
	public String getBoringColumn() {
		if (useInterestingAttributesForSearches) {
			return "boring_final";
		} else {
			return "boring";
		}
	}
	private String getParentIdColumn() {
		if (useInterestingAttributesForSearches) {
			return "interesting_parent_id";
		} else {
			return "parent_id";
		}
	}
	
	public static boolean isTopLevelRank(String latinName) {
		Set<String> set = new HashSet<String>();
		set.add("Eukaryota");
		set.add("Archaea");
		set.add("Bacteria");
//		set.add("Virus");
		return set.contains(latinName);
	}
	/**
	 * @return a random tree of given size.
	 */
	public CompleteEntry findRandomTree(int treeSize) {
		Set<Integer> idsToUse = new HashSet<Integer>();
		Integer[] all = cache.getAllIds(); // assumes the cache is loaded
		int size = all.length;
		while (idsToUse.size() < treeSize) {
			int randomIndex = RandomUtils.nextInt(0, size);
			Integer id = all[randomIndex];
			EntryProperties e = cache.getEntryProperties(id);
			if (e.image != null && e.commonName != null) {
				idsToUse.add(id);
			}
		}
		
		return findTreeForNodes(idsToUse);
	}
	public void updateInterestingCrunchedIds(Entry entry) {
//		logger.debug("updateInterestingCrunchedIds." + entry.getId());
		String ids = null;
		if (entry.getInterestingCrunchedIds() != null) {
			ids = entry.getInterestingCrunchedIds().getCrunchedIds();
		}
		template.update(
				"update species set interesting_crunched_ids = ? where id = ?", 
				ids, entry.getId());
	}
	List<CompleteEntry> findEntriesForLuceneIndex() {
		List<CompleteEntry> entries = template.query(
				"select id, common_name, latin_name from species where boring_final = 0", this);
		return entries;
	}
	List<Integer> findAllIdsForCaching() {
		List<Integer> ids = template.query(
				"select id from species where boring_final = 0", 
				new ParameterizedSingleColumnRowMapper<Integer>());
		return ids;
	}
	public Tree findCompleteTreeFromPersistence() {
		Collection<CompleteEntry> entries = template.query(
				"select id, parent_id, common_name, latin_name, image_link from species", this);
		return EntryUtilities.buildTree(entries);
	}
	public void updateCommonNamesSharedWithSiblingsFalse() {
		template.update("update species set shares_sibling_name = 0");
	}
	public void updateCommonNameSharedWithSiblings(Entry entry) {
		template.update("update species set shares_sibling_name = ? where id = ?", 
				entry.isCommonNameSharedWithSiblings(), entry.getId());
	}
	public Tree findInterestingTreeFromPersistence() {
		logger.info("findInterestingTreeFromPersistence >");
		List<CompleteEntry> entries = template.query("select " +
				getMinimalEntryColumns(false) + 
				", interesting_crunched_ids, linked_image_id " +
				" from species where boring_final = 0", this);
		logger.info("findInterestingTreeFromPersistence." + entries.size());
		for (CompleteEntry entry: entries) {
			cleanEntryFromPersistence(entry);
		}
		logger.info("findInterestingTreeFromPersistence < cleaned > buildTree");
		Tree tree = EntryUtilities.buildTree(entries);
		logger.info("findInterestingTreeFromPersistence <");
		return tree;
	}
	public void updateFromBoringWork(Collection<CompleteEntry> interesting, Collection<CompleteEntry> boring) {
		updateFromBoringWorkMarkInteresting(interesting);
		updateFromBoringWorkMarkBoring(boring);
	}
	public void updateFromBoringWorkMarkInteresting(Collection<CompleteEntry> interesting) {
		int count = 0;
		int showEvery = 10000;
		logger.info(">updateFromBoringWork.interesting." + interesting.size());
		for (CompleteEntry e: interesting) {
			if (count++ % showEvery == 0) {
				logger.info(">updateFromBoringWork.interesting." + count + "." + e.getLatinName());
			}
			template.update("update species set " +
					"interesting_parent_id = ?, " +
					"interesting_child_count = ?, " +
					"boring_final = 0 " +
					"where id = ?",
					e.getParentId(),
					e.getLoadedChildrenSize(),
					e.getId());
		}
	}
	public List<Integer> findIdsForParentIds(Collection<Integer> parentIds) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", parentIds);		
		String sql = "select id from species where parent_id in (:ids)";
		
		return template.query(
				sql, 
				new ParameterizedSingleColumnRowMapper<Integer>(), parameters);
	}
	public void updateFromBoringWorkMarkBoring(Collection<CompleteEntry> boring) {
		int count = 0;
		logger.info(">updateFromBoringWork.boring." + boring.size());
		count = 0;
		// this size is really uncertain, but I know 100 will do at least a 4x speedup from size 1
		int subSize = 100;
		String placeholders = getPlaceholders(subSize);
		int subCount = 0;
		Object[] subIds = new Integer[subSize];
		for (CompleteEntry e: boring) {
			subIds[subCount] = e.getId();
			count++;
			subCount++;
			if (subCount == subSize || count == boring.size()) {
				for (int i = subCount; i < subIds.length; i++) {
					subIds[i] = -1;
				}
				logger.info(">updateFromBoringWork.boring." + count + "." + e.getLatinName());
				subCount = 0;
				template.update("update species set " +
						"interesting_parent_id = null, " +
						"boring_final = 1 " +
						"where id in (" + placeholders + ")",
						subIds);
			}
		}
		logger.info("<updateFromBoringWork");
	}
	/**
	 * TODO replace with Map Sql thing
	 */
	private String getPlaceholders(int size) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append('?');
		}
		return buf.toString();
	}
	public void updateCommonName(CompleteEntry e) {
		template.update("update species set common_name = ? where id = ?", e.getCommonName(), e.getId());
	}
	public Collection<Rank> findUsedRanks() {
		Collection<Integer> nums = template.query("select distinct(rank) from species", 
				new ParameterizedSingleColumnRowMapper<Integer>());
		Set<Rank> ranks = new HashSet<Rank>();
		for (Integer num: nums) {
			ranks.add(Rank.valueOf(num));
		}
		return ranks;
	}
	
	public void fixExtinct() {
		logger.info(">fixExtinct");
		while (true) {
			// select all that are extinct (1)
			Collection<CompleteEntry> entries = template.query(
					"select id, parent_id from species where extinct = 1", this);
			// update all children
			int updated = 0;
			for (CompleteEntry entry: entries) {
				// only update those needing updating
				updated += template.update(
						"update species set extinct = 1 where parent_id = ? and not extinct = 1", 
						entry.getId());
			}
			logger.debug("fixExtinct.updated." + updated);
			if (updated == 0) {
				break;
			}
		}
		logger.info("<fixExtinct");
	}
	
	public void updateRedirect(String from, String to) {
		to = to.replace("_", " ");
		logger.info("updateRedirect." + from + " > " + to);
		// not sure I need to delete the old species - it will be orphaned, but might be helpful in research
		// another reason to leave the entry is that helps my parse done changer
//		template.update("delete from species where latin_name = ?", from);
		int count = template.update("update redirect set redirect_to = ? where redirect_from = ?", to, from);
		if (count == 0) {
			template.update("insert into redirect (redirect_from, redirect_to) values (?, ?)", from, to);
		}
	}
	/**
	 * Rare, but it happens, due to an improper refreshing when wikispecies has a
	 * template change, but the pages using the template aren't re-parsed.
	 * Also, something to do with redirects.
	 */
	private void fixSelfParentIds() {
		template.update("update species set parent_id = null where id = parent_id");
	}
	/**
	 * This happens usually because of a redirect page, but can also
	 * happen when a worker is stopped abnormally.
	 */
	public void assignParentIdsForNullOrMissingId() {
		logger.info(">assignParentIdsForNullOrMissingId");
		Collection<CompleteEntry> entries = template.query(
				"select id, parent_id, parent_latin_name, latin_name from species ", this);
		Map<String, String> redirects = findAllRedirectFromsMap();
		Map<String, Entry> entriesByLatinName = new HashMap<String, Entry>();
		Map<Integer, Entry> entriesById = new HashMap<Integer, Entry>();
		for (CompleteEntry e: entries) {
			entriesByLatinName.put(e.getLatinName(), e);
			entriesById.put(e.getId(), e);
		}
		
		logger.info("assignParentIdsForNullOrMissingId.entries." + entries.size());
		// hook them together
		int count = 0;
		for (CompleteEntry e: entries) {
			Entry p = entriesById.get(e.getParentId());
			if (p == null) {
//				logger.debug("missingParent." + e.getId());
				// see if we have a redirect-to
				String to = redirects.get(e.getParentLatinName()); // findRedirectTo(e.getParentLatinName());
				if (to != null) {
					p = entriesByLatinName.get(to);
					// not all redirect-to pages will be in the db 
					if (p != null) {
						logger.info("assignParentIdsForNullOrMissingId.fix." + (count++) + "." + e.getLatinName());
						template.update("update species set parent_id = ? where id = ?", p.getId(), e.getId());
					}
				}
			}
			// if we still don't have a parent, then set the id to null in the db
			if (p == null && e.getParentId() != null) {
				logger.info("assignParentIdsForNullOrMissingId.broken." + e.getLatinName());
				template.update("update species set parent_id = null where id = ?", e.getId());
			}
		}
		logger.info("<assignParentIdsForNullOrMissingId");
	}
	public String findRedirectTo(String from) {
		List<String> found = template.query(
				"select redirect_to from redirect where redirect_from = ?", 
				new EntityMapperRowMapper(), from);
		if (found.isEmpty()) {
			return null;
		} else {
			return found.get(0);
		}
	}
	public List<String> findRedirectFrom(String to) {
		List<String> found = template.query(
				"select redirect_from from redirect where redirect_to = ?", 
				new EntityMapperRowMapper(), to);
		return found;
	}
	public List<String> findAllRedirectFroms() {
		List<String> found = template.query(
				"select redirect_from from redirect", 
				new EntityMapperRowMapper());
		return found;
	}
	public Map<String, String> findAllRedirectFromsMap() {
		Map<String, String> map = new HashMap<String, String>();
		List<RedirectPair> found = template.query(
				"select redirect_to, redirect_from from redirect", 
				new RedirectPairMapper());
		for (RedirectPair pair: found) {
			map.put(pair.from, pair.to);
		}
		return map;
	}

	public Collection<CompleteEntry> findEntriesWithInvalidParent() {
		return template.query(
				"select id, latin_name, parent_latin_name from species where parent_id is null", 
				this);
	}
	public Collection<CompleteEntry> findEntriesForTreeReport() {
		return template.query(
				"select id, latin_name, parent_latin_name, parent_id, interesting_parent_id, common_name, image_link from species", 
				this);
	}
	public Collection<CompleteEntry> findEntriesForExtinctReport() {
		Collection<CompleteEntry> entries = template.query(
				"select latin_name, parent_latin_name from species where extinct = 0 and parent_latin_name is not null", 
				this);
		
		// filter out from extinct table - could do this as sql, but not really necessary
		List<String> extinctNamesList = template.query(
				"select latin_name from extinct_assigned where extinct = 1", 
				new EntityMapperRowMapper());
		Set<String> set = new HashSet<String>(extinctNamesList);
		Collection<CompleteEntry> filtered = new ArrayList<CompleteEntry>();
		for (CompleteEntry e: entries) {
			if (!set.contains(e.getLatinName())) {
				filtered.add(e);
			}
		}
		return filtered;
	}
	public void assignExtinct(String latinName) {
		int updated = template.update("update extinct_assigned set extinct = 1 where latin_name = ?", latinName);
		if (updated == 0) {
			template.update("insert into extinct_assigned (latin_name, extinct) values (?, 1)", latinName);
		}
	}
	public void assignExtinctToSpecies() {
		List<String> extinctNamesList = template.query(
				"select latin_name from extinct_assigned where extinct = 1", 
				new EntityMapperRowMapper());
		for (String name: extinctNamesList) {
			template.update("update species set extinct = 1 where latin_name = ?", name);
		}
	}
	public Collection<String> findAllUnmatchedParentNames() {
		List<String> unmatchedParents = template.query("select distinct(parent_latin_name) from species where (parent_id = 0 or parent_id is null)", 
				new EntityMapperRowMapper());
		
		Set<String> latinNames = new HashSet<String>();
		latinNames.addAll(unmatchedParents);
		
		Collection<String> allLatin = findAllLatinNames();
		latinNames.removeAll(allLatin);
		
		return latinNames;
	}
	public Collection<String> findAllLatinNames() {
		return template.query("select latin_name from species", 
				new EntityMapperRowMapper());
	}
	public Collection<String> findLatinNamesForParseDoneChangerToIgnore() {
		return template.query("select latin_name from species where rank > 0 and parent_latin_name is not null", 
				new EntityMapperRowMapper());
	}
	public Collection<CompleteEntry> findBoringEntries(boolean bothBoring) {
		String andOr;
		if (bothBoring) {
			andOr = "and";
		} else {
			andOr = "or";
		}
		return template.query(
				"select common_name, latin_name, image_link from species " +
				"where common_name is null " + andOr + " image_link is null", 
				this);
	}
	public Collection<CompleteEntry> findEntriesWithCommonNameAndNoImage() {
		return template.query(
				"select common_name, latin_name, image_link from species " +
				"where common_name is not null and image_link is null", 
				this);
	}
	public List<CompleteEntry> findChildren(Integer id) {
		List<CompleteEntry> children = new ArrayList<CompleteEntry>();
		Collection<Integer> ids = findChildrenIds(id);
		for (Integer cid: ids) {
			children.add(findEntry(cid));
		}
		return children;
	}
	public Collection<Integer> findChildrenIds(Integer id) {
		return getCache().getChildrenIds(id);
	}
	List<Integer> findChildrenIdsFromPersistence(Integer id) {
		return template.query(
				"select id from species where " + getParentIdColumn() + " = ? and " +
						getBoringColumn() + " = 0", 
				new ParameterizedSingleColumnRowMapper<Integer>(), id);
	}
	public Collection<CompleteEntry> getThumbnails() {
		String cols = "latin_name, image_link, id";
		return template.query("select " + cols + " from species where " +
				"not (image_link is null)", this);
	}
	
	public Set<Integer> findBestIds(String terms, Collection<Integer> existingIds) {
		String[] searches = terms.split(",");
		Set<String> names = new HashSet<String>();
		for (String one: searches) {
			one = StringUtils.trimToNull(one);
			if (one != null) {
				names.add(one);
			}
		}
		return findBestIds(names, existingIds);
	}
	
	public Set<Integer> findBestIds(Set<String> names, Collection<Integer> existingIds) {
		Set<Integer> ids = new HashSet<Integer>();
		for (String name: names) {
			int id = findBestId(name, existingIds);
			if (id > 0) {
				ids.add(id);
			}
		}
		return ids;
	}
	public int findBestId(String query, Collection<Integer> existingIds) {
		if (StringUtils.isNumeric(query)) {
			return Integer.parseInt(query);
		}
		return getSearcher().search(query, existingIds);
	}
	private EntrySearcher getSearcher() {
		if (searcher == null) {
//			searcher = new SqlSearcher(this);
			searcher = new LuceneSearcher(this);
		}
		return searcher;
	}
	public void setSearcher(EntrySearcher searcher) {
		this.searcher = searcher;
	}
	public Entry findTreeForNodes(Collection<Integer> ids, Entry existingRoot) {
		Set<Integer> set = new HashSet<Integer>(ids);
		if (existingRoot != null) {
			Map<Integer, CompleteEntry> map = EntryUtilities.toMap((CompleteEntry) existingRoot);
			// make sure the ids are all unique
			ids.removeAll(map.keySet());
			return findTreeForNodes(set, map);
		} else {
			return findTreeForNodes(set);
		}
	}
	public CompleteEntry findTreeForNodes(Set<Integer> ids) {
		return findTreeForNodes(ids, new HashMap<Integer, CompleteEntry>());
	}
	private CompleteEntry findTreeForNodes(Set<Integer> ids, Map<Integer, CompleteEntry> map) {
		
		Set<Integer> foundIds = new HashSet<Integer>(map.keySet());
		// get the list of all in the tree
		List<Entry> all = new ArrayList<Entry>();
		while (!ids.isEmpty()) {
			// this is a work-around while I'm assigning the parent ids
			ids.remove(0);
			ids.remove(null);
			
			foundIds.addAll(ids);
//			logger.debug("findTreeForNodes.while." + ids);
			
			// get all the entries matching the desired ids
			List<CompleteEntry> some = findEntries(ids);
			ids = new HashSet<Integer>();
			
			// iterate over the new entries - haven't found their parents yet
			for (CompleteEntry e: some) {
				// add each parent - if it's not already found
				if (!foundIds.contains(e.getParentId())) {
					ids.add(e.getParentId());
				}
				map.put(e.getId(), e);
				all.add(e);
			}
		}
		
		Tree top = EntryUtilities.buildTree(map);
		return top.getRoot();
	}
	public List<CompleteEntry> findEntries(Set<Integer> ids) {
		List<CompleteEntry> found = new ArrayList<CompleteEntry>();
		for (Integer id: ids) {
			CompleteEntry one = findEntry(id);
			found.add(one);
		}
		return found;
	}
	public CompleteEntry findEntry(Integer id) {
//		if (1 + 1 == 2) throw new RuntimeException("findEntry." + id);
		return getEntryFromCache(id);
	}
	public Entry findDepictedEntry(Entry entry) {
		Integer depictedId = entry.getDepictedId();
		Entry depictedEntry = null;
		if (depictedId != null) {
			depictedEntry = findEntry(depictedId);
			if (depictedEntry.isBoring()) {
				depictedEntry = null;
			}
		}
		return depictedEntry;
	}
	CompleteEntry findEntryFromPersistence(Integer id) {
		CompleteEntry entry = template.queryForObject("select " +
						getMinimalEntryColumns(false) + 
						", interesting_crunched_ids, shares_sibling_name, linked_image_id " +
						" from species where id = ?", this, id);
		cleanEntryFromPersistence(entry);
		return entry;
	}
	private void cleanEntryFromPersistence(CompleteEntry entry) {
		makeInteresting(entry);
		fixCommonName(entry);
		commonNameSplitter.assignCommonNames(entry);
	}
	private void fixCommonName(CompleteEntry entry) {
		String commonName = entry.getCommonName();
		// This should have been taken care of during load, but
		// there's some that snuck through, so we fix it now
		commonName = EntryUtilities.fixCommonName(commonName);
		entry.setCommonName(commonName);
	}
	public void clearCache() {
		cache.clear();
		searcher = null;
		// call one search to rebuild the indexes
		getSearcher().search("clear", new ArrayList<Integer>());
	}
	
	private Cache getCache() {
		return cache;
	}
	private CompleteEntry getEntryFromCache(Integer id) {
		EntryProperties p = getCache().getEntryProperties(id);
		return new CompleteEntry(p);
	}
	
	private void makeInteresting(CompleteEntry entry) {
		if (useInterestingAttributesForSearches) {
			entry.copyInterestingAttributes();
		}
	}
	public void updateParentLatinName(CompleteEntry entry) {
		template.update(
				"update species set " +
				" parent_latin_name = ?" +  
				" where id = ?", 
				entry.getParentLatinName(),
				entry.getId()
				);
	}
	public UpdateType updateOrInsertEntryMaybe(CompleteEntry entry) {
		CompleteEntry found = findEntryByLatinName(entry.getLatinName(), true);
		if (found != null) {
			// attempt to get updated information
			// the assumption is that any repeated parsing should get either
			// better results due to an improved parser,
			// or updated results due to the page updated
			boolean better = false;
			better = better || isBetter(found.getCommonName(), entry.getCommonName());
			better = better || isBetter(found.getParentLatinName(), entry.getParentLatinName());
			better = better || isBetter(found.getImageLink(), entry.getImageLink());
			if (entry.getRank() != Rank.Error) {
				better = better || isBetter(found.getRank().toString(), entry.getRank().toString());
			}
			better = better || isBetter(found.getDepictedLatinName(), entry.getDepictedLatinName());
			better = better || found.isExtinct() != entry.isExtinct();
			
			// just save these attributes - don't try and update parent, etc
			// that will have to get picked up later - after all, the boring attributes
			// would still need to be recalculated
			if (better) {
				template.update(
					"update species set " +
					" common_name = ?," +
					" parent_latin_name = ?," +  
					" depicted_latin_name = ?," +  
					" rank = ?," +
					" extinct = ?," +
					" image_link = ?" +
					" where id = ?", 
					entry.getCommonName(),
					entry.getParentLatinName(),
					entry.getDepictedLatinName(),
					entry.getRank().getRankIndex(),
					entry.isExtinct(),
					entry.getImageLink(),
					found.getId()
					);
				return UpdateType.Update;
			} else {
				return UpdateType.NoChange;
			}
		} else {
			insertEntry(entry);
			return UpdateType.Insert;
		}
	}
	
	private boolean isBetter(String old, String newer) {
		if (old == null) {
			return (newer != null);
		}
		if (newer == null) {
			// this case should hopefully be rare, but for example
			// if an image was deleted due to copyrights...
			// or the old common name was bad
			return true;
		}
		// as long as the newer one is different, we assume its better
		return !newer.equals(old);
	}
	
	private String getMinimalEntryColumns(boolean getParentLatinName) {
		String childCol;
		if (useInterestingAttributesForSearches) {
			childCol = "interesting_child_count";
		} else {
			childCol = "child_count";
		}
		String cols = "id, common_name, latin_name, extinct, image_link, rank, " +
			getParentIdColumn() + ", " + getBoringColumn() + ", " + childCol + ", depicted_id";
		if (getParentLatinName) {
			cols += ", parent_latin_name";
		}
		return cols;
	}
	
	public CompleteEntry findEntryByLatinName(String latinName) {
		return findEntryByLatinName(latinName, false);
	}
	public CompleteEntry findEntryByLatinName(String latinName, boolean getParentLatinName) {
		List<CompleteEntry> found = template.query(
				"select " +
				getMinimalEntryColumns(getParentLatinName) +
				" from species where latin_name = ?", this, latinName);
		if (found.isEmpty()) {
			return null;
		}
		if (found.size() == 1) {
			CompleteEntry entry = found.get(0);
			makeInteresting(entry);
			return entry;
		}
		throw new IncorrectResultSizeDataAccessException(latinName, 1, found.size());
	}
	public List<String> findChildNamesByParentLatinName(String parentLatinName) {
		return template.query(
				"select latin_name from species where parent_latin_name = ?",
				new EntityMapperRowMapper(), parentLatinName);
	}
	public List<CompleteEntry> findEntriesByParentLatinName(String parentLatinName) {
		return template.query(
				"select id, latin_name, parent_id from species where parent_latin_name = ?",
				this, parentLatinName);
	}
	
	public void fixBoringCommonNames() {
		logger.info(">fixBoringCommonNames");
		// this is technically the slow way to do it (could do with one query),
		// but I want to see the console output
		List<CompleteEntry> found = template.query(
				"select id from species where common_name = latin_name", this);
		for (CompleteEntry entry: found) {
			logger.debug("fixBoringCommonNames." + entry.getId());
			template.update(
					"update species set " +
					"common_name = null, " +
					"common_name_clean = null, " +
					"common_name_cleanest = null " +
					"where id = ?", 
					entry.getId());
		}
		logger.info("<fixBoringCommonNames");
	}
	public void updateLinkedImageIds() {
		logger.info(">updateLinkedImageIds");
		Tree tree = findInterestingTreeFromPersistence();
		List<CompleteEntry> entries = tree.getEntries();
		Map<Integer, Integer> subBranchLengths = new HashMap<Integer, Integer>();
		assignSubBranchLength(tree.getRoot(), subBranchLengths);
		Map<String, Integer> imageCounts = new HashMap<String, Integer>();
		assignImageCounts(entries, imageCounts);
		Map<Integer, Integer> treeDepths = new HashMap<Integer, Integer>();
		assignTreeDepths(tree.getRoot(), entries, treeDepths);
		assignLinkedImageIds(entries, subBranchLengths, treeDepths, imageCounts);
		logger.info("<updateLinkedImageIds");
	}
	/**
	 * Figures out how many times each image is used
	 */
	private void assignImageCounts(List<CompleteEntry> entries, Map<String, Integer> imageCounts) {
		for (CompleteEntry entry: entries) {
			String image = entry.getImageLink();
			if (image != null) {
				increaseImageCount(imageCounts, image);
			}
		}
	}
	private void increaseImageCount(Map<String, Integer> imageCounts, String image) {
		Integer count = imageCounts.get(image);
		if (count == null) {
			count = 0;
		} else {
			count = count + 1;
		}
		imageCounts.put(image, count);
	}
	/**
	 * Assigns each entry a distance to the leaf.  For example, a leaf = 0, and an entry with only leafs = 1
	 */
	private int assignSubBranchLength(CompleteEntry entry, Map<Integer, Integer> subBranchLengths) {
		if (!entry.hasChildren()) {
			subBranchLengths.put(entry.getId(), 0);
			return 0;
		}
		int longestChild = 0;
		for (CompleteEntry child: entry.getCompleteEntryChildren()) {
			Integer childLength = subBranchLengths.get(child.getId());
			if (childLength == null) {
				childLength = assignSubBranchLength(child, subBranchLengths);
			}
			if (childLength > longestChild) {
				longestChild = childLength	;
			}
		}
		int len = longestChild + 1;
		subBranchLengths.put(entry.getId(), len);
		return len;
	}
	/**
	 * Assigns each entry its depth in the tree - the distance from the true root element of the tree
	 */
	private void assignTreeDepths(CompleteEntry treeRoot, List<CompleteEntry> entries, Map<Integer, Integer> treeDepths) {
		for (CompleteEntry entry: entries) {
			int depth = getChildDepth(treeRoot, entry);
			treeDepths.put(entry.getId(), depth);
		}
	}
	private int getChildDepth(CompleteEntry treeRoot, CompleteEntry child) {
		int depth = 0;
		// TODO the null check is here because of data problems from wikispecies parsing
		while (child != treeRoot && child != null) {
			depth++;
			child = child.getParent();
		}
		return depth;
	}
	private void assignLinkedImageIds(List<CompleteEntry> entries, 
			Map<Integer, Integer> subBranchLengths, Map<Integer, Integer> treeDepths, Map<String, Integer> imageCounts) {
		List<Object[]> batchUpdates = new ArrayList<Object[]>();
		int maxLength = 100; // arbitrary number, but this should cover it, and won't really add much time
		for (int i = 1; i < maxLength; i++) {
			for (CompleteEntry entry: entries) {
				if (entry.hasChildren() && entry.getImageLink() == null) {
					Integer subBranchLength = subBranchLengths.get(entry.getId());
					// TODO null check here due to orphan branches
					if (subBranchLength != null && subBranchLength.intValue() == i) {
						CompleteEntry linkedEntry = 
							getLinkedImageEntry(entry, entry, null, treeDepths, imageCounts);
						if (linkedEntry != null) {
//							logger.debug("updateLinkedImageIds." + entry.getId() + "->" + linkedEntry.getId());
							increaseImageCount(imageCounts, linkedEntry.getImageLink());
							batchUpdates.add(new Object[] {linkedEntry.getId(), entry.getId()});
						}
					}
				}
			}
		}
		template.batchUpdate("update species set linked_image_id = ? where id = ?", batchUpdates);
	}
	/**
	 * depth-first search of all children looking for the child with the least-used image
	 * 
	 * Choose an image that fits these criteria
	 * - an image in a descendant
	 * - an image used the least amount as possible - means we check all the way up the chain for that image
	 * - an image pretty far down the tree - so we don't notice the duplicate right away
	 * 
	 * With these criteria, it means we might have to get creative because a simple algorithm might miss
	 * some opportunities to get as many parents filled in as possible.  The following points will help
	 * - first pass fill in all entries with descendants of just one depth
	 * - second pass, go to two deep - and keep repeating
	 */
	private CompleteEntry getLinkedImageEntry(CompleteEntry rootEntry, CompleteEntry checkEntry, 
			CompleteEntry currentBest, Map<Integer, Integer> treeDepths, Map<String, Integer> imageCounts) {
		// first check the leaf conditions
		if (!checkEntry.hasChildren()) {
			if (checkEntry == rootEntry) {
				return null;
			}
			if (checkEntry.getImageLink() == null) {
				return null;
			}
			if (currentBest == null) {
				return checkEntry;
			}
			// now we check if this entry "scores" better
			// - the lower image count is better
			// - if the image count is the same, the longer distance from the root is better
			int bestImageCount = imageCounts.get(currentBest.getImageLink());
			int checkImageCount = imageCounts.get(checkEntry.getImageLink());
			if (checkImageCount < bestImageCount) {
				return checkEntry;
			} else if (checkImageCount > bestImageCount) {
				return null;
			}
			int rootEntryDepth = treeDepths.get(rootEntry.getId());
			int bestDepth = treeDepths.get(currentBest.getId()) - rootEntryDepth;
			int checkDepth = treeDepths.get(checkEntry.getId()) - rootEntryDepth;
			if (checkDepth > bestDepth) {
				return checkEntry;
			} else {
				return null;
			}
		}
		for (CompleteEntry child: checkEntry.getCompleteEntryChildren()) {
			CompleteEntry found = getLinkedImageEntry(rootEntry, child, currentBest, treeDepths, imageCounts);
			if (found != null) {
				currentBest = found;
			}
		}
		return currentBest;
	}
	public void recreateCleanNames() {
		int count = 0;
		int showEvery = 10000;
		logger.info(">recreateCleanNames");
		List<CompleteEntry> found = template.query("select id, latin_name, common_name from species", this);
		for (CompleteEntry entry: found) {
			EntryUtilities.cleanEntry(entry);
			if (count++ % showEvery == 0) {
				logger.debug(">recreateCleanNames(" + count + ")." + entry.getId() + "." + entry.getLatinName());
			}
			template.update(
					"update species set " +
					" common_name_clean = ?," +
					" common_name_cleanest = ?," +
					" latin_name_clean = ?," +  
					" latin_name_cleanest = ?" +  
					" where id = ?", 
					entry.getCommonNameClean(),
					entry.getCommonNameCleanest(),
					entry.getLatinNameClean(),
					entry.getLatinNameCleanest(),
					entry.getId()
					);
		}
		logger.info("<recreateCleanNames");
	}
	public void fixParents() {
		logger.info(">fixParents");
		// start by fixing all parent ids based on latin name
		assignParentIdsByParentLatinName();
		// double check for any missing ids, or any redirect ids
		assignParentIdsForNullOrMissingId();
		// redirects might cause this
		fixSelfParentIds();
		logger.info("<fixParents");
	}
	
	private void assignParentIdsByParentLatinName() {
		int showEvery = 10000;
		logger.info(">assignParentIdsByParentLatinName");
		List<CompleteEntry> found = template.query("select id, parent_id, latin_name, parent_latin_name, depicted_latin_name from species", this);
		logger.info(">assignParentIdsByParentLatinName." + found.size());

		// build some helpful maps
		Set<String> depictedSet = new HashSet<String>();
		Map<String, Entry> entriesLatinNameMap = new HashMap<String, Entry>();
		for (CompleteEntry one: found) {
			if (one.getDepictedLatinName() != null) {
				depictedSet.add(one.getDepictedLatinName());
			}
			entriesLatinNameMap.put(one.getLatinName(), one);
		}
		// assign a new parent wherever it isn't already correct
		for (CompleteEntry one: found) {
			Entry parent = entriesLatinNameMap.get(one.getParentLatinName());
			if (parent != null && !parent.getId().equals(one.getParentId())) {
				template.update(
						"update species set parent_id = ? where id = ?", 
						parent.getId(), one.getId());
				logger.debug(">assignParentIdsByParentLatinName.update." + one.getId() + "." + one.getLatinName() + " > " + parent.getId());
			}
		}
		
		int count = 0;
		for (CompleteEntry one: found) {
			count++;
			if (count % showEvery == 0) {
				logger.debug(">assignParentIdsByParentLatinName.depicted(" + count + ")." + one.getId() + "." + one.getLatinName());
			}
			if (depictedSet.contains(one.getLatinName())) {
				template.update(
						"update species set depicted_id = ? where depicted_latin_name = ?", 
						one.getId(), one.getLatinName());
			}
		}
		logger.info("<assignParentIdsByParentLatinName." + found.size());
	}
	public void insertEntry(CompleteEntry entry) {
		EntryUtilities.cleanEntry(entry);
		template.update(
			"insert into species (" +
			"  id, " + 
			"  latin_name, " +
			"  latin_name_clean, " +  
			"  latin_name_cleanest , " +
			"  common_name , " +
			"  common_name_clean , " +
			"  common_name_cleanest , " +
//			"  parent_id , " +
			"  parent_latin_name, " +  
			"  depicted_latin_name, " +  
			"  rank , " +
			"  extinct , " +
			"  image_link) " +
			"values (?,?,?,?,?,?,?,?,?,?,?,?)", 
			entry.getId(),
			entry.getLatinName(),
			entry.getLatinNameClean(),
			entry.getLatinNameCleanest(),
			entry.getCommonName(),
			entry.getCommonNameClean(),
			entry.getCommonNameCleanest(),
//			entry.getParent(),
			entry.getParentLatinName(),
			entry.getDepictedLatinName(),
			entry.getRank().getRankIndex(),
			entry.isExtinct(),
			entry.getImageLink()
			);
	}
	
	public void setDataSource(DataSource dataSource) {
		this.template = new EntityMapperJdbcTemplate(dataSource);
	}
	public CompleteEntry mapRow(ResultSet rs, int row) throws SQLException {
		CompleteEntry entry = new CompleteEntry();
		ResultSetMetaData md = rs.getMetaData();
		int count = md.getColumnCount();
		for (int i = 1; i <= count; i++) {
			String c = md.getColumnName(i);
			if (c.equals("id")) {
				entry.setId(rs.getInt(c));
			} else if (c.equals("latin_name")) {
				entry.setLatinName(getString(rs, c));
			} else if (c.equals("latin_name_clean")) {
				entry.setLatinNameClean(getString(rs, c));  
			} else if (c.equals("latin_name_cleanest")) {
				entry.setLatinNameCleanest(getString(rs, c));
			} else if (c.equals("common_name")) {
				entry.setCommonName(getString(rs, c));
			} else if (c.equals("common_name_clean")) {
				entry.setCommonNameClean(getString(rs, c));
			} else if (c.equals("common_name_cleanest")) {
				entry.setCommonNameCleanest(getString(rs, c));
			} else if (c.equals("parent_id")) {
				entry.setParentId(rs.getInt(c));
				if (rs.wasNull()) {
					entry.setParentId(null);
				}
			} else if (c.equals("interesting_parent_id")) {
				entry.setInterestingParentId(rs.getInt(c));
				if (rs.wasNull()) {
					entry.setInterestingParentId(null);
				}
			} else if (c.equals("depicted_id")) {
				entry.setDepictedId(rs.getInt(c));
				if (rs.wasNull()) {
					entry.setDepictedId(null);
				}
			} else if (c.equals("parent_latin_name")) {
				entry.setParentLatinName(getString(rs, c));  
			} else if (c.equals("depicted_latin_name")) {
				entry.setDepictedLatinName(getString(rs, c));  
			} else if (c.equals("rank")) {
				entry.setRank(Rank.valueOf(rs.getInt(c)));
			} else if (c.equals("extinct")) {
				entry.setExtinct(rs.getBoolean(c));
			} else if (c.equals("boring")) {
				entry.setBoring(rs.getBoolean(c));
			} else if (c.equals("boring_final")) {
				// same property - reused
				entry.setBoring(rs.getBoolean(c));
			} else if (c.equals("shares_sibling_name")) {
				entry.setCommonNameSharedWithSiblings(rs.getBoolean(c));
			} else if (c.equals("image_link")) {
				String s = rs.getString(c);
				if (s != null) {
					entry.setImageLink(s);
				}
			} else if (c.equals("child_count")) {
				entry.setPersistedChildCount(rs.getInt(c));
			} else if (c.equals("interesting_child_count")) {
				entry.setInterestingChildCount(rs.getInt(c));
			} else if (c.equals("interesting_crunched_ids")) {
				String sids = rs.getString(c);
				if (sids != null) {
					CrunchedIds ids = new CrunchedIds(sids, EntryUtilities.CRUNCHER);
					entry.setInterestingCrunchedIds(ids);
				}
			} else if (c.equals("linked_image_id")) {
				entry.setLinkedImageId(rs.getInt(c));
			}
		}
		return entry;
	}
	public SimpleJdbcTemplate getTemplate() {
		return template;
	}
	public void setCache(Cache cache) {
		this.cache = cache;
	}
	
	private class RedirectPair {
		String from;
		String to;
	}
	private class RedirectPairMapper implements ParameterizedRowMapper<RedirectPair> {
		@Override
		public RedirectPair mapRow(ResultSet rs, int rowNum) throws SQLException {
			RedirectPair p = new RedirectPair();
			p.from = rs.getString("redirect_from");
			p.to = rs.getString("redirect_to");
			return p;
		}
	}
	
}
