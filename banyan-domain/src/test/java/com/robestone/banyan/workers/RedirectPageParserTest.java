package com.robestone.banyan.workers;

import java.io.IOException;

import com.robestone.banyan.wikispecies.RedirectPageParser;
import com.robestone.banyan.wikispecies.WikiSpeciesParserTest;

import junit.framework.TestCase;

public class RedirectPageParserTest extends TestCase {

	public void testRedirectPages() throws IOException {
		doTest("Oligomyodi", "Tyranni");
		doTest("Cryptochrysis virescens", "Chroomonas_virescens");
	}
	
	private void doTest(String name, String redirectTo) throws IOException {
		RedirectPageParser parser = new RedirectPageParser();
		String page = WikiSpeciesParserTest.getPage(name);
		String to = parser.getRedirectTo(page);
		assertEquals(redirectTo, to);
	}
	
}
