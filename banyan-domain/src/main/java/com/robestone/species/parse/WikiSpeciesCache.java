package com.robestone.species.parse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

public class WikiSpeciesCache extends AbstractSiteFileCache {

	public static void main(String[] args) {
		System.out.println(CACHE.getFile("Carcinus maenas"));
	}
	
	
	public static String LOCAL_STORAGE_DIR = "D:/banyan/caches/wikispecies/";
	public static final WikiSpeciesCache CACHE = new WikiSpeciesCache(LOCAL_STORAGE_DIR, "html");

	public WikiSpeciesCache(String localStorageDir, String pageExt) {
		super(localStorageDir, pageExt);
	}
	
	protected String toUrl(String pageKey) {
		pageKey = StringUtils.replace(pageKey, " ", "_");
		
		try {
			pageKey = URLEncoder.encode(pageKey, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		String link = "https://species.wikimedia.org/wiki/" + pageKey + "?redirect=no";

		return link;
	}
	public String getPage(String link) {
		link = StringUtils.replace(link, " ", "_");
		
		try {
			link = URLEncoder.encode(link, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		link = "https://species.wikimedia.org/wiki/" + link + "?redirect=no";
		return getPageForUrl(link);
	}

}
