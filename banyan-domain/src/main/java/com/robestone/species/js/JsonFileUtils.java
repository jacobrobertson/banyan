package com.robestone.species.js;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.robestone.species.CrunchedIds;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Example;
import com.robestone.species.ExampleGroup;
import com.robestone.species.SpeciesService;
import com.robestone.species.parse.ImagesWorker;
import com.robestone.species.parse.ImagesWorker.ImageInfo;

// mostly just a very dumb implementation for testing purposes
public class JsonFileUtils {

	private static final CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

	public static void main(String[] args) throws Exception {
		generateSiteMap();
	}

	private static final String baseUrl = "http://jacobrobertson.com/banyan/";
	public static final String outputDir = "D:/banyan/banyan-json/json";
//	public static final String jsonDir = "../banyan-js/src/main/webapp/json";
	private static final String additionalResourcesDir = "../banyan-js/src/main/resources/webapp/json";
	private static final String siteMapFileLocation = "../banyan-js/src/main/webapp/sitemap.txt";
	
	public static void generateSiteMap() throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append(baseUrl + "\n");
		
		addExamplesLines(buf);
		addRandomsLines(buf);
		
		System.out.println("generateSiteMap:");
		System.out.println(buf);
		FileUtils.writeStringToFile(new File(siteMapFileLocation), buf.toString(), Charset.defaultCharset());
	}

	private static void addExamplesLines(StringBuilder buf) throws Exception {
		buf.append(baseUrl + "q/t/examplesTab\n");
		File[] files = new File(outputDir + "/e").listFiles();
		for (File file : files) {
			String name = FilenameUtils.removeExtension(file.getName());
			if (!"examples-index".equals(name) && !"examples-structure".equals(name)) {
				buf.append(baseUrl + "q/e/" + name + "\n");
			}
		}
	}
	private static void addRandomsLines(StringBuilder buf) throws Exception {
		File[] files = new File(outputDir + "/r").listFiles();
		for (File file : files) {
			String name = FilenameUtils.removeExtension(file.getName());
			if (!"random-index".equals(name)) {
				int pos = name.lastIndexOf('-');
				name = name.substring(0,  pos);
				buf.append(baseUrl + "q/r/" + name + "\n");
			}
		}
	}
	
	private static Node parseFile(String f) throws Exception {
		File file = new File(outputDir + "\\" + f);
		if (!file.exists()) {
			return null;
		}
		return parseFile(file);
	}
	public static Node parseFile(File file) throws Exception {
		String s = FileUtils.readFileToString(file, Charset.defaultCharset());
		Map<Integer, Node> nodes = parseString(s);
		Node root = null;
		for (Integer id : nodes.keySet()) {
			Node n = nodes.get(id);
			Node p = nodes.get(n.getEntry().getParentId());
			if (p == null) {
				root = n;
			}
			n.setParent(p);
		}
		return root;
	}

	public static Node parseRecursive(Integer id) throws Exception {
		int sub = getSubFolder(id);
		String file = sub + "/" + id + ".json";
		Node node = parseFile(file);
		if (node == null) {
			return null;
		}
		List<Integer> actualChildIds = new ArrayList<>();
		for (Integer childId : node.getChildIds()) {
			Node childNode = parseRecursive(childId);
			if (childNode != null) {
				node.getChildren().add(childNode);
				actualChildIds.add(childId);
			}
		}
		node.getChildIds().clear();
		node.getChildIds().addAll(actualChildIds);
//		System.out.println(node.getId() + ": " + node.getChildIds());
		return node;
	}
	
	public static List<JsonEntry> parseWithApi(String s) throws Exception {
		List<JsonEntry> list = new ArrayList<>();
		JsonReader rdr = Json.createReader(new ByteArrayInputStream(s.getBytes()));
		JsonObject obj = rdr.readObject();
		JsonArray entries = obj.getJsonArray("entries");
		for (JsonValue val : entries) {
			obj = (JsonObject) val;
			JsonEntry entry = new JsonEntry();
			entry.setId(obj.getInt("id"));
			if (obj.containsKey("parentId")) {
				entry.setParentId(obj.getInt("parentId"));
			}
			entry.setLname(obj.getString("lname"));
			entry.setChildrenIds(getIntegers(obj, "childrenIds"));
			entry.setShowMoreLeafIds(getIntegers(obj, "showMoreLeafIds"));
			entry.setShowMoreOtherIds(getIntegers(obj, "showMoreOtherIds"));
			
			entry.setImg(getString(obj, "img"));
			if (entry.getImg() != null) {
				entry.settHeight(obj.getInt("tHeight"));
				entry.settWidth(obj.getInt("tWidth"));
				entry.setpHeight(obj.getInt("pHeight"));
				entry.setpWidth(obj.getInt("pWidth"));
				entry.setdHeight(obj.getInt("dHeight"));
				entry.setdWidth(obj.getInt("dWidth"));
				entry.setImgData(obj.getString("imgData"));
			}

			if (obj.containsKey("cnames")) {
				entry.setCnames(new ArrayList<>());
				JsonArray childrenIds = obj.getJsonArray("cnames");
				for (JsonValue cname : childrenIds) {
					JsonString string = (JsonString) cname;
					entry.getCnames().add(string.getString());
				}
			}
			
			list.add(entry);
		}
		return list;
	}

	private static List<Integer> getIntegers(JsonObject obj, String key) {
		List<Integer> ints = new ArrayList<>();
		if (obj.containsKey(key)) {
			JsonArray childrenIds = obj.getJsonArray(key);
			for (JsonValue cid : childrenIds) {
				JsonNumber id = (JsonNumber) cid;
				ints.add(id.intValue());
			}
		}
		return ints;
	}

	private static String getString(JsonObject obj, String key) {
		if (obj.containsKey(key)) {
			return obj.getString(key);
		}
		return null;
	}
	
	public static Map<Integer, Node> parseString(String s) throws Exception {
		List<JsonEntry> entries = parseWithApi(s);
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
		for (JsonEntry entry : entries) {
			nodes.put(entry.getId(), new Node(entry, entry.getId(), entry.getChildrenIds()));
		}
		return nodes;
	}

	private static void appendKey(StringBuilder buf, Object key) {
		buf.append('"');
		buf.append(key);
		buf.append("\": ");
	}
	private static void appendComma(StringBuilder buf, boolean comma) {
		if (comma) {
			buf.append(", ");
		}
	}
	private static void appendIntList(StringBuilder buf, boolean comma, Object key, Collection<Integer> vals) {
		if (vals == null || vals.isEmpty()) {
			return;
		}
		appendComma(buf, comma);
		appendKey(buf, key);
		buf.append("\"");
		List<Integer> list = new ArrayList<>(vals);
		Collections.sort(list);
		String cids = EntryUtilities.CRUNCHER.toString(vals);
		buf.append(cids);
		buf.append("\"");
	}
	private static void appendStringList(StringBuilder buf, boolean comma, Object key, Collection<String> vals) {
		if (vals == null || vals.isEmpty()) {
			return;
		}
		appendComma(buf, comma);
		appendKey(buf, key);
		buf.append("[");
		boolean first = true;
		for (String o : vals) {
			if (!first) {
				buf.append(", ");
			} else {
				first = false;
			}
			appendValue(buf, o);
		}
		buf.append("]");
	}
	private static void append(StringBuilder buf, boolean comma, Object key, Object val) {
		if (val == null) {
			// in this case we don't render, as js will see it as undefined
			return;
		}
		appendComma(buf, comma);
		appendKey(buf, key);
		appendValue(buf, val);
	}
	private static void appendBoolean(StringBuilder buf, boolean comma, Object key, boolean val) {
		if (!val) {
			// in this case we don't render, as js will see it as undefined
			return;
		}
		appendComma(buf, comma);
		appendKey(buf, key);
		buf.append("true");
	}
	private static void appendValue(StringBuilder buf, Object val) {
		if (val instanceof Integer) {
			buf.append(val);
		} else {
			buf.append('"');
			buf.append(escape(val));
			buf.append('"');
		}
	}
	public static String escape(Object val) {
		String v = val.toString();
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < v.length(); i++) {
			char c = v.charAt(i);
			if (!asciiEncoder.canEncode(c)) {
				buf.append("\\u");
				String h = Integer.toHexString((int) c);
				for (int j = 0; j < 4 - h.length(); j++) {
					buf.append('0');
				}
				buf.append(h);
			} else if (c == '"') {
				buf.append('\\');
				buf.append('"');
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	public static String toJsonString(List<ExampleGroup> groups, Map<Integer, Entry> entriesForImages) {
		StringBuilder buf = new StringBuilder();
		buf.append("{");
		appendKey(buf, "groups");
		buf.append("[");
		
		boolean firstGroup = true;
		for (ExampleGroup eg : groups) {
			if (!firstGroup) {
				buf.append(",");
			} else {
				firstGroup = false;
			}
			buf.append("\n{");
			append(buf, false, "caption", eg.getCaption());
			buf.append(", ");
			appendKey(buf, "examples");
			buf.append("[");
			boolean firstExample = true;
			for (Example ex : eg.getExamples()) {
				if (!firstExample) {
					buf.append(",");
				} else {
					firstExample = false;
				}
				buf.append("\n{");
				append(buf, false, "file", ex.getSimpleTitle());
				append(buf, true, "caption", ex.getCaption());
				// "6e/Hippotion rafflesii rafflesii.jpg"
				Entry e = entriesForImages.get(ex.getDepictedImage().getEntryId());
				ImageInfo ii = ImagesWorker.toImageInfo(e);
				append(buf, true, "image", ii.getFilePathRelative());
				append(buf, true, "width", ex.getDepictedImage().getPreviewWidth());
				append(buf, true, "height", ex.getDepictedImage().getPreviewHeight());
				buf.append("}");
			}
			buf.append("]}");
		}
		buf.append("]}");
		
		return buf.toString();
	}
	
	public static String toJsonString(Collection<JsonEntry> entries) {
		boolean firstEntry = true;
		StringBuilder buf = new StringBuilder("{\"entries\": [");
		for (JsonEntry e : entries) {
			if (!firstEntry) {
				buf.append(",\n");
			}
			firstEntry = false;
			buf.append('{');
			append(buf, false, "id", e.getId()); // first is always no comma, and id is always there
			
			appendBoolean(buf, true, "pinned", e.isPinned());
			appendStringList(buf, true, "cnames", e.getCnames());
			append(buf, true, "lname", e.getLname());
			append(buf, true, "parentId", e.getParentId());
			append(buf, true, "rank", e.getRank());
			if (e.isExtinct()) {
				String extinct = "true";
				if (!e.isAncestorExtinct()) {
					extinct = "top";
				}
				append(buf, true, "extinct", extinct);
			}
	
			if (e.getImg() != null) {
				String latinName = e.getImg();
				latinName = ImagesWorker.getCleanLatinName(latinName);

				append(buf, true, "img", e.getImg());
				append(buf, true, "tHeight", e.gettHeight());
				append(buf, true, "tWidth", e.gettWidth());
				append(buf, true, "pHeight", e.getpHeight());
				append(buf, true, "pWidth", e.getpWidth());
				append(buf, true, "dHeight", e.getdHeight());
				append(buf, true, "dWidth", e.getdWidth());
//				append(buf, true, "iLink", e.getWikiSpeciesLink());
				append(buf, true, "imgData", e.getImgData());
			}
	
			appendIntList(buf, true, "childrenIds", e.getChildrenIds());
			
			appendIntList(buf, true, "showMoreLeafIds", e.getShowMoreLeafIds());
			appendIntList(buf, true, "showMoreOtherIds", e.getShowMoreOtherIds());

			buf.append("}");
		}
		buf.append("]}");
		return buf.toString();
	}

	public static int getSubFolder(int id) {
		double d = id;
		d = d / 100d;
		d = Math.ceil(d);
		int i = (int) d;
		return i;
	}
	public static void saveByFolders(String subFolder, String name, List<JsonEntry> entries) throws Exception {
		// convention because javascript is tricky this way
//		String subfolder;
//		if (isName) {
//			subfolder = "f";
//		} else {
//			int id = Integer.parseInt(name);
//			subfolder = "n/" + String.valueOf(getSubFolder(id));
//		}
		String fileName = subFolder + "/" + name + ".json";
		saveByFileName(fileName, entries);
	}
	public static void saveByFileName(String fileName, List<JsonEntry> entries) throws Exception {
		String json = JsonFileUtils.toJsonString(entries);
		
		System.out.println(json);
		String folder = outputDir + "\\" + fileName;
		
		File file = new File(folder);
		FileUtils.writeStringToFile(file, json, Charset.defaultCharset());
	}

	public static void copyAdditionalJsonResources() throws Exception {
		FileUtils.copyDirectory(
				new File(additionalResourcesDir), 
				new File(outputDir));
	}

	public static void deleteJsonDir() throws Exception {
		System.out.println(">deleteJsonDir");
		FileUtils.deleteDirectory(new File(outputDir));
		System.out.println("<deleteJsonDir");
	}
	public static JsonEntry toJsonEntry(Entry e, Entry linkedImageEntry, SpeciesService speciesService) {
		JsonEntry je = new JsonEntry();
		je.setId(e.getId());
		je.setCnames(getCommonNames(e));
		je.setLname(e.getLatinName());
		je.setParentId(e.getParentId());
		je.setExtinct(e.isExtinct());
		je.setAncestorExtinct(e.isAncestorExtinct());
		je.setRank(e.getRank().getCommonName());
		je.setPinned(e.isPinned());

		// TODO research why some of these have one but not the other
		if (e.getImage() != null && e.getImageLink() != null) {
			je.settHeight(e.getImage().getTinyHeight());
			je.settWidth(e.getImage().getTinyWidth());
			je.setpHeight(e.getImage().getPreviewHeight());
			je.setpWidth(e.getImage().getPreviewWidth());
			je.setdHeight(e.getImage().getDetailHeight());
			je.setdWidth(e.getImage().getDetailWidth());

			
			Entry imageEntry = e;
			if (linkedImageEntry != null) {
				imageEntry = linkedImageEntry;
			}
			
			ImageInfo ii = ImagesWorker.toImageInfo(imageEntry);
			// "6e/Hippotion rafflesii rafflesii.jpg"
			je.setImg(ii.getFilePathRelative());
			je.setWikiSpeciesLink(ii.getUrlBasePath());
			String localImageFullPath = ii.getFilePath(ImagesWorker.TINY); // ImagesCreater.LOCAL_STORAGE_DIR + "/tiny/" + e.getImage().getImagePathPart();
			String data = createImageDataString(localImageFullPath);
			je.setImgData(data);
		}
		je.setChildrenIds(new ArrayList<Integer>(getChildrenIds(e, speciesService)));
		Collections.sort(je.getChildrenIds()); // for indexing in js
		
		ShowMore showMoreIds = getShowMoreIds(e, speciesService);
		je.setShowMoreLeafIds(showMoreIds.leafIds);
		je.setShowMoreOtherIds(showMoreIds.otherIds);
		
		return je;
	}
	private static Collection<Integer> getChildrenIds(Entry e, SpeciesService speciesService) {
		List<Integer> ids = new ArrayList<>();
		List<Entry> someChildren = speciesService.findChildren(e.getId());
		for (Entry s : someChildren) {
			// these won't be boring - it already filters those out with the query
			ids.add(s.getId());
		}
		return ids;
	}


	private static List<String> getCommonNames(Entry e) {
		List<String> cnames = e.getCommonNames();
		if (cnames == null) {
			cnames = new ArrayList<>();
		}
		if (cnames.isEmpty() && e.getCommonName() != null) {
			cnames.add(e.getCommonName());
		}
		return cnames;
	}

	/**
	 * Right now, these "tiny" images are actually much larger than how I'm rendering them.
	 * I should either get wikicommons to resize them all for me (could download "tiny" and "embedded size") or resize here
	 */
	public static String createImageDataString(String localImagePath) {
		File file = new File(localImagePath);
		byte[] bytes;
		try {
			bytes = IOUtils.toByteArray(new FileInputStream(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String encoded = Base64.getEncoder().encodeToString(bytes);
		return encoded;
	}
	/**
	 * This will have to include the intermediate nodes too, because of the way this works.
	 * In terms of perma-links, not sure how that will work, that can be calculated on the browser.
	 */
	private static class ShowMore {
		List<Integer> leafIds;
		List<Integer> otherIds;
	}
	/**
	 * Note that this might not match the way the Java app works, but here are the rules
	 * - The Show More Leaf Count refers only to leaves
	 * - The Show More Others is all other necessary intermediate nodes
	 * - this is so JS can calculate the number, and also load them all when needed
	 */
	private static ShowMore getShowMoreIds(Entry e, SpeciesService speciesService) {
		ShowMore showMore = new ShowMore();
		CrunchedIds cids = e.getInterestingCrunchedIds();
		if (cids == null) {
			return showMore;
		}
		List<Integer> ids = e.getInterestingCrunchedIds().getIds();
		Entry root = speciesService.findTreeForNodes(new HashSet<>(ids));
		Entry branch = EntryUtilities.findEntry(root, e.getId());
		Set<Integer> allBranchIds = EntryUtilities.getIds(branch);
		allBranchIds.remove(e.getId());
		
		Collection<Integer> leafIds = EntryUtilities.getLeavesIds(branch);
		
		showMore.leafIds = new ArrayList<>(leafIds);
		showMore.otherIds = new ArrayList<>(allBranchIds);
		showMore.otherIds.removeAll(leafIds);
		
		return showMore;
	}

	
	public static void outputNamesListJsonFile(List<String> names, String subDirAndFileName) throws Exception {
		StringBuilder buf = new StringBuilder();
		for (String name : names) {
			if (buf.length() == 0) {
				buf.append("{\"files\":[");
			} else {
				buf.append(",\n");
			}
			buf.append("\"");
			buf.append(name);
			buf.append("\"");
		}
		buf.append("]}");
		
		FileUtils.writeStringToFile(new File(outputDir + subDirAndFileName), buf.toString(), Charset.defaultCharset());
	}
	public static Map<Integer, Entry> getLinkedImageEntries(Collection<Entry> entries, SpeciesService speciesService) {
		Map<Integer, Entry> entriesForImages = new HashMap<Integer, Entry>();

		for (Entry e : entries) {
			if (e.getImage() != null) {
				Entry imageEntry = speciesService.findEntry(e.getImage().getEntryId());
				entriesForImages.put(imageEntry.getId(), imageEntry);
			}
		}
		
		return entriesForImages;
	}
	
	public static List<JsonEntry> toJsonEntries(Collection<Entry> entries, Map<Integer, Entry> linkedImageEntries, SpeciesService speciesService) {
		List<JsonEntry> jentries = new ArrayList<>();
		for (Entry e : entries) {
			Entry i = linkedImageEntries.get(e.getImage().getEntryId());
			JsonEntry je = JsonFileUtils.toJsonEntry(e, i, speciesService);
			jentries.add(je);
		}
		return jentries;
	}
}
