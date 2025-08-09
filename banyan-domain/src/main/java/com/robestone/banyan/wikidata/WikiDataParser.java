package com.robestone.banyan.wikidata;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.robestone.banyan.taxons.Rank;
import com.robestone.banyan.util.DerbyDataSource;
import com.robestone.banyan.workers.AbstractSiteFileCache;
import com.robestone.banyan.workers.ImagesWorker;

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
//		p.crawlLocalFile(new File("D:\\banyan\\caches\\wikidata\\Q\\3b\\7b\\Q25522.json").toPath(), "Q25522");
		p.findAndFixBrokenParents();
	}
	
	private static WikiDataService wdService;
	private WikiDataRanksCache ranksCache = new WikiDataRanksCache();
	private Set<String> qidsInDB = new HashSet<>();
	
	public void findAndFixBrokenParents() throws Exception {
		
		// find all parent qids without a row
		List<String> qids = wdService.findAllTaxonQidsWithBrokenParentTaxonId();
		Set<String> qset = new HashSet<>(qids);
		for (String parentQid : qset) {
			WdTaxon wdParentTaxon = wdService.findWdTaxon(parentQid);
			if (wdParentTaxon == null) {
				System.out.println("findBrokenParents.wdParentTaxon." + parentQid);
				File file = WikiDataParser.WikiDataCache.CACHE.getFile(parentQid);
				if (file.exists()) {
					String page = readTaxonOnlyToString(file.toPath());
					if (page == null) {
						System.out.println("findBrokenParents.wdParentTaxon.CANT_READ_CONTENT." + file);
					} else {
						// these will start parsing as I fix bugs in the parsing logic
						System.out.println("findBrokenParents.wdParentTaxon.FIXING." + file);
						try {
							visitQFileFromLocal(page, parentQid, false);
						} catch (Exception e) {
							// these are tricker, we will just keep moving for now
							e.printStackTrace();
						}
					}
				} else {
					System.out.println("findBrokenParents.wdParentTaxon.NO_FILE." + file);
				}
			}
		}
	}

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
		System.out.println("crawlLocalCache.findAllTaxonQids.taxons." + qidsInDB.size());
		qidsInDB.addAll(wdService.findAllNonTaxonQids());
		System.out.println("crawlLocalCache.findAllTaxonQids.all." + qidsInDB.size());
		
		crawlLocalCache(new File(WikiDataCache.WIKIDATA_LOCAL_PATH + "/Q").toPath());
	}
	private void crawlLocalCache(Path dir) throws Exception {
		
		System.out.println("crawlLocalCache." + dir.toString());
		int count = 0;

		DirectoryStream<Path> ds = Files.newDirectoryStream(dir);	
		Iterator<Path> paths = ds.iterator();
		
		while (paths.hasNext()) {
			Path child = paths.next();
			String name = child.getFileName().toString();
			if (name.endsWith(".json")) {
				String key = name.split("\\.")[0];
				boolean alreadyDone = qidsInDB.contains(key);
				if (!alreadyDone) {
					crawlLocalFile(child, key);
				}
				if (++count % 10000 == 0) {
					System.out.println("crawlLocalCache." + dir.toString() + ".count." + count);
				}
			} else {
				crawlLocalCache(child);
			}
		}
		
		ds.close();
	}
	private void crawlLocalFile(Path file, String qid) throws Exception {
		String doc = readTaxonOnlyToString(file);
		if (doc != null) {
			try {
				visitQFileFromLocal(doc, qid, true);
			} catch (Exception e) {
				System.out.println(qid);
				e.printStackTrace();
			}
		} else {
			wdService.insertNonTaxonQid(qid);
		}
	}

	private void visitQFileFromLocal(String doc, String qid, boolean addNonTaxon) throws Exception {
		WdTaxon taxon = parseQFileFromLocal(doc, qid);
		if (taxon == null) {
			// this is not a taxon file - anything else will throw an exception
			if (addNonTaxon) {
				wdService.insertNonTaxonQid(qid);
			}
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
	
	static {
		
	}
	private static final List<String> TAXON_TOKENS = Arrays.stream(Value.values()).map(v -> v.numericId).collect(Collectors.toList());

	private String readTaxonOnlyToString(Path file) throws Exception {
		
		try (InputStream in = Files.newInputStream(file, StandardOpenOption.READ)) {
		
			// 0. read in some of the file - we know the token is later on, and this simplifies the logic later
			int headLen = 100; // could probably be much higher
			byte[] head = new byte[headLen];
			in.read(head);
			String s = new String(head);
			StringBuilder buf = new StringBuilder(s);

			// we'll repeat this until we don't find another P31 section
			while (true) {
			
				// 1. read until we find "P31"
				String token1 = "\"property\":\"P31\"";
				int found1 = find(in, buf, token1);
				if (found1 < 0) {
					return null;
				}
				
				// 2. read until this part is found (white space will be different)
				/*
				 "property": "P31",
							"datavalue": {
								"value": {
									"entity-type": "item",
									"numeric-id": 16521,
				 */
				String token2 = "\"numeric-id\":";
				int found2 = find(in, buf, token2);
				if (found2 < 0) {
					return null;
				}
				find(in, buf, ",");
	//			int start = buf.length() - token2.length() * 3;
	//			int pos = buf.indexOf(token2, start);
				int comma = buf.indexOf(",", found2);
				String key = buf.substring(found2 + token2.length(), comma);
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
					
					return buf.toString();
				} else {
					// we don't return here - we'll loop and try again
//					return null;
				}
			}
		
		}
	}
	private int find(InputStream in, StringBuilder buf, String token) throws Exception {
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
				return found;
			} else if (read < 0) {
				return -1;
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

		/*// I don't think there is any value in this, and it's causing errors as-is
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
		*/
		
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
		Ichnotaxon("Q2568288", "taxon based on the fossilized work of an organism"),
		Candidatus("Q857968", "indication in bacteriological nomenclature"),
		MonotypicFossilTaxon("Q47487597", "monotypic fossil taxon"),
		MonotypicTaxon("Q310890", "monotypic taxon"),
		Taxon("Q16521", "taxon"),
		FossilTaxon("Q23038290", "fossil taxon"),
		ExtinctTaxon("Q98961713", "extinct taxon"),
		MontageImage("Q310890", "monotypic taxon"),
		Clade("Q713623", "group of a common ancestor and all descendants")
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
			String hash1 = ImagesWorker.getImagePathHashed(pageKey);
			String part = hash1 + "/" + pageKey;
			String hash2 = ImagesWorker.getImagePathHashed(part);
			String left = pageKey.substring(0, 1);
			return left + "/" + hash1 + "/" + hash2;
		}
		
		@Override
		protected String toUrl(String pageKey) {
			return "https://www.wikidata.org/wiki/Special:EntityData/" + pageKey + ".json";
		}
	}
	
}
