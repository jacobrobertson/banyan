package com.robestone.species;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class SqlSearcher implements EntrySearcher {

	private Logger logger = Logger.getLogger(SqlSearcher.class);

	private SpeciesService service;
	
	public SqlSearcher(SpeciesService service) {
		this.service = service;
	}

	public int search(String name, Collection<Integer> existingIds) {
		name = name.toUpperCase().trim();
		// TODO check that this number exists
		if (StringUtils.isNotEmpty(name) && StringUtils.isNumeric(name)) {
			return Integer.parseInt(name);
		}
		int found;
		
		// -- primary searches (i.e. targetted for alpha-1, not necessarily
				// the ones I think are "sorted" to the top for results)
		// exact match for common name
		// exact match between whitespace in common name
		// same 2 for latin
		String likesQuery = "COL like ?";
		String[] likes = {"% " + name, name + " %", "% " + name + " %", "%" + name, name + "%", "%" + name + "%"};
		String[] cols = {"common_name_clean", "latin_name_clean"};
		for (String col: cols) {
			found = findBestIdBySqlPart("COL = ?", col, name, existingIds);
			if (found > 0) {
				return found;
			}
			// do the two "s" logic matches
			if (name.endsWith("S")) {
				String withoutS = name.substring(0, name.length() - 1);
				found = findBestIdBySqlPart("COL = ?", col, withoutS, existingIds);
			} else {
				found = findBestIdBySqlPart("COL = ?", col, name + "S", existingIds);
			}
			if (found > 0) {
				return found;
			}
			
			// do like matches in priority order
			for (String likePart: likes) {
				found = findBestIdBySqlPart(likesQuery, col, likePart, existingIds);
				if (found > 0) {
					return found;
				}
			}
		}
		
		// checks for the name or the cleanest name within the cleanest name for all entries
		String cleanest = EntryUtilities.getClean(name, true);
		String[] names = {name, cleanest};
		for (String query: names) {
			found = findBestIdBySqlPart("COL like ?", "common_name_cleanest", "%" + query + "%", existingIds);
			if (found > 0) {
				return found;
			}
		}
		
		return -1;
	}
	private int findBestIdBySqlPart(String sqlPart, String column, String query, Collection<Integer> existingIds) {
		sqlPart = sqlPart.replace("COL", column);
		String sql = "select " + column + ", id from species where " + sqlPart + " " +
			" and " + service.getBoringColumn() + " = 0 " +
			getNotIdInPart(existingIds) +
			" limit 10";
		logger.debug("findBestIdBySqlPart.sql." + sql);
		List<IdAndString> found = service.getTemplate().query(sql, IdAndStringMapper, query);
		int size = found.size();
		if (size == 0) {
			return -1;
		}
		if (size == 1) {
			return found.get(0).getId();
		}
		
		// look for the shortest one - that means it's a closer match
		int minLen = Integer.MAX_VALUE;
		IdAndString chosen = null;
		for (IdAndString one: found) {
			int len = one.getString().length();
			if (len < minLen) {
				minLen = len;
				chosen = one;
			}
		}
		return chosen.getId();
	}
	private String getNotIdInPart(Collection<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		buf.append("and not (id in (");
		boolean first = true;
		for (Integer id: ids) {
			if (!first) {
				buf.append(",");
			} else {
				first = false;
			}
			buf.append(id);
		}
		buf.append("))");
		return buf.toString();
	}
	private static class IdAndString {
		private int id;
		private String string;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getString() {
			return string;
		}
		public void setString(String string) {
			this.string = string;
		}
	}

	private static final IdAndStringMapper IdAndStringMapper = new IdAndStringMapper();
	private static class IdAndStringMapper implements ParameterizedRowMapper<IdAndString> {
		public IdAndString mapRow(ResultSet rs, int rowNum) throws SQLException {
			IdAndString i = new IdAndString();
			i.setId(rs.getInt("id"));
			i.setString(rs.getString(1));
			return i;
		}
	}

}
