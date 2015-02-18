package com.robestone.species.parse;


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
		System.out.println(toTemplate(box));
		System.out.println("---------------------");
		System.out.println(toPage(box, true));
	}
	public void showOne(String key) {
		Taxobox box = toTaxobox(key);
		System.out.println(toPage(box, false));
	}
	public String toPage(Taxobox box, boolean hasTemplate) {
		String converted = formatter.toWikispeciesPage(box, hasTemplate);
		return converted;
	}
	public Taxobox toTaxobox(String latinName) {
		String page = getWikipediaPage(latinName);
		Taxobox box = parser.parseHtmlPage(latinName, page);
		return box;
	}
	public String toTemplate(Taxobox box) {
		String converted = formatter.toTemplatePage(box);
		return converted;
	}
	private String toLink(String key) {
		key = key.replace(" ", "_");
		return "http://en.wikipedia.org/w/index.php?title=" + key + "&action=edit";
	}
	private String getWikipediaPage(String latinName) {
		String link = toLink(latinName);
		String redirect = "#Redirect [[";
		String page = WikiSpeciesCrawler.getPageForUrl(link);
		int pos = page.indexOf(redirect);
		if (pos < 0) {
			return page;
		}
		int end = page.indexOf("]]", pos);
		String commonName = page.substring(pos + redirect.length(), end);
		link = toLink(commonName);
		page = WikiSpeciesCrawler.getPageForUrl(link);
		return page;
	}
}
