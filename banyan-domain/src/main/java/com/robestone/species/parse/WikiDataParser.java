package com.robestone.species.parse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.robestone.species.DerbyDataSource;
import com.robestone.species.Rank;
import com.robestone.species.WdImage;
import com.robestone.species.WdTaxon;
import com.robestone.species.WikiDataService;

public class WikiDataParser {

	public static void main(String[] args) throws Exception {
//		String text = IOUtils.toString(new FileInputStream("D:\\banyan\\caches\\wikidata\\test.json"), Charset.defaultCharset());
//		parseQFile(text, "Q12198609");
		
		WikiDataParser p = new WikiDataParser();
//		p.crawlToCache("Q12198609");
//		p.splitAllJsonDownloadBz2File();
//		p.crawlLocalCache();
		
		WikiDataParser.wdService = new WikiDataService();
		wdService.setDataSource(DerbyDataSource.getDataSource());
		
		p.ranksCache.loadRanks();
//		p.parseQFileFromLocal("Q131092");
		p.crawlLocalCache();
//		p.fixLatinNames();
	}
	
	private static WikiDataService wdService;
	private WikiDataRanksCache ranksCache = new WikiDataRanksCache();
	private Set<String> qidsInDB = new HashSet<>();
	
	/**
	 * Just adding them in later, just needs to be done once.
	 */
	public void fixLatinNames() throws Exception {
		List<String> qids = wdService.findAllTaxonQids();
		for (String qid : qids) {
			String doc = WikiDataCache.CACHE.readLocalFile(qid);
			try {
				WdTaxon taxon = parseQFileFromLocal(doc, qid);
				wdService.updateTaxon(qid, taxon.getLatinName());
			} catch (Exception e) {
				System.out.println(qid);
				e.printStackTrace();
			}
		}
	}
	
	public void crawlLocalCache() throws Exception {
		qidsInDB.addAll(wdService.findAllTaxonQids());
		qidsInDB.addAll(wdService.findAllNonTaxonQids());
		
		crawlLocalCache(new File(WikiDataCache.WIKIDATA_LOCAL_PATH + "/Q"));
	}
	private void crawlLocalCache(File dir) throws Exception {
		File[] childrenArray = dir.listFiles();
		List<File> childrenList = Arrays.asList(childrenArray);
		Collections.shuffle(childrenList);
		
		for (File child : childrenList) {
			if (child.isFile()) {
				String key = child.getName();
				key = key.split("\\.")[0];
				boolean alreadyDone = qidsInDB.contains(key);
				if (!alreadyDone) {
					String doc = readTaxonOnlyToString(child);
					if (doc != null) {
						try {
							visitQFileFromLocal(doc, key);
						} catch (Exception e) {
							System.out.println(key);
							e.printStackTrace();
						}
					} else {
						wdService.insertNonTaxonQid(key);
					}
				}
			} else {
				crawlLocalCache(child);
			}
		}
	}
	private void visitQFileFromLocal(String doc, String qid) throws Exception {
		WdTaxon taxon = parseQFileFromLocal(doc, qid);
		if (taxon == null) {
			// this is not a taxon file - anything else will throw an exception
			wdService.insertNonTaxonQid(qid);
		} else {
			wdService.insertTaxon(taxon);
			for (WdImage image : taxon.getImages()) {
				wdService.insertImage(image);
			}
		}
	}
	private WdTaxon parseQFileFromLocal(String doc, String qid) throws Exception {
		JsonObject obj = parseToRootObject(doc, qid);
		
		JsonObject claims = obj.getJsonObject("claims");
		
		String valueProp = "id";
		
		String latinName = getSnakDataValue(claims, Property.LatinName, null);
		if (latinName == null) {
			return null;
		}
		
		String instanceOfId = getSnakDataValue(claims, Property.InstanceOf, valueProp);
		String rankString = getSnakDataValue(claims, Property.Rank, valueProp);
		if (rankString == null) {
			// this happens in things like Viruses
			rankString = instanceOfId;
		}
		
		Rank rank;
		try {
			rank = ranksCache.getRank(rankString);
		} catch (Exception e) {
			System.out.println(qid);
			e.printStackTrace();
			throw e;
		}
		
		String parentId = getSnakDataValue(claims, Property.ParentTaxon, valueProp);
		boolean extinct = isExtinct(instanceOfId);
		
		List<WdImage> images = getImagesFromClaims(claims);
		
		String common = getCommonName(claims);
		
		System.out.printf("%s: latinName: %s; rank: %s; parent: %s; common: %s; images: %s\n", qid, latinName, rankString, parentId, common, images);
		
		WdTaxon taxon = new WdTaxon();
		taxon.setQid(qid);
		taxon.setParentQid(parentId);
		taxon.setCommonName(common);
		taxon.setLatinName(latinName);
		taxon.setExtinct(extinct);
		taxon.setRank(rank);
		taxon.setImages(images);
		
		for (WdImage image : images) {
			image.setQid(qid);
		}
		
		return taxon;
	}
	public static JsonObject parseToRootObject(String doc, String expectdEtityQid) throws Exception {
		JsonReader rdr = Json.createReader(new ByteArrayInputStream(doc.getBytes()));
		JsonObject obj = rdr.readObject();
		
		JsonObject entities = obj.getJsonObject("entities");
		// Web docs have a wrapper
		if (entities != null) {
			JsonObject entity = entities.getJsonObject(expectdEtityQid);
			obj = entity;
		}
		
		return obj;
	}
	
