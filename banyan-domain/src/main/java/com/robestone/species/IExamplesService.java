package com.robestone.species;

import java.util.List;

import javax.sql.DataSource;

public interface IExamplesService {

	void setDataSource(DataSource dataSource);

	void setSpeciesService(SpeciesService speciesService);

	void crunchIds(boolean updateTerms);

	List<ExampleGroup> findExampleGroups();

	void clearCache();
	
	Example findExampleByCrunchedIds(String ids);
	ExampleGroup findExampleGroupByCrunchedIds(String ids);

	ExampleGroup getFamilies();
	ExampleGroup getOtherFamilies();
	ExampleGroup getHaveYouHeardOf();
	ExampleGroup getYouMightNotKnow();

	String getSearchExample1();
	String getSearchExample2();
	String getSearchExample3();
	String getSearchExample4();

}