package com.robestone.species.parse;

/**
 * Looks at old parse links, and crawls them.
 * @author jacob
 */
public class OldLinksCrawler {

	public static void main(String[] args) throws Exception {
		int numberToCrawl = 1000;
		if (args != null && args.length == 1) {
			numberToCrawl = Integer.parseInt(args[0]);
		}
		RecentChangesUpdater rcu = new RecentChangesUpdater();
		rcu.run(false, numberToCrawl);
	}
	
}
