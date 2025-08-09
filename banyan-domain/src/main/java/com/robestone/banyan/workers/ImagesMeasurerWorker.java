package com.robestone.banyan.workers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.robestone.banyan.taxons.Image;
import com.robestone.banyan.taxons.Taxon;
import com.robestone.banyan.taxons.TaxonNode;
import com.robestone.banyan.util.ImageIoUtilities;

public class ImagesMeasurerWorker extends AbstractWorker {

	public static void main(String[] args) {
//		ImagesCreater.LOCAL_STORAGE_DIR = "C:/temp/";
		new ImagesMeasurerWorker().run();
	}
	
	private Map<Integer, Image> imagesMap;
	public ImagesMeasurerWorker() {
		Collection<Image> images = imageService.findAllImages();
		imagesMap = new HashMap<Integer, Image>();
		for (Image i: images) {
			imagesMap.put(i.getEntryId(), i);
		}
	}
	public void run() {
		Collection<TaxonNode> entries = getTaxonService().findAllImageFilePaths();
		
		for (Taxon entry: entries) {
			runOne(entry);
		}
	}
	public void runOne(Taxon entry) {
		// measure
		Image measured = measure(entry);
		if (measured == null) {
			return;
		}
		Image current = imagesMap.get(entry.getTaxonId());
		
		// record to db
		if (current == null) {
			imageService.insertImage(measured);
		} else {
			imageService.updateImage(measured);
		}
	}
	private Image measure(Taxon entry) {
		Image image = new Image();
		image.setEntryId(entry.getTaxonId());
		boolean okay = true;
		okay = okay && measure(entry, image, ImagesWorker.TINY);
		okay = okay && measure(entry, image, ImagesWorker.PREVIEW);
		okay = okay && measure(entry, image, ImagesWorker.DETAIL);
		if (!okay) {
			return null;
		}
		return image;
	}
	private boolean measure(Taxon entry, Image image, String type) {
		try {
			String path = ImagesWorker.getImageFilePath(entry, type);
			File file = new File(path);
//			LogHelper.speciesLogger.info("measure." + file);
			BufferedImage bi = ImageIoUtilities.getImage(file);
			int h = bi.getHeight();
			int w = bi.getWidth();
			if (type.equals(ImagesWorker.TINY)) {
				// even though we are downloading larger, we will scale to a fixed size
				// for now, just assume we download at "40" and divide here to half
				int fh = h / 2;
				int fw = w / 2;
				image.setTinyHeight(fh);
				image.setTinyWidth(fw);
			} else if (type.equals(ImagesWorker.PREVIEW)) {
				image.setPreviewHeight(h);
				image.setPreviewWidth(w);
			} else if (type.equals(ImagesWorker.DETAIL)) {
				image.setDetailHeight(h);
				image.setDetailWidth(w);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
}
