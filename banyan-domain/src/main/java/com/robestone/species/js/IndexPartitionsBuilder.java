package com.robestone.species.js;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.robestone.species.Entry;
import com.robestone.species.SpeciesService;

/**
 * Create json files from the master tree
 * @author jacob
 */
public class IndexPartitionsBuilder {

	private int maxPartitionSize = 300;
	private String pathChars = "0123456789abcdefghijklmnopqrstuvwxyz";
	private SpeciesService speciesService;

	public IndexPartitionsBuilder(SpeciesService speciesService) {
		this.speciesService = speciesService;
	}
	public void partition(Node node) {
		System.out.println(">JsonPartitioner.assignParents");
		assignParents(node);
		System.out.println(">JsonPartitioner.assignSinglePartitions");
		assignSinglePartitions(node);
		System.out.println(">JsonPartitioner.analyze");
		new Stats().analyze(node).output();
		System.out.println(">JsonPartitioner.mergeMiscPartitions");
		mergeMiscPartitions(node);
		new Stats().analyze(node).output();
		System.out.println(">JsonPartitioner.assignPaths");
		node.setFileKey(getPathToken(0));
		assignPaths(node);
		System.out.println(">JsonPartitioner.testAllNodesCanFindTheirPartition");
		testAllNodesCanFindTheirPartition(node);
		System.out.println("<JsonPartitioner");
	}
	private void assignPaths(Node node) {
		String pathPart = node.getFileKey();
		List<Node> byNodeId = new ArrayList<>(node.getChildren());
		Collections.sort(byNodeId, NodeIdComparator);
		int pos = 0;
		for (Node child : byNodeId) {
			child.setFileKey(pathPart + getPathToken(pos));
			pos++;
			assignPaths(child);
		}
//		System.out.println("createPaths." + node.getId() + "." + node.getFileKey());
	}
	private void assignSinglePartitions(Node node) {
		node.getPartition().add(node);
		for (Node child : node.getChildren()) {
			assignSinglePartitions(child);
		}
	}
	private void assignParents(Node node) {
		for (Node child : node.getChildren()) {
			child.setParent(node);
			assignParents(child);
		}
	}
	private String getPathToken(int pos) {
		if (pos < pathChars.length()) {
			return pathChars.substring(pos, pos + 1);
		} else {
			return "_" + getPathToken(pos - pathChars.length());
		}
	}
	// merges nearby misc partitions that are small enough
	private void mergeMiscPartitions(Node node) {
		// walk down to the very lowest level first, and work our way back up
		boolean areAnyChildrenInAncestorPartitions = false;
		for (Node child : node.getChildren()) {
			if (child.getPartition().size() < maxPartitionSize) {
				mergeMiscPartitions(child);
			}
			if (child.isInAncestorPartition()) {
				areAnyChildrenInAncestorPartitions = true;
			}
		}
		
		// we can't start a new partition if any of this node's children are already in an ancestor partition
		// because that would break the lookup algorithm
		if (areAnyChildrenInAncestorPartitions) {
			return;
		}
		
		// now that the children are merged, try to merge as many of those as possible into this partition
		// merge partitions from smallest to largest, to allow us to merge as many as we can
		List<Node> byPartitionSize = new ArrayList<>(node.getChildren());
		Collections.sort(byPartitionSize, PartitionSizeComparator);
		for (Node child : byPartitionSize) {
			int nSize = node.getPartition().size();
			int cSize = child.getPartition().size();
			boolean isChildLeaf = child.getChildIds().isEmpty();
			if ((cSize > 0 && nSize + cSize <= maxPartitionSize) || isChildLeaf) {
				node.getPartition().addAll(child.getPartition());
				child.getPartition().clear();
			}
		}
	}
	private PartitionSizeComparator PartitionSizeComparator = new PartitionSizeComparator();
	private class PartitionSizeComparator implements Comparator<Node> {
		@Override
		public int compare(Node n1, Node n2) {
			return n1.getPartition().size() - n2.getPartition().size();
		}
	}
	private NodeIdComparator NodeIdComparator = new NodeIdComparator();
	private class NodeIdComparator implements Comparator<Node> {
		@Override
		public int compare(Node n1, Node n2) {
			return n1.getId() - n2.getId();
		}
	}
	
	private class Stats {
		private int partitionSizeFactor = 10;
		private Map<Integer, Integer> partitionSizeCounts = new HashMap<Integer, Integer>();
		
		private int mostChildrenId;
		private int mostChildren = 0;
		private int largestPartition = 0;
		private int totalNodes = 0;
		private int totalPartitions = 0;
		private int totalMiscPartitions = 0;
		// this is just a double-check that everything is working
		private int nodesInPartitions = 0;

		public Stats analyze(Node node) {
			totalNodes++;
			int children = node.getChildren().size();
			if (children > mostChildren) {
				mostChildren = children;
				mostChildrenId = node.getId();
			}
			int pSize = node.getPartition().size();
			if (pSize > 0) {
				if (pSize > largestPartition) {
					largestPartition = pSize;
				}
				totalPartitions++;
				nodesInPartitions += pSize;
				
				int partitionBucket = (pSize / partitionSizeFactor) * partitionSizeFactor;
				Integer countPartitionBucket = partitionSizeCounts.get(partitionBucket);
				if (countPartitionBucket == null) {
					countPartitionBucket = 0;
				}
				countPartitionBucket++;
				partitionSizeCounts.put(partitionBucket, countPartitionBucket);
				if (!node.getPartition().contains(node)) {
					totalMiscPartitions++;
				}
			}
			for (Node child : node.getChildren()) {
				analyze(child);
			}
			return this;
		}
		
