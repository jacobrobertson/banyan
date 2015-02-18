package com.robestone.species.parse;

import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Tree;

/**
 * Doesn't 
 * @author Jacob Robertson
 *
 */
public class CommonNameFixer extends AbstractWorker {

	public static void main(String[] args) {
		new CommonNameFixer().run();
	}
	public void run() {
		Tree all = speciesService.findCompleteTreeFromPersistence();
		for (CompleteEntry e: all.getEntries()) {
			String cn = e.getCommonName();
			if (cn != null) {
				String fixed = EntryUtilities.fixCommonName(cn);
				if (!cn.equals(fixed)) {
					e.setCommonName(fixed);
					speciesService.updateCommonName(e);
					System.out.println("CommonNameFixer." + cn + " > " + fixed);
				}
			}
		}
		speciesService.recreateCleanNames();
	}
	
}
