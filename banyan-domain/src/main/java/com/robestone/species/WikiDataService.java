package com.robestone.species;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class WikiDataService {

	private SimpleJdbcTemplate template;
	
	public void setDataSource(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}

	public void insertTaxon(WdTaxon t) {
		template.update(
				"insert into wd_taxons " +
				"(qid, latin_name, parent_qid, rank, extinct, common_name) " +
				"values " +
				"(?, ?, ?, ?, ?, ?)", 
				t.getQid(), t.getLatinName(), t.getParentQid(), t.getRank().getRankIndex(), t.isExtinct(), t.getCommonName());
	}
	
	/**
	 * Just useful because I messed up.
	 */
	public void updateTaxon(String qid, String latinName) {
		template.update("update wd_taxons set latin_name = ? where qid = ?", latinName, qid);
	}
	public void insertImage(WdImage i) {
		template.update(
				"insert into wd_images " +
				"(qid, image_link, depicts_qid) " +
				"values " +
				"(?, ?, ?)", 
				i.getQid(), i.getImageLink(), i.getDepictsQid());
	}
	public void insertNonTaxonQid(String qID) {
		template.update("insert into wd_non_taxons (qid) values (?)", qID);
	}
	
	public WdTaxon findTaxon(String qID) {
		List<WdTaxon> found = template.query(
				"select qid, latin_name, parent_qid, rank, extinct, common_name from wd_taxons where qid = ?", WdTaxonMapper, qID);
		if (found.isEmpty()) {
			return null;
		} else {
			return found.get(0);
		}
	}
	
	public Map<String, WdTaxon> findAllTaxons() {
		Map<String, WdTaxon> map = new HashMap<String, WdTaxon>();
		List<WdTaxon> found = template.query(
				"select qid, latin_name, parent_qid, rank, extinct, common_name from wd_taxons", WdTaxonMapper);
		found.forEach(t -> map.put(t.getQid(), t));
		return map;
	}

	public List<WdImage> findImagesForTaxon(String qID) {
		List<WdImage> found = template.query(
				"select qid, image_link, depicts_qid from wd_images where qid = ?", WdImageMapper, qID);
		return found;
	}
	
	public List<String> findAllTaxonQids() {
		List<String> found = template.query(
				"select qid from wd_taxons", new EntityMapperRowMapper(false));
		return found;
	}
	public List<String> findAllNonTaxonQids() {
		List<String> found = template.query(
				"select qid from wd_non_taxons", new EntityMapperRowMapper(false));
		return found;
	}
	
	private static final WdTaxonMapper WdTaxonMapper = new WdTaxonMapper();
	private static class WdTaxonMapper implements ParameterizedRowMapper<WdTaxon> {
		public WdTaxon mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			WdTaxon t = new WdTaxon();
			t.setExtinct(rs.getBoolean("extinct"));
			t.setQid(rs.getString("qid").trim());
			t.setLatinName(rs.getString("latin_name").trim());
			t.setParentQid(StringUtils.trimToNull(rs.getString("parent_qid")));
			t.setCommonName(StringUtils.trimToNull(rs.getString("common_name")));
			
			int rankInt = rs.getInt("rank");
			if (rankInt > 0) {
				Rank rank = Rank.valueOf(rankInt);
				t.setRank(rank);
			}
			return t;
		}
	}
	private static final WdImageMapper WdImageMapper = new WdImageMapper();
	private static class WdImageMapper implements ParameterizedRowMapper<WdImage> {
		public WdImage mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			WdImage i = new WdImage();
			i.setQid(rs.getString("qid").trim());
			i.setImageLink(rs.getString("image_link").trim());
			i.setDepictsQid(StringUtils.trimToNull(rs.getString("depicts_qid")));
			return i;
		}
	}

}
