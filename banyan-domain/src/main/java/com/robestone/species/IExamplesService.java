package com.robestone.species;

import java.util.List;

import javax.sql.DataSource;

public interface IExamplesService {

	void setDataSource(DataSource dataSource);

	void setSpeciesService(SpeciesService speciesService);

	void crunchIds();

	List<ExampleGroup> findExampleGroups();

	void clearCache();
	
	Example findExampleByCrunchedIds(String ids);
	ExampleGroup findExampleGroupByCrunchedIds(String ids);

	ExampleGroup getFamilies();
	ExampleGroup getOtherFamilies();
	ExampleGroup getHaveYouHeardOf();
	ExampleGroup getYouMightNotKnow();

}