package com.robestone.species.js;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.io.FileUtils;

import com.robestone.species.EntryUtilities;

// mostly just a very dumb implementation for testing purposes
public class JsonParser {

	public static void main(String[] args) throws Exception {
		JsonParser p = new JsonParser();
		p.testPartition();
	}

	private String jsonDir = System.getProperty("user.home") + "/Desktop/json-orig";;
	
	public void testPartition() throws Exception {
		Node root = parseRecursive(1);
		new JsonPartitioner().partition(root);
	}
	
	public Node parseFile(String f) throws Exception {
		File file = new File(jsonDir + "\\" + f);
		if (!file.exists()) {
			return null;
		}
		return parseFile(file);
	}
	public Node parseFile(File file) throws Exception {
		String s = FileUtils.readFileToString(file);
		Map<Integer, Node> nodes = parseString(s);
		Node root = null;
		for (Integer id : nodes.keySet()) {
			Node n = nodes.get(id);
			Node p = nodes.get(n.getEntry().getParentId());
			if (p == null) {
				root = n;
			}
			n.setParent(p);
		}
		return root;
	}

	
	public Node parseRecursive(Integer id) throws Exception {
		int sub = JsonBuilder.getSubFolder(id);
		String file = sub + "/" + id + ".json";
		Node node = parseFile(file);
		if (node == null) {
			return null;
		}
		List<Integer> actualChildIds = new ArrayList<>();
		for (Integer childId : node.getChildIds()) {
			Node childNode = parseRecursive(childId);
			if (childNode != null) {
				node.getChildren().add(childNode);
				actualChildIds.add(childId);
			}
		}
		node.getChildIds().clear();
		node.getChildIds().addAll(actualChildIds);
//		System.out.println(node.getId() + ": " + node.getChildIds());
		return node;
	}
	
	/*
	 * {"entries": [
	 * {"id": 22, "cnames": ["Thorny-headed worms"], 
	 * "lname": "Acanthocephala", "parentId": 13, 
	 * "img": "4a/Acanthocephala.jpg", "tHeight": 15, "tWidth": 20, "pHeight": 188, "pWidth": 250, 
	 * "childrenIds": [7326, 7327, 7328], "showMoreLeafIds": [225763, 225609, 94618, 225656, 225608, 7375, 7327], 
	 * "showMoreOtherIds": [7328, 7373, 7326]}]}
	 */
	public List<JsonEntry> parseWithApi(String s) throws Exception {
		List<JsonEntry> list = new ArrayList<>();
		JsonReader rdr = Json.createReader(new ByteArrayInputStream(s.getBytes()));
		JsonObject obj = rdr.readObject();
		JsonArray entries = obj.getJsonArray("entries");
		for (JsonValue val : entries) {
			obj = (JsonObject) val;
			JsonEntry entry = new JsonEntry();
			entry.setId(obj.getInt("id"));
			if (obj.containsKey("parentId")) {
				entry.setParentId(obj.getInt("parentId"));
			}
			entry.setLname(obj.getString("lname"));
			entry.setChildrenIds(getIntegers(obj, "childrenIds"));
			entry.setShowMoreLeafIds(getIntegers(obj, "showMoreLeafIds"));
			entry.setShowMoreOtherIds(getIntegers(obj, "showMoreOtherIds"));
			
			entry.setImg(getString(obj, "img"));
			if (entry.getImg() != null) {
				entry.settHeight(obj.getInt("tHeight"));
				entry.settWidth(obj.getInt("tWidth"));
				entry.setpHeight(obj.getInt("pHeight"));
				entry.setpWidth(obj.getInt("pWidth"));
			}

			if (obj.containsKey("cnames")) {
				entry.setCnames(new ArrayList<>());
				JsonArray childrenIds = obj.getJsonArray("cnames");
				for (JsonValue cname : childrenIds) {
					JsonString string = (JsonString) cname;
					entry.getCnames().add(string.getString());
				}
			}
			
			list.add(entry);
		}
		return list;
	}

	private List<Integer> getIntegers(JsonObject obj, String key) {
		List<Integer> ints = new ArrayList<>();
		if (obj.containsKey(key)) {
			JsonArray childrenIds = obj.getJsonArray(key);
			for (JsonValue cid : childrenIds) {
				JsonNumber id = (JsonNumber) cid;
				ints.add(id.intValue());
			}
		}
		return ints;
	}

	private String getString(JsonObject obj, String key) {
		if (obj.containsKey(key)) {
			return obj.getString(key);
		}
		return null;
	}
	
	public Map<Integer, Node> parseString(String s) throws Exception {
		List<JsonEntry> entries = parseWithApi(s);
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
		for (JsonEntry entry : entries) {
			nodes.put(entry.getId(), new Node(entry, entry.getId(), entry.getChildrenIds()));
		}
		return nodes;
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
	private void appendIntList(StringBuilder buf, boolean comma, Object key, Collection<Integer> vals) {
		if (vals == null || vals.isEmpty()) {
			return;
		}
		appendComma(buf, comma);
		appendKey(buf, key);
		buf.append("\"");
		List<Integer> list = new ArrayList<>(vals);
		Collections.sort(list);
		String cids = EntryUtilities.CRUNCHER.toString(vals);
		buf.append(cids);
		buf.append("\"");
	}
	private void appendStringList(StringBuilder buf, boolean comma, Object key, Collection<String> vals) {
		if (vals == null || vals.isEmpty()) {
			return;
		}
		appendComma(buf, comma);
		appendKey(buf, key);
		buf.append("[");
		boolean first = true;
		for (String o : vals) {
			if (!first) {
				buf.append(", ");
			} else {
				first = false;
			}
			appendValue(buf, o);
		}
		buf.append("]");
	}
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

	public String toJsonString(List<JsonEntry> entries) {
		boolean firstEntry = true;
		StringBuilder buf = new StringBuilder("{\"entries\": [");
		for (JsonEntry e : entries) {
			if (!firstEntry) {
				buf.append(",\n");
			}
			firstEntry = false;
			buf.append('{');
			append(buf, false, "id", e.getId()); // first is always no comma, and id is always there
			
			appendStringList(buf, true, "cnames", e.getCnames());
			append(buf, true, "lname", e.getLname());
			append(buf, true, "parentId", e.getParentId());
			append(buf, true, "rank", e.getRank());
			if (e.isExtinct()) {
				String extinct = "true";
				if (!e.isAncestorExtinct()) {
					extinct = "top";
				}
				append(buf, true, "extinct", extinct);
			}
	
			if (e.getImg() != null) {
				append(buf, true, "img", e.getImg());
				append(buf, true, "tHeight", e.gettHeight());
				append(buf, true, "tWidth", e.gettWidth());
				append(buf, true, "pHeight", e.getpHeight());
				append(buf, true, "pWidth", e.getpWidth());
				append(buf, true, "iLink", e.getWikiSpeciesLink());
			}
	
			appendIntList(buf, true, "childrenIds", e.getChildrenIds());
			
			appendIntList(buf, true, "showMoreLeafIds", e.getShowMoreLeafIds());
			appendIntList(buf, true, "showMoreOtherIds", e.getShowMoreOtherIds());

			buf.append("}");
		}
		buf.append("]}");
		return buf.toString();
	}

}
