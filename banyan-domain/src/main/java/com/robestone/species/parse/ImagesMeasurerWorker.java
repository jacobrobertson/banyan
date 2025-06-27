package com.robestone.species.parse;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.robestone.image.ImageIoUtilities;
import com.robestone.species.Entry;
import com.robestone.species.Image;

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
		Collection<Entry> entries = speciesService.getThumbnails();
		
		for (Entry entry: entries) {
			runOne(entry);
		}
	}
	public void runOne(Entry entry) {
		// measure
		Image measured = measure(entry);
		if (measured == null) {
			return;
		}
		Image current = imagesMap.get(entry.getId());
		
		// record to db
		if (current == null) {
			imageService.insertImage(measured);
		} else {
			imageService.updateImage(measured);
		}
	}
	private Image measure(Entry entry) {
		Image image = new Image();
		image.setEntryId(entry.getId());
		boolean okay = true;
		okay = okay && measure(entry, image, ImagesCreater.TINY);
		okay = okay && measure(entry, image, ImagesCreater.PREVIEW);
		okay = okay && measure(entry, image, ImagesCreater.DETAIL);
		if (!okay) {
			return null;
		}
		return image;
	}
	private boolean measure(Entry entry, Image image, String type) {
		try {
			String path = ImagesCreater.getImageFilePath(entry, type);
			File file = new File(path);
//			LogHelper.speciesLogger.info("measure." + file);
			BufferedImage bi = ImageIoUtilities.getImage(file);
			int h = bi.getHeight();
			int w = bi.getWidth();
			if (type.equals(ImagesCreater.TINY)) {
				// even though we are downloading larger, we will scale to a fixed size
				// for now, just assume we download at "40" and divide here to half
				int fh = h / 2;
				int fw = w / 2;
				image.setTinyHeight(fh);
				image.setTinyWidth(fw);
			} else if (type.equals(ImagesCreater.PREVIEW)) {
				image.setPreviewHeight(h);
				image.setPreviewWidth(w);
			} else if (type.equals(ImagesCreater.DETAIL)) {
				image.setDetailHeight(h);
				image.setDetailWidth(w);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
}
