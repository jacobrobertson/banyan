package com.robestone.species.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

import com.robestone.species.LuceneSearcher;

/**
 * Do I understand how Lucene works!!?!?!?!
 * 
 * @author jacob
 */
public class LuceneComprehensionTest extends TestCase {

	private Searcher buildSearcher(List<Document> docs) throws Exception {
		RAMDirectory directory = new RAMDirectory();
		IndexWriter writer = new IndexWriter(directory, new SimpleAnalyzer(),
				true, IndexWriter.MaxFieldLength.UNLIMITED);

		for (Document doc : docs) {
			writer.addDocument(doc);
		}

		writer.optimize();
		writer.close();

		Searcher searcher = new IndexSearcher(directory);
		return searcher;
	}

	public void testExample() throws Exception {
		List<Document> docs = new ArrayList<Document>();
		addDoc(docs, "Lucene in Action");
		addDoc(docs, "Lucene for Dummies");
		addDoc(docs, "Managing Gigabytes");
		addDoc(docs, "The Art of Computer Science");

		Searcher s = buildSearcher(docs);

		Query q = new TermQuery(new Term("title", "action"));
		TopDocs td = s.search(q, null, 1);

		ScoreDoc[] hits = td.scoreDocs;
		Document foundDoc = s.doc(hits[0].doc);
		String foundTitle = foundDoc.get("title");
		assertEquals("Lucene in Action", foundTitle);
	}

	private static void addDoc(List<Document> docs, String value) throws Exception {
		Document doc = new Document();
		doc.add(new Field("title", value, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("dummy", "funk", Field.Store.YES, Field.Index.ANALYZED));
		docs.add(doc);
	}
	public void testLuceneExample() throws IOException {
		RAMDirectory directory = new RAMDirectory();
		IndexWriter writer = new IndexWriter(directory, new SimpleAnalyzer(),
				true, IndexWriter.MaxFieldLength.UNLIMITED);

		Document doc = new Document();
		doc.add(new Field("partnum", "Q36", Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field("description", "Illidium Space Modulator",
				Field.Store.YES, Field.Index.ANALYZED));
		writer.addDocument(doc);
		writer.close();

		IndexSearcher searcher = new IndexSearcher(directory);
		Query query = new TermQuery(new Term("partnum", "Q36"));
		TopDocs rs = searcher.search(query, null, 10);
		assertEquals(1, rs.totalHits);

		Document firstHit = searcher.doc(rs.scoreDocs[0].doc);
		assertEquals("partnum", firstHit.getField("partnum").name());
	}
	
	/**
	 * Check how phrases work, find some examples.
	 */
	public void testPhrases() throws Exception {
		Document doc = new Document();
		doc.add(new Field("whole_name", "Jacob CS Robertson", Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("nick_name", "Jojo Mc Beans", Field.Store.YES, Field.Index.ANALYZED));

		List<Document> docs = new ArrayList<Document>();
		docs.add(doc);
		Searcher searcher = buildSearcher(docs);
		
		Query query = new TermQuery(new Term("whole_name", "jacob robertson"));
		TopDocs rs = searcher.search(query, null, 1);
		assertEquals(0, rs.totalHits);

		query = LuceneSearcher.buildPhraseQuery("whole_name", "jacob robertson", new SimpleAnalyzer());
		rs = searcher.search(query, null, 1);
		assertEquals(0, rs.totalHits);

		query = LuceneSearcher.buildPhraseQuery("nick_name", "jojo beans", new SimpleAnalyzer());
		rs = searcher.search(query, null, 1);
		assertEquals(1, rs.totalHits);

		Document firstHit = searcher.doc(rs.scoreDocs[0].doc);
		assertEquals("Jojo Mc Beans", firstHit.get("nick_name"));
	}

	/**
	 * Test that "funk" does not match exactly "funk house"
	 */
	public void testSpaces() throws Exception {
		Document doc = new Document();
		doc.add(new Field("whole_name", "Jacob Robertson", Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("nick_name", "Jojo McBeans", Field.Store.YES, Field.Index.ANALYZED));

		List<Document> docs = new ArrayList<Document>();
		docs.add(doc);
		Searcher searcher = buildSearcher(docs);
		
		Query query = new TermQuery(new Term("whole_name", "jacob"));
		TopDocs rs = searcher.search(query, null, 1);
		assertEquals(0, rs.totalHits);

		query = new TermQuery(new Term("nick_name", "jojo"));
		rs = searcher.search(query, null, 1);
		assertEquals(1, rs.totalHits);

		Document firstHit = searcher.doc(rs.scoreDocs[0].doc);
		assertEquals("Jojo McBeans", firstHit.get("nick_name"));
	}

}
