package com.robestone.species.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.robestone.species.CompleteEntry;
import com.robestone.species.LogHelper;

public class LettersAnalyzer extends AbstractWorker {

	public static void main(String[] args) {
		new LettersAnalyzer().run();
	}
	public void run() {
		Map<Character, Set<String>> letters = new HashMap<Character, Set<String>>();
		Collection<CompleteEntry> all = 
			speciesService.findCompleteTreeFromPersistence().getEntries();
		for (CompleteEntry e: all) {
//			addLetters(letters, e.getCommonName());
			addLetters(letters, e.getLatinName());
		}
		List<Character> list = new ArrayList<Character>(letters.keySet());
		Collections.sort(list);
		for (Character c: list) {
			if (Character.isLetter(c)) {
				continue;
			}
			int pos = okayLetters.indexOf(c);
			if (pos >= 0) {
				continue;
			}
			LogHelper.speciesLogger.info(c + " (" + letters.get(c).size() + "): " + letters.get(c));
		}
	}
//	String okayLetters = " &'(),\"/-.";
	String okayLetters = "()- .";
	private void addLetters(Map<Character, Set<String>> map, String s) {
		s = StringUtils.trimToEmpty(s);
		s = s.toUpperCase();
		for (int i = 0; i < s.length(); i++) {
			Character c = s.charAt(i);
			Set<String> set = map.get(c);
			if (set == null) {
				set = new HashSet<String>();
				map.put(c, set);
			}
			set.add(s);
		}
	}
	
}
