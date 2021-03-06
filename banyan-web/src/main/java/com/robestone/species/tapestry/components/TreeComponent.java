package com.robestone.species.tapestry.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Element;

import com.robestone.species.Entry;
import com.robestone.species.EntryUtilities;
import com.robestone.species.UrlIdUtilities;

public class TreeComponent extends AbstractTreeComponent {

	private static enum Button {
		detail_first, detail_indented, tree_root, menu_more, menu_less, open_children
	}
	
	public boolean beginRender(MarkupWriter writer) {

		// TODO this is happening a whole lot...
		if (getRoot() == null) {
			throw new IllegalStateException("cannot beginRender with no root");
		}

		renderNodeRecursively(writer, getRoot());

		return true;
	}
	/**
	 * Instead of rendering each child separately, we want to "clump" them together.
	 */
	private List<List<Entry>> getChildNodes(Entry entry) {
		List<List<Entry>> nodes = new ArrayList<List<Entry>>();
		if (!entry.hasChildren()) {
			return nodes;
		}
		List<Entry> node = null;
		for (Entry child: entry.getChildren()) {
			// if the child has loaded children, it will get its own node
			if (child.getLoadedChildrenSize() == 0 || child.isPinned()) {
				if (node == null) {
					node = new ArrayList<Entry>();
					nodes.add(node);
				}
				node.add(child);
			} else {
				// add child to its own node
				node = new ArrayList<Entry>();
				node.add(child);
				nodes.add(node);
				node = null;
			}
		}

		return nodes;
	}
	private void renderNodeRecursively(MarkupWriter writer, Entry... entries) {
		writer.element("table");

		// start the first row
		writer.element("tr");

		// write the node cell, and the line cell
		Element td = writer.element("td");
		Entry entry = renderNodeTable(writer, entries);

		List<List<Entry>> nodes = getChildNodes(entry);

		int nodesSize = nodes.size();
		int rowspan = nodesSize * 2;

		td.attributes("rowspan", String.valueOf(rowspan));
		writer.end(); // td

		if (entry.hasChildren()) {
			int row = 0;
			int lastRow = nodesSize - 1;
			for (List<Entry> node: nodes) {
				boolean first = (row == 0);
				// row one (if not the very first one)
				if (!first) {
					writer.element("tr");
				}

				// pad cell
				td = writer.element("td", "class", "b").raw("&nbsp;");
				if (!first) {
					td.addClassName("l");
				}
				writer.end();
				// recursive cell
				writer.element("td", "rowspan", "2");
				if (node.size() == 1) {
					Entry child = node.get(0);
					renderNodeRecursively(writer, child);
				} else {
					renderNodeRecursively(writer, (Entry[]) node.toArray(new Entry[node.size()]));
				}
				writer.end(); // td

				writer.end(); // tr

				// row two, just pad cell
				writer.element("tr");
				td = writer.element("td").raw("&nbsp;");
				if (row != lastRow) {
					td.addClassName("l");
				}
				writer.end();
				writer.end(); // tr
				row++;
			}
		} else {
			writer.end(); // tr
		}

		writer.end(); //table
	}
	private Entry renderNodeTable(MarkupWriter writer, Entry... entries) {
		writer.element("table");

		writer.element("tr");

		writer.element("td", "rowspan", 2, "class", "n");
		Entry entry = renderEntryCellContents(writer, entries);
		writer.end();

		// TODO shouldn't have to do this twice - could think of a way to return this
		List<List<Entry>> nodes = getChildNodes(entry);
		boolean showLine = nodes.size() > 1;

		if (showLine) {
			Element td = writer.element("td").raw("&nbsp;");
			if (entry.hasChildren()) {
				td.addClassName("b");
			}
			writer.end();
		}

		writer.end(); // tr for first row

		writer.element("tr");
		if (showLine) {
			writer.element("td").raw("&nbsp;");
			writer.end();
		}
		writer.end(); // tr

		writer.end(); // table

		return entry;
	}

