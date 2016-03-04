package com.robestone.species.parse;

import com.robestone.species.WikiSpeciesTreeFixer;


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
		new WikiSpeciesTreeFixer(speciesService).run();
	}
	
}
