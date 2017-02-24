package com.robestone.species;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class ImageService implements ParameterizedRowMapper<Image> {

	private SimpleJdbcTemplate template;
	
	public Collection<Image> findAllImages() {
		return template.query("select * from images", this);
	}
	public Image findImage(Integer id) {
		List<Image> found = template.query("select * from images where entry_id = ?", this, id);
		if (found.isEmpty()) {
			return null;
		} else {
			return found.get(0);
		}
	}
	
	public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
		Image i = new Image();
		i.setEntryId(rs.getInt("entry_id"));
		i.setTinyWidth(rs.getInt("tiny_width"));
		i.setTinyHeight(rs.getInt("tiny_height"));
		i.setPreviewWidth(rs.getInt("preview_width"));
		i.setPreviewHeight(rs.getInt("preview_height"));
		i.setDetailWidth(rs.getInt("detail_width"));
		i.setDetailHeight(rs.getInt("detail_height"));
		return i;
	}

	public void updateImage(Image i) {
		template.update("update images set " +
				"tiny_width = ?, tiny_height = ?, " +
				"preview_width = ?, preview_height = ?, " +
				"detail_width = ?, detail_height = ? " +
				"where entry_id = ?", 
				i.getTinyWidth(), i.getTinyHeight(), 
				i.getPreviewWidth(), i.getPreviewHeight(), 
				i.getDetailWidth(), i.getDetailHeight(), 
				i.getEntryId());
	}
	public void insertImage(Image i) {
		template.update("insert into images " +
				"(" +
				"tiny_width, tiny_height, " +
				"preview_width, preview_height, " +
				"detail_width, detail_height, " +
				"entry_id) " +
				"values " +
				"(?, ?, ?, ?, ?, ?, ?)", 
				i.getTinyWidth(), i.getTinyHeight(), 
				i.getPreviewWidth(), i.getPreviewHeight(), 
				i.getDetailWidth(), i.getDetailHeight(), 
				i.getEntryId());
	}
	
	public void setDataSource(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}
	
}
