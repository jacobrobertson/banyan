package com.robestone.species.parse;

/**
 * Should be re-run in entirety each time, or at least at some final point.
 * This is because some of this work is being done in steps, and will become
 * obsolete.
 * 
 * @author jacob
 */
public class BoringWorker extends AbstractWorker {

	public static void main(String[] args) {
		BoringWorker w = new BoringWorker();
		
		w.speciesService.fixBoringCommonNames();
		new BoringPrunerWorker().run();
		
		/*
		// first pass - this is simple
		
		// we already have the parents assigned,
		// so we assign the child counts (which just checks the parent ids) 
		w.speciesService.assignChildCounts(false);
		// and then using that information (and link and common name), assign boring attributes
		w.speciesService.assignBoringAttributes(false);
		
		// second pass - a little more confusing

		// assign the interesting parents 
		w.speciesService.assignInterestingParents();
		// for redundant siblings, mark their interesting parent as null
		w.speciesService.removeRedundantSiblings();
		// based only off the interesting parents id, assign the child counts
		w.speciesService.assignChildCounts(true);
		// use the new information to assign boring attributes
		w.speciesService.assignBoringAttributes(true);
		// one more check to get rid of boring children from the counts
		w.speciesService.assignChildCounts(true, true);
		*/
	}
	
}
