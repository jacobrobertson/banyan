package com.robestone.banyan.taxons;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class TaxonService {

	private Logger logger = Logger.getLogger(TaxonService.class);
	
	// If I always insert this first in a new DB, it will be 1
	public static final Integer TREE_OF_LIFE_ID = 1;

	private SimpleJdbcTemplate template;
	
	public void setDataSource(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}

	public void insertTaxon(Taxon t) {
		template.update(
				"insert into common_taxons " +
				"(latin_name, rank, extinct, common_name, image_file_path, parent_taxon_id) " +
				"values " +
				"(?, ?, ?, ?, ?, ?)", 
				t.getLatinName(), t.getRank().name(), t.isExtinct(), t.getCommonName(), t.getImage().getFilePath(), t.getParentTaxonId());
	}
	
	/**
	 * TODO if I am updating the image, then I need to update the measurements too
	 * @param t
	 */
	public void updateTaxonWithMergedInfo(Taxon t) {
		template.update(
				"update common_taxons " +
				"set parent_taxon_id = ?, extinct = ?, common_name = ?, rank = ?, image_file_path = ? " +
				"where taxon_id = ?", 
				t.getParentTaxonId(), t.isExtinct(), t.getCommonName(), t.getRank().name(), t.getImage().getFilePath(),
				t.getTaxonId());
	}
	public void updateInterestingCrunchedIds(Taxon entry) {
//		logger.debug("updateInterestingCrunchedIds." + entry.getId());
		String ids = null;
		if (entry.getInterestingCrunchedIds() != null) {
			ids = entry.getInterestingCrunchedIds().getCrunchedIds();
		}
		template.update(
				"update species set interesting_crunched_ids = ? where taxon_id = ?", 
				ids, entry.getTaxonId());
	}
	public void updateCommonName(Taxon e) {
		template.update("update species set common_name = ? where taxon_id = ?", e.getCommonName(), e.getTaxonId());
	}
	public Collection<TaxonNode> findAllImageFilePaths() {
		return template.query("select latin_name, image_file_path, taxon_id from common_taxons where " +
				"not (image_file_path is null)", TaxonMapper);
	}

	public List<Integer> findAllTaxonIds() {
		List<Integer> found = template.query(
				"select taxon_id from wd_taxons", new ParameterizedSingleColumnRowMapper<Integer>());
		return found;
	}
	
	public Integer findTaxonIdByLatinName(String latinName) {
		List<Integer> found = template.query(
				"select taxon_id from common_taxons where latin_name = ?", new ParameterizedSingleColumnRowMapper<Integer>(), latinName);
		if (found.isEmpty()) {
			return null;
		} else {
			return found.get(0);
		}
	}

	public TaxonNode findTaxonById(Integer taxonId) {
		List<TaxonNode> found = template.query(
				"select taxon_id, latin_name, parent_taxon_id, rank, extinct, common_name, image_file_path from common_taxons where taxon_id = ?", 
				TaxonMapper, taxonId);
		if (found.isEmpty()) {
			return null;
		} else {
			return found.get(0);
		}
	}
	
	public Taxon findTaxonByLatinName(String latinName) {
		List<TaxonNode> found = template.query(
				"select taxon_id, latin_name, parent_taxon_id, rank, extinct, common_name, image_file_path from common_taxons where latin_name = ?", 
				TaxonMapper, latinName);
		if (found.isEmpty()) {
			return null;
		} else {
			return found.get(0);
		}
	}
	public Taxon findEntryByLatinName(String latinName) {
		List<TaxonNode> found = template.query(
				"select " +
		// TODO
//				getMinimalEntryColumns(getParentLatinName) +
				" from species where latin_name = ?", TaxonMapper, latinName);
		if (found.isEmpty()) {
			return null;
		}
		if (found.size() == 1) {
			Taxon entry = found.get(0);
			return entry;
		}
		throw new IncorrectResultSizeDataAccessException(latinName, 1, found.size());
	}

	public List<TaxonNode> findChildren(Integer id) {
		return template.query(
				// TODO columns
				"select * from species where "  
//		getParentIdColumn() + " = ? and " +
//						getBoringColumn() 
				+ " = false", 
				TaxonMapper, id);
	}
	public List<Integer> findChildrenIdsNonBoring(Integer id) {
		return template.query(
				"select id from common_taxons where parent_taxon_id = ? and " +
		// TODO columns
//						getBoringColumn() + 
						" = false", 
				new ParameterizedSingleColumnRowMapper<Integer>(), id);
	}
	
	public List<TaxonNode> findTaxonsForIds(Set<Integer> ids) {
		List<TaxonNode> found = new ArrayList<>();
		for (Integer id: ids) {
			TaxonNode one = findTaxonById(id);
			found.add(one);
		}
		return found;
	}

	public Tree<TaxonNode> findTreeForTaxonIds(Set<Integer> ids) {
		return findTree(ids, new HashMap<Integer, TaxonNode>());
	}
	private Tree<TaxonNode> findTree(Set<Integer> ids, Map<Integer, TaxonNode> map) {
		
		Set<Integer> foundIds = new HashSet<Integer>(map.keySet());
		// get the list of all in the tree
		List<TaxonNode> all = new ArrayList<>();
		while (!ids.isEmpty()) {
			// this is a work-around while I'm assigning the parent ids
			ids.remove(0);
			ids.remove(null);
			
			foundIds.addAll(ids);
//			logger.debug("findTreeForNodes.while." + ids);
			
			// get all the entries matching the desired ids
			List<TaxonNode> some = findTaxonsForIds(ids);
			ids = new HashSet<Integer>();
			
			// iterate over the new entries - haven't found their parents yet
			for (TaxonNode e: some) {
				if (e.getParentId() == null && e.getId().intValue() != TREE_OF_LIFE_ID) {
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
		
		Tree<TaxonNode> top = TreeNodeUtilities.buildTree(map);
		return top;
	}

	public void fixBoringCommonNames() {
		logger.info("> fixBoringCommonNames");
		// this is technically the slow way to do it (could do with one query),
		// but I want to see the console output
		List<TaxonNode> found = template.query(
				"select id from species where common_name = latin_name", TaxonMapper);
		for (Taxon entry: found) {
//			logger.debug("fixBoringCommonNames." + entry.getId());
			template.update(
					"update species set " +
					"common_name = null, " +
					"common_name_clean = null, " +
					"common_name_cleanest = null " +
					"where id = ?", 
					entry.getTaxonId());
		}
		logger.info("< fixBoringCommonNames");
	}
	public void updateFromBoringWork(Collection<TaxonNode> interesting, Collection<TaxonNode> boring) {
		updateFromBoringWorkMarkInteresting(interesting);
		updateFromBoringWorkMarkBoring(boring);
	}
	public void updateFromBoringWorkMarkInteresting(Collection<TaxonNode> interesting) {
		int count = 0;
		int showEvery = 1000;
		logger.info(">updateFromBoringWork.interesting." + interesting.size());
		for (TaxonNode e: interesting) {
			if (count++ % showEvery == 0) {
				logger.info(">updateFromBoringWork.interesting." + count + "." + e.getLatinName());
			}
			template.update("update species set " +
					"interesting_parent_id = ?, " +
					"interesting_child_count = ?, " +
					"boring_final = false " +
					"where id = ?",
					e.getParentId(),
					e.getLoadedChildrenSize(),
					e.getId());
		}
	}
	public void updateFromBoringWorkMarkBoring(Collection<TaxonNode> boring) {
		int count = 0;
		logger.info(">updateFromBoringWork.boring." + boring.size());
		count = 0;
		// this size is really uncertain, but I know 100 will do at least a 4x speedup from size 1
		int subSize = 100;
		String placeholders = getPlaceholders(subSize);
		int subCount = 0;
		Object[] subIds = new Integer[subSize];
		for (TaxonNode e: boring) {
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
						"boring_final = true " +
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
	public Tree<TaxonNode> findCompleteTreeFromPersistence() {
		Collection<TaxonNode> entries = template.query(
				"select id, parent_id, common_name, latin_name, image_link from species", TaxonMapper);
		return TreeNodeUtilities.buildTree(entries);
	}
	public Tree<TaxonNode> findInterestingTreeFromPersistence() {
		logger.info("findInterestingTreeFromPersistence >");
		List<TaxonNode> entries = template.query("select " +
		// TODO
//				getMinimalEntryColumns(false) + 
				", interesting_crunched_ids, linked_image_id " +
				" from species where boring_final = false", TaxonMapper);
		logger.info("findInterestingTreeFromPersistence." + entries.size());
		logger.info("findInterestingTreeFromPersistence < cleaned > buildTree");
		Tree<TaxonNode> tree = TreeNodeUtilities.buildTree(entries);
		logger.info("findInterestingTreeFromPersistence <");
		return tree;
	}

	private static final TaxonMapper TaxonMapper = new TaxonMapper();
	private static class TaxonMapper implements ParameterizedRowMapper<TaxonNode> {
		public TaxonNode mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			TaxonNode t = new TaxonNode();
			t.setTaxonId(rs.getInt("taxon_id"));
			t.setLatinName(rs.getString("latin_name").trim());
			t.setParentTaxonId(rs.getInt("parent_taxon_id"));
			t.setCommonName(StringUtils.trimToNull(rs.getString("common_name")));
			t.setExtinct(rs.getBoolean("extinct"));
			
			Image i = new Image();
			i.setFilePath(StringUtils.trimToNull(rs.getString("image_file_path")));
			// we don't get the other info because it wasn't requested
			if (i.getFilePath() != null) {
				i.setEntryId(rs.getInt("entry_id"));
				i.setTinyWidth(rs.getInt("tiny_width"));
				i.setTinyHeight(rs.getInt("tiny_height"));
				i.setPreviewWidth(rs.getInt("preview_width"));
				i.setPreviewHeight(rs.getInt("preview_height"));
				i.setDetailWidth(rs.getInt("detail_width"));
				i.setDetailHeight(rs.getInt("detail_height"));
			}
			t.setImage(i);
			
			// these can be null if I don't select them in the query
			String rankName = StringUtils.trimToNull(rs.getString("rank"));
			if (rankName != null) {
				Rank rank = Rank.valueOf(rankName);
				t.setRank(rank);
			}
			return t;
		}
	}
	
}
