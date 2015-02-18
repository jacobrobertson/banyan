package com.robestone.species;

import java.util.List;

public class ExampleGroup {

	private String caption;
	private int id;
	private boolean showExampleGroupName;
	
	private List<Example> examples;
	
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public List<Example> getExamples() {
		return examples;
	}
	public void setExamples(List<Example> examples) {
		this.examples = examples;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isShowExampleGroupName() {
		return showExampleGroupName;
	}
	public void setShowExampleGroupName(boolean showExampleGroupName) {
		this.showExampleGroupName = showExampleGroupName;
	}
	
}
