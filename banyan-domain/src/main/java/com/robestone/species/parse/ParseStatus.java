package com.robestone.species.parse;

import java.util.Date;

public class ParseStatus {

	public static final String FOUND = "FOUND";
	public static final String DONE = "DONE";
	
	public static final String AUTHORITY = "AUTH";
	
	private String url;
	private String urlUpper;
	private String status;
	private Date date;
	private String type;
	private boolean isDeleted;
	private Integer crawlId;
	
	public Integer getCrawlId() {
		return crawlId;
	}
	public void setCrawlId(Integer crawlId) {
		this.crawlId = crawlId;
	}
	public boolean isAuth() {
		return AUTHORITY.equals(type);
	}
	public boolean isDone() {
		return DONE.equals(status);
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLatinName() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
		this.urlUpper = url.toUpperCase();
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	@Override
	public String toString() {
		return url;
	}
	@Override
	public int hashCode() {
		return urlUpper.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		ParseStatus that = (ParseStatus) obj;
		return this.urlUpper.equals(that.urlUpper);
	}
	
}
