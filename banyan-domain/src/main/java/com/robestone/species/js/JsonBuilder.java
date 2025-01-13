package com.robestone.species.js;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.CrunchedIds;
import com.robestone.species.DerbyDataSource;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Example;
import com.robestone.species.ExampleGroup;
import com.robestone.species.parse.AbstractWorker;
import com.robestone.species.parse.ImagesCreater;

public class JsonBuilder extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		
		DerbyDataSource.defaultWindowsPath = "D:\\banyan-db\\derby";
		
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
	
	private String imagesDir = "D:/banyan-images";
	private String outputDir = "../banyan-js/src/main/webapp/json";
	private String additionalResourcesDir = "../banyan-js/src/main/resources/webapp/json";
	private JsonParser parser = new JsonParser();
	private RandomTreeBuilder randomTreeBuilder = new RandomTreeBuilder();
	
	public void deleteJsonDir() throws Exception {
		FileUtils.deleteDirectory(new File(outputDir));
	}
	
	public void rebuildAllJson() throws Exception {
		partitionFromDB();
		runExamples();
		// this is the longest running
		buildRandomFiles();
		copyAdditionalJsonResources();
	}
	
	public void copyAdditionalJsonResources() throws Exception {
		FileUtils.copyDirectory(
				new File(additionalResourcesDir), 
				new File(outputDir));
	}
	
	public void buildExampleIndexFile() throws Exception {
		examplesService.findExampleGroups(); // inits it
		List<ExampleGroup> groups = new ArrayList<>();
		groups.add(examplesService.getYouMightNotKnow());
		groups.add(examplesService.getHaveYouHeardOf());
		groups.add(examplesService.getFamilies());
		groups.add(examplesService.getOtherFamilies());
		
		// need to set the image path
		for (ExampleGroup eg : groups) {
			for (Example ex : eg.getExamples()) {
				String term = ex.getDepictedTerm();
				CompleteEntry imageEntry = speciesService.findEntryByLatinName(term);
				imageEntry = speciesService.findEntry(imageEntry.getId());
				ex.setDepictedImage(imageEntry.getImage());
			}
		}
		
		String s = parser.toJsonString(groups);
		File file = new File(outputDir + "/e/examples-index.json");
		FileUtils.writeStringToFile(file, s, Charset.defaultCharset());
	}
	
	public void buildRandomFiles() throws Exception {
		// load the index
		List<String> lines = 
				FileUtils.readLines(new File("../banyan-js/src/main/resources/random-seed-list.txt"), Charset.defaultCharset());
		Set<String> terms = new HashSet<>();
		for (String line : lines) {
			terms.add(line.toLowerCase().trim());
		}
		System.out.println(terms.size() + " unique terms");

		for (String term : terms) {
			buildOneRandomFileFromQuery(term);
		}
		outputRandomFileIndex();
	}
	private void buildOneRandomFileFromQuery(String query) throws Exception {
		query = query.trim();
		CompleteEntry e = findEntryFromQuery(query);
		if (e != null) {
			try {
				Collection<CompleteEntry> entries = randomTreeBuilder.buildRandomTree(e.getId());
				if (entries != null) {
					List<JsonEntry> jentries = toJsonEntries(entries.toArray(new Entry[entries.size()]));
					String fileName = randomTreeBuilder.toRandomFileName(query, e.getId());
					saveByFolders("r", fileName, jentries);
				}
			} catch (NullPointerException ex) {
				// for now we just swallow this - we can't do much, but it's only when there are other issues
				System.out.println("buildOneRandomFileFromQuery." + query + ".Failed");
			}
		}
	}
	private CompleteEntry findEntryFromQuery(String query) throws Exception {
		int id = speciesService.findBestId(query, new ArrayList<>());
		if (id >= 0) {
			CompleteEntry e = speciesService.findEntry(id);
			System.out.println(query + " >> " + e.getLatinName() + " // " + e.getCommonNames() + " // " + e.getCommonName());
			return e;
		} else {
			System.out.println(query + " >> Not Found");
			return null;
		}
	}
	
	public void partitionFromDB() throws Exception {
		Node root = buildTree();
		partitionAndSave(root);
	}
	// scans all files in a dir, ignoring "index.json"
	public void partitionFromFileSystem2() throws Exception {
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
		File dir = new File(outputDir + "-1");
		loadAllJsonFiles(dir, nodes);
		Node root = null;
		
		for (Integer id : nodes.keySet()) {
			Node n = nodes.get(id);
			n.getChildren().clear();
		}
		for (Integer id : nodes.keySet()) {
			Node n = nodes.get(id);
			Node p = nodes.get(n.getEntry().getParentId());
			if (p == null) {
				root = n;
			} else {
				n.setParent(p);
				p.getChildren().add(n);
			}
		}
		
		partitionAndSave(root);
	}
	private void loadAllJsonFiles(File dir, Map<Integer, Node> map) throws Exception {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				loadAllJsonFiles(file, map);
			} else if (!"index.json".equals(file.getName())) {
				Node node = parser.parseFile(file);
				map.put(node.getId(), node);
			}
		}
	}
	// recursively starts with one file
	public void partitionFromFileSystem() throws Exception {
		Node root = parser.parseRecursive(1);
		partitionAndSave(root);
	}
	public void partitionAndSave(Node root) throws Exception {
		
		JsonPartitioner partitioner = new JsonPartitioner();
		partitioner.partition(root);
		
		Map<String, String> pMap = partitioner.getAndAssignPartitionMap(root);
		
		outputPartitions(root);
		String index = partitioner.getPartitionIndexFile(pMap);
		String folder = outputDir + "/p/index.json";
		File file = new File(folder);
		FileUtils.writeStringToFile(file, index, Charset.defaultCharset());
	}
	
	public void outputPartitions(Node node) throws Exception {
		if (!node.getPartition().isEmpty()) {
			String fileName = "p/" + node.getFilePath() + ".json";
			List<JsonEntry> entries = new ArrayList<>();
			for (Node pn : node.getPartition()) {
				JsonEntry jentry = pn.getEntry();
				if (jentry == null) {
					Entry eentry = speciesService.findEntry(pn.getId());
					jentry = toJsonEntry(eentry);
				}
				entries.add(jentry);
			}
			saveByFileName(fileName, entries);
		}
		for (Node child : node.getChildren()) {
			outputPartitions(child);
		}
	}
	
	private Node buildTree() {
		return buildNodeRecursively(1, 0);
	}
	private Node buildNodeRecursively(Integer id, int depth) {
		Collection<Integer> ids = speciesService.findChildrenIds(id);
		Node node = new Node(null, id, new ArrayList<>(ids));
		int descendants = ids.size();
		for (Integer cid : ids) {
			Node cnode = buildNodeRecursively(cid, depth + 1);
			node.getChildren().add(cnode);
			cnode.setParent(node);
			descendants += cnode.getTotalDescendants();
		}
//		System.out.println("buildNodeRecursively id=" + id + ", depth=" + depth + 
//				", children=" + node.getChildIds().size() + ", desc=" + descendants);
		node.setTotalDescendants(descendants);
		return node;
	}
	
	public void runExamples() throws Exception {
		examplesService.crunchIds(false);
		List<ExampleGroup> egs = examplesService.findExampleGroups();
		for (ExampleGroup eg : egs) {
			for (Example ex : eg.getExamples()) {
				String cids = ex.getCrunchedIds();
				List<Integer> ids = EntryUtilities.CRUNCHER.toList(cids);
				Set<Integer> set = new HashSet<>(ids);
				CompleteEntry root = speciesService.findTreeForNodes(set);
				Set<CompleteEntry> entries = EntryUtilities.getEntries(root);
				Entry[] array = new Entry[entries.size()];
				int index = 0;
				for (Entry e : entries) {
					array[index++] = e;
				}
				// save one "fat" file for the example
				saveExampleFile(ex.getSimpleTitle(), ex.getPinnedTerms(), array);
			}
		}
		buildExampleIndexFile();
	}
	
	/**
	 * This will have to include the intermediate nodes too, because of the way this works.
	 * In terms of perma-links, not sure how that will work, that can be calculated on the browser.
	 */
	private class ShowMore {
		List<Integer> leafIds;
		List<Integer> otherIds;
	}
	/**
	 * Note that this might not match the way the Java app works, but here are the rules
	 * - The Show More Leaf Count refers only to leaves
	 * - The Show More Others is all other necessary intermediate nodes
	 * - this is so JS can calculate the number, and also load them all when needed
	 */
	private ShowMore getShowMoreIds(Entry e) {
		ShowMore showMore = new ShowMore();
		CrunchedIds cids = e.getInterestingCrunchedIds();
		if (cids == null) {
			return showMore;
		}
		List<Integer> ids = e.getInterestingCrunchedIds().getIds();
		Entry root = speciesService.findTreeForNodes(new HashSet<>(ids));
		Entry branch = EntryUtilities.findEntry(root, e.getId());
		Set<Integer> allBranchIds = EntryUtilities.getIds(branch);
		allBranchIds.remove(e.getId());
		
		Collection<Integer> leafIds = EntryUtilities.getLeavesIds(branch);
		
		showMore.leafIds = new ArrayList<>(leafIds);
		showMore.otherIds = new ArrayList<>(allBranchIds);
		showMore.otherIds.removeAll(leafIds);
		
		return showMore;
	}
	/*
	public void runRecursively(Entry e, int depth, int maxDepth, Set<Integer> idsRun) throws Exception {
		if (idsRun.add(e.getId())) {
			save(e);
		}
		if (depth > maxDepth) {
			return;
		}
		List<CompleteEntry> children = speciesService.findChildren(e.getId());
		for (Entry c : children) {
			runRecursively(c, depth + 1, maxDepth, idsRun);
		}
		System.out.println("runRecursively - completed " + idsRun.size());
	}
	*/
	/*
	private void save(Entry e) throws Exception {
		
		// each file is going to have all the entries needed
		// - all ancestors up to the root
		// - all interesting ids
		// - all children ids
		
		save(false, e.getId().toString(), e);
	}
	*/
	public static int getSubFolder(int id) {
		double d = id;
		d = d / 100d;
		d = Math.ceil(d);
		int i = (int) d;
		return i;
	}
	private void saveExampleFile(String name, Set<String> pinnedTerms, Entry... entries) throws Exception {
		List<JsonEntry> jentries = new ArrayList<>();
		for (Entry e : entries) {
			JsonEntry je = toJsonEntry(e);
			if (pinnedTerms.contains(e.getLatinName())) {
				je.setPinned(true); // TODO allow us to not pin all the "terms"
			}
			jentries.add(je);
		}
		saveByFolders("e", name, jentries);
	}
	private void saveByFolders(String subFolder, String name, List<JsonEntry> entries) throws Exception {
		// convention because javascript is tricky this way
//		String subfolder;
//		if (isName) {
//			subfolder = "f";
//		} else {
//			int id = Integer.parseInt(name);
//			subfolder = "n/" + String.valueOf(getSubFolder(id));
//		}
		String fileName = subFolder + "/" + name + ".json";
		saveByFileName(fileName, entries);
	}
	private void saveByFileName(String fileName, List<JsonEntry> entries) throws Exception {
		String json = parser.toJsonString(entries);
		
		System.out.println(json);
		String folder = outputDir + "\\" + fileName;
		
		File file = new File(folder);
		FileUtils.writeStringToFile(file, json, Charset.defaultCharset());
	}
	
	//"6691": { "cname": "Complete Metamorphosis Insects", "parentId": "6692", "alt": "Endopterygota", 
	//	"img": "15/Endopterygota.jpg", "href": "Complete_Metamorphosis_Insects_Endopterygota_6691", 
	// "height": 16, "width": 20},
	public String toJson(Entry... entries) {
		List<JsonEntry> jentries = toJsonEntries(entries);
		return parser.toJsonString(jentries);
	}
	public List<JsonEntry> toJsonEntries(Entry... entries) {
		List<JsonEntry> jentries = new ArrayList<>();
		for (Entry e : entries) {
			JsonEntry je = toJsonEntry(e);
			jentries.add(je);
		}
		return jentries;
	}
	public JsonEntry toJsonEntry(Entry e) {
		JsonEntry je = new JsonEntry();
		je.setId(e.getId());
		je.setCnames(getCommonNames(e));
		je.setLname(e.getLatinName());
		je.setParentId(e.getParentId());
		je.setExtinct(e.isExtinct());
		je.setAncestorExtinct(e.isAncestorExtinct());
		je.setRank(e.getRank().getCommonName());
		je.setPinned(e.isPinned());

		// TODO research why some of these have one but not the other
		if (e.getImage() != null && e.getImageLink() != null) {
			je.setImg(e.getImage().getImagePathPart());
			je.settHeight(e.getImage().getTinyHeight());
			je.settWidth(e.getImage().getTinyWidth());
			je.setpHeight(e.getImage().getPreviewHeight());
			je.setpWidth(e.getImage().getPreviewWidth());
			je.setdHeight(e.getImage().getDetailHeight());
			je.setdWidth(e.getImage().getDetailWidth());
			je.setWikiSpeciesLink(ImagesCreater.getImageFileName(e));
			String localImageRelativePath = imagesDir + "/tiny/" + e.getImage().getImagePathPart();
			String data = createImageDataString(localImageRelativePath);
			je.setImgData(data);
		}
		je.setChildrenIds(new ArrayList<Integer>(getChildrenIds(e)));
		Collections.sort(je.getChildrenIds()); // for indexing in js
		
		ShowMore showMoreIds = getShowMoreIds(e);
		je.setShowMoreLeafIds(showMoreIds.leafIds);
		je.setShowMoreOtherIds(showMoreIds.otherIds);
		
		return je;
	}
	private List<String> getCommonNames(Entry e) {
		List<String> cnames = e.getCommonNames();
		if (cnames == null) {
			cnames = new ArrayList<>();
		}
		if (cnames.isEmpty() && e.getCommonName() != null) {
			cnames.add(e.getCommonName());
		}
		return cnames;
	}
	private Collection<Integer> getChildrenIds(Entry e) {
		List<Integer> ids = new ArrayList<>();
		List<CompleteEntry> someChildren = speciesService.findChildren(e.getId());
		for (CompleteEntry s : someChildren) {
			// these won't be boring - it already filters those out with the query
			ids.add(s.getId());
		}
		return ids;
	}
	
	public void outputRandomFileIndex() throws Exception {
		String fileName = "random-index";
		File dir = new File(outputDir + "/r");
		List<String> names = new ArrayList<>();
		for (File f : dir.listFiles()) {
			String name = f.getName();
			int pos = name.indexOf('.');
			name = name.substring(0, pos);
			if (!fileName.equals(name)) {
				names.add(name);
			}
		}
		String file = "/r/" + fileName + ".json";
		outputNamesListJsonFile(names, file);
	}
	public void outputNamesListJsonFile(List<String> names, String subDirAndFileName) throws Exception {
		StringBuilder buf = new StringBuilder();
		for (String name : names) {
			if (buf.length() == 0) {
				buf.append("{\"files\":[");
			} else {
				buf.append(",\n");
			}
			buf.append("\"");
			buf.append(name);
			buf.append("\"");
		}
		buf.append("]}");
		
		FileUtils.writeStringToFile(new File(outputDir + subDirAndFileName), buf.toString(), Charset.defaultCharset());
	}
	
	// (504, 504, 5, 'example-searches', 'Caption...', 'Ursidae,Viola arvensis,Viola epipsila,Vulpes', '')
	public void outputExampleFromCrunchedIds() {
		String cids = "04u13P2mn2nD5UxkVmWfH";
		List<Integer> ids = EntryUtilities.CRUNCHER.toList(cids);
		StringBuilder buf = new StringBuilder();
		for (Integer id : ids) {
			if (buf.length() > 0) {
				buf.append(",");
			}
			buf.append(speciesService.findEntry(id).getLatinName());
		}
		System.out.println(buf);
	}
	
	/**
	 * Right now, these "tiny" images are actually much larger than how I'm rendering them.
	 * I should either get wikicommons to resize them all for me (could download "tiny" and "embedded size") or resize here
	 */
	public String createImageDataString(String localImageRelativePath) {
		File file = new File(localImageRelativePath);
		byte[] bytes;
		try {
			bytes = IOUtils.toByteArray(new FileInputStream(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String encoded = Base64.getEncoder().encodeToString(bytes);
		return encoded;
	}
}
