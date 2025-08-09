package com.robestone.banyan.wikispecies;

import static com.robestone.banyan.util.EntityMapperJdbcTemplate.getString;

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
import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.robestone.banyan.taxons.AnalyzableTreeNode;
import com.robestone.banyan.taxons.CrunchedIds;
import com.robestone.banyan.taxons.NameInfo;
import com.robestone.banyan.taxons.Rank;
import com.robestone.banyan.taxons.TaxonService;
import com.robestone.banyan.taxons.Tree;
import com.robestone.banyan.util.EntityMapperJdbcTemplate;
import com.robestone.banyan.util.EntityMapperRowMapper;
import com.robestone.banyan.util.ParseUtilities;

public class WikiSpeciesService implements ParameterizedRowMapper<Entry> {

	private Logger logger = Logger.getLogger(WikiSpeciesService.class);
	
	public static final Entry TREE_OF_LIFE_ENTRY = new Entry(Rank.Cladus, "Tree of Life", "Arbor vitae"); {
		TREE_OF_LIFE_ENTRY.setId(TaxonService.TREE_OF_LIFE_ID);
	}
	
	private SimpleJdbcTemplate template;

	private Cache cache;
	
	/**
	 * @return a random tree of given size.
	 */
	public AnalyzableTreeNode findRandomTree(int treeSize) {
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
	List<Integer> findAllIdsForCaching() {
		List<Integer> ids = template.query(
				"select id from species where boring_final = false", 
				new ParameterizedSingleColumnRowMapper<Integer>());
		return ids;
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
	public Tree<Entry> findCompleteTreeFromPersistence() {
		Collection<Entry> entries = template.query(
				"select id, parent_id, common_name, latin_name, image_link from species", this);
		return EntryUtilities.buildTree(entries);
	}
	public void updateCommonNamesSharedWithSiblingsFalse() {
		template.update("update species set shares_sibling_name = false");
	}
	public void updateCommonNameSharedWithSiblings(Entry entry) {
		template.update("update species set shares_sibling_name = ? where id = ?", 
				entry.isCommonNameSharedWithSiblings(), entry.getId());
	}
	public List<Integer> findIdsForParentIds(Collection<Integer> parentIds) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", parentIds);		
		String sql = "select id from species where parent_id in (:ids)";
		
		return template.query(
				sql, 
				new ParameterizedSingleColumnRowMapper<Integer>(), parameters);
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
	public void deleteRedirect(String from) {
		logger.info("deleteRedirect." + from);
		template.update("delete from redirect where redirect_from = ?", from);
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

	public Collection<Entry> findEntriesWithInvalidParent() {
		return template.query(
				"select id, latin_name, parent_latin_name from species where parent_id is null", 
				this);
	}
	public Collection<Entry> findEntriesForTreeReport() {
		return template.query(
				"select id, latin_name, parent_latin_name, parent_id, interesting_parent_id, common_name, image_link from species", 
				this);
	}
	public Collection<Entry> findEntriesWithBasicParentInfo() {
		return template.query(
				"select id, latin_name, parent_latin_name, parent_id from species", 
				this);
	}
	public Collection<Entry> findEntriesForExtinctReport() {
		Collection<Entry> entries = template.query(
				"select latin_name, parent_latin_name from species where extinct = false and parent_latin_name is not null", 
				this);
		
		// filter out from extinct table - could do this as sql, but not really necessary
		List<String> extinctNamesList = template.query(
				"select latin_name from extinct_assigned where extinct = true", 
				new EntityMapperRowMapper());
		Set<String> set = new HashSet<String>(extinctNamesList);
		Collection<Entry> filtered = new ArrayList<Entry>();
		for (Entry e: entries) {
			if (!set.contains(e.getLatinName())) {
				filtered.add(e);
			}
		}
		return filtered;
	}
	public void assignExtinct(String latinName) {
		int updated = template.update("update extinct_assigned set extinct = true where latin_name = ?", latinName);
		if (updated == 0) {
			template.update("insert into extinct_assigned (latin_name, extinct) values (?, true)", latinName);
		}
	}
	private boolean isExtinctAssignedReady = false;
	public void assignExtinctToSpecies() {
		if (!isExtinctAssignedReady) {
			return;
		}
		List<String> extinctNamesList = template.query(
				"select latin_name from extinct_assigned where extinct = true", 
				new EntityMapperRowMapper());
		for (String name: extinctNamesList) {
			template.update("update species set extinct = true where latin_name = ?", name);
		}
	}
	public Collection<String> findAllLatinNames() {
		return template.query("select latin_name from species", 
				new EntityMapperRowMapper());
	}
	public Collection<String> findLatinNamesForParseDoneChangerToIgnore() {
		return template.query("select latin_name from species where rank > 0 and parent_latin_name is not null", 
				new EntityMapperRowMapper());
	}
	public Set<String> findLatinNamesInTree(String rootLatinName, int distance, boolean downstreamOnly) {
		Set<String> set = new HashSet<>();
		set.add(rootLatinName);
		findLatinNamesInTree(rootLatinName, distance, downstreamOnly, set);
		return set;
	}
	private void findLatinNamesInTree(String rootLatinName, int distance, boolean downstreamOnly, Set<String> found) {
		
		// find the root id
		Entry entry = findEntryByLatinName(rootLatinName);
		
		// get each adjoining node - up and down
		List<Integer> childIds = findChildrenIdsFromPersistence(entry.getId());
		List<Entry> nodes = new ArrayList<>();
		for (Integer childId: childIds) {
			Entry child = findEntry(childId);
			nodes.add(child);
		}

		if (!downstreamOnly) {
			Entry parent = findEntry(entry.getParentId());
			nodes.add(parent);
		}
		
		distance--;
		for (Entry node: nodes) {
			
			boolean added = found.add(node.getLatinName());
			
			// recurse, checking the distance
			if (distance > 0 && added) {
				findLatinNamesInTree(node.getLatinName(), distance, downstreamOnly, found);
			}
		}
		
	}
	
	public Collection<Entry> findEntriesWithCommonNameAndNoImage() {
		return template.query(
				"select common_name, latin_name, image_link from species " +
				"where common_name is not null and image_link is null", 
				this);
	}
	public List<Entry> findChildren(Integer id) {
		List<Entry> children = new ArrayList<Entry>();
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
				"select id from species where parent_id = ?", 
				new ParameterizedSingleColumnRowMapper<Integer>(), id);
	}
	public void udpateBlacklistedImages(String[] blacklist) {
		for (String link: blacklist) {
			Collection<Entry> found = template.query("select id from species where image_link like '%" + link + "%'", this);
			System.out.println("udpateBlacklistedImages." + link + " > found." + found.size());
			for (Entry entry: found) {
				template.update("update species set image_link = null where id = ?", entry.getId());
			}
		}
	}

	public AnalyzableTreeNode findTreeForNodes(Collection<Integer> ids, AnalyzableTreeNode existingRoot) {
		Set<Integer> set = new HashSet<Integer>(ids);
		if (existingRoot != null) {
			Map<Integer, Entry> map = EntryUtilities.toMap((Entry) existingRoot);
			// make sure the ids are all unique
			ids.removeAll(map.keySet());
			return findTreeForNodes(set, map);
		} else {
			return findTreeForNodes(set);
		}
	}
	public Entry findTreeForNodes(Set<Integer> ids) {
		return findTreeForNodes(ids, new HashMap<Integer, Entry>());
	}
	private Entry findTreeForNodes(Set<Integer> ids, Map<Integer, Entry> map) {
		
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
			List<Entry> some = findEntries(ids);
			ids = new HashSet<Integer>();
			
			// iterate over the new entries - haven't found their parents yet
			for (Entry e: some) {
				if (e.getParentId() == null && e.getId().intValue() != TaxonService.TREE_OF_LIFE_ID) {
					// something wrong here... need to fix this or understand why it's null sometimes right now
					logger.info(">findTreeForNodes." + e.getId() + ".parentId=null");
				}
				// add each parent - if it's not already found
				if (!foundIds.contains(e.getParentId())) {
					ids.add(e.getParentId());
				}
				map.put(e.getId(), e);
				all.add(e);
			}
		}
		
		Tree<Entry> top = EntryUtilities.buildTree(map);
		return top.getRoot();
	}
	public List<Entry> findEntries(Set<Integer> ids) {
		List<Entry> found = new ArrayList<Entry>();
		for (Integer id: ids) {
			Entry one = findEntry(id);
			found.add(one);
		}
		return found;
	}
	public Entry findEntry(Integer id) {
		return getEntryFromCache(id);
	}
	Entry findEntryFromPersistence(Integer id) {
		try {
			Entry entry = template.queryForObject("select " +
							getMinimalEntryColumns(false) + 
							", interesting_crunched_ids, shares_sibling_name, linked_image_id " +
							" from species where id = ?", this, id);
			cleanEntryFromPersistence(entry);
			return entry;
		} catch (Exception e) {
			logger.error("Could not find: " + id);
			return null;
		}
	}
	private void cleanEntryFromPersistence(Entry entry) {
		fixCommonName(entry);
		NameInfo name = new NameInfo(entry.getCommonName(), entry.getLatinName());
		entry.setCommonName(name.getCommonNameFixed());
		entry.setCommonNames(name.getCommonNames());
	}
	private void fixCommonName(Entry entry) {
		String commonName = entry.getCommonName();
		// This should have been taken care of during load, but
		// there's some that snuck through, so we fix it now
		commonName = ParseUtilities.fixCommonName(commonName);
		entry.setCommonName(commonName);
	}
	public void clearCache() {
		cache.clear();
	}
	
	private Cache getCache() {
		return cache;
	}
	private Entry getEntryFromCache(Integer id) {
		EntryProperties p = getCache().getEntryProperties(id);
		if (p == null) {
			return null;
		} else {
			return new Entry(p);
		}
	}
	
	public void updateParent(Entry entry) {
		template.update(
				"update species set " +
						" parent_latin_name = ?," +  
						" parent_id = ?," +  
						" interesting_parent_id = ?" +
						" where id = ?", 
				entry.getParentLatinName(),
				entry.getParentId(),
				entry.getInterestingParentId(),
				entry.getId()
				);
	}
	public void updateParentId(Entry entry) {
		template.update(
				"update species set parent_id = ? where id = ?", 
				entry.getParentId(),
				entry.getId()
				);
	}
	public void updateParentIdToNull(Entry entry) {
		template.update(
				"update species set " +
						" parent_id = null" +  
						" where id = ?", 
				entry.getId()
				);
	}
	public UpdateType insertEntryMaybe(Entry entry) {
		AnalyzableTreeNode found = findEntryByLatinName(entry.getLatinName(), true);
		if (found != null) {
			return UpdateType.NoChange;
		} else {
			insertEntry(entry);
			return UpdateType.Insert;
		}
	}
	public UpdateType updateOrInsertEntryMaybe(Entry entry) {
		Entry found = findEntryByLatinName(entry.getLatinName(), true);
		if (found != null) {
			// attempt to get updated information
			// the assumption is that any repeated parsing should get either
			// better results due to an improved parser,
			// or updated results due to the page updated
			boolean better = false;
			better = better || isBetter(found.getCommonName(), entry.getCommonName());
			better = better || isBetter(found.getParentLatinName(), entry.getParentLatinName());
			better = better || isBetter(found.getImageLink(), entry.getImageLink());
			better = better || isBetter(found.getRank().toString(), entry.getRank().toString());
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
		String cols = "id, common_name, latin_name, extinct, image_link, rank, parent_id, depicted_id";
		if (getParentLatinName) {
			cols += ", parent_latin_name";
		}
		return cols;
	}
	
	public Entry findEntryByLatinName(String latinName) {
		return findEntryByLatinName(latinName, false);
	}
	public Entry findEntryByLatinName(String latinName, boolean getParentLatinName) {
		List<Entry> found = template.query(
				"select " +
				getMinimalEntryColumns(getParentLatinName) +
				" from species where latin_name = ?", this, latinName);
		if (found.isEmpty()) {
			return null;
		}
		if (found.size() == 1) {
			Entry entry = found.get(0);
			return entry;
		}
		throw new IncorrectResultSizeDataAccessException(latinName, 1, found.size());
	}
	public AnalyzableTreeNode findEntryById(Integer id, boolean getParentLatinName) {
		List<Entry> found = template.query(
				"select " +
				getMinimalEntryColumns(getParentLatinName) +
				" from species where id = ?", this, id);
		if (found.isEmpty()) {
			return null;
		}
		if (found.size() == 1) {
			AnalyzableTreeNode entry = found.get(0);
			return entry;
		}
		throw new IncorrectResultSizeDataAccessException("" + id, 1, found.size());
	}
	public List<String> findChildNamesByParentLatinName(String parentLatinName) {
		return template.query(
				"select latin_name from species where parent_latin_name = ?",
				new EntityMapperRowMapper(), parentLatinName);
	}
	public List<Entry> findEntriesByParentLatinName(String parentLatinName) {
		return template.query(
				"select id, latin_name, parent_id from species where parent_latin_name = ?",
				this, parentLatinName);
	}
	
	public void insertEntry(Entry entry) {
		EntryUtilities.cleanEntry(entry);
		template.update(
			"insert into species (" +
//			"  id, " + // derby doesn't want me to specify this, but mysql did
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
			"values (?,?,?,?,?,?,?,?,?,?,?)", 
//			entry.getId(),
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
	public Entry mapRow(ResultSet rs, int row) throws SQLException {
		Entry entry = new Entry();
		ResultSetMetaData md = rs.getMetaData();
		int count = md.getColumnCount();
		for (int i = 1; i <= count; i++) {
			String c = md.getColumnName(i);
			if (c.equalsIgnoreCase("id")) {
				entry.setId(rs.getInt(c));
			} else if (c.equalsIgnoreCase("latin_name")) {
				entry.setLatinName(getString(rs, c));
			} else if (c.equalsIgnoreCase("latin_name_clean")) {
				entry.setLatinNameClean(getString(rs, c));  
			} else if (c.equalsIgnoreCase("latin_name_cleanest")) {
				entry.setLatinNameCleanest(getString(rs, c));
			} else if (c.equalsIgnoreCase("common_name")) {
				entry.setCommonName(getString(rs, c));
			} else if (c.equalsIgnoreCase("common_name_clean")) {
				entry.setCommonNameClean(getString(rs, c));
			} else if (c.equalsIgnoreCase("common_name_cleanest")) {
				entry.setCommonNameCleanest(getString(rs, c));
			} else if (c.equalsIgnoreCase("parent_id")) {
				entry.setParentId(rs.getInt(c));
				if (rs.wasNull()) {
					entry.setParentId(null);
				}
			} else if (c.equalsIgnoreCase("interesting_parent_id")) {
				entry.setInterestingParentId(rs.getInt(c));
				if (rs.wasNull()) {
					entry.setInterestingParentId(null);
				}
			} else if (c.equalsIgnoreCase("depicted_id")) {
				entry.setDepictedId(rs.getInt(c));
				if (rs.wasNull()) {
					entry.setDepictedId(null);
				}
			} else if (c.equalsIgnoreCase("parent_latin_name")) {
				entry.setParentLatinName(getString(rs, c));  
			} else if (c.equalsIgnoreCase("depicted_latin_name")) {
				entry.setDepictedLatinName(getString(rs, c));  
			} else if (c.equalsIgnoreCase("rank")) {
				entry.setRank(Rank.valueOf(rs.getInt(c)));
			} else if (c.equalsIgnoreCase("extinct")) {
				entry.setExtinct(rs.getBoolean(c));
			} else if (c.equalsIgnoreCase("boring")) {
				entry.setBoring(rs.getBoolean(c));
			} else if (c.equalsIgnoreCase("boring_final")) {
				// same property - reused
				entry.setBoring(rs.getBoolean(c));
			} else if (c.equalsIgnoreCase("shares_sibling_name")) {
				entry.setCommonNameSharedWithSiblings(rs.getBoolean(c));
			} else if (c.equalsIgnoreCase("image_link")) {
				String s = getString(rs, c);
				if (s != null) {
					entry.setImageLink(s);
				}
			} else if (c.equalsIgnoreCase("child_count")) {
				entry.setPersistedChildCount(rs.getInt(c));
			} else if (c.equalsIgnoreCase("interesting_child_count")) {
				entry.setInterestingChildCount(rs.getInt(c));
			} else if (c.equalsIgnoreCase("interesting_crunched_ids")) {
				String sids = getString(rs, c);
				if (sids != null) {
					CrunchedIds ids = EntryUtilities.CRUNCHER.parse(sids);
					entry.setInterestingCrunchedIds(ids);
				}
			} else if (c.equalsIgnoreCase("linked_image_id")) {
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
			p.from = getString(rs, "redirect_from");
			p.to = getString(rs, "redirect_to");
			return p;
		}
	}
	
}
