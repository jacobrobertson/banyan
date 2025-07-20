package com.robestone.species.parse;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.robestone.image.ImageIoUtilities;
import com.robestone.species.Entry;
import com.robestone.species.Image;
import com.robestone.species.LogHelper;

public class ImagesWorker extends AbstractWorker {

	public static final String TINY = "tiny";
	public static final String PREVIEW = "preview";
	public static final String DETAIL = "detail";
	
	private static final int SLEEP_AFTER_REQUEST = 1000;
	private static float sleepAfterRequest = SLEEP_AFTER_REQUEST;
	private static final int SLEEP_AFTER_429 = 10000;
	private static float sleepAfter429 = SLEEP_AFTER_429;
	
	private static final int TINY_LENGTH = 40;
	public static final String LOCAL_STORAGE_DIR = "D:/banyan/banyan-images/";

	private boolean requireAllImagesExistForCheck = false;
	
	public static void main(String[] args) throws Exception {
		
//		System.out.println(getImagePathHashed("Ptilostemon chamaepeuce"));
		
		ImagesWorker ic = new ImagesWorker();
		ic.requireAllImagesExistForCheck = true;
		ic.downloadAll(true, false);
//		ic.deleteImagesWithNoEntry();
//		ic.fixOldBadImages(true);
//		ic.fixOldBadImagesFromList();
		

	}
	
	private ImagesMeasurerWorker imagesMeasurer = new ImagesMeasurerWorker();
	private boolean forceMeasuring = false;
	
	// These are being blocked by mediawiki commons - I get 429 for any thumb even if there is a "standard thumb" already generated
	private static String[] BLACKLIST = {
			"6/6c/Prosopis_nigra_2c.JPG", 
			"a/ae/Hoya_benitotanii_2011_stamp_of_the_Philippines.jpg", "b/b9/Osedax_rubiplumus.jpg"
	};
	
	public ImagesWorker() {
	}

	/**
	 * This happens if an image gets deleted, or put on a blacklist.
	 */
	public void deleteImagesWithNoEntry() {
		System.out.println(">images.deleteImagesWithNoEntry");
		List<Image> images = imageService.findAllImages();
		System.out.println(">images.deleteImagesWithNoEntry." + images.size());
		for (Image image : images) {
			Entry entry = speciesService.findEntry(image.getEntryId());
			if (entry == null) {
				System.out.println(">images.deleteImagesWithNoEntry." + image.getEntryId());
				imageService.deleteImage(image);
			} else if (entry.getImageLink() == null) {
				System.out.println(">images.deleteImagesWithNoEntry." + image.getEntryId() + "." + entry.getLatinName());
				imageService.deleteImage(image);
			}
		}
	}
	public void runMaintenance() {
		removeBlackListedImages();
		deleteImagesWithNoEntry();
	}
	public void removeBlackListedImages() {
		speciesService.udpateBlacklistedImages(BLACKLIST);
	}
	
	public void downloadAll(boolean onlyNew, boolean onlyTiny) throws IOException {
		System.out.println(">images.downloadAll");
		Collection<Entry> links = speciesService.getThumbnails();
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
		ImageInfo info = toImageInfo(entry);
		
		String latinName = entry.getLatinName();
		String link = entry.getImageLink();
		
		boolean download;
		if (onlyNew) {
			boolean exists = isThumbDownloaded(entry);
			download = !exists;
		} else {
			download = true;
		}
		
		boolean downloaded = false;
		if (download) {
			LogHelper.speciesLogger.info("downloadOne(" + size + "/" + count + "): " + latinName + " (" + info.fileHashDir + ") " + link);
			// create the thumbs
			try {
				downloaded = downloadThumbs(info, onlyTiny);
			} catch (Exception e) {
				// fails on some images - haven't figured out why yet
				e.printStackTrace();
			}
		}
		// this worker also inserts/updates new and existing entries
		if (downloaded || forceMeasuring) {
			imagesMeasurer.runOne(entry);
		}
	}
	/**
	 * Specifically to fix things like this - Group VI: ssRNA
	 */
	public static String getCleanLatinName(String name) {
		name = StringUtils.remove(name, ':');
		return name;
	}
	private static String getExtensionFromLink(String link) {
		int dotPos = link.lastIndexOf('.');
		String fileExtension = link.substring(dotPos + 1);
		return fileExtension.toLowerCase();
	}
	private static Pattern pagePattern = Pattern.compile("/(page[0-9]+-)");
	private static String getPageTokenFromLink(String link) {
		// https://upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Flora_Graeca%2C_Volume_9.djvu/page85-161px-Flora_Graeca%2C_Volume_9.djvu.jpg
		// https://commons.wikimedia.org/wiki/File:Flora_Graeca,_Volume_8.djvu?page=95
		// should always be the first link, not the second, don't bother with that for now
		
		Matcher m = pagePattern.matcher(link);
		if (m.find()) {
			return m.group(1);
		} else {
			return null;
		}
	}
	
