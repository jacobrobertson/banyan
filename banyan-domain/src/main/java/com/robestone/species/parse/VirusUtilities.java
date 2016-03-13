package com.robestone.species.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class VirusUtilities {

	/*
	 * Group I: dsDNA
	 * Group II: ssDNA
	 * Group III: dsRNA
	 * Group IV: ssRNA(+)
	 * Group V: ssRNA(-)
	 * Group VI: ssRNA(RT)
	 * Group VII: dsDNA(RT)
	 */
	
	
	/**
	 * Very specific situation for "direct" children of the virus groups
	 * 
	 * Given this:
	 * <a href="/wiki/Group_V:_ssRNA(-)" title="Group V: ssRNA(-)">Group V: ssRNA(-)</a><br />
	 * 
	 * Turn it into a valid parent link
	 * Group: <i><a href="/wiki/Group V: ssRNA(-)" title="Group V: ssRNA(-)">Group V: ssRNA(-)</a></i><br />
	 * 
	 * But the caveat is that then we still can't parse the parent name because it has a ":" in it,
	 * unless I adjust the regex.
	 */
	private static String n = "Group[_ ][IV]+\\:[ _][ds]s[RD]NA(?:\\((?:RT|\\+|\\-)\\))?";
	private static Pattern preProcessVirusGroupsPattern = Pattern.compile(
			"<a href=\"/wiki/(" + n + ")\" title=\".*?\">.*?<"	);
	public static String preProcessVirusGroups(String page) {
		
		// just for "+" need to fix that
		page = page.replaceAll("\\(%2B\\)", "(+)");
		
		Matcher m = preProcessVirusGroupsPattern.matcher(page);
		String fixedPage = page;
		while (m.find()) {
			String toReplace = m.group();
			String name = m.group(1);
			String replaceWith = "Group: " + "<a href=\"/wiki/" + name + "\" >VIRUSGROUP<";
			fixedPage = fixedPage.replace(toReplace, replaceWith);
		}
		return fixedPage;
	}
	public static String preProcessVirusGroups(String pageName, String page) {
		if (isVirusName(pageName)) {
			return preProcessVirusGroups(page);
		} else {
			return page;
		}
	}
	public static boolean isVirusName(String name) {
		return StringUtils.containsIgnoreCase(name, "vir") || StringUtils.containsIgnoreCase(name, "phage");
	}

}
