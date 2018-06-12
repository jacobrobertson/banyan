package com.robestone.species.js;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.CrunchedIds;
import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.Example;
import com.robestone.species.ExampleGroup;
import com.robestone.species.parse.AbstractWorker;

public class JsonBuilder extends AbstractWorker {

	public static void main(String[] args) throws Exception {
//		new JsonBuilder().runExamples();
//		new JsonBuilder().runOneId(1, 6);
		new JsonBuilder().partition();
	}
	
	public void partition() throws Exception {
		
		Node root = buildTree();
		new JsonPartitioner().partition(root);
	}
	
	private Node buildTree() {
		return buildNodeRecursively(1, 0);
	}
	private Node buildNodeRecursively(Integer id, int depth) {
		Collection<Integer> ids = speciesService.findChildrenIds(id);
		Node node = new Node(id, new ArrayList<>(ids));
		int descendants = ids.size();
		for (Integer cid : ids) {
			Node cnode = buildNodeRecursively(cid, depth + 1);
			node.getChildren().add(cnode);
			descendants += cnode.getTotalDescendants();
		}
		System.out.println("buildNodeRecursively id=" + id + ", depth=" + depth + 
				", children=" + node.getChildIds().size() + ", desc=" + descendants);
		node.setTotalDescendants(descendants);
		return node;
	}
	
	public void runOneId(int id, int maxDepth) throws Exception {
		Entry e = speciesService.findEntry(id);
		Set<Integer> idsRun = new HashSet<>();
		runRecursively(e, 0, maxDepth, idsRun);
	}
	
	public void runExamples() throws Exception {
		int exampleDepth = 5;
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
					runRecursively(e, 0, exampleDepth, idsRun);
				}
				// save one "fat" file for the example
				save("f-example-" + ex.getId(), array);
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
		
		save(e.getId().toString(), e);
	}
	private void save(String name, Entry... entries) throws Exception {
		String json = toJson(entries);
		// convention because javascript is tricky this way
		String subfolder;
		if (name.charAt(0) == 'f') {
			subfolder = "f";
		} else {
			double d = Integer.parseInt(name);
			d = d / 100d;
			d = Math.ceil(d);
			int i = (int) d;
			subfolder = String.valueOf(i);
		}
		
		System.out.println(json);
		String folder = 
				"D:\\eclipse-workspaces\\git\\banyan-parent\\banyan-js\\src\\main\\webapp\\"
				+ "json\\" + subfolder + "\\" + name + ".json";
				;
		File file = new File(folder);
		FileUtils.writeStringToFile(file, json);
	}
	
	//"6691": { "cname": "Complete Metamorphosis Insects", "parentId": "6692", "alt": "Endopterygota", 
	//	"img": "15/Endopterygota.jpg", "href": "Complete_Metamorphosis_Insects_Endopterygota_6691", 
	// "height": 16, "width": 20},
	public String toJson(Entry... entries) {
		boolean firstEntry = true;
		StringBuilder buf = new StringBuilder("{\"entries\": [");
		for (Entry e : entries) {
			if (!firstEntry) {
				buf.append(", ");
			}
			firstEntry = false;
			buf.append('{');
			append(buf, false, "id", e.getId()); // first is always no comma, and id is always there
			
			append(buf, true, "cnames", getCommonNames(e));
			append(buf, true, "lname", e.getLatinName());
			append(buf, true, "parentId", e.getParentId());
			if (e.isExtinct()) {
				String extinct = "true";
				if (!e.isAncestorExtinct()) {
					extinct = "top";
				}
				append(buf, true, "extinct", extinct);
			}
	
			// TODO alt and href can be calculated on browser
//			append(buf, true, "alt", "TBD");
			// append(buf, true, "href", "TBD");
			
			if (e.getImage() != null) {
				append(buf, true, "img", e.getImage().getImagePathPart());
				append(buf, true, "tHeight", e.getImage().getTinyHeight());
				append(buf, true, "tWidth", e.getImage().getTinyWidth());
				append(buf, true, "pHeight", e.getImage().getPreviewHeight());
				append(buf, true, "pWidth", e.getImage().getPreviewWidth());
			}
	
			append(buf, true, "childrenIds", getChildrenIds(e));
			
			ShowMore showMoreIds = getShowMoreIds(e);
			
			append(buf, true, "showMoreLeafIds", showMoreIds.leafIds);
			append(buf, true, "showMoreOtherIds", showMoreIds.otherIds);

			buf.append("}");
		}
		buf.append("]}");
		return buf.toString();
	}
	private List<String> getCommonNames(Entry e) {
		List<String> cnames = e.getCommonNames();
		if (cnames == null) {
			cnames = new ArrayList<>();
		}
		if (cnames.isEmpty() && e.getCommonName() != null) {
			cnames.add(e.getCommonName());
		}
		if (cnames.size() > 1) {
			System.out.println(">>>>>>>>>>>>>>>");
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
	private void appendKey(StringBuilder buf, Object key) {
		buf.append('"');
		buf.append(key);
		buf.append("\": ");
	}
	private void appendComma(StringBuilder buf, boolean comma) {
		if (comma) {
			buf.append(", ");
		}
	}
	private <T> void append(StringBuilder buf, boolean comma, Object key, Collection<T> vals) {
		if (vals == null || vals.isEmpty()) {
			return;
		}
		appendComma(buf, comma);
		appendKey(buf, key);
		buf.append("[");
		boolean first = true;
		for (Object o : vals) {
			if (!first) {
				buf.append(", ");
			} else {
				first = false;
			}
			appendValue(buf, o);
		}
		buf.append("]");
	}
//	private void appendStrings(StringBuilder buf, boolean comma, Object key, Collection<String> vals) {
//		if (vals == null || vals.isEmpty()) {
//			return;
//		}
//		append(buf, comma, key, (Object) vals);
//	}
	private void append(StringBuilder buf, boolean comma, Object key, Object val) {
		if (val == null) {
			// in this case we don't render, as js will see it as undefined
			return;
		}
		appendComma(buf, comma);
		appendKey(buf, key);
		appendValue(buf, val);
	}
	private void appendValue(StringBuilder buf, Object val) {
		if (val instanceof Integer) {
			buf.append(val);
		} else {
			buf.append('"');
			buf.append(escape(val));
			buf.append('"');
		}
	}
	private String escape(Object val) {
		String v = val.toString();
		v = v.replace("\"", "\\\"");
		return v;
	}
	
}
