package com.robestone.species.parse;

import java.io.File;

import com.robestone.species.LuceneSearcher;

public class LuceneWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		new LuceneWorker().run();
	}
	
	public void run() throws Exception {
//		String indexDir = "./target/clean-lucene-index";
//		new File(indexDir).mkdir();
//		String indexDir = "D:\\banyan-db\\lucene2";
		String indexDir = "/home/private/banyan-lucene";
		System.out.println(new File(indexDir).getAbsolutePath());
		new LuceneSearcher(speciesService, indexDir);
	}
	
}
