package com.jacobrobertson.banyan.search;

public class Entry {

	private Integer id;
	private String cids;
	
	public Entry() {
	}

	public Entry(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCids() {
		return cids;
	}

	public void setCids(String cids) {
		this.cids = cids;
	}

}
