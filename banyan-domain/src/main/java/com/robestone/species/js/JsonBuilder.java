package com.robestone.species.js;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.CrunchedIds;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Example;
import com.robestone.species.ExampleGroup;
import com.robestone.species.parse.AbstractWorker;
import com.robestone.species.parse.ImagesCreater;

public class JsonBuilder extends AbstractWorker {

	public static void main(String[] args) throws Exception {

		// should delete all json first
		new JsonBuilder().runGenerateFullJsonDB();
		
//		new JsonBuilder().runExamples();
//		new JsonBuilder().outputRandomFileIndex();
//		new JsonBuilder().partitionFromFileSystem2();
		
//		new JsonBuilder().outputExampleFromCrunchedIds();
		
//		new JsonBuilder().runOneId(1, 6);
//		new JsonBuilder().partitionFromFileSystem2();
	}
	
	private String outputDir = "../banyan-js/src/main/webapp/json";
	private JsonParser parser = new JsonParser();
	
	public void runGenerateFullJsonDB() throws Exception {
		runExamples();
		outputRandomFileIndex();
		partitionFromDB();
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
		FileUtils.writeStringToFile(file, index);
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
	
	public void runOneId(int id, int maxDepth) throws Exception {
		Entry e = speciesService.findEntry(id);
		Set<Integer> idsRun = new HashSet<>();
		runRecursively(e, 0, maxDepth, idsRun);
	}
	
	public void runExamples() throws Exception {
		int exampleDepth = 0;
		Set<Integer> idsRun = new HashSet<>();
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
					// save all descendants files so I can test opening those
					if (exampleDepth > 0) {
						runRecursively(e, 0, exampleDepth, idsRun);
					}
				}
				// save one "fat" file for the example
				save(true, ex.getSimpleTitle(), array);
			}
		}
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
	
	private void save(Entry e) throws Exception {
		
		// each file is going to have all the entries needed
		// - all ancestors up to the root
		// - all interesting ids
		// - all children ids
		
		save(false, e.getId().toString(), e);
	}
	public static int getSubFolder(int id) {
		double d = id;
		d = d / 100d;
		d = Math.ceil(d);
		int i = (int) d;
		return i;
	}
	private void save(boolean isName, String name, Entry... entries) throws Exception {
		List<JsonEntry> jentries = new ArrayList<>();
		for (Entry e : entries) {
			JsonEntry je = toJsonEntry(e);
			jentries.add(je);
		}
		saveByFolders(isName, name, jentries);
	}
	private void saveByFolders(boolean isName, String name, List<JsonEntry> entries) throws Exception {
		// convention because javascript is tricky this way
		String subfolder;
		if (isName) {
			subfolder = "f";
		} else {
			int id = Integer.parseInt(name);
			subfolder = "n/" + String.valueOf(getSubFolder(id));
		}
		String fileName = subfolder + "/" + name + ".json";
		saveByFileName(fileName, entries);
	}
	private void saveByFileName(String fileName, List<JsonEntry> entries) throws Exception {
		String json = parser.toJsonString(entries);
		
		System.out.println(json);
		String folder = outputDir + "\\" + fileName;
		
		File file = new File(folder);
		FileUtils.writeStringToFile(file, json);
	}
	
	//"6691": { "cname": "Complete Metamorphosis Insects", "parentId": "6692", "alt": "Endopterygota", 
	//	"img": "15/Endopterygota.jpg", "href": "Complete_Metamorphosis_Insects_Endopterygota_6691", 
	// "height": 16, "width": 20},
	public String toJson(Entry... entries) {
		List<JsonEntry> jentries = new ArrayList<>();
		for (Entry e : entries) {
			JsonEntry je = toJsonEntry(e);
			jentries.add(je);
		}
		return parser.toJsonString(jentries);
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

		// TODO research why some of these have one but not the other
		if (e.getImage() != null && e.getImageLink() != null) {
			je.setImg(e.getImage().getImagePathPart());
			je.settHeight(e.getImage().getTinyHeight());
			je.settWidth(e.getImage().getTinyWidth());
			je.setpHeight(e.getImage().getPreviewHeight());
			je.setpWidth(e.getImage().getPreviewWidth());
			je.setWikiSpeciesLink(ImagesCreater.getImageFileName(e));
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
		// this is a temp impl until I have the real deal
		List<ExampleGroup> groups = new ArrayList<>();
		// I'm just getting the ones that aren't duplicates
		groups.add(examplesService.getFamilies());
		groups.add(examplesService.getHaveYouHeardOf());
		groups.add(examplesService.getOtherFamilies());
		groups.add(examplesService.getYouMightNotKnow());
		List<String> names = new ArrayList<>();
		for (ExampleGroup eg : groups) {
			for (Example ex : eg.getExamples()) {
				String name = ex.getSimpleTitle();
				names.add(name);
			}
		}
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
		
		FileUtils.writeStringToFile(new File(outputDir + "/f/random.json"), buf.toString());
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
	
}
