package com.robestone.species.js;

import com.robestone.species.CompleteEntry;
import com.robestone.species.LuceneSearcher;
import com.robestone.species.parse.AbstractWorker;

/**
 * Use this main class whenever you want to rebuild all banyan-js assets immediately prior to synching them.
 * 
 * @author jacob
 */
public class JsWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {

//		new JsWorker().miscTest();

//		System.setProperty("banyan.lucene.dir", LuceneSearcher.defaultLinuxPath);
//		new JsWorker().run();
		
//		new JsWorker().rebuildLuceneIndex();
		JsonBuilder b = new JsonBuilder();
		b.deleteJsonDir();
		b.rebuildAllJson();
		
	}
	
	public void miscTest() throws Exception {
		CompleteEntry c1 = speciesService.findEntryByLatinName("Virus");
		CompleteEntry c2 = speciesService.findEntry(c1.getId());
		System.out.println(c1 + "/" + c1.getCommonName());
		System.out.println(c2);
	}
	
	public void run() throws Exception {
		rebuildLuceneIndex();
		
		JsonBuilder b = new JsonBuilder();
		b.deleteJsonDir();
		b.rebuildAllJson();
	}
	public void rebuildLuceneIndex() throws Exception {
		new LuceneSearcher(speciesService, LuceneSearcher.defaultLinuxPath);
	}
	

}
