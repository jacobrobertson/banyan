package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TaxoboxFormatter {

	public String toWikispeciesPage(Taxobox box, boolean hasTemplate) {
		StringBuilder buf = new StringBuilder();

		buf.append(formatImage(box));
		buf.append(formatTaxonavigation(box, hasTemplate));
		buf.append(formatName(box));
		buf.append(formatVernacularNames(box));
		buf.append(formatWikispeciesLink(box));
		
		return buf.toString();
	}
	public String toJsPageEntry(Taxobox box, boolean isNamesInteresting) {
		StringBuilder buf = new StringBuilder();
		List<String> entries = new ArrayList<String>();
		String image = formatImage(box);
		entries.add("latin:'" + box.getLatinNameFormatted() + "'");
		if (!StringUtils.isEmpty(image)) {
			entries.add("image:\"" + image.trim() + "\"");
		}
		if (isNamesInteresting) {
			String common = box.getCommonName();
			if (!StringUtils.isEmpty(common)) {
				entries.add("common:'" + common.replace("'", "\\'") + "'");
			}
		}
		for (int i = 0; i < entries.size(); i++) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(entries.get(i));
		}
		return buf.toString();
	}
	public String toTemplatePage(Taxobox box) {
		return
			"{{" + box.getParentLatinName() + "}}\n"
		+	box.getRank().toString() + ": "
		+	"[[" + box.getLatinName() + "]]<br/>\n\n";
	}
	/**
	 [[Image:Platycercus_caledonicus_-Tasmania-6.jpg|thumb|250px|[[Platycercus caledonicus]]]]
	 */
	public String formatImage(Taxobox box) {
		if (StringUtils.isEmpty(box.getImage())) {
			return "";
		}
		String d = box.getImageSpeciesDepicted();
		if (StringUtils.isEmpty(d)) {
			d = box.getLatinName();
		}
		boolean isSameDepicted = box.getLatinName().equalsIgnoreCase(d);
		String imageName = box.getImage();//.replace("'", "\\'");
		String text = 
			"[[File:"
			+ imageName + "|thumb|250px|''";
		if (isSameDepicted) {
			text += d;
		} else {
			text += ("[[" + d + "]]");
		}
		text += "'']]\n\n";
		return text;
	}
	/**
	 == Taxonavigation ==
	{{Microcebus}}
	Species: ''[[Microcebus bongolavensis]]''
	 */
	public String formatTaxonavigation(Taxobox box, boolean hasTemplate) {
		StringBuilder buf = new StringBuilder();
		buf.append("== Taxonavigation ==\n");
		if (hasTemplate) {
			buf.append("{{").append(box.getLatinName()).append("}}\n\n");
		} else {
			buf.append("{{").append(box.getParentLatinName()).append("}}\n");
			buf.append(box.getRank().toString()).append(": ");
			buf.append("[[").append(box.getLatinName()).append("]]\n\n");
		}
		return buf.toString();
	}
	/**
	== Name ==
	''Platycercus caledonicus''  ([[Gmelin]], 1788)
	 */
	public String formatName(Taxobox box) {
		return
			"== Name ==\n"
		+	"''" + box.getLatinName() + "'' "
		+	box.getBinomialAuthorityRaw() + "\n\n";
	}
	/**
	== Vernacular names ==
	{{VN
	|en=Bongolava Mouse Lemur
	}}
	*/
	public String formatVernacularNames(Taxobox box) {
		if (StringUtils.isEmpty(box.getCommonName())) {
			return "";
		}
		return
			"== Vernacular names ==\n"
		+	"{{VN\n"
		+	"|en=" + box.getCommonName() + "\n"
		+	"}}\n\n";
	}
	/**
	 * [[en:Bongolava Mouse Lemur]]
	 */
	public String formatWikispeciesLink(Taxobox box) {
		String n = box.getCommonName();
		if (StringUtils.isEmpty(n)) {
			n = box.getLatinName();
		}
		return "[[en:" + n + "]]\n\n";
	}
}
