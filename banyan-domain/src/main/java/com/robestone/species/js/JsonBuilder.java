package com.robestone.species.js;

import com.robestone.species.parse.AbstractWorker;

public class JsonBuilder extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		JsonBuilder b = new JsonBuilder();
		
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
		new JsonPartitioner(speciesService).partitionFromDB();
		new ExamplesBuilder(this).runExamples();
		// this is the longest running
		System.out.println(">buildRandomFiles");
		new RandomTreeBuilder().buildRandomFiles();
		System.out.println(">copyAdditionalJsonResources");
		JsonFileUtils.copyAdditionalJsonResources();
	}
	
	public void runMaintenance() throws Exception {
		JsonFileUtils.deleteJsonDir();
		rebuildAllJson();
	}

}
