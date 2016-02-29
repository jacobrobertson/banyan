package com.robestone.species.parse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.robestone.species.EntityMapperJdbcTemplate;

public class ParseStatusService implements ParameterizedRowMapper<ParseStatus> {

	private SimpleJdbcTemplate template;
	
	public int updateStatus(ParseStatus status) {
		if (status.isDeleted()) {
			return template.update("delete from crawl where link = ?", status.getLatinName());
		} else if (status.getDate() == null) {
			// insert
			status.setDate(new Date());
			return template.update(
					"insert into crawl (link, status, status_date, type)" +
					"values (?, ?, ?, ?)", 
					status.getLatinName(), status.getStatus(), status.getDate(), status.getType());
		} else {
			// update
			return template.update(
					"update crawl set status = ?, status_date = ?, type = ? " +
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
				"where type is null " +
				"order by status_date limit ?", this, oldestCount);
		Set<String> links = new HashSet<String>();
		for (ParseStatus one: found) {
			links.add(one.getLatinName());
		}
		return links;
	}
	public List<ParseStatus> findAllStatus() {
		List<ParseStatus> all = template.query("select * from crawl", this);
		Set<String> links = new HashSet<String>();
		for (ParseStatus one: all) {
			boolean added = links.add(one.getLatinName());
			if (!added) {
				throw new IllegalArgumentException("Duplicate status: " + one.getLatinName());
			}
		}
		return all;
	}
	public List<ParseStatus> findAllDoneNonAuth() {
		return template.query("select * from crawl where status = 'DONE' and (type <> 'AUTH' or type is null)", this);
	}
	public ParseStatus mapRow(ResultSet rs, int row) throws SQLException {
		ParseStatus status = new ParseStatus();
		status.setDate(rs.getDate("status_date"));
		status.setUrl(EntityMapperJdbcTemplate.getString(rs, "link"));
		status.setStatus(rs.getString("status"));
		status.setType(rs.getString("type"));
		return status;
	}
	public void setDataSource(DataSource dataSource) {
		this.template = new EntityMapperJdbcTemplate(dataSource);
	}
	
}
