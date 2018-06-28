package com.robestone.species;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import com.robestone.util.html.EntityMapper;

/**
 * TODO or think about
 * "contains" - should be better than fuzzy, but the WildcardQuery docs say starting with * will
 * 	be very slow.  Need to test, because technically that should still be faster than fuzzy.
 * match on id
 * match on crunched id
 * match on latin stemming - would be in place of or in addition to "plural variation" for latin
 * if search is "Funk House" then we should actually run first all queries looking for that phrase
 * 	and then run n ^ x queries matching the "product" of each term.  I.e. "funk + house", "funks + house"
 *  - phrase queries - used as an alternative to that "product" strategy
 * 
 * @author jacob
 */
public class LuceneSearcher implements EntrySearcher {

	public static final String LATIN = "latin_name";
	public static final String COMMON = "common_name";
	public static final String ID = "id";
	static final String LATIN_NOTOKEN = "latin_name_notoken";
	static final String COMMON_NOTOKEN = "common_name_notoken";
	private static final IdCruncher CRUNCHER = IdCruncher.R26_4;

	static enum MatchType {
		Exact, StartsWith, Phrase, 
			Fuzzy9(.9f), Fuzzy8(.8f),  
			Contains, 
			Fuzzy7(.7f), Fuzzy6(.6f), Fuzzy5(.5f) ;
		
		public final float fuzzy;
		MatchType(float fuzzy) {
			this.fuzzy = fuzzy;
		}
		MatchType() {
			this(0);
		}
		public boolean isFuzzy() {
			return fuzzy > 0;
		}
	}

	static class SearchType {
		MatchType matchType;
		boolean isTokens;
		boolean isPlural;
		boolean isLatin;
		public SearchType(MatchType matchType, boolean isTokens,
				boolean isPlural, boolean isLatin) {
			this.matchType = matchType;
			this.isTokens = isTokens;
			this.isPlural = isPlural;
			this.isLatin = isLatin;
		}
	}

	private static List<SearchType> createSearchTypes() {
		List<SearchType> types = new ArrayList<SearchType>();
		
		boolean[] isLatin = { false, true };
		boolean[] isPlural = { false, true };
		boolean[] isTokens = { false, true };
		
		for (MatchType matchType: MatchType.values()) {
			for (boolean tokens: isTokens) {
				for (boolean plural: isPlural) {
					for (boolean latin: isLatin) {
						SearchType type = new SearchType(matchType, tokens, plural, latin);
						types.add(type);
					}
				}
			}
		}
		
		return types;
	}
	
	private CommonNameSplitter commonNameSplitter = new CommonNameSplitter();
	private List<SearchType> searchTypes = createSearchTypes();
	private PluralMaker pluralMaker = new PluralMaker();
	private Analyzer analyzer = new StandardAnalyzer();
	private IndexSearcher searcher;
	private String indexDir;

