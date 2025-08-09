package com.robestone.banyan.util;

import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;

public class ParseUtilities {

	public static final String COMMON_NAME_FROM_DESCENDENTS_INDICATOR = "...";

	public static String fixCommonName(String name) {
		name = StringUtils.trimToNull(name);
		if (name == null) {
			return null;
		}
		name = name.replace("&#160;", "");
		name = name.replace("&amp;", "&");
		name = name.replace("<br />", ";");
		name = name.replace("<i>", "");
		name = name.replace("</i>", "");
		name = name.replace("`", "");
		name = name.replace("_", " ");
		
		if (name.indexOf("</a>") > 0) {
			name = name.substring(0, name.length() - 4);
			int pos = name.lastIndexOf(">");
			name = name.substring(pos + 1);
		}
		
		while (name.contains("  ")) {
			name = name.replace("  ", " ");
		}
		name = urlDecode(name);
		
		// remove "." from the end - pretty common
		if (name.indexOf(COMMON_NAME_FROM_DESCENDENTS_INDICATOR) < 0) {
			name = StringUtils.removeEnd(name, ".");
		}
		
		name = StringUtils.trimToNull(name);
		
		if (name != null) {
			char first = Character.toUpperCase(name.charAt(0));
			name = first + name.substring(1);
		}
		
		return name;
	}
	public static String urlDecode(String n) {
		try {
			return URLDecoder.decode(n, "UTF-8");
		} catch (Exception e) {
			// fails if "UTF-8" is invalid encoding!
			throw new RuntimeException(e);
		}
	}
	

}
