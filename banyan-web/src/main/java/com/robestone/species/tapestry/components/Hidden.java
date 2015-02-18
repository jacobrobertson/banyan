package com.robestone.species.tapestry.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.corelib.base.AbstractTextField;

public class Hidden extends AbstractTextField {

	@Override
	protected void writeFieldTag(MarkupWriter writer, String value) {
		writer.element("input",

		"type", "hidden",

		"name", getControlName(),

		"id", getClientId(),

		"value", value);

		writer.end();
	}

}