	/**
	 * @param speciesService For building the index
	 */
	public LuceneSearcher(ISpeciesService speciesService) {
		this(speciesService, null);
	}
	public LuceneSearcher(ISpeciesService speciesService, String indexDir) {
		List<CompleteEntry> entries = speciesService.findEntriesForLuceneIndex();
		LogHelper.speciesLogger.info("LuceneSearcher.entries." + entries.size());
		this.indexDir = indexDir;
		buildIndex(entries);
	}
	public LuceneSearcher(Collection<? extends Entry> entries, String indexDir) {
		this(entries, false, indexDir);
	}
	public LuceneSearcher(Collection<? extends Entry> entries, boolean isTesting) {
		this(entries, isTesting, null);
	}
	public LuceneSearcher(Collection<? extends Entry> entries, boolean isTesting, String indexDir) {
		this.indexDir = indexDir;
		buildIndex(entries);
	}
	private void buildIndex(Collection<? extends Entry> entries) {
		// sort so we have predictable index - don't know if this matters...
		List<Entry> list = new ArrayList<Entry>(entries);
		Collections.sort(list, new EntryComparator());
		try {
			doBuildIndex(list);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private static String defaultWindowsPath = "D:\\banyan-db\\lucene";
	private static String defaultLinuxPath = "/home/private/banyan-lucene";
	private File getDirectory() {
		String fileName = indexDir;
		if (fileName == null) {
			fileName = System.getProperty("banyan.lucene.dir");
		}
		if (fileName == null) {
			fileName = defaultWindowsPath;
		}

		if (!new File(fileName).exists()) {
			fileName = defaultLinuxPath;
		}
		File file = new File(fileName);
		LogHelper.speciesLogger.info("getDirectory." + file.getAbsolutePath());
		return file;
	}
	private void doBuildIndex(Collection<? extends Entry> entries) throws IOException {
		Directory directory =  NIOFSDirectory.open(getDirectory().toPath()); // FSDirectory.getDirectory(getDirectory()); // new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(directory, config);
		int count = 0;
		for (Entry entry: entries) {
			Document doc = buildDocument(entry);
//			LogHelper.speciesLogger.info(EntryComparator.getCompareName(entry) + " =>" + doc);
			writer.addDocument(doc);
			count++;
		}
		LogHelper.speciesLogger.info("doBuildIndex." + count);
		// This article recomends not to use this option, and it's been removed
		// https://stackoverflow.com/questions/14599297/lucene-migration-from-3-x-to-4-1-0-and-index-optimisation
//		writer.optimize();
		writer.close();

		IndexReader reader = DirectoryReader.open(directory);  
		searcher = new IndexSearcher(reader);
	}
	protected Document buildDocument(Entry entry) {
		List<String> commonNames = createCommonNames(entry);
		Document doc = buildDocument(commonNames, entry.getLatinName(), entry.getId());
		return doc;
	}
	protected Document buildDocument(List<String> commonNames, String latinName, Integer entryId) {
		Document doc = new Document();
		
		String id = toQueryId(entryId);
		
		FieldType notAnalyzed = new FieldType(StringField.TYPE_STORED);
		notAnalyzed.setOmitNorms(false);
		FieldType analyzed = new FieldType(StringField.TYPE_STORED);
		
		/*
		 https://lucene.apache.org/core/4_0_0/MIGRATE.html
		 OLD: new Field("field", "value", Field.Store.YES, Field.Indexed.NOT_ANALYZED)
		 NEW:
		  	FieldType ft = new FieldType(StringField.TYPE_STORED);
			ft.setOmitNorms(false);
			new Field("field", "value", ft)
		 
		 */
		
		doc.add(new Field(ID, id, notAnalyzed));
		
		latinName = normalize(latinName);

		doc.add(new Field(LATIN, latinName, analyzed));
		doc.add(new Field(LATIN_NOTOKEN, latinName, notAnalyzed));
		
		if (commonNames != null) {
			for (String commonName: commonNames) {
				commonName = normalize(commonName);
				if (StringUtils.isEmpty(commonName)) {
					continue;
				}
				doc.add(new Field(COMMON, commonName, analyzed));
				doc.add(new Field(COMMON_NOTOKEN, commonName, notAnalyzed));
			}
			doc.add(new Field(COMMON_NOTOKEN, StringUtils.join(commonNames, " "), notAnalyzed));
		}
//		LogHelper.speciesLogger.info("buildDocument." + doc);
		return doc;
	}
	protected String toQueryId(Integer id) {
		return CRUNCHER.toString(id);
	}
	protected Integer toEntryId(String id) {
		return CRUNCHER.toInt(id);
	}
	protected String normalize(String s) {
		if (s == null) {
			return "";
		}
		s = s.toLowerCase();
		s = EntityMapper.convertToSearchText(s, ' ');
		s = EntryUtilities.getClean(s, false);
		s = s.trim().toLowerCase();
		
		StringBuilder buf = new StringBuilder();
		String[] split = s.split(" ");
		for (String one: split) {
			if (buf.length() > 0 && buf.charAt(buf.length() - 1) != ' ') {
				buf.append(" ");
			}
			if (one.length() > 2) {
				buf.append(one);
			}
		}
		
		s = buf.toString();
		return s;
	}

	public Query buildQuery(String queryString, SearchType searchType) {
		if (searchType.isPlural) {
			queryString = pluralMaker.getPluralAlternate(queryString);
		}
		
		String fieldName;
		if (searchType.isLatin) {
			if (searchType.isTokens) {
				fieldName = LATIN;
			} else {
				fieldName = LATIN_NOTOKEN;
			}
		} else {
			if (searchType.isTokens) {
				fieldName = COMMON;
			} else {
				fieldName = COMMON_NOTOKEN;
			}
		}
		
		Query query;
		if (searchType.matchType == MatchType.Phrase) {
			// some searches won't work with phrase or are redundant
			// TODO we've already built the plural alternative, and now we say don't bother...
			if (!searchType.isTokens || searchType.isPlural) {
				return null;
			}
			query = buildPhraseQuery(fieldName, queryString, analyzer);
		} else if (searchType.matchType == MatchType.Contains) {
			if (!searchType.isTokens) {
				// don't bother searching both tokens and non-tokens for contains
				return null;
			}
			// TODO performance tests
			Term term = new Term(fieldName, "*" + queryString + "*");
			query = new WildcardQuery(term);
		} else {
			Term term = new Term(fieldName, queryString);
			
			if (searchType.matchType == MatchType.Exact) {
				query = new TermQuery(term);
			} else if (searchType.matchType.isFuzzy()) {
				// the API changed to be based on # of edits, and has
				// a preferred default setting
				query = new FuzzyQuery(term); // , searchType.matchType.fuzzy, 0);
			} else if (searchType.matchType == MatchType.StartsWith) {
				query = new PrefixQuery(term);
			} else {
				return null; // TODO add the rest of the types
			}
		}
		
		return query;
	}
	public static Query buildPhraseQuery(String fieldName, String queryString, Analyzer analyzer) {
		// TODO this looks pretty ridiculous -- need to validate
		try {
			QueryParser parser = new QueryParser(fieldName, analyzer);
			queryString = "\"" + queryString + "\"~3";
			Query query = parser.parse(queryString);
			return query;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	public int search(String queryString, Collection<Integer> existingIds) {
		// TODO normalize the queryString
		for (SearchType searchType: searchTypes) {
			Query baseQuery = buildQuery(queryString, searchType);
			if (baseQuery == null) {
				continue;
			}
			int found = search(baseQuery, existingIds);
			if (found >= 0) {
				return found;
			}
		}
		return -1;
	}
	private int search(Query baseQuery, Collection<Integer> existingIds) {
		BooleanQuery.Builder queryBuilder = buildExistingIdsSubQuery(existingIds);
		queryBuilder.add(baseQuery, BooleanClause.Occur.MUST);
		try {
			TopDocs rs = searcher.search(queryBuilder.build(), 1);
			if (rs.totalHits == 0) {
				return -1;
			}
			ScoreDoc[] hits = rs.scoreDocs;
			Document foundDoc = searcher.doc(hits[0].doc);
			String rawId = foundDoc.get(ID);
			Integer id = toEntryId(rawId);
			return id;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	protected BooleanQuery.Builder buildExistingIdsSubQuery(Collection<Integer> existingIds) {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		for (Integer id: existingIds) {
			queryBuilder.add(new TermQuery(new Term(ID, toQueryId(id))), BooleanClause.Occur.MUST_NOT);
		}
		return queryBuilder;
	}
	public List<String> createCommonNames(Entry entry) {
		if (entry.getCommonName() == null) {
			return null;
		} else {
			List<String> list = commonNameSplitter.splitCommonName(entry, -1);
			if (list == null) {
				list = new ArrayList<String>();
				list.add(entry.getCommonName());
			}
			Collections.sort(list); // predictable document
			return list;
		}

	}

}
