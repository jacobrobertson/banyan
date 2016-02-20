package com.robestone.species.parse;

/**
 * Just so I can run whatever service commands I like...
 * 
 * @author jacob
 */
public class MiscWorker extends AbstractWorker {

	public static void main(String[] args) {
		new MiscWorker().run();
	}
	public void run() {
		
//		w.speciesService.findCompleteTreeFromPersistence();
//		speciesService.fixParents();
		
//		speciesService.fixExtinct();
		 
		// clean names
//		speciesService.recreateCleanNames();
		 
		// run full boring suite
//		BoringWorker.main(null);
		
		// create new interesting crunched ids
		new InterestingSubspeciesWorker().run();
		
//		new SiblingsWithSameCommonNamesAnalyzer().run();
		
//		new LinkedImagesWorker().run();
	}
	
}
