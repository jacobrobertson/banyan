package com.robestone.species.js;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.robestone.species.CompleteEntry;
import com.robestone.species.parse.AbstractWorker;

public class SearchIndexBuilder extends AbstractWorker {

	public static void main(String[] args) {
		new SearchIndexBuilder().iterateOverKeys();
	}

	private int topMatchesMax = 5;
	private String letters = "abcdefghijklmnopqrstuvwxyz".toUpperCase();
	private int minKeyLen = 3;
	private int maxKeyLen = 4;
	private List<String> entryNames;
	
	private static class KeyEntry {
		String key;
		List<String> topMatches = new ArrayList<String>();
		List<KeyEntry> children = new ArrayList<KeyEntry>();
	}

	public SearchIndexBuilder() {
	}
	
	public void iterateOverKeys() {
		// init method - only call once
		entryNames = getNames();
		
		KeyEntry root = new KeyEntry();
		root.key = "";
		iterateOverKeys(root);
	}
	private void iterateOverKeys(KeyEntry key) {
		if (key.key.length() == maxKeyLen) {
			return;
		}
		for (int i = 0; i < letters.length(); i++) {
			KeyEntry nextKey = new KeyEntry();
			nextKey.key = key.key + letters.charAt(i);
			// check the min length - no point to visit 1 letter?
			boolean foundMatches = true;
			if (nextKey.key.length() >= minKeyLen) {
				foundMatches = visitKey(nextKey);
			}
				
			if (foundMatches) {
				key.children.add(nextKey);
				// we only go deeper if we found a match, otherwise there's no point
				iterateOverKeys(nextKey);
			}
		}
	}
	
	private boolean visitKey(KeyEntry key) {
//		System.out.println("visitKey." + key.key);
		
		for (String name : entryNames) {
			if (name.contains(key.key)) {
				if (key.topMatches.size() <= topMatchesMax) {
					key.topMatches.add(name);
				}
			}
		}

		boolean found = !key.topMatches.isEmpty();
		if (found) {
			System.out.println("visitKey." + key.key + "." + key.topMatches);
		}
		
		return found;
	}
	
	private List<String> getNames() {
		List<String> list = new ArrayList<String>();
		
		Collection<String> latinNames = speciesService.findAllLatinNames();
		for (String latin : latinNames) {
			CompleteEntry entry = speciesService.findEntryByLatinName(latin);
			if (entry != null) {

				if (entry.getLatinNameCleanest() != null) {
					list.add(entry.getLatinNameCleanest());
				} else if (entry.getLatinName() != null) {
					list.add(entry.getLatinName().toUpperCase());
				}
				
				if (entry.getCommonNameCleanest() != null) {
					list.add(entry.getCommonNameCleanest());
				} else if (entry.getCommonName() != null) {
					list.add(entry.getCommonName().toUpperCase());
				}
				if (list.size() % 1000 == 0) {
					System.out.println("getNames." + list.size());
				}
			}
		}
		
		return list;
	}
	
}
