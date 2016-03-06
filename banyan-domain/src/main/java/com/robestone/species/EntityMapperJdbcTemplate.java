package com.robestone.species;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.robestone.util.html.EntityMapper;

public class EntityMapperJdbcTemplate extends SimpleJdbcTemplate {

	public EntityMapperJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}
	@Override
	public <T> List<T> query(String sql, ParameterizedRowMapper<T> rm,
			Object... args) throws DataAccessException {
		convert(args);
		return super.query(sql, rm, args);
	}
	@Override
	public int update(String sql, Object... args) throws DataAccessException {
		convert(args);
		return super.update(sql, args);
	}
	private void convert(Object[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] instanceof String) {
				String s = (String) array[i];
				s = EntityMapper.convertToBracketEntities(s, true);
				array[i] = s;
			}
		}
	}
	public static String getString(ResultSet rs, String name) throws SQLException {
		String s = rs.getString(name);
		s = StringUtils.trimToNull(s);
		s = unencode(s);
		return s;
	}
	private static String unencode(String s) {
		if (s == null) {
			return null;
		}
		s = EntityMapper.convertToSymbolsText(s, true);
		return s;
	}
	
}
