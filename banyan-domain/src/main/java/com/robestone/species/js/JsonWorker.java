package com.robestone.species.js;

import com.robestone.species.parse.AbstractWorker;

public class JsonWorker extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		JsonWorker b = new JsonWorker();
		
		// recreate json
		b.rebuildAllJson();
		
		// or ...
		
		// fine-tune what you want to d
//		b.copyAdditionalJsonResources();
//		b.partitionFromDB();
//		b.buildRandomFiles();
//		b.runExamples();
	}
	
	public void rebuildAllJson() throws Exception {
		System.out.println(">copyAdditionalJsonResources");
		JsonFileUtils.copyAdditionalJsonResources();
		System.out.println(">partitionFromDB");
		new IndexPartitionsBuilder(speciesService).partitionFromDB();
		System.out.println(">runExamples");
		new ExamplesBuilder(this).runExamples();
		// this is the longest running
		System.out.println(">buildRandomFiles");
		new RandomTreeBuilder().buildRandomFiles();
		System.out.println(">buildSearchIndex");
		new SearchIndexBuilder().run();
	}
	
	public void runMaintenance() throws Exception {
		JsonFileUtils.deleteJsonDir();
		rebuildAllJson();
		JsonFileUtils.generateSiteMap();
	}

}
