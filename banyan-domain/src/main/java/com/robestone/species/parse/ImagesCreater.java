package com.robestone.species.parse;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FileUtils;

import com.robestone.image.ImageIoUtilities;
import com.robestone.species.Entry;
import com.robestone.species.LogHelper;

public class ImagesCreater extends AbstractWorker {

	public static final String TINY = "tiny";
	public static final String PREVIEW = "preview";
	public static final String DETAIL = "detail";
	
	private static final int TINY_LENGTH = 40;
	public static String LOCAL_STORAGE_DIR = "D:/banyan-images/";

	public static void main(String[] args) throws IOException {
		
		new ImagesCreater().
		downloadOne("Chordata Craniata", 1, 1, false, false);
//		downloadAll(true, false);

/*		
		if (args != null && args.length > 0) {
			if (args[0].equals("fixTiny")) {
				new ImagesCreater().downloadAll(false, true);
			}
		} else {
	//		LOCAL_STORAGE_DIR = "C:/Users/jacob/Desktop/Wikispecies/thumbs/";
			new ImagesCreater().
	//		downloadTests("Tree of Life")
			downloadAll(false, true)
			;
		}
		*/
	}
	
	private ImagesMeasurerWorker imagesMeasurer = new ImagesMeasurerWorker();
	private boolean forceMeasuring = false;
	
	public void downloadAll(boolean onlyNew, boolean onlyTiny) throws IOException {
		System.out.println(">images.downloadAll");
		Collection<? extends Entry> links = speciesService.getThumbnails();
		int size = links.size();
		int count = 0;
		int chunk = 0;
		for (Entry entry: links) {
			try {
				downloadOne(entry, size, count, onlyNew, onlyTiny);
			} catch (Exception e) {
				e.printStackTrace();
				// I don't want to fail these!  and they fail once in a while!
			}
			count++;
			chunk++;
			if (chunk > 10000) {
				System.out.println("images.downloadAll." + count);
				chunk = 0;
			}
		}
		System.out.println("<images.downloadAll");
	}
	public void downloadTests(String... latinNames) throws IOException {
		for (String latinName: latinNames) {
			Entry entry = speciesService.findEntryByLatinName(latinName);
			downloadOne(entry, 1, 1, false, false);
		}
	}
	public void downloadOne(String latinName, int count, int size, boolean onlyNew, boolean onlyTiny) throws IOException {
		Entry entry = this.speciesService.findEntryByLatinName(latinName);
		downloadOne(entry, count, size, onlyNew, onlyTiny);
	}
	public void downloadOne(Entry entry, int count, int size, boolean onlyNew, boolean onlyTiny) throws IOException {
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
		
		boolean downloaded = false;
		if (download) {
			String hashPath = getImagePathHashed(latinName);
			System.out.print(latinName + "(" + hashPath + ") " + link + " > ");
			// create the thumbs
			try {
				downloaded = downloadThumbs(entry, latinName, fileExtension, link, onlyTiny);
			} catch (Exception e) {
				// fails on some images - haven't figured out why yet
				e.printStackTrace();
			}
			LogHelper.speciesLogger.info("made thumbs > " + count + "/" + size);
		}
		// this worker also inserts/updates new and existing entries
		if (downloaded || forceMeasuring) {
			imagesMeasurer.runOne(entry);
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
		return getImageFilePath(e.getImageLink(), e.getLatinName(), type);
	}
	public static final String getImageFilePath(String link, String latinName, String type) {
		String fileExtension = getExtensionFromLink(link);
		String path = getFilePath(latinName, type, fileExtension);
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
				LogHelper.speciesLogger.info(ioe.getMessage());
				fails++;
			}
		}
		System.out.print(" X(" + fails + ") ");
		return false;
	}
	
	private boolean downloadThumbs(Entry entry, String latinName, String fileExtension, String link, boolean onlyTiny) throws IOException {
		boolean downloaded = false;
		File saved = downloadThumb(latinName, TINY, fileExtension, null, TINY_LENGTH, link, false);
		if (saved == null) {
			// in this case, the image was removed
			return false;
		}
		float xratio = getXRatioFromThumb(saved);
		if (xratio < 1f) {
			downloadThumb(latinName, TINY, fileExtension, null, 
					getWidthToDownload(TINY_LENGTH, xratio), link, false);
			downloaded = true;
		}
		if (!onlyTiny) {
			int detailWidth = getWidthToDownload(400, xratio);
			saved = downloadThumb(latinName, DETAIL, fileExtension, null, detailWidth, link, true);
			int previewWidth = getWidthToDownload(250, xratio);
			downloadThumb(latinName, PREVIEW, fileExtension, saved, previewWidth, link, true);
			downloaded = true;
		}
		return downloaded;
	}
	/*
	public static String getLegalFileNamePart(String latinName) {
		
	}
	*/
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

