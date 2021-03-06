package com.robestone.species;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.robestone.species.parse.ParseStatus;

public class ParseStatusService implements ParameterizedRowMapper<ParseStatus> {

	private SimpleJdbcTemplate template;
	
	public boolean updateToAuth(String link) {
		int count = template.update("update crawl set crawl_type = 'AUTH' where link = ? and (crawl_type <> 'AUTH' or crawl_type is null)", link);
		return (count == 1);
	}
	public int deleteStatus(ParseStatus status) {
		return template.update("delete from crawl where crawl_id = ?", status.getCrawlId());
	}
		
	public int updateStatus(ParseStatus status) {
		if (status.isDeleted()) {
			return template.update("delete from crawl where link = ?", status.getLatinName());
		} else if (status.getDate() == null) {
			// insert
			status.setDate(new Date());
			return template.update(
					"insert into crawl (link, status, status_date, crawl_type)" +
					"values (?, ?, ?, ?)", 
					status.getLatinName(), status.getStatus(), status.getDate(), status.getType());
		} else {
			// update
			return template.update(
					"update crawl set status = ?, status_date = ?, crawl_type = ? " +
					"where link = ?", status.getStatus(), status.getDate(), status.getType(), status.getLatinName());
		}
	}
	public int setAllAsDone() {
		return template.update(
				"update crawl set status = ?", "DONE");
	}
	public ParseStatus findForLatinName(String latinName) {
		List<ParseStatus> found = template.query("select * from crawl where link = ?", this, latinName);
		if (found.isEmpty()) {
			return null;
		} else {
			return found.get(0);
		}
	}
	public Collection<String> findOldestLinks(int oldestCount) {
		List<ParseStatus> found = template.query(
				"select * from crawl " +
				"where crawl_type is null " +
				"order by status_date fetch first ? rows only", this, oldestCount);
		Set<String> links = new HashSet<String>();
		for (ParseStatus one: found) {
			links.add(one.getLatinName());
		}
		return links;
	}
	public List<ParseStatus> findAllStatusDuplicatesOkay() {
		return template.query("select * from crawl", this);
	}
	public List<ParseStatus> findAllStatus() {
		List<ParseStatus> all = template.query("select * from crawl", this);
		Map<String, ParseStatus> map = new HashMap<String, ParseStatus>();
		for (ParseStatus one: all) {
			String key = one.getLatinName();
			if (map.containsKey(key)) {
//				throw new IllegalArgumentException("Duplicate status: " + one.getLatinName());
				LogHelper.speciesLogger.error("Duplicate status: " + key);
				
				// for now, we just choose the one with the DONE
				// we need to delete these later
				if (one.isDone()) {
					map.put(key, one);
				}
			} else {
				map.put(key, one);
			}
		}
		return new ArrayList<ParseStatus>(map.values());
	}
	public List<ParseStatus> findAllAuth() {
		List<ParseStatus> all = template.query("select * from crawl where crawl_type = 'AUTH'", this);
		return all;
	}
	public List<ParseStatus> findAllNonAuth() {
		return template.query("select * from crawl where (crawl_type <> 'AUTH' or crawl_type is null)", this);
	}
	public List<ParseStatus> findAllDoneNonAuth() {
		return template.query("select * from crawl where status = 'DONE' and (crawl_type <> 'AUTH' or crawl_type is null)", this);
	}
	public ParseStatus mapRow(ResultSet rs, int row) throws SQLException {
		ParseStatus status = new ParseStatus();
		status.setDate(rs.getDate("status_date"));
		status.setUrl(EntityMapperJdbcTemplate.getString(rs, "link"));
		status.setStatus(getString(rs, "status"));
		status.setType(getString(rs, "crawl_type"));
		status.setCrawlId(rs.getInt("crawl_id"));
		return status;
	}
	public void setDataSource(DataSource dataSource) {
		this.template = new EntityMapperJdbcTemplate(dataSource);
	}
	private String getString(ResultSet rs, String col) throws SQLException {
		String s = rs.getString(col);
		if (s != null) {
			return s.trim();
		}
		return s;
	}
	
}
