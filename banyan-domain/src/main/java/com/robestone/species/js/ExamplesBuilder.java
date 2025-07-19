package com.robestone.species.js;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Example;
import com.robestone.species.ExampleGroup;
import com.robestone.species.ExamplesService;
import com.robestone.species.SpeciesService;
import com.robestone.species.parse.AbstractWorker;

public class ExamplesBuilder {

	public static void main(String[] args) throws Exception {
		ExamplesBuilder eb = new ExamplesBuilder(new JsonWorker());
		
//		eb.convertCrunchedIdsToLatinDbList("c:.1_1vnj-1111113121411111111311223111111112_4h1u-1751651z_gG0f_3Pb:p:-tR1v-c751_hf_3Pq");
		eb.runExamples();
	}
	
	private SpeciesService speciesService;
	private ExamplesService examplesService;
	
	public ExamplesBuilder(AbstractWorker worker) {
		this(worker.speciesService, worker.examplesService);
	}
	public ExamplesBuilder(SpeciesService speciesService, ExamplesService examplesService) {
		this.speciesService = speciesService;
		this.examplesService = examplesService;
	}

	
	public void runExamples() throws Exception {
		System.out.println(">runExamples");
		examplesService.crunchIds(false);
		List<ExampleGroup> egs = examplesService.findExampleGroups();
		for (ExampleGroup eg : egs) {
			for (Example ex : eg.getExamples()) {
				String cids = ex.getCrunchedIds();
				List<Integer> ids = EntryUtilities.CRUNCHER.toList(cids);
				Set<Integer> set = new HashSet<>(ids);
				Entry root = speciesService.findTreeForNodes(set);
				Set<Entry> entries = EntryUtilities.getEntries(root);
				Map<Integer, Entry> imageEntries = JsonFileUtils.getLinkedImageEntries(entries, speciesService);
				// save one "fat" file for the example
				saveExampleFile(ex.getSimpleTitle(), ex.getPinnedTerms(), imageEntries, entries);
			}
		}
		buildExampleIndexFile();
		System.out.println("<runExamples");
	}

	private void saveExampleFile(String name, Set<String> pinnedTerms, Map<Integer, Entry> linkedImageEntries, Collection<Entry> entries) throws Exception {
		List<JsonEntry> jentries = new ArrayList<>();
		for (Entry e : entries) {
			Entry imageEntry = null;
			if (e.getImage() != null) {
				imageEntry = linkedImageEntries.get(e.getImage().getEntryId());
			}
			JsonEntry je = JsonFileUtils.toJsonEntry(e, imageEntry, speciesService);
			if (pinnedTerms.contains(e.getLatinName())) {
				je.setPinned(true); // TODO allow us to not pin all the "terms"
			}
			jentries.add(je);
		}
		JsonFileUtils.saveByFolders("e", name, jentries);
	}
	public void buildExampleIndexFile() throws Exception {
		examplesService.findExampleGroups(); // inits it
		List<ExampleGroup> groups = new ArrayList<>();
		groups.add(examplesService.getYouMightNotKnow());
		groups.add(examplesService.getHaveYouHeardOf());
		groups.add(examplesService.getFamilies());
		groups.add(examplesService.getOtherFamilies());
		
		Map<Integer, Entry> entriesForImages = new HashMap<Integer, Entry>();
		
		// need to set the image path
		for (ExampleGroup eg : groups) {
			for (Example ex : eg.getExamples()) {
				String term = ex.getDepictedTerm();
				Entry imageEntry = speciesService.findEntryByLatinName(term);
				imageEntry = speciesService.findEntry(imageEntry.getId());
				ex.setDepictedImage(imageEntry.getImage());
				entriesForImages.put(imageEntry.getId(), imageEntry);
			}
		}
		
		String s = JsonFileUtils.toJsonString(groups, entriesForImages);
		File file = new File(JsonFileUtils.outputDir + "/e/examples-index.json");
		FileUtils.writeStringToFile(file, s, Charset.defaultCharset());
	}
	
	/**
	 * Turn c:.1_1vnh-21111113111121111111111111112212111111112e_43-xdX1485_8Y1g7j6x4X2l_1Hp2tM09S1bS.1.3iFUzI:p:-pa056j0it1_1JK2DE4uy
	 * Into Proboscidea,Etc,Etc,$PinnedLatin,Etc
	 * This is helpful for me when I'm manually creating a new Example, and just need the list of Pinned Latin Names
	 */
	public void convertCrunchedIdsToLatinDbList(String ids) {
		int pPos = ids.indexOf(":p:");
		int cPos = ids.indexOf("c:");
		
		String pids = ids.substring(pPos + 3);
		String cids = ids.substring(cPos + 2, pPos);
		
		Set<Integer> pidsSet = EntryUtilities.CRUNCHER.toSet(pids);
		Set<Integer> cidsSet = EntryUtilities.CRUNCHER.toSet(cids);
		
		Set<Integer> all = new HashSet<Integer>();
		all.addAll(pidsSet);
		all.addAll(cidsSet);
		Entry root = speciesService.findTreeForNodes(all);
		
		Set<Entry> pinned = EntryUtilities.getEntries(root, pidsSet);
		Set<Entry> leaves = EntryUtilities.getLeaves(root);
		leaves.removeAll(pinned);
		
		StringBuilder buf = new StringBuilder();
		for (Entry e : leaves) {
			buf.append("!");
			buf.append(e.getLatinName());
			buf.append(",");
		}
		
		for (Entry e : pinned) {
			buf.append(e.getLatinName());
			buf.append(",");
		}
		buf.setLength(buf.length() - 1);
		
		System.out.println(buf);
	}

}
