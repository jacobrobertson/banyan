package com.robestone.species.parse;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;

/**
 * Crawls pages to look for problems.
 * @author jacob
 */
public class WikiSpeciesProblemsFinder {

	public static void main(String[] args) throws Exception {
		FileTester tester;
		
		tester = new StubFinder();
		
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
		File[] files = dir.listFiles();
		for (File file: files) {
			if (file.isDirectory()) {
				testAll(file);
			} else {
				testFile(file);
			}
		}
	}
	private void testFile(File file) throws Exception {
		String contents = IOUtils.toString(new FileInputStream(file));
		tester.testFile(file, contents);
	}

	public static class StubFinder implements FileTester {
		private String imageLinkStubToken = "width=\"120\" height=\"90\"";
		@Override
		public void testFile(File file, String contents) throws Exception {
			if (contents.indexOf(imageLinkStubToken) > 0) {
//				System.out.println(file.getPath());
				String name = file.getName();
				int pos = name.lastIndexOf('.');
				String latinName = name.substring(0, pos);
				System.out.println("\"" + latinName + "\",");
			}
		}
	}
	
}
