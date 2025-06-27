package com.robestone.species.parse;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.robestone.species.Entry;

public class ExtinctReporter extends AbstractWorker {

	public static void main(String[] args) throws Exception {
		// Proganochelys
		new ExtinctReporter().runAssignExtinct();
//		new ExtinctReporter().runEntry("Proganochelys", "Incertae sedis (Testudines)");
	}
	
	public void runAssignExtinct() throws Exception {
		// get list of all entries with no parent id
		Collection<Entry> entries = speciesService.findEntriesForExtinctReport();
		System.out.println("extinct." + entries.size());
		
		int count = 0;
		int show = 10000;
		
		// for each of those, get the parent page
		for (Entry entry: entries) {
			String name = entry.getLatinName();
			count++;
			if (count % show == 0) {
				System.out.println("extinct." + count + "." + name);
			}
			String parent = entry.getParentLatinName();
			runEntry(name, parent);
		}
	}
	public void runEntry(String name, String parent) throws Exception {
		String page = WikiSpeciesCache.CACHE.readFile(parent, false);
		//Familiae (1): �<a href="/wiki/Xinjiangchelyidae" title="Xinjiangchelyidae">Xinjiangchelyidae</a></p>
		// �<i><a href="/wiki/Proganochelys
		String ename = WikiSpeciesParser.getEscapedName(name);
		Pattern pattern = Pattern.compile("�(<i>|<b>|\\s|:)*<a href=\"/wiki/" + ename + "\"");
		Matcher matcher = pattern.matcher(page);
		if (matcher.find()) {
			System.out.println("extinct." + name + " > " + parent + " >> " + matcher.group());
			speciesService.assignExtinct(name);
		}
	}

}
