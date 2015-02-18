package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robestone.species.CommonNameSplitter;
import com.robestone.species.CompleteEntry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.ParenthesisSplitter;

public class CommonNamesWithParenthesisAnalyzer extends AbstractWorker {

	public static void main(String[] args) {
		new CommonNamesWithParenthesisAnalyzer().fix();
	}
	private CommonNameSplitter commonNameSplitter = new CommonNameSplitter();

	public void fix() {
		Collection<CompleteEntry> all = 
			speciesService.findCompleteTreeFromPersistence().getEntries();
		for (CompleteEntry e: all) {
			fix(e);
		}
	}
	public void fix(CompleteEntry e) {
		String originalName = e.getCommonName();
		commonNameSplitter.assignCommonNames(e);
		List<String> names = e.getCommonNames();
		if (names == null) {
			names = new ArrayList<String>();
			names.add(e.getCommonName());
		}
		boolean changed = false;
		for (int i = 0; i < names.size(); i++) {
			String commonName = names.get(i);
			if (commonName == null) {
				continue;
			}
			String fixed = ParenthesisSplitter.removeUnwantedParenthenticalParts(commonName);
			if (fixed != null && !commonName.equals(fixed)) {
				changed = true;
				names.set(i, fixed);
			}
		}
		if (changed) {
			e.setCommonNames(names);
			commonNameSplitter.joinCommonName(e);
			System.out.println(e.getLatinName() + " / " + originalName + " >> " + e.getCommonName());
			// TODO persist
		}
	}
	public void analyze() {
		Set<String> innersClean = new HashSet<String>();
		Set<String> inners = new HashSet<String>();
		Collection<CompleteEntry> all = 
			speciesService.findCompleteTreeFromPersistence().getEntries();
		for (CompleteEntry e: all) {
			commonNameSplitter.assignCommonNames(e);
			Collection<String> names = e.getCommonNames();
			if (names == null) {
				names = new ArrayList<String>();
				names.add(e.getCommonName());
			}
			for (String cn: names) {
				if (cn == null) {
					continue;
				}
				cn = ParenthesisSplitter.removeUnwantedParenthenticalParts(cn);
				int quote = cn.indexOf('"');
				if (quote > 0) {
					continue;
				}
				int pos = cn.indexOf('(');
				if (pos < 0) {
					continue;
				}
				int p2 = cn.indexOf(')');
				if (p2 < 0) {
					System.out.println("Weird: " + cn);
					continue;
				}
				String inner = cn.substring(pos + 1, p2);
				String innerClean = EntryUtilities.getClean(inner, false);
				boolean added = innersClean.add(innerClean);
				if (added) {
					System.out.println(e.getCommonName() + "/" + cn + "/" + e.getLatinName());
					inners.add(inner);
				}
			}
		}
		List<String> list = new ArrayList<String>(inners);
		Collections.sort(list);
		for (String s: list) {
			System.out.println("\"" + s + "\", Disambiguation,");
		}
	}
	
}
