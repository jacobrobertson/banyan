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
	static String LOCAL_STORAGE_DIR = "D:/speciesimages/";

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
	/**
	 * For some files, (or file types?)
	 * we need to use this syntax
	 * 
	 * https://upload.wikimedia.org/wikipedia/commons
	 * /thumb/9/99/Parasite140015-fig2_Protoopalina_pingi_%28Opalinidae%29_Microscopy.tif
	 * /lossy-page1-194px-Parasite140015-fig2_Protoopalina_pingi_%28Opalinidae%29_Microscopy.tif.jpg
	 * 
	 * Instead of
	 * 
	 * https://upload.wikimedia.org/wikipedia/commons
	 * /thumb/9/99/Parasite140015-fig2_Protoopalina_pingi_%28Opalinidae%29_Microscopy.tif
	 * /20px-Parasite140015-fig2_Protoopalina_pingi_%28Opalinidae%29_Microscopy.tif
	 * 
	 * Also, in cases of PDFs!! use "page1-"
	 * 
	 * is there also a "-" sometimes? AFTER the 800px, not before
	 * /thumb/4/43/xxx.ogv/800px--xxx.ogv.jpg
	 * 
	 *  Another pattern, is to simply place this after the image url - seems to be when the name itself is way too long
	 *  /90px-thumbnail.jpg
	 *  https://upload.wikimedia.org/wikipedia/commons/thumb/7/76/xxx.jpg/90px-thumbnail.jpg
	 */
	private boolean downloadWithAlternatives(String dbLink, int width, String outFilePath, String type) {
		
		String[] keys = {null, "page1-", "lossy-page1-"};
		String[] exts = {null, "jpg", "png"};
		
		for (String key: keys) {
			for (String ext: exts) {
				boolean okay = false;
				okay = okay || doDownloadWithAlternatives(dbLink, width, outFilePath, type, ext, key, false);
				okay = okay || doDownloadWithAlternatives(dbLink, width, outFilePath, type, ext, key, true);
				if (okay) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean doDownloadWithAlternatives(String dbLink, int width, String outFilePath, String type, String extension, String extraKey, boolean doubleDashPixels) {
		String url = getThumbUrl(dbLink, width, extension, extraKey, doubleDashPixels);
		return download(url, outFilePath, type);
	}
	
	private static boolean download(String link, String outFilePath, String type) {
		int tries = 1;
		int fails = 0;
		for (int i = 0; i < tries; i++) {
			BufferedImage bi = null;
			URL url = null;
			URLConnection con = null;
			try {
				url = new URL(link);
				con = url.openConnection();
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
				System.out.println(ioe.getMessage());
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

		boolean okay = downloadWithAlternatives(link, width, iconFileName, fileExtension);
		if (!okay && recoverOnFail) {
			if (saved == null) {
				// download un-changed image
				String url = getThumbUrl(link, -1, null, null, false);
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
	private String getThumbUrl(String link, int width, String extraExtension, String extraKey, boolean doubleDashPixels) {
		String prefix = "https://upload.wikimedia.org/wikipedia/commons/";
		String fileName = parseFileName(link);
		String url = prefix;
		if (width > 0) {
			url += "thumb/";
		}
		url += fileName;
		if (width > 0) {
			url += "/";
			if (extraKey != null) {
				url += extraKey;
			}
			url += (width + "px-");
			if (doubleDashPixels) {
				url += "-";
			}
			int slash = fileName.lastIndexOf("/");
			String baseFileName = fileName.substring(slash + 1);
			String thumbnailName = getThumnailName(baseFileName);
			url += thumbnailName;
		}
		if (extraExtension != null) {
			url += ("." + extraExtension);
		}
		return url;
	}
	
	/**
The_North_American_sylva%3B_or%2C_A_description_of_the_forest_trees_of_the_United_States%2C_Canada_and_Nova_Scotia._Considered_particularly_with_respect_to_their_use_in_the_arts_and_their_introduction_into_%2814778618571%29.jpg
Skeptrostachys_rupestris_%28as_Spiranthes_r.%29_-_Veyretia_cogniauxiana_%28as_Spiranthes_c.%29_-_Skeptrostachys_balanophorostachya_%28as_Stenorrhynchos_b.%27-um%29_-_Flora_Brasiliensis_3-4-48.jpg
	 * @param baseFileName
	 * @return
	 */
	private static int MAX_THUMBNAIL_NAME_LEN = 195; // not sure what the actual length is
	private String getThumnailName(String baseFileName) {
		if (baseFileName.length() < MAX_THUMBNAIL_NAME_LEN) {
			return baseFileName;
		} else {
			int pos = baseFileName.lastIndexOf('.');
			String ext = baseFileName.substring(pos);
			return "thumbnail" + ext;
		}
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
		try {
			BufferedImage image = ImageIoUtilities.getImage(file);
			int h = image.getHeight();
			if (h > TINY_LENGTH) {
				return ((float) image.getWidth()) / ((float) h);
			} else {
				return 1f;
			}
		} catch (Exception e) {
			throw new RuntimeException("File: " + file.getAbsolutePath(), e);
		}
	}
}