		public void output() {
			System.out.println(
					"totalNodes: " + totalNodes + 
					", nodesInPartitions: " + nodesInPartitions + 
					", totalPartitions: " + totalPartitions + 
					", totalMiscPartitions: " + totalMiscPartitions + 
					", mostChildren: " + mostChildren + "/" + mostChildrenId +
					", partitionSizeCounts: " + partitionSizeCounts + 
					", largestPartition: " + largestPartition
					);
		}
	}
	
	public void testAllNodesCanFindTheirPartition(Node node) {
		
		// the strategy is more complex in JS, because we are trying to find the right file
		// but for now just confirm that each node is found in the right place
		Node test = node;
		while (true) {
			String message = ("Looking for " + node.getId() + "/" + node.getFileKey() + " in partition " + test.getId() + "/" + test.getFileKey());
			if (test.getPartition().contains(node)) {
				if (!node.getFileKey().startsWith(test.getFileKey())) {
					throw new IllegalStateException("Paths are messed up: " + message);
				}
//				System.out.println("Found: " + message);
				break;
			} else if (test != node && !test.getPartition().isEmpty()) {
				throw new IllegalStateException("Node not found in expected parent: " + message);
			}
			test = test.getParent();
			if (test == null) {
				throw new IllegalStateException("Ran out of parents: " + message);
			}
		}
		
		for (Node child : node.getChildren()) {
			testAllNodesCanFindTheirPartition(child);
		}
	}
	
	public Map<String, String> getAndAssignPartitionMap(Node node) {
		// build up the list of path Keys
		// sort them (not that important though)
		// the index is a map, and will allow us to lookup the file name by fileKey
		// the file name is split up into 10 folders
		
		List<String> keys = new ArrayList<>();
		getAllPartitionKeys(node, keys);
		
		Collections.sort(keys);
		int max = keys.size();
		int numberOfBuckets = 10;
		int bucketSize = max / numberOfBuckets + 1;
		
		Map<String, String> map = new HashMap<>();
		
		for (int i = 0; i < max; i++) {
			int bucket = i / bucketSize;
			map.put(keys.get(i), bucket + "/" + keys.get(i));
		}
		
		assignPartitionPaths(node, map);
		
		return map;
	}
	private void getAllPartitionKeys(Node node, List<String> keys) {
		if (!node.getPartition().isEmpty()) {
			keys.add(node.getFileKey());
		}
		for (Node child : node.getChildren()) {
			getAllPartitionKeys(child, keys);
		}
	}
	private void assignPartitionPaths(Node node, Map<String, String> map) {
		if (!node.getPartition().isEmpty()) {
			String path = map.get(node.getFileKey());
			node.setFilePath(path);
		}
		for (Node child : node.getChildren()) {
			assignPartitionPaths(child, map);
		}
	}
	
	public String getPartitionIndexFile(Map<String, String> keys) {
		StringBuilder buf = new StringBuilder();
		
		buf.append("{");
		boolean first = true;
		for (String name : keys.keySet()) {
			if (!first) {
				buf.append(", ");
			} else {
				first = false;
			}
			buf.append("\"");
			buf.append(name);
			buf.append("\": \"");
			buf.append(keys.get(name));
			buf.append("\"");
		}
		buf.append("}");
		return buf.toString();
	}
	public void partitionFromDB() throws Exception {
		System.out.println(">partitionFromDB.buildTree");
		Node root = buildTree(speciesService);
		System.out.println(">partitionFromDB.partitionAndSave");
		partitionAndSave(root);
		System.out.println("<partitionFromDB");
	}
	// recursively starts with one file
	public void partitionFromFileSystem() throws Exception {
		Node root = JsonFileUtils.parseRecursive(1);
		partitionAndSave(root);
	}
	public void partitionAndSave(Node root) throws Exception {
		
		System.out.println(">partitionAndSave.partitioner.partition");
		partition(root);
		
		System.out.println(">partitionAndSave.partitioner.getAndAssignPartitionMap");
		Map<String, String> pMap = getAndAssignPartitionMap(root);
		
		System.out.println(">partitionAndSave.outputPartitions");
		outputPartitions(root);
		String index = getPartitionIndexFile(pMap);
		String folder = JsonFileUtils.outputDir + "/p/index.json";
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
					Entry ientry = null;
					if (eentry.getImage() != null) {
						ientry = speciesService.findEntry(eentry.getImage().getEntryId());
					}
					jentry = JsonFileUtils.toJsonEntry(eentry, ientry, speciesService);
				}
				entries.add(jentry);
			}
			JsonFileUtils.saveByFileName(fileName, entries);
		}
		for (Node child : node.getChildren()) {
			outputPartitions(child);
		}
	}
	
	public static Node buildTree(SpeciesService speciesService) {
		int[] count = {0};
		return buildNodeRecursively(1, 0, count, speciesService);
	}
	private static Node buildNodeRecursively(Integer id, int depth, int[] count, SpeciesService speciesService) {
		count[0]++;
		Collection<Integer> ids = speciesService.findChildrenIds(id);
		Node node = new Node(null, id, new ArrayList<>(ids));
		int descendants = ids.size();
		if (count[0] % 10_000 == 0) {
			System.out.println("buildNodeRecursively id=" + id + ", depth=" + depth + 
					", children=" + node.getChildIds().size() + ", desc=" + descendants + ",count=" + count[0]);
		}
		for (Integer cid : ids) {
			Node cnode = buildNodeRecursively(cid, depth + 1, count, speciesService);
			node.getChildren().add(cnode);
			cnode.setParent(node);
			descendants += cnode.getTotalDescendants();
		}
		node.setTotalDescendants(descendants);
		return node;
	}
	public static void testPartition() throws Exception {
		Node root = JsonFileUtils.parseRecursive(1);
		new IndexPartitionsBuilder(null).partition(root);
	}

}
