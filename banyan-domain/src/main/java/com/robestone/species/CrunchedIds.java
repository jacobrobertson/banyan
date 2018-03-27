package com.robestone.species;

import java.util.Set;

public class CrunchedIds {

	private Set<Integer> ids;
	private Set<Integer> pinnedIds;
	private String crunchedIds;
	private IdCruncher cruncher;
	private String pinnedMask;

	public CrunchedIds(Set<Integer> ids, Set<Integer> pinnedIds,
			String crunchedIds, String pinnedMask) {
		this.ids = ids;
		this.pinnedIds = pinnedIds;
		this.crunchedIds = crunchedIds;
		this.pinnedMask = pinnedMask;
	}
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
	public Set<Integer> getPinnedIds() {
		return pinnedIds;
	}
	public String getPinnedMask() {
		return pinnedMask;
	}
}
