package com.robestone.species.parse;

public class ThumbnailDownloader {

	public static void main(String[] args) throws Exception {
		if (args != null && args.length > 0) {
			if ("all".equals(args[0])) {
				new ImagesCreater().downloadAll(false);
			} else {
				new ImagesCreater().downloadTests(args);
			}
		} else {
			new ImagesCreater().downloadAll(true);
		}
	}
	
}
