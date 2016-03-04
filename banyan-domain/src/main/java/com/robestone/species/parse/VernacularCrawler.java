package com.robestone.species.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LogHelper;

/**
 * Attempts to "crawl" (really just iterate)
 * over wikispecies and see where vernacular names haven't been added 
 * @author Jacob Robertson
 */
public class VernacularCrawler {

	public static void main(String[] args) throws IOException {
		new VernacularCrawler().run();
	}
	
	private String dir = "E:\\WikiSpeciesCache\\";
	private WikiSpeciesParser parser = new WikiSpeciesParser();
	// 
	private static Pattern[] patterns = {
			Pattern.compile(
					"<div class=\"description en\" lang=\"en\" xml:lang=\"en\"><span class=\"language en\" title=\"English\"><b>English:</b></span>(.*?)</div>"
					),
			Pattern.compile(
					"<li class=\"interwiki-en\"><a href=\"http://en.wikipedia.org/wiki/(.*?)\"(?:\\stitle=\".*?\")?>English</a></li>"
					),
	};
			
	public void run() throws IOException {
		String[] files = new File(dir).list();
		LogHelper.speciesLogger.info("files.length." + files.length);
		for (String file: files) {
			run(file);
		}
	}
	
	public void run(String latinName) throws IOException {
		String file = dir + latinName;
		String page = IOUtils.toString(new FileInputStream(file));
		CompleteEntry e = parser.parse(latinName, page, false);
		if (e != null && e.getCommonName() == null) {
			String sidebar = getSidebar(latinName, page);
			if (sidebar != null) {
				LogHelper.speciesLogger.info("missingVernacular." + latinName + " > " + sidebar);
			}
		}
	}
	
	/**
	 * Get the english link from the sidebar
	 */
	public static String getSidebar(String latin, String page) {
		for (Pattern pattern: patterns) {
			Matcher matcher = pattern.matcher(page);
			if (matcher.find()) {
				String found = matcher.group(1);
				found = EntryUtilities.fixCommonName(found);
				found = StringUtils.trimToNull(found);
				if (found != null && !found.equals(latin)) {
					return getLink(found);
				}
			}
		}
		return null;
	}
	/**
	 * Solves this one case: Tuatara#Brothers_Island_tuatara
	 */
	private static String getLink(String link) {
		int pos = link.indexOf("#");
		if (pos > 0) {
			return link.substring(pos + 1);
		} else {
			return link;
		}
	}
	
}
