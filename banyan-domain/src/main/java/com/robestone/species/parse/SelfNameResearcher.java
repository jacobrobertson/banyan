package com.robestone.species.parse;

import java.io.File;

import com.robestone.species.parse.WikiSpeciesProblemsFinder.FileTester;

public class SelfNameResearcher implements FileTester {

	public static void main(String[] args) throws Exception {
		SelfNameResearcher s = new SelfNameResearcher();
		WikiSpeciesProblemsFinder finder = new WikiSpeciesProblemsFinder(s);
		finder.run();
	}
	private int foundCount = 0;
	
	/**
	 * This will fail on names with "/" in it, but that isn't important.
	 */
	@Override
	public void testFile(File file, String page) throws Exception {
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
