package com.robestone.species.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.robestone.species.LogHelper;

public class WikiSpeciesCache {

	public static final WikiSpeciesCache CACHE = new WikiSpeciesCache();
	
	public static String LOCAL_STORAGE_DIR = "D:/wikispecies-cache/";
	public static String DELETED_PAGE = "This page has been deleted.";

	private static int SLEEP_AFTER_REQUEST = 2000;
	private static int SLEEP_AFTER_429 = 10000;
	public static int DEFAULT_MAX_RETRIES = 3;
	
	public boolean isFilePresent(String latinName, boolean forceDownload) throws Exception {
		File file = getFile(latinName);
		return file.exists();
	}
	public String readFile(String latinName, boolean forceDownload) throws Exception {

		String text = null;
		
		// check for expiration
		if (!forceDownload && !isExpired(latinName)) {
			// check local cache
			text = readLocalFile(latinName);
		}
		
		text = StringUtils.trimToNull(text);
		
		// download from URL
		if (text == null) {
			text = getPage(latinName);
			// store
			writeFile(latinName, text);
		}
		
		return text;
	}
	
	public boolean isExpired(String latinName) throws Exception {
		return false;
	}
	
	public void writeFile(String latinName, String text) throws Exception {
		File file = getFile(latinName);
		file.getParentFile().mkdirs();
		try {
			IOUtils.write(text, new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
			// TODO I need to handle this case - the problem is names like Chalcodryidae "Cyphaleus" which have illegal chars
			//	but I need to ensure that the web page, etc. can get the file name correctly too
		}
	}
	private File getFile(String latinName) {
		String hash = ImagesCreater.getImagePathHashed(latinName);
		String fileName = LOCAL_STORAGE_DIR + hash + "/" + latinName + ".html";
		File file = new File(fileName);
		return file;
	}
	
	public String readLocalFile(String latinName) throws Exception {
		File file = getFile(latinName);
		if (!file.exists()) {
			return null;
		}
		String text = IOUtils.toString(new FileInputStream(file));
		return text;
	}
	public static String getPage(String link) {
		link = StringUtils.replace(link, " ", "_");
		
		try {
			link = URLEncoder.encode(link, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		link = "https://species.wikimedia.org/wiki/" + link + "?redirect=no";
		return getPageForUrl(link);
	}
	public static String getPageForUrl(String link) {
		return getPageForUrl(link, DEFAULT_MAX_RETRIES);
	}
	public static String getPageForUrl(String link, int maxRetries) {
		int sleepAfter429 = SLEEP_AFTER_429;
		try {
			URL url = new URL(link);
			URLConnection con = url.openConnection();
			HttpURLConnection hconn = (HttpURLConnection) con;
			con.setConnectTimeout(15000); // not sure what a good number is here?
			hconn.addRequestProperty("Content-Type", "text/html;charset=UTF-8");
			hconn.setUseCaches(false);
			int status = hconn.getResponseCode();
			if (status == HttpURLConnection.HTTP_NOT_FOUND) {
				// 401 means it was deleted
				// this is a pretty big workaround, but it should work...
//				LogHelper.speciesLogger.info("status." + status + "." + link);
				return DELETED_PAGE;
			}
			InputStream in = con.getInputStream();
			String contentEncoding = con.getHeaderField("Content-Encoding");
	        if ("gzip".equalsIgnoreCase(contentEncoding)) {
				in = new GZIPInputStream(in);
	        }
			String page = IOUtils.toString(in, "UTF-8");
			// I do this to avoid spamming wikispecies too hard
			// not sure how long a sleep I need
			Thread.sleep(SLEEP_AFTER_REQUEST);
			return page;
		} catch (IOException ioe) {
			LogHelper.speciesLogger.info("getPageForUrl.IOException." + ioe.getMessage() +  "(" + maxRetries + ")." + link);
			if (maxRetries == 0) {
				return null;
			} else {
				try {
					String reason = ioe.getMessage();
					if (reason.contains("response code: 429")) {
						Thread.sleep(sleepAfter429);
						sleepAfter429 += sleepAfter429;
					} else {
						Thread.sleep(SLEEP_AFTER_REQUEST);
					}
				} catch (InterruptedException e) {
				}
				return getPageForUrl(link, maxRetries - 1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