	private boolean isThumbDownloaded(Entry entry) {
		if (entry.getImageLink() == null) {
			return false;
		}
		
		String[] types;
		if (requireAllImagesExistForCheck) {
			types = new String[] {TINY, DETAIL, PREVIEW};
		} else {
			types = new String[] {TINY};
		}
		for (String type : types) {
			String path = getImageFilePath(entry, type);
			boolean exists = new File(path).exists();
			if (!exists) {
				return false;
			}
		}
		return true;
	}
	public static final String getImageFilePath(Entry e, String type) {
		ImageInfo info = toImageInfo(e);
		String path = info.getFilePath(type);
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
	
	public static ImageInfo toImageInfo(Entry entry) {
		ImageInfo info = new ImageInfo();
		info.entry = entry;
		info.fileHashDir = getImagePathHashed(entry.getLatinName());
		
		String imageLink = parseFileName(entry.getImageLink());
		int pos = imageLink.lastIndexOf('/');
		String fileName = imageLink.substring(pos + 1);

		String name = fileName.toUpperCase();
		
		if (name.endsWith(".SVG") || name.endsWith(".WEBP") || name.endsWith(".XCF")) {
			info.urlExtraFileTypeToken = "";
			info.urlExtraFileExtension = "png";
		} else if (name.endsWith(".WEBM") || name.endsWith(".OGV")) {
			// https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Amphileptus.webm/320px--Amphileptus.webm.jpg
			info.urlExtraFileTypeToken = "";
			info.urlExtraDash = true;
			info.urlExtraFileExtension = "jpg";
		} else if (name.endsWith(".TIF") || name.endsWith(".TIFF")) {
			info.urlExtraFileTypeToken = "lossy-page1-";
			info.urlExtraFileExtension = "jpg";
		} else if (name.endsWith(".PDF") || name.endsWith(".DJVU")) {
			// https://commons.wikimedia.org/wiki/File:Flora_Graeca,_Volume_8.djvu?page=95
			String page = getPageTokenFromLink(entry.getImageLink());
			if (page == null) {
				page = "page1-";
			}
			info.urlExtraFileTypeToken = page;
			info.urlExtraFileExtension = "jpg";
		} else {
			info.urlExtraFileTypeToken = "";
			info.urlExtraFileExtension = null;
		}

		info.fileExtension = getExtensionFromLink(fileName);
		
		info.urlBasePath = parseFileName(entry.getImageLink());
		
		return info;
	}
	
	public static class ImageInfo {
		Entry entry;
		
		/**
		 * For example x.svg -> x.svg.png
		 */
		String urlExtraFileExtension;

		/**
		 * For example x.pdf -> x .. page1 .. x.pdf
		 */
		String urlExtraFileTypeToken;

		/**
		 * 9/99/Parasite140015-fig2_Protoopalina_pingi_%28Opalinidae%29_Microscopy.tif
		 */
		String urlBasePath;

		boolean urlExtraDash = false;
		
//		String fileName;
		
		String fileExtension;
		
		String fileHashDir;
		
		public String getUrlBasePath() {
			return urlBasePath;
		}
		public String getFilePath(String thumbType) {
			String iconFileName = LOCAL_STORAGE_DIR + thumbType + "/" +	getFilePathRelative();
			return iconFileName;
		}
		public String getFilePathRelative() {
			String thumbFileExtension = "." + getFileFinalExtension();
			String latinName = entry.getLatinName();
			latinName = getCleanLatinName(latinName);
			String iconFileName = fileHashDir + "/" + latinName + thumbFileExtension;
			return iconFileName;
		}
		
		String getFileFinalExtension() {
			if (urlExtraFileExtension != null) {
				return urlExtraFileExtension;
			} else {
				return fileExtension;
			}
		}
		String getThumbUrl(int width) {
			String prefix = "https://upload.wikimedia.org/wikipedia/commons/";
			String fileName = urlBasePath;
			String url = prefix;
			if (width > 0) {
				url += "thumb/";
			}
			url += fileName;
			if (width > 0) {
				url += "/";
				url += urlExtraFileTypeToken;
				url += (width + "px-");
				if (urlExtraDash) {
					url += "-";
				}
				int slash = fileName.lastIndexOf("/");
				String baseFileName = fileName.substring(slash + 1);
				String thumbnailName = getThumnailName(baseFileName);
				url += thumbnailName;
			}
			if (urlExtraFileExtension != null) {
				url += ("." + urlExtraFileExtension);
			}
			return url;
		}

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
	 *  
	 *  rules
  		// x.svg -> x.svg.png
		// x.tif or x.tiff -> lossy-page1 + jpg
		// x.pdf or x.webp -> page1 + jpg

	 */
	
	private static boolean download(String link, String outFilePath, String type) {
		int tries = 1;
		int fails = 0;
		for (int i = 0; i < tries; i++) {
			BufferedImage bi = null;
			URL url = null;
			URLConnection con = null;
			try {
				
				url = new URI(link).toURL();
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
				// we have to do this or wikispecies will start returning 429
				Thread.sleep((int) sleepAfterRequest);
				return true;
			} catch (Exception ioe) {
				LogHelper.speciesLogger.info(ioe.getMessage());
				if (ioe.getMessage().contains("response code: 429")) {
					try {
						Thread.sleep((int) sleepAfter429);
						sleepAfterRequest *= 1.01;
						sleepAfter429 *= 1.05;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				fails++;
			}
		}
		System.out.print(" X(" + fails + ") ");
		return false;
	}
	
	private boolean downloadThumbs(ImageInfo info, boolean onlyTiny) throws IOException {
		boolean downloaded = false;
		File saved = downloadThumb(info, TINY, null, TINY_LENGTH, false);
		if (saved == null) {
			// in this case, the image was removed
			return false;
		}
		float xratio = getXRatioFromThumb(saved);
		if (xratio < 1f) {
			downloadThumb(info, TINY, null, 
					getWidthToDownload(TINY_LENGTH, xratio), false);
			downloaded = true;
		}
		if (!onlyTiny) {
			int detailWidth = getWidthToDownload(400, xratio);
			saved = downloadThumb(info, DETAIL, null, detailWidth, true);
			int previewWidth = getWidthToDownload(250, xratio);
			downloadThumb(info, PREVIEW, saved, previewWidth, true);
			downloaded = true;
		}
		return downloaded;
	}
	/*
	public static String getLegalFileNamePart(String latinName) {
		
	}
	*/
	private File downloadThumb(ImageInfo info, String thumbType, 
			File saved, int width, boolean recoverOnFail) throws IOException {
		String iconFileName = info.getFilePath(thumbType);
		File fileToMake = new File(iconFileName);
		fileToMake.getParentFile().mkdirs();

		String url = info.getThumbUrl(width);

		System.out.println("\t>" + thumbType + "(" + width + ")["  + url +  "] -> " + fileToMake.getPath());
		
		String ext = info.getFileFinalExtension();
		boolean okay = download(url, iconFileName, ext);
		if (!okay && recoverOnFail) {
			if (saved == null) {
				// download un-changed image
				url = info.getThumbUrl(-1);
				okay = download(url, iconFileName, ext);
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
	private static String getThumnailName(String baseFileName) {
		
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

	public void fixOldBadImagesFromList() throws Exception {
		for (String latin : DELETE_SET) {
			String hash = getImagePathHashed(latin);
			for (String type : new String[] {TINY, DETAIL, PREVIEW }) {
				File dir = new File(LOCAL_STORAGE_DIR + type + "/" + hash);
				fixOldBadImages(dir);
			}
		}
	}

	/**
	 * Images that got downloaded, but should be re-downloaded
	 */
	public void fixOldBadImages(boolean downloadAll) throws Exception {
		// find all images not in the expected formats
		File dir = new File(LOCAL_STORAGE_DIR);
		fixOldBadImages(dir);
		if (downloadAll) {
			downloadAll(true, false);
		}
	}
	private void fixOldBadImages(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				String ext = getExtensionFromLink(file.getName()).toUpperCase();
				boolean delete = !(ext.equals("PNG") || ext.equals("GIF") || ext.equals("JPG") || ext.equals("JPEG"));
				if (!delete) {
					String fileLatin = StringUtils.split (file.getName(), '.')[0];
					delete = DELETE_SET.contains(fileLatin);
				}
				if (delete) {
					System.out.println(file.getPath());
					file.delete();
				}
			}
		}
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				fixOldBadImages(file);
			}
		}
	}
	private Set<String> DELETE_SET = new HashSet<>(Arrays.asList(StringUtils.split(DELETE_THESE, "\r\n")));
	
	private static String DELETE_THESE = 
			  "Scutellaria hirta\r\n"
					  + "Symphytum creticum\r\n"
					  + "Teucrium alpestre\r\n"
					  + "Teucrium arduini\r\n"
					  + "Teucrium creticum\r\n"
					  + "Teucrium cuneifolium\r\n"
					  + "Teucrium lucidum\r\n"
					  + "Teucrium spinosum"
					  ;
	
}
