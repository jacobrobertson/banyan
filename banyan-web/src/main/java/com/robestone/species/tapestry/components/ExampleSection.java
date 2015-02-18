package com.robestone.species.tapestry.components;

import org.apache.tapestry5.annotations.Parameter;

import com.robestone.species.Example;
import com.robestone.species.ExampleGroup;

public class ExampleSection {

	@Parameter(required = true)
	private ExampleGroup group;
	private Example example;
	
	public Example getExample() {
		return example;
	}
	public void setExample(Example example) {
		this.example = example;
	}

	public ExampleGroup getGroup() {
		return group;
	}
	
}
