package com.robestone.species.js;

import java.util.ArrayList;
import java.util.List;

/**
 * Create json files from the master tree
 * @author jacob
 */
public class JsonPartitioner {

	public int partition(Node node) {
		
		int maxCount = 100;
		
		List<Node> allGathered = new ArrayList<>();
		List<Node> nextChildren = new ArrayList<>();
		List<Node> lastChildren = null;
		nextChildren.add(node);
		
		while (nextChildren != null && nextChildren.isEmpty() == false) {
			allGathered.addAll(nextChildren);
			lastChildren = nextChildren;
			// empty or null list means there's no more work to do "at this level"
			nextChildren = gather(lastChildren, maxCount - allGathered.size());
		}
		
		// TODO first pass of this, don't worry about misc buckets, just output all
		
		// TODO find the leafs - they should all be one level
		
		// TODO create the miscellaneous buckets
		
		int countPartitions = 1;
		
		// TODO recurse last children (children with enough descendants to make it worth)
		List<Node> toRecurse = gather(lastChildren, -1);
		for (Node child : toRecurse) {
			countPartitions += partition(child);
		}

		output(node.getId().toString(), allGathered, countPartitions);
		
		return countPartitions;
	}
	
	private void output(String fileName, List<Node> nodes, int countPartitions) {
		System.out.println(fileName + ".json(size=" + nodes.size() + ", partitions=" + countPartitions + 
				") = " + nodes.toString());
	}
	
	private List<Node> gather(List<Node> children, int maxToGather) {
		List<Node> all = new ArrayList<>();
		for (Node child : children) {
			all.addAll(child.getChildren());
			if (maxToGather > 0 && all.size() > maxToGather) {
				return null;
			}
		}
		return all;
	}
	
}
