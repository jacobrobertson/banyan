package com.robestone.banyan.json;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.robestone.banyan.taxons.Example;
import com.robestone.banyan.taxons.ExampleGroup;
import com.robestone.banyan.taxons.ExamplesService;
import com.robestone.banyan.taxons.Taxon;
import com.robestone.banyan.taxons.TaxonNode;
import com.robestone.banyan.taxons.TaxonService;
import com.robestone.banyan.taxons.Tree;
import com.robestone.banyan.taxons.TreeNodeUtilities;
import com.robestone.banyan.wikispecies.EntryUtilities;

public class ExamplesBuilder {

	public static void main(String[] args) throws Exception {
//		ExamplesBuilder eb = new ExamplesBuilder(new JsonWorker());
		
//		eb.convertCrunchedIdsToLatinDbList("c:.1_1vnj-1111113121411111111311223111111112_4h1u-1751651z_gG0f_3Pb:p:-tR1v-c751_hf_3Pq");
//		eb.runExamples();
	}
	
	private TaxonService taxonService;
	private ExamplesService examplesService;
	
	public ExamplesBuilder(TaxonService speciesService, ExamplesService examplesService) {
		this.taxonService = speciesService;
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
				Tree<TaxonNode> tree = taxonService.findTreeForTaxonIds(set);
//				Set<TaxonNode> entries = TreeNodeUtilities.getEntries(root);
//				Map<Integer, TaxonNode> imageEntries = tree.getNodesMap();// JsonFileUtils.getLinkedImageEntries(entries, taxonService);
				// save one "fat" file for the example
				saveExampleFile(ex.getSimpleTitle(), ex.getPinnedTerms(), tree);
			}
		}
		buildExampleIndexFile();
		System.out.println("<runExamples");
	}

	private void saveExampleFile(String name, Set<String> pinnedTerms, 
			Tree<TaxonNode> tree
//			Map<Integer, TaxonNode> linkedImageEntries, Collection<TaxonNode> entries
			) throws Exception {
		List<JsonEntry> jentries = new ArrayList<>();
		for (TaxonNode e : tree.getNodesList()) {
//			Taxon imageEntry = null;
//			if (e.getImage() != null) {
//				imageEntry = linkedImageEntries.get(e.getImage().getEntryId());
//			}
			JsonEntry je = JsonFileUtils.toJsonEntry(e, taxonService);
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
		
		Map<Integer, Taxon> entriesForImages = new HashMap<>();
		
		// need to set the image path
		for (ExampleGroup eg : groups) {
			for (Example ex : eg.getExamples()) {
				String term = ex.getDepictedTerm();
				Taxon imageEntry = taxonService.findTaxonByLatinName(term);
				imageEntry = taxonService.findTaxonById(imageEntry.getTaxonId());
				ex.setDepictedImage(imageEntry.getImage());
				entriesForImages.put(imageEntry.getTaxonId(), imageEntry);
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
		Tree<TaxonNode> tree = taxonService.findTreeForTaxonIds(all);
		
		Set<TaxonNode> pinned = TreeNodeUtilities.getEntries(tree.getRoot(), pidsSet);
		Set<TaxonNode> leaves = TreeNodeUtilities.getLeaves(tree.getRoot());
		leaves.removeAll(pinned);
		
		StringBuilder buf = new StringBuilder();
		for (Taxon e : leaves) {
			buf.append("!");
			buf.append(e.getLatinName());
			buf.append(",");
		}
		
		for (Taxon e : pinned) {
			buf.append(e.getLatinName());
			buf.append(",");
		}
		buf.setLength(buf.length() - 1);
		
		System.out.println(buf);
	}

}
