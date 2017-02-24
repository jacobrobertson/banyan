package com.robestone.species.tapestry.components;

import org.apache.tapestry5.MarkupWriter;

public class Corners {

	void beginRender(MarkupWriter writer) {
		writer.writeRaw(
//				"<b class=\"xtop\"><b class=\"xb1\"></b><b class=\"xb2\"></b><b class=\"xb3\"></b><b class=\"xb4\"></b></b>"
						"<div class=\"Node\">");
    }

    void afterRender(MarkupWriter writer) {
        writer.writeRaw(
        		"</div>");
        //<b class=\"xbottom\"><b class=\"xb4\"></b><b class=\"xb3\"></b><b class=\"xb2\"></b><b class=\"xb1\"></b></b>");
    }

}
