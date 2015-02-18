package com.robestone.species.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedirectPageParser {

	private static final Pattern pattern = Pattern.compile(
		"<img src=\".*?\" alt=\"#REDIRECT ?\" ?/><span class=\"redirectText\"><a href=\"/wiki/(.*?)\"");
	
	public String getRedirectTo(String page) {
		Matcher matcher = pattern.matcher(page);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
	
}
