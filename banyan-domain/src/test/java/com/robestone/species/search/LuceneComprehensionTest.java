package com.robestone.species.search;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import junit.framework.TestCase;

/**
 * Do I understand how Lucene works!!?!?!?!
 * 
 * @author jacob
 */
public class LuceneComprehensionTest extends TestCase {

	public void testSimpleTermQueryNoDoc() throws Exception {
		List<Document> docs = new ArrayList<>();
		IndexSearcher searcher = buildSearcher(docs);
		Query query = buildQuery("key", "abc");
		TopDocs top = searcher.search(query, 1);
		assertEquals(0, top.totalHits);
	}
	public void testSimpleTermQueryOneDoc() throws Exception {
		Document doc = new Document();
		doc.add(new StringField("key", "abc", Store.YES));
		List<Document> docs = new ArrayList<>();
		docs.add(doc);
		IndexSearcher searcher = buildSearcher(docs);
		Query query = buildQuery("key", "abc");
		TopDocs top = searcher.search(query, 1);
		assertEquals(1, top.totalHits);
	}
	public void testTermQuery() throws Exception {
		Document doc = new Document();
		doc.add(new StringField("abc", "123", Store.YES));
		doc.add(new StringField("xyz", "456", Store.YES));
		List<Document> docs = new ArrayList<>();
		docs.add(doc);
		IndexSearcher searcher = buildSearcher(docs);

		// incorrect terms don't find it
		Query query = buildQuery("abc", "1234", "xyz", "456");
		TopDocs top = searcher.search(query, 1);
		assertEquals(0, top.totalHits);

		// correct terms are good
		query = buildQuery("abc", "123", "xyz", "456");
		top = searcher.search(query, 1);
		assertEquals(1, top.totalHits);
	}
	public void testFigureOutSpellingErrors() throws Exception {
		doTestFigureOutSpellingErrors("lucene", "lusene", 1);

		doTestFigureOutSpellingErrors("abc", "abz", 1);
		doTestFigureOutSpellingErrors("abcd", "abcz", 1);
		doTestFigureOutSpellingErrors("abcde", "abcdz", 1);

		// it doesn't return the word itself if it's a match
		doTestFigureOutSpellingErrors("abcdefghijkl", "abcdefghijkl", 0);

		doTestFigureOutSpellingErrors("abcdefghijkl", "accdefghijkl", 1);
		
		doTestFigureOutSpellingErrors("Balaena mysticetus", "Balaena misticetus", 1);
		
		doTestFigureOutSpellingErrors("Balaena mysticetus\nBolearamysticetus", "Balaena misticetus", 2);
		doTestFigureOutSpellingErrors("Balaena mysticetus\nBolearamysticetus\nzzzyyyssss", "Balaena misticetus", 2);
	}
	public void doTestFigureOutSpellingErrors(String words, String search, int expect) throws Exception {
		words = words.toUpperCase();
		search = search.toUpperCase();
		SpellChecker sp = buildSpellChecker(words);

		String[] suggestions = sp.suggestSimilar(search, 5);
		
		assertEquals(expect, suggestions.length);
	}
	private Query buildQuery(String... keyValues) {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		for (int i = 0; i < keyValues.length; i+=2) {
			queryBuilder.add(new TermQuery(new Term(keyValues[i], keyValues[i + 1])), BooleanClause.Occur.MUST);
		}
		return queryBuilder.build();
	}
	private SpellChecker buildSpellChecker(String words) throws Exception {
		Directory emptyDir =  new RAMDirectory();
		SpellChecker sp = new SpellChecker(emptyDir);
		
		StringReader reader = new StringReader(words);
		PlainTextDictionary dict = new PlainTextDictionary(reader);
		
		IndexWriterConfig config = new IndexWriterConfig(new KeywordAnalyzer());
		
		sp.indexDictionary(dict, config, false);
		
		return sp;
	}
	private IndexSearcher buildSearcher(List<Document> docs) throws Exception {
		Directory directory = buildDirectory(docs);
		IndexReader reader = DirectoryReader.open(directory);  
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}
	private Directory buildDirectory(List<Document> docs) throws Exception {
		Directory directory =  new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(directory, config);
		for (Document doc : docs) {
			if (doc != null) {
				writer.addDocument(doc);
			}
		}
		writer.close();
		return directory;
	}

//
//	public void testExample() throws Exception {
//		List<Document> docs = new ArrayList<Document>();
//		addDoc(docs, "Lucene in Action");
//		addDoc(docs, "Lucene for Dummies");
//		addDoc(docs, "Managing Gigabytes");
//		addDoc(docs, "The Art of Computer Science");
//
//		Searcher s = buildSearcher(docs);
//
//		Query q = new TermQuery(new Term("title", "action"));
//		TopDocs td = s.search(q, null, 1);
//
//		ScoreDoc[] hits = td.scoreDocs;
//		Document foundDoc = s.doc(hits[0].doc);
//		String foundTitle = foundDoc.get("title");
//		assertEquals("Lucene in Action", foundTitle);
//	}
//
//	private static void addDoc(List<Document> docs, String value) throws Exception {
//		Document doc = new Document();
//		doc.add(new Field("title", value, Field.Store.YES, Field.Index.ANALYZED));
//		doc.add(new Field("dummy", "funk", Field.Store.YES, Field.Index.ANALYZED));
//		docs.add(doc);
//	}
//	public void testLuceneExample() throws IOException {
//		RAMDirectory directory = new RAMDirectory();
//		IndexWriter writer = new IndexWriter(directory, new SimpleAnalyzer(),
//				true, IndexWriter.MaxFieldLength.UNLIMITED);
//
//		Document doc = new Document();
//		doc.add(new Field("partnum", "Q36", Field.Store.YES,
//				Field.Index.NOT_ANALYZED));
//		doc.add(new Field("description", "Illidium Space Modulator",
//				Field.Store.YES, Field.Index.ANALYZED));
//		writer.addDocument(doc);
//		writer.close();
//
//		IndexSearcher searcher = new IndexSearcher(directory);
//		Query query = new TermQuery(new Term("partnum", "Q36"));
//		TopDocs rs = searcher.search(query, null, 10);
//		assertEquals(1, rs.totalHits);
//
//		Document firstHit = searcher.doc(rs.scoreDocs[0].doc);
//		assertEquals("partnum", firstHit.getField("partnum").name());
//	}
//	
//	/**
//	 * Check how phrases work, find some examples.
//	 */
//	public void testPhrases() throws Exception {
//		Document doc = new Document();
//		doc.add(new Field("whole_name", "Jacob CS Robertson", Field.Store.YES, Field.Index.NOT_ANALYZED));
//		doc.add(new Field("nick_name", "Jojo Mc Beans", Field.Store.YES, Field.Index.ANALYZED));
//
//		List<Document> docs = new ArrayList<Document>();
//		docs.add(doc);
//		Searcher searcher = buildSearcher(docs);
//		
//		Query query = new TermQuery(new Term("whole_name", "jacob robertson"));
//		TopDocs rs = searcher.search(query, null, 1);
//		assertEquals(0, rs.totalHits);
//
//		query = LuceneSearcher.buildPhraseQuery("whole_name", "jacob robertson", new SimpleAnalyzer());
//		rs = searcher.search(query, null, 1);
//		assertEquals(0, rs.totalHits);
//
//		query = LuceneSearcher.buildPhraseQuery("nick_name", "jojo beans", new SimpleAnalyzer());
//		rs = searcher.search(query, null, 1);
//		assertEquals(1, rs.totalHits);
//
//		Document firstHit = searcher.doc(rs.scoreDocs[0].doc);
//		assertEquals("Jojo Mc Beans", firstHit.get("nick_name"));
//	}
//
//	/**
//	 * Test that "funk" does not match exactly "funk house"
//	 */
//	public void testSpaces() throws Exception {
//		Document doc = new Document();
//		doc.add(new Field("whole_name", "Jacob Robertson", Field.Store.YES, Field.Index.NOT_ANALYZED));
//		doc.add(new Field("nick_name", "Jojo McBeans", Field.Store.YES, Field.Index.ANALYZED));
//
//		List<Document> docs = new ArrayList<Document>();
//		docs.add(doc);
//		Searcher searcher = buildSearcher(docs);
//		
//		Query query = new TermQuery(new Term("whole_name", "jacob"));
//		TopDocs rs = searcher.search(query, null, 1);
//		assertEquals(0, rs.totalHits);
//
//		query = new TermQuery(new Term("nick_name", "jojo"));
//		rs = searcher.search(query, null, 1);
//		assertEquals(1, rs.totalHits);
//
//		Document firstHit = searcher.doc(rs.scoreDocs[0].doc);
//		assertEquals("Jojo McBeans", firstHit.get("nick_name"));
//	}

}
