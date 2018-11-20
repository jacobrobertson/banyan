package com.robestone.species.js;

import com.robestone.species.LuceneSearcher;
import com.robestone.species.parse.AbstractWorker;

/**
 * Use this main class whenever you want to rebuild all banyan-js assets immediately prior to synching them.
 * 
 * @author jacob
 */
public class JsWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		System.setProperty("banyan.lucene.dir", LuceneSearcher.defaultLinuxPath);
		new JsWorker().rebuildLuceneIndex();
		
//		JsonBuilder b = new JsonBuilder();
//		b.deleteJsonDir();
//		b.rebuildAllJson();
		
	}
	public void rebuildLuceneIndex() throws Exception {
		new LuceneSearcher(speciesService, LuceneSearcher.defaultLinuxPath);
	}
	

}
