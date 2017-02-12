package com.robestone.species.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedirectPageParser {

	private static final Pattern[] patterns = {
		Pattern.compile(
				"<img src=\".*?\" alt=\"#REDIRECT ?\" ?/><span class=\"redirectText\"><a href=\"/wiki/(.*?)\""),
		Pattern.compile(
    		//   <div class="redirectMsg">      <p>Redirect to:    </p>    <ul class="redirectText">      <li>    <a href="/wiki/Rajomorphii" title="Rajomorphii">Rajomorphii</a></li></ul></div>
				"<div class=\"redirectMsg\">\\s*<p>Redirect to:\\s*</p>\\s*<ul class=\"redirectText\">\\s*<li>\\s*<a href=\"/wiki/(.*?)\" title=\""),
		Pattern.compile(
				"<div class=\"redirectMsg\">\\s*<p>Redirect to:\\s*</p>\\s*<ul class=\"redirectText\">\\s*<li>\\s*<a href=\"/w/index.php\\?title=(.*?)&amp;redirect=no"),
		Pattern.compile(
				"<li>REDIRECT <a href=\"/wiki/(.*?)\" title=\".*?\">.*?</a></li>"),
	};
	
	
	public String getRedirectTo(String page) {
		for (Pattern pattern: patterns) {
			Matcher matcher = pattern.matcher(page);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}
	
}
