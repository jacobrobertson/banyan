package com.robestone.species.parse;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FileUtils;

import com.robestone.image.ImageIoUtilities;
import com.robestone.species.Entry;

public class ImagesCreater extends AbstractWorker {

	public static final String TINY = "tiny";
	public static final String PREVIEW = "preview";
	public static final String DETAIL = "detail";
	
	private static final int TINY_LENGTH = 20;
	static String LOCAL_STORAGE_DIR = "E:/speciesimages/";

	public static void main(String[] args) throws IOException {
		LOCAL_STORAGE_DIR = "C:/Users/jacob/Desktop/Wikispecies/thumbs/";
		new ImagesCreater().
		downloadTests("Rhinobatidae")
//		downloadAll(true)
		;
		
	}
	
	private ImagesMeasurerWorker imagesMeasurer = new ImagesMeasurerWorker();
	
	public void downloadAll(boolean onlyNew) throws IOException {
		Collection<? extends Entry> links = speciesService.getThumbnails();
		int size = links.size();
		int count = 0;
		for (Entry entry: links) {
			try {
				downloadOne(entry, size, count, onlyNew);
			} catch (Exception e) {
				e.printStackTrace();
				// I don't want to fail these!  and they fail once in a while!
			}
		}
	}
	public void downloadTests(String... latinNames) throws IOException {
		for (String latinName: latinNames) {
			Entry entry = speciesService.findEntryByLatinName(latinName);
			downloadOne(entry, 1, 1, false);
		}
	}
	public void downloadOne(Entry entry, int count, int size, boolean onlyNew) throws IOException {
		String latinName = entry.getLatinName();
		String link = entry.getImageLink();
		
		String fileExtension = getExtensionFromLink(link);
		
		boolean download;
		if (onlyNew) {
			boolean exists = isThumbDownloaded(entry);
			download = !exists;
		} else {
			download = true;
		}
		
		if (download) {
			String hashPath = getImagePathHashed(latinName);
			System.out.print(latinName + "(" + hashPath + ") " + link + " > ");
			// create the thumbs
			try {
				downloadThumbs(entry, latinName, fileExtension, link);
			} catch (Exception e) {
				// fails on some images - haven't figured out why yet
				e.printStackTrace();
			}
			count++;
			System.out.println("made thumbs > " + count + "/" + size);
		}
	}
	private static String getExtensionFromLink(String link) {
		int dotPos = link.lastIndexOf('.');
		String fileExtension = link.substring(dotPos + 1);
		return fileExtension.toLowerCase();
	}
	
	private static boolean isThumbDownloaded(Entry entry) {
		if (entry.getImageLink() == null) {
			return false;
		}
		String path = getImageFilePath(entry, TINY);
		return new File(path).exists();
	}
	public static final String getImageFilePath(Entry e, String type) {
		String l = e.getImageLink();
		String fileExtension = getExtensionFromLink(l);
		String path = getFilePath(e.getLatinName(), type, fileExtension);
		return path;
	}
	public static String parseFileName(String link) {
		// examples
		// thumb/9/9d/Triceratopsskull.jpg/250px-Triceratopsskull.jpg
		// f/fa/Oncifelis_colocolo.jpg
		
		if (link.startsWith("thumb/")) {
			int pos = link.lastIndexOf("/");
			link = link.substring(6, pos);
		}
		return link;
	}
	private static boolean download(String link, String outFilePath, String type) {
		int tries = 10;
		int fails = 0;
		for (int i = 0; i < tries; i++) {
			BufferedImage bi = null;
			try {
				URL url = new URL(link);
				URLConnection con = url.openConnection();
				con.setConnectTimeout(15000); // not sure what a good number is here?
				InputStream in = con.getInputStream();
				String contentEncoding = con.getHeaderField("Content-Encoding");
		        if ("gzip".equalsIgnoreCase(contentEncoding)) {
					in = new GZIPInputStream(in);
		        }
		        
				File outFile = new File(outFilePath);
				outFile.getParentFile().mkdirs();
				ImageOutputStream out = new FileImageOutputStream(outFile);
				bi = ImageIO.read(in);
				ImageIO.write(bi, type, out);
				out.flush();
				out.close();
				return true;
			} catch (Exception ioe) {
				fails++;
			}
		}
		System.out.print(" X(" + fails + ") ");
		return false;
	}
	
