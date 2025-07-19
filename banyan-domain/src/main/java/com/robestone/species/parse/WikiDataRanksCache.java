package com.robestone.species.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.robestone.species.Rank;

/**
 * The Q files for ranks didn't download as part of the dump, so we need to manage a way to get those all filled in.
 */
public class WikiDataRanksCache {

	public static void main(String[] args) {
		File f = new File("D:\\banyan\\caches\\wikidata\\Q");
		countFiles(f);
	}
	public static int countFiles(File f) {
		System.out.println(f.getAbsolutePath() + ".>");
		int count = 0;
		
		File[] children = f.listFiles();
		System.out.println(f.getAbsolutePath() + ".children." + children.length);
		for (File c : children) {
			if (!c.getName().endsWith(".json")) {
				count += countFiles(c);
			} else {
				count++;
			}
		}
		
		System.out.println(f.getAbsolutePath() + "." + count);
		return count;
	}
	
	private static final String RANKS_FILE = WikiDataParser.WikiDataCache.WIKIDATA_LOCAL_PATH + "/WikiDataRanks.txt";
	private static final char SPLIT_TOKEN = '|';
	
	// QID to Rank
	private Map<String, RankHolder> ranksCache = new HashMap<>();
	private boolean autoFlush = true;
	
	private static class RankHolder {
		Rank rank;
		String description;
		RankHolder(Rank rank, String description) {
			this.rank = rank;
			this.description = description;
		}
	}
	
	public WikiDataRanksCache() {
		/* don't worry about this until I actually have them to add - the cache file is fine for now
		for (Rank rank : Rank.values()) {
			if (rank.getQid() != null) {
				ranksCache.put(rank.getQid(), rank);
			}
		}
		*/
	}
	
	public void loadRanks() throws Exception {
		List<String> lines = IOUtils.readLines(new FileInputStream(RANKS_FILE), Charset.defaultCharset());
		for (String line : lines) {
			String[] split = StringUtils.split(line, SPLIT_TOKEN);
			String qid = split[0];
			RankHolder rank = ranksCache.get(qid);
			if (rank == null) {
				Rank r = Rank.valueOfWithAlternates(split[1]);
				rank = new RankHolder(r, split[2]);
				ranksCache.put(qid, rank);
			}
		}
	}
	public void writeRanks() throws Exception {
		List<String> lines = new ArrayList<>();
		for (String qid : ranksCache.keySet()) {
			RankHolder rank = ranksCache.get(qid);
			String line = qid + SPLIT_TOKEN + rank.rank.name() + SPLIT_TOKEN + rank.description;
			lines.add(line);
		}
		IOUtils.writeLines(lines, "\n", new FileOutputStream(RANKS_FILE), Charset.defaultCharset());
	}
	
	/**
	 * @param qID can be either the QID or the rank name
	 */
	public Rank getRank(String qID) throws Exception {
		RankHolder rank = ranksCache.get(qID);
		if (rank == null) {
			rank = readRank(qID);
			if (rank == null) {
				return null;
			}
			ranksCache.put(qID, rank);
			// if we had to get from the file, add to the cache file
			if (autoFlush) {
				writeRanks();
			}
		}
		return rank.rank;
	}
	private RankHolder readRank(String qID) throws Exception {
		String file = WikiDataParser.WikiDataCache.CACHE.readFile(qID, false);
		JsonObject obj = WikiDataParser.parseToRootObject(file, qID);
		
		if (obj == null) {
			return null;
		}
		
		JsonObject labels = obj.getJsonObject("labels");
		JsonObject enLabel = labels.getJsonObject("en");
		String value = enLabel.getJsonString("value").getString();
		
		Rank rank = Rank.valueOfWithAlternates(value);
		
		JsonObject descriptions = obj.getJsonObject("descriptions");
		JsonObject enDescription = descriptions.getJsonObject("en");
		String description = enDescription.getJsonString("value").getString();
		
		RankHolder holder = new RankHolder(rank, description);
		
		return holder;
	}

}