Herpestes_ichneumon_%D0%95%D0%B3%D0%B8%D0%BF%D0%B5%D1%82%D1%81%D0%BA%D0%B8%D0%B9_%D0%BC%D0%B0%D0%BD%D0%B3%D1%83%D1%81%D1%82%2C_%D0%B8%D0%BB%D0%B8_%D1%84%D0%B0%D1%80%D0%B0%D0%BE%D0%BD%D0%BE%D0%B2%D0%B0_%D0%BA%D1%80%D1%8B%D1%81%D0%B0%2C_%D0%B8%D0%BB%D0%B8_%D0%B8%D1%85%D0%BD%D0%B5%D0%B2%D0%BC%D0%BE%CC%81%D0%BD.jpg/245px-Herpestes_ichneumon_%D0%95%D0%B3%D0%B8%D0%BF%D0%B5%D1%82%D1%81%D0%BA%D0%B8%D0%B9_%D0%BC%D0%B0%D0%BD%D0%B3%D1%83%D1%81%D1%82%2C_%D0%B8%D0%BB%D0%B8_%D1%84%D0%B0%D1%80%D0%B0%D0%BE%D0%BD%D0%BE%D0%B2%D0%B0_%D0%BA%D1%80%D1%8B%D1%81%D0%B0%2C_%D0%B8%D0%BB%D0%B8_%D0%B8%D1%85%D0%BD%D0%B5%D0%B2%D0%BC%D0%BE%CC%81%D0%BD.jpg > tiny(20) >
Maxillaria_lilacea_-_Brasiliorchis_polyantha_%28as_Maxillaria_p.%29_-_Brasiliorchis_phoenicanthera_%28as_Maxillaria_p.%29_-_Brasiliorchis_chrysantha_%28as_Maxillaria_c.%29_-_Fl.Br._3-6-09.jpg
Maxillaria_lilacea_-_Brasiliorchis_polyantha_(as_Maxillaria_p.)_-_Brasiliorchis_phoenicanthera_(as_Maxillaria_p.)_-_Brasiliorchis_chrysantha_(as_Maxillaria_c.)_-_Fl.Br._3-6-09.jpg
Stelis_pellifeloidis_%as_Pleurothallis_p.%_-_Pleurothallis_cristata_-_Acianthera_rodriguesii_%as_Pleurothallis_r.%_-_Anathallis_pusilla_-_%as_Pl._exigua%_-_Fl.Br.3-4-104.jpg
Malaxis_cogniauxiana_%as_Microstylis_gracilis%_-_Malaxis_excavata_%as_Microstylis_quadrangularis%_-_Prosthechea_pygmaea_%as_Microstylis_humilis%_-_Fl.Br._3-6-114.jpg

This image was failing because wikimedia has the wrong library installed to handle large TIF files
https://upload.wikimedia.org/wikipedia/commons/thumb/7/7c/Aedes_vexans.tif/lossy-page1-20px-Aedes_vexans.tif.jpg

	 */
	// get this number through testing - can't make it too short or that will fail also
	private static int MAX_THUMBNAIL_NAME_LEN = 165; 
	private String getThumnailName(String baseFileName) {
		
		// convert base out of URL encoding before checking length
		String decoded;
		try {
			decoded = URLDecoder.decode(baseFileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		if (decoded.length() < MAX_THUMBNAIL_NAME_LEN) {
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
