package com.robestone.species;

import com.robestone.util.html.EntityMapper;

public class UrlIdUtilities {
	
	public static Integer getIdFromUrlId(String urlId) {
		if (urlId == null) {
			return null;
		}
		int pos = urlId.lastIndexOf('_');
		String id;
		if (pos < 0) {
			id = urlId;
		} else {
			id = urlId.substring(pos + 1, urlId.length());
		}
		return Integer.parseInt(id);
	}
	public static String getUrlId(Entry entry) {
		String urlId = entry.getLatinName();
		if (entry.getCommonName() != null) {
			urlId = entry.getCommonName() + "_" + urlId;
		}
		urlId = urlId + "_" + entry.getId();
		urlId = format(urlId);
		return urlId;
	}
	private static String format(String s) {
		if (s == null) {
			return s;
		}
		s = EntityMapper.convertToSearchText(s, ' ');
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (isOkayLetter(c)) {
				b.append(c);
			} else if (b.length() > 0) {
				char last = b.charAt(b.length() - 1);
				if (last != '_') {
					b.append('_');
				}
			}
		}
		return b.toString();
	}
	private static final String URL_ENCODED = "0123456789";
	private static boolean isOkayLetter(char c) {
		if (Character.isLetter(c)) {
			return true;
		}
		int pos = URL_ENCODED.indexOf(c);
		return pos >= 0;
	}

}
