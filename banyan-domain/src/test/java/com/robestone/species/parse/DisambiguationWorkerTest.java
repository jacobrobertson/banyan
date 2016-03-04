package com.robestone.species.parse;

import java.util.List;

import junit.framework.TestCase;

public class DisambiguationWorkerTest extends TestCase {

	public void testParseNames() throws Exception {
		String page = WikiSpeciesParserTest.getPage("Zygomyia submarginata");
		DisambiguationWorker worker = new DisambiguationWorker();
		List<String> names = worker.parseDisambiguationPage(page);
		assertEquals(2, names.size());
		assertEquals("Zygomyia submarginata Harrison", names.get(0));
		assertEquals("Zygomyia submarginata Zaitzev", names.get(1));
		
		System.out.println(worker.parseLinksOnParentPage(page));
	}
	
}
