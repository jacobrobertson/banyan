package com.robestone.species.parse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.robestone.species.Entry;

public class ImagesWorkerTest {

	@Test
	public void testDjvu() {
		Entry e = new Entry();
		e.setLatinName("Anything");
		e.setImageLink("thumb/2/27/Flora_Graeca%2C_Volume_6.djvu/page153-250px-Flora_Graeca%2C_Volume_6.djvu.jpg");
		ImagesWorker.ImageInfo ii = ImagesWorker.toImageInfo(e);
		assertEquals(ii.fileExtension, "djvu");
		
		assertEquals("65/Anything.jpg", ii.getFilePathRelative());
		assertEquals("2/27/Flora_Graeca%2C_Volume_6.djvu", ii.getUrlBasePath());
		assertEquals("https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Flora_Graeca%2C_Volume_6.djvu/page153-25px-Flora_Graeca%2C_Volume_6.djvu.jpg", ii.getThumbUrl(25));
	}
	
}