	private Entry renderEntryCellContents(MarkupWriter writer, Entry... entries) {
//		addTopCorners(writer);
		Element div = writer.element("div");
		div.addClassName("Node");
		String originalId = getId(entries[0].getId());
		div.attribute("id", originalId);
		Entry entry;
		if (entries.length == 1) {
			
			 // TODO here is probably the best candidate the render the pinned
			if (entries[0].isPinned()) {
				// ...
				// ???
				entry = null;
			} else {
				entry = renderNodeLines(writer, entries[0]);
			}
		} else {
			entry = renderAggregatedChildNodeLines(writer, entries);
		}
		writer.end(); // div
//		addBottomCorners(writer);
		return entry;
	}
	/*
	private void addTopCorners(MarkupWriter writer) {
//		jQuery(divNode).before('<b class="xtop"><b class="xb1"></b><b class="xb2"></b><b class="xb3"></b><b class="xb4"></b></b>');
		writer.element("b", "class", "xtop");
		writer.element("b", "class", "xb1");
		writer.end();
		writer.element("b", "class", "xb2");
		writer.end();
		writer.element("b", "class", "xb3");
		writer.end();
		writer.element("b", "class", "xb4");
		writer.end();
		writer.end();
	}
	private void addBottomCorners(MarkupWriter writer) {
//		jQuery(divNode).after('<b class="xbottom"><b class="xb4"></b><b class="xb3"></b><b class="xb2"></b><b class="xb1"></b></b>');
		writer.element("b", "class", "xbottom");
		writer.element("b", "class", "xb4");
		writer.end();
		writer.element("b", "class", "xb3");
		writer.end();
		writer.element("b", "class", "xb2");
		writer.end();
		writer.element("b", "class", "xb1");
		writer.end();
		writer.end();
	}
	*/
	private String getId(int id) {
		return "node-" + id;
	}
	private Entry renderAggregatedChildNodeLines(MarkupWriter writer, Entry... entries) {
		Entry toReturn = null;
		boolean first = true;
		for (Entry entry: entries) {
			toReturn = entry;
			renderEntryLine(writer, entry, false, first);
			writer.element("br");
			writer.end();
			first = false;
		}
		return toReturn;
	}
	private void renderEntryLine(MarkupWriter writer, Entry entry, boolean isHierarchy, boolean isFirst) {
		String className;
		if (isFirst) {
			className = "EntryLine EntryLineTop";
		} else {
			className = "EntryLine";
		}
		writer.element("span", "class", className);
		boolean isRoot = (entry.getParent() == null);
		if (!isRoot) {
			writeGotoDetail(writer, entry, isHierarchy);
		} else {
			writeButton(writer, entry, "New Tree", "search.navigationbar.startover", Button.tree_root);
		}
		renderNameImageLink(writer, entry);
		writeMenuButton(writer, entry);
		writer.end(); // span EntryLine
	}
	private Entry renderNodeLines(MarkupWriter writer, Entry entry) {
		
//		System.out.println("renderNodeLines." + entry);
		
		// TODO this is the exact spot where I need to take a look at the chain of children, and turn
		// 		them into a list of children that can be looped over
		//		once the "chain" code is working, you can test the "collapse" code
		List<Entry> chain = EntryUtilities.toChain(entry);
		
		chain = EntryUtilities.collapseList(chain);
		
		// writes the first entry in the chain
		renderEntryLine(writer, entry, false, true);
		
		// look for any "chained" children like ()-()-()
		int indent = 1;
		for (int i = 1; i < chain.size(); i++) {
			writer.element("br");
			writer.end();
			writer.element("span", "class", "EntryLine");
			for (int j = 0; j < indent; j++) {
				writer.writeRaw("&nbsp;");
			}
			entry = chain.get(i);
			if (entry.isCollapsed()) {
				writeGotoDetail(writer, entry, Button.open_children);
			} else {
				writeGotoDetail(writer, entry, true);
			}

			renderNameImageLink(writer, entry);
			writeMenuButton(writer, entry);
			indent++;
			writer.end(); // span EntryLine
		}
		// returns the last node that was rendered
		return entry;
	}
	private void writeGotoDetail(MarkupWriter writer, Entry entry, boolean isHierarchy) {
		Button button = Button.detail_indented;
		if (!isHierarchy) {
			button = Button.detail_first;
		}
		writeGotoDetail(writer, entry, button);
	}
	private void writeGotoDetail(MarkupWriter writer, Entry entry, Button button) {
		writeButton(writer, entry, "Go to Details", "search.detail", button,
				UrlIdUtilities.getUrlId(entry));
	}
	private void writeMenuButton(MarkupWriter writer, Entry entry) {
		String ids;
		Button image;
		boolean isMoreOptionsAvailable = isShowChildrenNeeded(entry) || isShowMoreNeeded(entry);
		if (isMoreOptionsAvailable) {
			ids = getCrunchedIdsForShowChildren(entry);
			image = Button.menu_more;
		} else {
			ids = EntryUtilities.getCrunchedIdsForClose(getRoot(), entry.getId());;
			image = Button.menu_less;
		}
		writer.writeRaw("<a href=\"search.tree/" +
				ids + "#" + entry.getId() + "\" name=\"" + entry.getId() + "\" class=\"opener\">" +
				"<img src=\"icons/" + image.name() +
				".png\" alt=\"menu\" /></a>");
	}
	private void writeButton(MarkupWriter writer, Entry entry,
			String caption,
			String method, Button button) {
		writeButton(
				writer, entry, caption, method, button, entry.getId() + "#" + entry.getId());
	}
	private void writeButton(MarkupWriter writer, Entry entry,
			String caption,
			String method, Button button, String data) {
		if (entry.getId() > 0) {
			writer.writeRaw("<a title=\"" +
					caption + "\" href=\"" +
					method + "/" +
					data + "\">" +
					"<img src=\"icons/" +
					button.name() + ".png\" class=\"tree-" + button.name() + "\" alt=\"" + method + "\" /></a>");
		}
	}
	private Element createPreviewElement(MarkupWriter writer, Entry entry) {
		Element a = writer.element("a",
				"href", "search.detail/" + UrlIdUtilities.getUrlId(entry),
				"name", entry.getId()
				);
		if (entry.getImageLink() != null) {
			a.addClassName("preview");
		} else {
			// we only want a title if we're not doing the preview
			String title = getHoverTitle(entry);
			if (title != null) {
				a.attribute("title", title);
			}
		}
		return a;
	}
	private void renderNameImageLink(MarkupWriter writer, Entry entry) {
		if (entry.isExtinct()) {
			String eclass;
			if (!entry.isAncestorExtinct()) {
				eclass = "TopExtinct";
			} else {
				eclass = "Extinct";
			}
			writer.writeRaw("<a title=\"Extinct\" href=\"#\"><span class=\"" + eclass + "\">�</span></a>");
		}
		Element a = createPreviewElement(writer, entry);
		String commonName = getShortenedRenderableCommonName(entry);
		if (StringUtils.isEmpty(commonName)) {
			writeLatinName(writer, entry);
		} else if (entry.isCommonNameBoring()) {
			Element t = a.element("span", "class", "Boring");
			t.text(commonName);
		} else {
			a.text(commonName);
		}
		if (entry.isCommonNameSharedWithSiblings()) {
			writer.writeRaw("&nbsp;");
			writeLatinName(writer, entry);
		}
		if (entry.getImageLink() != null) {
			writer.element("img",
					"class", "Thumb",
					"src", getThumbnailUrl(entry),
					"width", entry.getImage().getTinyWidth(),
					"height", entry.getImage().getTinyHeight(),
					"alt", entry.getLatinName()
					);
			writer.end(); // img
		}
		writer.end(); // a
	}
	private void writeLatinName(MarkupWriter writer, Entry entry) {
		Element i = writer.element("i");
		i.text("(");
		i.text(entry.getLatinName());
		i.text(")");
		writer.end(); // i
	}
	public static String getThumbnailUrl(Entry entry) {
		return getThumbnailUrl(entry, "tiny");
	}
	public static String getThumbnailUrl(Entry entry, String type) {
		return "/banyan-images/" + type + "/" +
			entry.getImage().getImagePathPart();
	}
	public static String getHoverTitle(Entry entry) {
		String title;
		if (entry.getImageLink() != null) {
			if (entry.getCommonName() != null) {
				String cn;
				if (entry.getCommonNames() != null) {
					StringBuilder buf = new StringBuilder();
					for (String name: entry.getCommonNames()) {
						if (buf.length() > 0) {
							buf.append("</b><br/><b>");
						}
						buf.append(name);
					}
					cn = buf.toString();
				} else {
					cn = getFullRenderableCommonName(entry);
				}
				title = "<b>" + cn + "</b><br/>" + getLatinHtml(entry);
			} else {
				title = getLatinHtml(entry);
			}
		} else {
			if (entry.getCommonName() != null) {
				// just for the hover text - won't have html in it
				title = getFullRenderableCommonName(entry) + " (" + entry.getLatinName() + ")";
			} else {
				title = null;
			}
		}
		return title;
	}
	private static String getLatinHtml(Entry entry) {
		return "<span class=\'PopupLatin\'>(" + entry.getLatinName() + ")</span>";
	}
	public static String getFullRenderableCommonName(Entry entry) {
		return entry.getCommonName();
	}
	/**
	 * @return Ellipsed name (if needed)
	 */
	public static String getShortenedRenderableCommonName(Entry entry) {
		String commonName = entry.getCommonName();
		if (commonName == null) {
			return null;
		}
		if (entry.getCommonNames() != null) {
			commonName = entry.getCommonNames().get(0);
			if (entry.getCommonNames().size() > 1) {
				commonName = commonName + "...";
			}
		}
		return commonName;
	}
}