	private void downloadThumbs(Entry entry, String latinName, String fileExtension, String link) throws IOException {
		File saved = downloadThumb(latinName, TINY, fileExtension, null, TINY_LENGTH, link, false);
		if (saved == null) {
			// in this case, the image was removed
			return;
		}
		float xratio = getXRatioFromThumb(saved);
		if (xratio < 1f) {
			downloadThumb(latinName, TINY, fileExtension, null, 
					getWidthToDownload(TINY_LENGTH, xratio), link, false);
		}
		int detailWidth = getWidthToDownload(400, xratio);
		saved = downloadThumb(latinName, DETAIL, fileExtension, null, detailWidth, link, true);
		int previewWidth = getWidthToDownload(250, xratio);
		downloadThumb(latinName, PREVIEW, fileExtension, saved, previewWidth, link, true);
		imagesMeasurer.runOne(entry);
	}
	private static String getFilePath(String latinName, String thumbType, String fileExtension) {
		String hashPath = getImagePathHashed(latinName);
		String iconFileName = 
			LOCAL_STORAGE_DIR + thumbType + "/" +
			hashPath + "/" + 
			latinName + "." + fileExtension;
		return iconFileName;
	}
	private File downloadThumb(String latinName, String thumbType, String fileExtension, 
			File saved, int width, String link, boolean recoverOnFail) throws IOException {
		System.out.print(thumbType + "(" + width + ")"  + " > ");
		String iconFileName = getFilePath(latinName, thumbType, fileExtension);
		File fileToMake = new File(iconFileName);
		fileToMake.getParentFile().mkdirs();

		String url = getThumbUrl(link, width);
		boolean okay = download(url, iconFileName, fileExtension);
		if (!okay && recoverOnFail) {
			if (saved == null) {
				// download un-changed image
				url = getThumbUrl(link, -1);
				okay = download(url, iconFileName, fileExtension);
				// if even that one failed, then don't pass the file back
				if (!okay) {
					fileToMake = null;
				}
			} else {
				// copy saved over
				FileUtils.copyFile(saved, fileToMake);
			}
		}
		
		return fileToMake;
	}
	private String getThumbUrl(String link, int width) {
		String prefix = "http://upload.wikimedia.org/wikipedia/commons/";
		String fileName = parseFileName(link);
		String url = prefix;
		if (width > 0) {
			url += "thumb/";
		}
		url += fileName;
		if (width > 0) {
			url += "/" + width + "px-";
			int slash = fileName.lastIndexOf("/");
			String baseFileName = fileName.substring(slash + 1);
			url += baseFileName;
		}
		return url;
	}

	/**
	 * @param latinName like "Aplodontia"
	 * @return like "af/ff3";
	 */
	public static String getImagePathHashed(String latinName) {
		int hash = Math.abs(latinName.hashCode());
		int i = hash % 125;
		String hex = Integer.toHexString(i);
		return hex;
	}
	private int getWidthToDownload(int desiredWidth, float xratio) {
		return (int) (desiredWidth * xratio);
	}
	/**
	 * Assume that the width of the given image is exactly TINY_LENGTH.
	 * @return the value to multiply the width by for other uses.
	 */
	private float getXRatioFromThumb(File file) {
		BufferedImage image = ImageIoUtilities.getImage(file);
		int h = image.getHeight();
		if (h > TINY_LENGTH) {
			return ((float) image.getWidth()) / ((float) h);
		} else {
			return 1f;
		}
	}
}