	private static final String[] TAXON_TOKENS = {	
			Value.MonotypicTaxon.getNumericId(),
			Value.Taxon.getNumericId(),
			Value.FossilTaxon.getNumericId(),
			Value.ExtinctTaxon.getNumericId()};

	private int lowestPositionForP31 = Integer.MAX_VALUE;
	
	private String readTaxonOnlyToString(File file) throws Exception {
		
		try (InputStream in = new FileInputStream(file)) {
		
			// 0. read in some of the file - we know the token is later on, and this simplifies the logic later
			int headLen = 100; // could probably be much higher
			byte[] head = new byte[headLen];
			in.read(head);
			String s = new String(head);
			StringBuilder buf = new StringBuilder(s);
			
			// 1. read until we find "P31"
			String token1 = "\"property\":\"P31\"";
			boolean found1 = find(in, buf, token1);
			if (!found1) {
				return null;
			}
			
			// check the position - so we can figure out how far to seek
			int bufLenAtP31 = buf.length();
			
			// 2. read until this part is found (white space will be different)
			/*
			 "property": "P31",
						"datavalue": {
							"value": {
								"entity-type": "item",
								"numeric-id": 16521,
			 */
			String token2 = "\"numeric-id\":";
			boolean found2 = find(in, buf, token2);
			if (!found2) {
				return null;
			}
			find(in, buf, ",");
			int start = buf.length() - token2.length() * 3;
			int pos = buf.indexOf(token2, start);
			int comma = buf.indexOf(",", pos);
			String key = buf.substring(pos + token2.length(), comma);
			boolean matches = false;
			for (String token : TAXON_TOKENS) {
				matches = token.equals(key);
				if (matches) {
					break;
				}
			}
			if (matches) {
				int read;
				while ((read = in.read()) >= 0) {
					buf.append((char) read);
				}
				
				if (bufLenAtP31 < lowestPositionForP31) {
					lowestPositionForP31 = bufLenAtP31;
					System.out.println("lowestPositionForP31." + lowestPositionForP31 + "." + file.getAbsolutePath());
				}
				
				return buf.toString();
			} else {
				return null;
			}
		
		}
	}
	private boolean find(InputStream in, StringBuilder buf, String token) throws Exception {
		int max = token.length();
		while (true) {
			// read in enough that we'll get the token
			int read = -1;
			for (int i = 0; i < max; i++) {
				read = in.read();
				if (read < 0) {
					break;
				}
				buf.append((char) read);
			}
			// check if we got the token
			int found = buf.indexOf(token, buf.length() - max * 2);
			if (found >= 0) {
				return true;
			} else if (read < 0) {
				return false;
			}
		}
	}
	
	private static boolean isExtinct(String instanceOfId) {
		return Value.ExtinctTaxon.qID.equals(instanceOfId)
				|| Value.FossilTaxon.qID.equals(instanceOfId);
	}

