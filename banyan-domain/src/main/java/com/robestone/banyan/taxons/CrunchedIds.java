package com.robestone.banyan.taxons;

import java.util.List;

public class CrunchedIds {

	private List<Integer> ids;
	private List<Integer> pinnedIds;
	private String crunchedIds;
	private String pinnedMask;

	public CrunchedIds(List<Integer> ids, List<Integer> pinnedIds,
			String crunchedIds, String pinnedMask) {
		this.ids = ids;
		this.pinnedIds = pinnedIds;
		this.crunchedIds = crunchedIds;
		this.pinnedMask = pinnedMask;
	}

	public List<Integer> getIds() {
		return ids;
	}
	public String getCrunchedIds() {
		return crunchedIds;
	}
	public String toString() {
		return getCrunchedIds();
	}
	public int size() {
		return ids.size();
	}
	public List<Integer> getPinnedIds() {
		return pinnedIds;
	}
	public String getPinnedMask() {
		return pinnedMask;
	}
}
