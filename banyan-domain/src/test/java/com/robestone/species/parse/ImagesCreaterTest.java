package com.robestone.species.parse;

import java.io.File;

import junit.framework.TestCase;

public class ImagesCreaterTest extends TestCase {

	public void testImages() throws Exception {
		String dir = ImagesCreater.LOCAL_STORAGE_DIR;
		doTestImage("funky.jpg", "Erica × darleyensis", "tiny", dir + "tiny/36/Erica × darleyensis.jpg");
	}
	
	public void doTestImage(String link, String latinName, String type, String expectPath) throws Exception {
		String path = ImagesCreater.getImageFilePath(link, latinName, type);
		assertEquals(expectPath, path);
		File f = new File(path);
		assertTrue(f.exists());
	}
	
}