	public static String getCommonName(JsonObject claims) {
		JsonArray arr = claims.getJsonArray(Property.CommonName.pID);
		if (arr == null) {
			return null;
		}
		String name = null;
		for (JsonValue one : arr.subList(0, arr.size())) {
			JsonObject oneObj = (JsonObject) one;
			JsonObject valueObj = oneObj.getJsonObject("mainsnak").getJsonObject("datavalue").getJsonObject("value");
			String language = valueObj.getString("language");
			if ("en".equals(language)) {
				name = valueObj.getString("text");
				break;
			}
		}
		return name;
	}
	
	public static List<WdImage> getImagesFromClaims(JsonObject claims) {
		JsonArray arr = claims.getJsonArray(Property.Image.pID);
		final List<WdImage> values = new ArrayList<>();
		if (arr != null) {
			arr.forEach(v -> {
				WdImage image = toImage((JsonObject) v);
				values.add(image);
			});
		}

		return values;
	}
	private static WdImage toImage(JsonObject obj) {
		String name = getSnakDataValue(obj, null, null);
		WdImage image = new WdImage();
		image.setImageLink(name);

		obj = obj.getJsonObject("qualifiers");
		if (obj != null) {
			// we are not equipped to handle multiple depicts in the main DB
			JsonArray arr = obj.getJsonArray(Property.Depicts.pID);
			if (arr != null) {
				obj = arr.getJsonObject(0);
				obj = obj.getJsonObject("datavalue");
				obj = obj.getJsonObject("value");
				String depictsId = obj.getJsonString("id").getString();
				image.setDepictsQid(depictsId);
			}
		}
		
		return image;
	}
	public static String getSnakDataValue(JsonObject obj, Property rootProp, String valueProp) {
		if (rootProp != null) {
			JsonArray arr = obj.getJsonArray(rootProp.pID);
			if (arr == null) {
				return null;
			}
			obj = arr.getJsonObject(0);
		}
		JsonObject mainsnak = obj.getJsonObject("mainsnak");
		JsonObject datavalue = mainsnak.getJsonObject("datavalue");
		if (datavalue == null) {
			return null;
		}
		JsonValue value = datavalue.get("value");
		if (valueProp != null) {
			value = ((JsonObject) value).get(valueProp);
		}
		String valueString = ((JsonString) value).getString();
		return valueString;
	}
	public enum Value {
		MonotypicTaxon("Q310890", "monotypic taxon"),
		Taxon("Q16521", "taxon"),
		MontageImage("Q310890", "monotypic taxon"),
		FossilTaxon("Q23038290", "fossil taxon"),
		ExtinctTaxon("Q98961713", "extinct taxon"),
		;
		
		private String qID;
		private String qName;
		private String numericId;
		
		private Value(String qID, String qName) {
			this.qID = qID;
			this.qName = qName;
			this.numericId = qID.substring(1);
		}
		public String getqID() {
			return qID;
		}
		public String getqName() {
			return qName;
		}
		public String getNumericId() {
			return numericId;
		}
		
	}
	public enum Property {
		
		CommonName("P1843", "taxon common name"), 
		InstanceOf("P31", "instance of"),
		LatinName("P225", "taxon name"),
		Rank("P105", "taxon rank"),
		ParentTaxon("P171", "parent taxon"),
		Image("P18", "image"),
		MontageImage("P2716", "montage image"),
		Depicts("P180", "depicts"),
		;
		
		private String pID;
		private String pName;
		
		private Property(String pID, String pName) {
			this.pID = pID;
			this.pName = pName;
		}
		public String getpID() {
			return pID;
		}
		public String getpName() {
			return pName;
		}
	}
	
	public static class WikiDataCache extends AbstractSiteFileCache {

		public static final String WIKIDATA_LOCAL_PATH = "D:/banyan/caches/wikidata";

		public static final WikiDataCache CACHE = new WikiDataCache(WIKIDATA_LOCAL_PATH, "json");
		
		public WikiDataCache(String localStorageDir, String pageExt) {
			super(localStorageDir, pageExt);
		}

		@Override
		protected String toHash(String pageKey) {
			String hash1 = ImagesCreater.getImagePathHashed(pageKey);
			String part = hash1 + "/" + pageKey;
			String hash2 = ImagesCreater.getImagePathHashed(part);
			String left = pageKey.substring(0, 1);
			return left + "/" + hash1 + "/" + hash2;
		}
		
		@Override
		protected String toUrl(String pageKey) {
			return "https://www.wikidata.org/wiki/Special:EntityData/" + pageKey + ".json";
		}
	}
	
}
