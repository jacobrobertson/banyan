package com.robestone.species;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;

import com.robestone.util.html.EntityMapper;

public class EntityMapperRowMapper extends ParameterizedSingleColumnRowMapper<String> {

	private boolean convert = true;
	
	public EntityMapperRowMapper() {
		this(true);
	}
	public EntityMapperRowMapper(boolean convert) {
		this.convert = convert;
	}
	
	@Override
	public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
		String s = super.mapRow(rs, rowNumber);
		if (s != null) {
			s = s.trim();
			if (convert) {
				s = EntityMapper.convertToSymbolsText(s, '_');
			}
		}
		return s;
	}

}
