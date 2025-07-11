package com.robestone.species.parse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.robestone.species.LogHelper;
import com.robestone.species.parse.WikiDataParser.Property;
import com.robestone.species.parse.WikiDataParser.WikiDataCache;

public class WikiDataWebUtils {

	private Pattern BZ2_QID_PATTERN = Pattern.compile("\\{\"type\":\"item\",\"id\":\"([QP][0-9]+)\".*");
//	private Pattern BZ2_TAXON_PATTERN = Pattern.compile("\"P31\":\\[\\{\"mainsnak\"\\:{\"snaktype\":\"value\",\"property\":\"P31\",\"datavalue\":\\{\"value\":\\{\"entity-type\":\"item\",\"numeric-id\":[0-9]+,\"id\":\"(Q[0-9]+)\"}");
//	private Pattern BZ2_TAXON_QUICK_PATTERN = Pattern.compile("\"P31\"");
	
	// just checks that the page has a taxon name, which is on every taxon
	private static final String BZ2_TAXON_QUICK_PATTERN = "\"P225\"";
	public void splitAllJsonDownloadBz2File() throws Exception {
		FileInputStream fin = new FileInputStream("D:\\banyan\\caches\\latest-all.json\\latest-all.json");
//		BZip2CompressorInputStream zin = new BZip2CompressorInputStream(fin);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
		int count = 0;
		int skipLines = 15_000_000;
		
		String line;
		while ((line = reader.readLine()) != null) {
			if (count % 1_000 == 0) {
				System.out.println(count + "." + (new Date()) + "." + line);
			}
			count++;
			
			// we skip lines because we already did millions of these
			if (count < skipLines) {
				continue;
			}
			
			// don't persist non-taxon - this is taking too much processing power
			int pos = line.indexOf(BZ2_TAXON_QUICK_PATTERN);
			if (pos < 0) {
				continue;
			}
			
			// get the id - we will still store this file
			Matcher m = BZ2_QID_PATTERN.matcher(line);
			if (m.matches()) {
				String qid = m.group(1);
				if (!WikiDataCache.CACHE.isFilePresent(qid)) {
					CharSequence toWrite = line;
					if (line.endsWith(",")) {
						toWrite = line.subSequence(0, line.length() - 1);
					}
					WikiDataCache.CACHE.writeFile(qid, toWrite);
				}
			}
			
		}
		reader.close();
	}
	
	/**
	 * Don't do anything other than download and cache.  Not checking dates, etc.
	 */
	public void crawlToCache(String startQ) throws Exception {
		Set<String> ids = new HashSet<String>();
		crawlToCache(startQ, ids);
	}
	private void crawlToCache(String startQ, Set<String> allIds) throws Exception {
		
		// download (or pull locally)
		String file = WikiDataParser.WikiDataCache.CACHE.readFile(startQ, false);

		// simple check to see if it is a taxon or not (needs some refinement)
		if (isTaxonFile(file)) {
			
			LogHelper.speciesLogger.debug("recurse." + startQ + "." + allIds.size());
			
			// pull out all Q codes only if it is a taxon
			Set<String> ids = extractQids(file);
			ids.removeAll(allIds);
			allIds.addAll(ids);
			
			for (String id : ids) {
				crawlToCache(id, allIds);
			}
		}
		
	}
	/**
	 * As long as we have the taxon id somewhere in the page, we can recurse, should be minimal overhead
	 */
	private static Pattern IS_TAXON_FILE = Pattern.compile("\"id\"\\s*:\\s*\"(Q16521|Q310890)\"");
	private boolean isTaxonFile(String file) {
		return IS_TAXON_FILE.matcher(file).find();
	}
	private static Pattern QID_PATTERN = Pattern.compile("\"(Q[0-9]+)\"");
	private Set<String> extractQids(String file) {
		Set<String> ids = new HashSet<String>();
		Matcher m = QID_PATTERN.matcher(file);
		while (m.find()) {
			String id = m.group(1);
			ids.add(id);
		}
		return ids;
	}
	
	public static void parseQFileFroMWeb(String doc, String expectdEtityQid) {
		JsonReader rdr = Json.createReader(new ByteArrayInputStream(doc.getBytes()));
		JsonObject obj = rdr.readObject();
		JsonObject entities = obj.getJsonObject("entities");
		
		// in this case there's only one entity
		JsonObject entity = entities.getJsonObject(expectdEtityQid);
		JsonObject claims = entity.getJsonObject("claims");
		
		String valueProp = "id";
		
		String latinName = WikiDataParser.getSnakDataValue(claims, Property.LatinName, null);
		String rank = WikiDataParser.getSnakDataValue(claims, Property.Rank, valueProp);
		String parentId = WikiDataParser.getSnakDataValue(claims, Property.ParentTaxon, valueProp);
		String image = WikiDataParser.getSnakDataValue(claims, Property.Image, null);
		if (image == null) {
			image = WikiDataParser.getSnakDataValue(claims, Property.MontageImage, null);
		}
		
		String common = WikiDataParser.getCommonName(claims);
		
		System.out.printf("latinName: %s; rank: %s; parent: %s; common: %s; image: [%s]\n", latinName, rank, parentId, common, image);
	}
}
