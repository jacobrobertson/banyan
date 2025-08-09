package com.robestone.banyan.workers;

import javax.sql.DataSource;

import com.robestone.banyan.taxons.ExamplesService;
import com.robestone.banyan.taxons.TaxonService;
import com.robestone.banyan.util.DerbyDataSource;
import com.robestone.banyan.wikidata.WikiDataService;
import com.robestone.banyan.wikispecies.Cache;
import com.robestone.banyan.wikispecies.ImageService;
import com.robestone.banyan.wikispecies.ParseStatusService;
import com.robestone.banyan.wikispecies.WikiSpeciesService;

public class AbstractWorker {

	public AbstractWorker() {
		if (!inited) {
			
			Cache cache = new Cache();
			DataSource dataSource = DerbyDataSource.getDataSource();
			
			S_parseStatusService = new ParseStatusService();
			S_parseStatusService.setDataSource(dataSource);
			
			S_wikiSpeciesService = new WikiSpeciesService();
			S_wikiSpeciesService.setDataSource(dataSource);
			cache.setWikiSpeciesService(S_wikiSpeciesService);
			S_wikiSpeciesService.setCache(cache);
					
			S_imageService = new ImageService();
			S_imageService.setDataSource(dataSource);
			cache.setImageService(S_imageService);
			
			S_wikiDataService = new WikiDataService();
			S_wikiDataService.setDataSource(dataSource);
			
			S_taxonService = new TaxonService();
			S_taxonService.setDataSource(dataSource);

			S_examplesService = new ExamplesService();
			S_examplesService.setDataSource(dataSource);
			S_examplesService.setTaxonService(S_taxonService);
			
			
			inited = true;
		}
		this.parseStatusService = S_parseStatusService;
		this.wikiSpeciesService = S_wikiSpeciesService;
		this.examplesService = S_examplesService;
		this.imageService = S_imageService;
		this.wikidataService = S_wikiDataService;
		this.taxonService = S_taxonService;
	}
	
	protected ParseStatusService parseStatusService;
	public WikiSpeciesService wikiSpeciesService;
	public ExamplesService examplesService;
	public ImageService imageService;
	public WikiDataService wikidataService;
	public TaxonService taxonService;
	
	private static boolean inited = false;
	private static ParseStatusService S_parseStatusService;
	private static WikiSpeciesService S_wikiSpeciesService;
	private static ImageService S_imageService;
	private static ExamplesService S_examplesService;
	private static WikiDataService S_wikiDataService;
	private static TaxonService S_taxonService;
	protected ParseStatusService getParseStatusService() {
		return parseStatusService;
	}
	protected WikiSpeciesService getWikiSpeciesService() {
		return wikiSpeciesService;
	}
	protected ExamplesService getExamplesService() {
		return examplesService;
	}
	protected ImageService getImageService() {
		return imageService;
	}
	protected WikiDataService getWikidataService() {
		return wikidataService;
	}
	protected TaxonService getTaxonService() {
		return taxonService;
	}
	
}
