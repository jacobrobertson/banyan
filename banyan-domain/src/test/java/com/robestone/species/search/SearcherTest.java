package com.robestone.species.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.RAMDirectory;

import com.robestone.species.CommonNameSplitter;
import com.robestone.species.CompleteEntry;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.IdCruncher;
import com.robestone.species.LuceneSearcher;
import com.robestone.species.parse.Mocks;

public class SearcherTest extends AbstractSearcherTester {
	
	private LuceneSearcher searcher;
	private SimpleAnalyzer analyzer = new SimpleAnalyzer();
	private Mocks mocks = new Mocks();
	
	@Override
	protected void setUp() throws Exception {
		Set<? extends Entry> entries = EntryUtilities.getEntries(
				(CompleteEntry) mocks.getRoot());
		searcher = new LuceneSearcher(entries, true);
	}
	public void testSplitCommonName() {
		CompleteEntry entry = new CompleteEntry();
		entry.setCommonName("Bowhead Whale, Bowhead, Arctic Whale, Greenland Right Whale");
		entry.setLatinName("Funk");
		List<String> split = new CommonNameSplitter().splitCommonName(entry, 25);
		Set<String> set = new HashSet<String>(split);
		assertFalse(set.contains("Whale,"));
		assertTrue(set.contains("Arctic Whale"));
	}
	public void testSimpleQuery() throws IOException {
		// note that the term value has to be lower case or it doesn't work
		doTestSimpleQuery("snakes", "Serpentes");
		doTestSimpleQuery("grass", "Natrix natrix");
		doTestSimpleQuery("snake", "Natrix natrix");
	}
	private void doTestSimpleQuery(String commonNameQuery, String latinNameExpect) throws IOException {
		Query query = new TermQuery(new Term("common_name", commonNameQuery));
		doTestQuery(query, "latin_name", latinNameExpect);
	}
	public void testSearcherQuery1a() throws IOException {
		doTestSearcherQuery("aniconda", "Eunectes francois", 35, 38);
	}
	
	protected Integer doTestSearcherQuery(String queryString, String... expectedLatinNames) {
		try {
			return doTestSearcherQuery(queryString, expectedLatinNames[0], new Integer[0]);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private Integer doTestSearcherQuery(String queryString, String expectedLatinName, Integer... ids) throws IOException {
		Integer foundId = searcher.search(queryString, Arrays.asList(ids));
		Entry root = mocks.getRoot();
		Entry foundEntry = EntryUtilities.findEntry(root, foundId);
		assertNotNull(foundEntry);
		assertEquals(expectedLatinName, foundEntry.getLatinName());
		return foundId;
	}
	public void testSpanQuery() throws IOException {
		SpanQuery[] terms = {
				new SpanTermQuery(new Term("common_name", "snake")),
				new SpanTermQuery(new Term("common_name", "grass")),
		};
		Query query = new SpanNearQuery(terms, 10, false);
		doTestQuery(query, "latin_name", "Natrix natrix");
	}
	public void testFuzzyQuery() throws IOException {
		Query query = new FuzzyQuery(new Term("common_name", "snaks"), .75f, 1);
		doTestQuery(query, "latin_name", "Serpentes");
	}
	private void doTestQuery(Query query, String testField, String expectValue) throws IOException {
		IndexSearcher searcher = buildIndexSearcher();
		
		TopDocs rs = searcher.search(query, null, 1);
		if (expectValue != null) {
			assertTrue(rs.totalHits > 0);

			Document firstHit = searcher.doc(rs.scoreDocs[0].doc);
			System.out.println(query + " > " + firstHit.getField("id").stringValue() + " > " + firstHit.getField(testField).stringValue());
			assertEquals(expectValue, firstHit.getField(testField).stringValue());
		} else {
			assertEquals(0, rs.totalHits);
		}
	}
	private IndexSearcher buildIndexSearcher() throws IOException {
		CompleteEntry reptiles = (CompleteEntry) mocks.getRoot();
		Set<CompleteEntry> entries = EntryUtilities.getEntries(reptiles);

		RAMDirectory directory = new RAMDirectory();
		IndexWriter writer = new IndexWriter(directory, analyzer,
				true, IndexWriter.MaxFieldLength.UNLIMITED);
		
		for (CompleteEntry entry: entries) {
			Document doc = buildDocument(entry);
			writer.addDocument(doc);
		}
		
		writer.close();

		IndexSearcher searcher = new IndexSearcher(directory);
		return searcher;
	}
	public Document buildDocument(Entry entry) {
		Document doc = new Document();
		String id = IdCruncher.R26_4.toString(entry.getId());
//		System.out.println(entry.getId() + " >> " + id);
		doc.add(new Field(LuceneSearcher.ID, id, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field(LuceneSearcher.LATIN, entry.getLatinName(), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field(LuceneSearcher.COMMON, entry.getCommonName(), Field.Store.NO, Field.Index.ANALYZED));
		return doc;
	}

}
