package com.robestone.species.parse;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;

public class SelfNameResearcher {

	public static void main(String[] args) throws Exception {
		SelfNameResearcher s = new SelfNameResearcher();
		File file = new File(WikiSpeciesCache.LOCAL_STORAGE_DIR);
		s.testAll(file);
	}
	private int foundCount = 0;
	public void testAll(File dir) throws Exception {
		File[] files = dir.listFiles();
		for (File file: files) {
			if (file.isDirectory()) {
				testAll(file);
			} else {
				testOneFile(file);
			}
		}
	}
	
	/**
	 * This will fail on names with "/" in it, but that isn't important.
	 */
	public void testOneFile(File file) throws Exception {
		
		String page = IOUtils.toString(new FileInputStream(file));
		String fileName = file.getName();
		int pos = fileName.lastIndexOf('.');
		String latinName = fileName.substring(0, pos);
		// Looking for 
		// <i><b>Moeritherium</b></i>
		String lookFor = "<i><b>" + latinName + "</b></i>";
		pos = page.indexOf(lookFor);
		if (pos > 0) {
			System.out.println((foundCount++) + "." + file.getPath());
		}
	}
	
}
