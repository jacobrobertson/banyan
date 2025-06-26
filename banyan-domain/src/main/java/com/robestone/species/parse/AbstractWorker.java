package com.robestone.species.parse;

import javax.sql.DataSource;

import com.robestone.species.Cache;
import com.robestone.species.DerbyDataSource;
import com.robestone.species.ExamplesService;
import com.robestone.species.ImageService;
import com.robestone.species.ParseStatusService;
import com.robestone.species.SpeciesService;

public class AbstractWorker {

	public AbstractWorker() {
		if (!inited) {
			
			Cache cache = new Cache();
			DataSource dataSource = DerbyDataSource.getDataSource();
			
			S_parseStatusService = new ParseStatusService();
			S_parseStatusService.setDataSource(dataSource);
			
			S_speciesService = new SpeciesService();
			S_speciesService.setDataSource(dataSource);
			cache.setSpeciesService(S_speciesService);
			S_speciesService.setCache(cache);
					
			S_examplesService = new ExamplesService();
			S_examplesService.setDataSource(dataSource);
			S_examplesService.setSpeciesService(S_speciesService);
			
			S_imageService = new ImageService();
			S_imageService.setDataSource(dataSource);
			cache.setImageService(S_imageService);
					
			inited = true;
		}
		this.parseStatusService = S_parseStatusService;
		this.speciesService = S_speciesService;
		this.examplesService = S_examplesService;
		this.imageService = S_imageService;
	}
	
	protected ParseStatusService parseStatusService;
	public SpeciesService speciesService;
	protected ExamplesService examplesService;
	protected ImageService imageService;
	
	private static boolean inited = false;
	private static ParseStatusService S_parseStatusService;
	private static SpeciesService S_speciesService;
	private static ImageService S_imageService;
	private static ExamplesService S_examplesService;

}
