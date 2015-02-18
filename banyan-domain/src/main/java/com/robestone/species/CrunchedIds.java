package com.robestone.species;

import java.util.Set;

public class CrunchedIds {

	private Set<Integer> ids;
	private String crunchedIds;
	private IdCruncher cruncher;

	public CrunchedIds(Set<Integer> ids, IdCruncher cruncher) {
		this.ids = ids;
		this.cruncher = cruncher;
	}
	public CrunchedIds(String crunchedIds, IdCruncher cruncher) {
		this.crunchedIds = crunchedIds;
		this.cruncher = cruncher;
	}
	public Set<Integer> getIds() {
		if (ids == null) {
			ids = cruncher.toSet(crunchedIds);
		}
		return ids;
	}
	public String getCrunchedIds() {
		if (crunchedIds == null) {
			crunchedIds = cruncher.toString(ids);
		}
		return crunchedIds;
	}
	public String toString() {
		return getCrunchedIds();
	}
	public int size() {
		if (ids == null) {
			return getIds().size();
		} else {
			return ids.size();
		}
	}
}
