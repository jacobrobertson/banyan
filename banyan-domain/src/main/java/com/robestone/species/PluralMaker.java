package com.robestone.species;

import java.util.ArrayList;
import java.util.List;

public class PluralMaker {
	
	private class Ending {
		String from;
		String to;
		public Ending(String from, String to) {
			this.from = from;
			this.to = to;
		}
		public String getFrom() {
			return from;
		}
		public String getTo() {
			return to;
		}
	}

	private List<Ending> endings = getEndings();

	private List<Ending> getEndings() {
		List<Ending> list = new ArrayList<Ending>();
		list.add(new Ending("esses", "ess"));
		list.add(new Ending("ies", "y"));
		list.add(new Ending("ees", "ee"));

		list.add(new Ending("ess", "esses"));
		list.add(new Ending("ee", "ees"));
		list.add(new Ending("y", "ies"));

		list.add(new Ending("s", ""));
		return list;
	}
	public String getPluralAlternate(String q) {
		for (Ending e: endings) {
			if (q.endsWith(e.getFrom())) {
				String a = q.substring(0, q.length() - e.getFrom().length());
				a = a + e.getTo();
				return a;
			}
		}
		return q + "s";
	}

}
