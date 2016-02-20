package com.robestone.species;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class ExamplesService implements IExamplesService {

	private SpeciesService speciesService;
	private SimpleJdbcTemplate template;
	private IdCruncher cruncher = EntryUtilities.CRUNCHER;
	private List<ExampleGroup> groups;
	private Map<String, Example> examples;
	private Map<String, ExampleGroup> exampleGroupsByExampleCrunchedIds;

	public void setDataSource(DataSource dataSource) {
		this.template = new SimpleJdbcTemplate(dataSource);
	}
	public void setSpeciesService(SpeciesService speciesService) {
		this.speciesService = speciesService;
	}

	public void crunchIds() {
		List<Example> examples = template.query("select * from example order by `index`",
				new ExampleMapper());
		for (Example example: examples) {
			String terms = example.getTerms();
			String crunched;
			if (terms.startsWith("#")) {
				String ids = terms.substring(1);
				crunched = getUpdatedCrunchedIds(ids);
				terms = "#" + crunched;
			} else {
				Set<Integer> ids = speciesService.findBestIds(terms, new ArrayList<Integer>());
				crunched = cruncher.toString(ids);
			}
			System.out.println("crunchIds." + terms + " >> " + crunched);
			template.update("update example set crunched_ids = ?, terms = ? where example_id = ?", crunched, terms, example.getId());
		}
	}
	/**
	 * Only really helps in cases where the terms were the ids hard-coded, then I added new crunching logic.
	 */
	private String getUpdatedCrunchedIds(String ids) {
		List<Integer> ints = cruncher.toList(ids);
		String reCrunched = cruncher.toString(ints);
		return reCrunched;
	}
	public Example findExampleByCrunchedIds(String ids) {
		findExampleGroups();
		return examples.get(ids);
	}
	public ExampleGroup findExampleGroupByCrunchedIds(String ids) {
		findExampleGroups();
		return exampleGroupsByExampleCrunchedIds.get(ids);
	}
	public List<ExampleGroup> findExampleGroups() {
		if (groups == null) {
			List<ExampleGroup> groups = template.query("select * from example_group order by `index`",
					new ExampleGroupMapper());
			
			Map<Integer, ExampleGroup> map = new HashMap<Integer, ExampleGroup>();
			for (ExampleGroup group: groups) {
				map.put(group.getId(), group);
				group.setExamples(new ArrayList<Example>());
			}
			
			List<Example> examples = template.query("select * from example order by `index`",
					new ExampleMapper());
			
			for (Example example: examples) {
				ExampleGroup group = map.get(example.getGroupId());
				// it's quite possible we'll have an example that isn't yet in a group,
				// and for now we just won't return it
				if (group != null) {
					group.getExamples().add(example);
				}
			}
			this.groups = groups;
			buildExamplesMap();
		}
		return groups;
	}
	private void buildExamplesMap() {
		examples = new HashMap<String, Example>();
		exampleGroupsByExampleCrunchedIds = new HashMap<String, ExampleGroup>();
		for (ExampleGroup group: groups) {
			for (Example example: group.getExamples()) {
				examples.put(example.getCrunchedIds(), example);
				exampleGroupsByExampleCrunchedIds.put(example.getCrunchedIds(), group);
			}
		}
		getFamilies().setShowExampleGroupName(true);
		getOtherFamilies().setShowExampleGroupName(false);
		getHaveYouHeardOf().setShowExampleGroupName(true);
		getYouMightNotKnow().setShowExampleGroupName(true);
	}
	private static final int FAMILIES = 0;
	private static final int OTHER_FAMILIES = 0;
	private static final int HAVE_YOU_HEARD_OF = 0;
	private static final int YOU_MIGHT_NOT_KNOW = 0;
	public ExampleGroup getFamilies() {
		return getGroups().get(FAMILIES);
	}
	public ExampleGroup getOtherFamilies() {
		return getGroups().get(OTHER_FAMILIES);
	}
	public ExampleGroup getHaveYouHeardOf() {
		return getGroups().get(HAVE_YOU_HEARD_OF);
	}
	public ExampleGroup getYouMightNotKnow() {
		return getGroups().get(YOU_MIGHT_NOT_KNOW);
	}
	private List<ExampleGroup> getGroups() {
		return groups;
	}
	public void clearCache() {
		groups = null;
		examples = null;
		exampleGroupsByExampleCrunchedIds = null;
	} 
	private static class ExampleGroupMapper implements ParameterizedRowMapper<ExampleGroup> {
		public ExampleGroup mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			ExampleGroup g = new ExampleGroup();
			g.setId(rs.getInt("group_id"));
			g.setCaption(rs.getString("caption"));
			return g;
		}
	}
	private static class ExampleMapper implements ParameterizedRowMapper<Example> {
		public Example mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			Example e = new Example();
			e.setId(rs.getInt("example_id"));
			e.setGroupId(rs.getInt("group_id"));
			e.setCaption(rs.getString("caption"));
			e.setTerms(rs.getString("terms"));
			e.setCrunchedIds(rs.getString("crunched_ids"));
			return e;
		}
	}

}
