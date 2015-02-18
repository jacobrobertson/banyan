package com.robestone.species;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;

import com.robestone.util.html.EntityMapper;

public class EntityMapperRowMapper extends ParameterizedSingleColumnRowMapper<String> {

	@Override
	public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
		String s = super.mapRow(rs, rowNumber);
		s = EntityMapper.convertToSymbolsText(s, '_');
		return s;
	}

}
