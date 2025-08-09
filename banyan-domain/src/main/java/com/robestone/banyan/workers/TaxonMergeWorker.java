package com.robestone.banyan.workers;

import java.util.List;

import com.robestone.banyan.taxons.Image;
import com.robestone.banyan.taxons.Taxon;
import com.robestone.banyan.wikidata.WdImage;
import com.robestone.banyan.wikidata.WdTaxon;
import com.robestone.banyan.wikispecies.Entry;

public class TaxonMergeWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		TaxonMergeWorker worker = new TaxonMergeWorker();
		worker.runUpdateTaxonsFromSources();
	}
	
	private boolean isNewTaxonsOnly = true;
	
	public void runUpdateTaxonsFromSources() throws Exception {
		int count = 0;
		// insert / update values
		List<String> qids = wikidataService.findAllTaxonQids();
		for (String qid : qids) {
			WdTaxon wdTaxon = wikidataService.findWdTaxon(qid);
			runUpdateTaxonsFromSources(wdTaxon);
			if (count++ % 1000 == 0) {
				System.out.println("runUpdateTaxonsFromSources." + count + "/" + qids.size());
			}
		}
	}
	private void runUpdateTaxonsFromSources(WdTaxon wdTaxon) throws Exception {
		Integer existingParentTaxonId = null;
		
		// this can be null, but it's pretty rare - only happens when there are parsing issues of some kind
		WdTaxon wdParentTaxon = wikidataService.findWdTaxon(wdTaxon.getParentQid());
		if (wdParentTaxon != null && !wdParentTaxon.getQid().equals(wdTaxon.getQid())) {
			existingParentTaxonId = taxonService.findTaxonIdByLatinName(wdParentTaxon.getLatinName());
			if (existingParentTaxonId == null) {
				runUpdateTaxonsFromSources(wdParentTaxon);
				existingParentTaxonId = taxonService.findTaxonIdByLatinName(wdParentTaxon.getLatinName());
			}
		}
		runUpdateTaxonsFromSources(wdTaxon, existingParentTaxonId);
	}
	private void runUpdateTaxonsFromSources(WdTaxon wdTaxon, Integer existingParentTaxonId) throws Exception {
		
		
		Integer existingId = taxonService.findTaxonIdByLatinName(wdTaxon.getLatinName());
		if (isNewTaxonsOnly && existingId != null) {
			return;
		}

		
		List<WdImage> images = wikidataService.findImagesForTaxon(wdTaxon.getQid());
		wdTaxon.setImages(images);
		
		Entry wsTaxon = getWikiSpeciesService().findEntryByLatinName(wdTaxon.getLatinName());
		Taxon taxon = merge(wdTaxon, wsTaxon);
		
		// TODO I should try and actually figure out which is better
		taxon.setParentTaxonId(existingParentTaxonId);

		if (existingId == null) {
			taxonService.insertTaxon(taxon);
		} else {
			taxon.setTaxonId(existingId);
			taxonService.updateTaxonWithMergedInfo(taxon);
		}
	}
	
	private Taxon merge(WdTaxon wdTaxon, Entry wsTaxon) {
		Taxon taxon = new Taxon();
		taxon.setImage(new Image());
		
		taxon.setLatinName(wdTaxon.getLatinName());
		taxon.setCommonName(wdTaxon.getCommonName());
		taxon.setExtinct(wdTaxon.isExtinct());
		taxon.setRank(wdTaxon.getRank());

		String imageLink = null;
		if (!wdTaxon.getImages().isEmpty()) {
			// TODO need to choose the correct one - but I can add that later and it will update then
			WdImage image = wdTaxon.getImages().get(0);
			imageLink = image.getImageLink();
		}
		
		if (wsTaxon != null) {
			if (wsTaxon.getCommonName() != null && wdTaxon.getCommonName() == null) {
				taxon.setCommonName(wsTaxon.getCommonName());
			}
			if (wsTaxon.getImageLink() != null && imageLink == null) {
				imageLink = wsTaxon.getImageLink();
			}
		}

		if (imageLink != null) {
			String imageFilePath = getImageFilePath(taxon.getLatinName(), imageLink);
			taxon.getImage().setFilePath(imageFilePath);
		}
		
		return taxon;
	}
	
	private String getImageFilePath(String latinName, String filePath) {
		ImagesWorker.ImageInfo ii = new ImagesWorker.ImageInfo(latinName, filePath);
		return ii.getFilePathRelative();
	}
	
}
