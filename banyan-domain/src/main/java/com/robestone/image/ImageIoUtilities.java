package com.robestone.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

public class ImageIoUtilities {

	public static void main(String[] args) throws Exception {
		ImageIoUtilities.compressImage("D:\\banyan-images\\banyan-images\\detail\\2f\\Papaver bracteatum.jpg");
	}
	
	public static BufferedImage getImage(String imageFileAndPath) {
		File file = new File(imageFileAndPath);
		return getImage(file);
	}
	public static BufferedImage getImage(File file) {
		try {
			return ImageIO.read(file);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	public static BufferedImage getImage(InputStream in) {
		try {
			return ImageIO.read(in);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
    /**
     * Finds the image in the right test
     * directory, and chops the "Test" off
     * the given class name.
     */
    public static BufferedImage getTestImage(Class<?> cls, int index) {
    	return getTestImage(cls, null, index);
    }
    public static BufferedImage getTestImage(Class<?> cls, String method, int index) {
    	String imageRelative = cls.getSimpleName();
    	int len = imageRelative.length() - 4;
    	imageRelative = imageRelative.substring(0, len);
    	if (method != null) {
    		imageRelative = imageRelative + "." + method; 
    	}
    	imageRelative = imageRelative + "-" + index + ".bmp";
    	return getClasspathImage(imageRelative, cls);
    }
    public static BufferedImage getClasspathImage(String imageRelative, Class<?> cls) {
    	URL url = getClasspathURL(imageRelative, cls);
    	if (url == null) {
    		throw new IllegalArgumentException(
    				"Could not find image [" + imageRelative + "] for class " +
    				cls.getName());
    	}
		try {
    		InputStream in = url.openStream();
			BufferedImage bi = ImageIO.read(in);
			return bi;
		} catch (Exception e) {
			throw new RuntimeException(imageRelative, e);
		}
    }
	public static URL getClasspathURL(String relative) {
		return getClasspathURL(relative, null);
	}
	public static URL getClasspathURL(String relative, Class<?> cls) {
		relative = getClasspathPath(relative, cls);
		URL url = getClassLoader().getResource(relative);
		if (url == null) {
			url = getClassLoader().getResource("/" + relative);
		}
		return url;
	}
	private static ClassLoader getClassLoader() {
		return ImageIoUtilities.class.getClassLoader();
	}
	public static String getClasspathPath(String relative, Class<?> cls) {
		if (relative.startsWith("/")) {
			relative = relative.substring(1);
		}
		if (cls != null) {
			String pre = cls.getPackage().getName().replace(".", "/");
			relative = pre + "/" + relative;
		}
		return relative;
	}
	
	public static boolean compressImage(String inPath) throws Exception {
		// D:\banyan-images\detail\1c\Tarbosaurus.jpg
		// D:\banyan-images\detail-compress\1c\Tarbosaurus.jpg
		
		File inFile = new File(inPath);
		
		File subFolder = inFile.getParentFile();
		String subFolderName = subFolder.getName();

		File sizeFolder = subFolder.getParentFile();
		String sizeFolderName = sizeFolder.getName();

		File rootFolder = sizeFolder.getParentFile();
		
		String fileName = inFile.getName();

		int dot = fileName.lastIndexOf('.');
		String extension = fileName.substring(dot + 1);
		
		if (!"jpg".equalsIgnoreCase(extension)) {
			return false;
		}
		
//		String left = fileName.substring(0, dot);
//		String newFileName = left + ".jpg";
		
		File compressFolder = new File(rootFolder, sizeFolderName + "-compress/" + subFolderName);
		File compressFile = new File(compressFolder, fileName);
		
		compressFolder.mkdirs();
		
		ImageWriter writer = null;
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
		while (writers.hasNext()) {
			writer = writers.next();
			if (writer.getClass().getName().indexOf("monkey") >= 0) {
				continue;
			} else {
				break;
			}
		}
		// specifies where the jpg image has to be written
		writer.setOutput(new FileImageOutputStream(compressFile));

		float compression = .15f;
		
//		ImageWriteParam param = writer.getDefaultWriteParam();
//		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//		param.setCompressionQuality(compression);
		
		ImageWriteParam param = new JPEGImageWriteParam(null);
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(compression);
		
		// writes the file with given compression level 
		// from your JPEGImageWriteParam instance
		BufferedImage input = getImage(inFile);
		writer.write(null, new IIOImage(input, null, null), param);
		
		return true;
	}
	
	public static void outputImage(BufferedImage image, File file) {
		try {
			file.getParentFile().mkdirs();
			ImageIO.write(image, "BMP", file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
