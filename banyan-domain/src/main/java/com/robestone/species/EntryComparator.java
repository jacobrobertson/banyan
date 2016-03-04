package com.robestone.species;

import java.util.Comparator;

public class EntryComparator implements Comparator<Entry> {
	
	public int compare(Entry e1, Entry e2) {
		String n1 = getCompareName(e1);
		String n2 = getCompareName(e2);
		// allow this to sort when we have no names...
		// this is really just to handle some assembling type code
		if (n1 == null || n2 == null) {
			return e1.getId().compareTo(e2.getId());
		}
		try {
			return n1.compareToIgnoreCase(n2);
		} catch (NullPointerException npe) {
			LogHelper.speciesLogger.info("EntryComparator.compare." + e1.getId() + "/" + e2.getId());
			throw new RuntimeException(npe);
		}
	}
	
	public static String getCompareName(Entry e1) {
		String name = e1.getCommonName();
		if (name == null) {
			name = e1.getLatinName();
		} else {
			name = name + " - " + e1.getLatinName();
		}
		return name;
	}
}
