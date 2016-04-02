package com.robestone.species.parse;

import org.apache.commons.lang3.StringUtils;

import com.robestone.species.LogHelper;


public class WikipediaCrawler {

	public static void main(String[] args) {
		WikipediaCrawler c = new WikipediaCrawler();
		c.showBoth("Eriocranioidea");
//		c.showOne("Meghimatium fruhstorferi");
	}
	private WikipediaTaxoboxParser parser = new WikipediaTaxoboxParser();
	private TaxoboxFormatter formatter = new TaxoboxFormatter();
	
	public void showBoth(String key) {
		Taxobox box = toTaxobox(key);
		LogHelper.speciesLogger.info(toTemplate(box));
		LogHelper.speciesLogger.info("---------------------");
		LogHelper.speciesLogger.info(toPage(box, true));
	}
	public void showOne(String key) {
		Taxobox box = toTaxobox(key);
		LogHelper.speciesLogger.info(toPage(box, false));
	}
	public String toPage(Taxobox box, boolean hasTemplate) {
		String converted = formatter.toWikispeciesPage(box, hasTemplate);
		return converted;
	}
	public Taxobox toTaxobox(String latinName) {
		String page = getWikipediaPage(latinName);
		if (page == null) {
			return null;
		}
		Taxobox box = parser.parseHtmlPage(latinName, page);
		return box;
	}
	public String toTemplate(Taxobox box) {
		String converted = formatter.toTemplatePage(box);
		return converted;
	}
	private String toLink(String key) {
		key = key.replace(" ", "_");
		return "https://en.wikipedia.org/w/index.php?title=" + key + "&action=edit";
	}
	private String getWikipediaPage(String latinName) {
		String link = toLink(latinName);
		String redirect = "#Redirect [[";
		String page = WikiSpeciesCache.getPageForUrl(link);
		int pos = StringUtils.indexOfIgnoreCase(page, redirect);
		if (pos < 0) {
			return page;
		}
		int end = page.indexOf("]]", pos);
		String commonName = page.substring(pos + redirect.length(), end);
		link = toLink(commonName);
		page = WikiSpeciesCache.getPageForUrl(link);
		return page;
	}
}
