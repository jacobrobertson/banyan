package com.robestone.species.parse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.CommonNameSimilarityChecker;
import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.LogHelper;

public class WikipediaImageAndNameFinder extends AbstractWorker {
	
	public static void main(String[] args) {
		
		args = new String[] {"Cetraria islandica"};
		
		if (args != null && args.length == 1) {
			new WikipediaImageAndNameFinder().runOne(args[0]);
		} else {
			new WikipediaImageAndNameFinder().run();
		}
	}
	private String outFilePath = "D:\\eclipse-workspaces\\git\\banyan-parent\\banyan-jstests\\src\\main\\webapp\\page-list.js";
	private WikipediaCrawler crawler = new WikipediaCrawler();
	private TaxoboxFormatter formatter = new TaxoboxFormatter();

	public void runOne(String latinName) {
		CompleteEntry one = speciesService.findEntryByLatinName(latinName);
		List<CompleteEntry> entries = new ArrayList<CompleteEntry>();
		entries.add(one);
		run(entries);
	}
	public void run() {
		// get list of species that are missing either common or image
		Collection<CompleteEntry> entriesCol;
		boolean allBoring = false;
		if (allBoring) {
			entriesCol = speciesService.findBoringEntries(true);
		} else {
			entriesCol = speciesService.findEntriesWithCommonNameAndNoImage();
		}
		List<CompleteEntry> entries = new ArrayList<CompleteEntry>(entriesCol);
		Collections.shuffle(entries);
		run(entries);
	}
	public void run(List<CompleteEntry> entries) {
		int maxToShow = 200;
		int totalCount = 0;
		int interestingCount = 0;
		boolean onlyKeepBest = false;
		
		Set<String> latinNames = new HashSet<>();
		for (CompleteEntry entry: entries) {
			latinNames.add(entry.getLatinName());
		}
		
		
		LogHelper.speciesLogger.debug("WikipediaImageAndNameFinder.run. total/max " + entries.size() + "/" + maxToShow);
		
		for (CompleteEntry entry: entries) {
			try {
				totalCount++;
				Taxobox box = crawler.toTaxobox(entry.getLatinName());
				
				// we will skip this if it was (for example) a redirect to a non-existent, or a page we don't want to look at
				if (!latinNames.contains(box.getLatinName())) {
					continue;
				}
				
				if (isBoxInteresting(box, entry)) {
					LogHelper.speciesLogger.info("=============================== "  + interestingCount + "/" + totalCount);
					LogHelper.speciesLogger.info(entry.getLatinName());
					boolean isImageInteresting = (box.getImage() != null && entry.getImageLink() == null);
					if (isImageInteresting) {
						LogHelper.speciesLogger.info(formatter.formatImage(box).trim());
					}
					boolean isNamesInteresting = isNamesInteresting(box, entry);
					if (isNamesInteresting) {
						LogHelper.speciesLogger.info(formatter.formatVernacularNames(box).trim());
					}
					boolean outputAndIncrement;
					if (onlyKeepBest) {
						outputAndIncrement = isNamesInteresting && isImageInteresting;
					} else {
						outputAndIncrement = isNamesInteresting || isImageInteresting;
					}
					if (outputAndIncrement) {
						interestingCount++;
						outputToJsPage(box, isNamesInteresting);
					}
					if (interestingCount > maxToShow) {
						break;
					}
				}
			} catch (RuntimeException e) {
				LogHelper.speciesLogger.info(totalCount + " - " + entry.getLatinName());
				throw e;
			}
		}
	}
	private void outputToJsPage(Taxobox box, boolean isNamesInteresting) {
		try {
			FileOutputStream fout = new FileOutputStream(outFilePath, true);
			OutputStreamWriter outWriter = new OutputStreamWriter(fout, Charset.forName("UTF-8"));
			String entry = formatter.toJsPageEntry(box, isNamesInteresting);
			String key = String.valueOf(System.currentTimeMillis());
//			String line = "pages['" + key + "'] = {id:'" +key + "'," + entry + "};\n";
			String line = "{\"id\":\"" +key + "\"," + entry + "}\n";
			outWriter.write(line);
			outWriter.flush();
			outWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private boolean isBoxInteresting(Taxobox box, CompleteEntry entry) {
		if (box == null) {
			return false;
		}
		if (box.getLatinName() == null) {
			return false;
		}
		if (box.getImage() != null && entry.getImageLink() == null) {
			return true;
		}
		if (isNamesInteresting(box, entry)) {
			return true;
		}
		return false;
		
	}
	private boolean isNamesInteresting(Taxobox box, CompleteEntry entry) {
		if (entry.getCommonName() != null) {
			return false;
		}
		if (box.getCommonName() == null) {
			return false;
		}
		String latin = EntryUtilities.getClean(entry.getLatinName(), false);
		String cname = EntryUtilities.getClean(box.getCommonName(), false);
		if (!CommonNameSimilarityChecker.isCommonNameCleanBoring(cname, latin)) {
			return true;
		}
		return false;
		
	}

}
