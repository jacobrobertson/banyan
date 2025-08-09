package com.robestone.banyan.wikispecies;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.robestone.banyan.workers.AbstractWorker;

/**
 * Crawls pages to look for problems.
 * @author jacob
 */
public class WikiSpeciesProblemsFinder {

	public static void main(String[] args) throws Exception {
		FileTester tester;
		
		tester = new RedirectFinder();
		
		new WikiSpeciesProblemsFinder(tester).run();
	}
	
	public static interface FileTester {
		void testFile(File file, String contents) throws Exception;
	}
	
	private FileTester tester;
	
	public WikiSpeciesProblemsFinder(FileTester tester) {
		this.tester = tester;
	}
	public void run() throws Exception {
		File file = new File(WikiSpeciesCache.LOCAL_STORAGE_DIR);
		testAll(file);
	}

	public void testAll(File dir) throws Exception {
		int count = 0;
		int chunk = 0;
		int maxChunk = 10000;
		File[] files = dir.listFiles();
		for (File file: files) {
			if (file.isDirectory()) {
				testAll(file);
			} else {
				testFile(file);
				count++;
				chunk++;
				if (chunk == maxChunk) {
					System.out.println(tester.getClass().getSimpleName() + "." + count);
					chunk = 0;
				}
			}
		}
	}
	
	public static String getLatinName(File file) {
		String name = file.getName();
		int pos = name.lastIndexOf('.');
		String latinName = name.substring(0, pos);
		return latinName;
	}
	
	private void testFile(File file) throws Exception {
		String contents = IOUtils.toString(new FileInputStream(file), Charset.defaultCharset());
		tester.testFile(file, contents);
	}

	public static class StubFinder implements FileTester {
		private String imageLinkStubToken = "width=\"120\" height=\"90\"";
		@Override
		public void testFile(File file, String contents) throws Exception {
			if (contents.indexOf(imageLinkStubToken) > 0) {
//				System.out.println(file.getPath());
				String latinName = getLatinName(file);
				System.out.println("\"" + latinName + "\",");
			}
		}
	}
	
	public static class RedirectFinder extends AbstractWorker implements FileTester {
		private RedirectPageParser redirectPageParser = new RedirectPageParser();
		@Override
		public void testFile(File file, String contents) throws Exception {
			String redirect = redirectPageParser.getRedirectTo(contents);
			if (redirect != null) {
				String latinName = getLatinName(file);
				String existingRedirect = getWikiSpeciesService().findRedirectTo(latinName);
//				if (redirect.equals(existingRedirect)) {
				System.out.println(latinName + ", dbRedirect=" + existingRedirect + ", fileRedirect=" + redirect);
//				}
				if (existingRedirect == null) {
					getWikiSpeciesService().updateRedirect(latinName, redirect);
				}
			}

		}
	}
	
}
