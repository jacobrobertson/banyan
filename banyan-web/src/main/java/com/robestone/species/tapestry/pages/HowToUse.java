package com.robestone.species.tapestry.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Cached;

import com.robestone.species.Entry;

public class HowToUse extends AbstractPage {

	@Cached
	public List<Entry> getEntries() {
		Entry one = getSpeciesService().findEntry(73488);
		List<Entry> list = new ArrayList<Entry>();
		list.add(one);
		return list;
	}
	
}
