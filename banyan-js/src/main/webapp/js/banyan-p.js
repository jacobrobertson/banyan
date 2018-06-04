function testFunction() {
	linkChildren(db);
	buildTree($("#tree"), db[0]);
}

/**
 * @param h root Html Element
 * @param e root data Entry
 */
function buildTree(h, e) {
	var table = $("<table></table>").appendTo(h);
	var tr = $("<tr></tr>").appendTo(table);
	// this td is for the root element's info
	var td = $("<td rowspan='" + (e.children.length * 2) + "'></td>").appendTo(tr);
	buildNodeTable(td, e);
	var lastIndex = e.children.length - 1;
	for (var index = 0; index < e.children.length; index++) {
		var firstChild = (index == 0);
		if (!firstChild) {
			tr = $("<tr></tr>").appendTo(table);
		}
		// a blank td for the line segments
		var blankClass = "b";
		if (!firstChild) {
			blankClass += " l";
		}
		tr.append("<td class='" + blankClass + "'>&nbsp</td>");
		// this td holds the given child
		var childTd = $("<td rowspan='" + e.children.length + "'></td>").appendTo(tr);
		// render recursively
		buildTree(childTd, e.children[index]);
		if (index != lastIndex) {
			blankClass = " class='l'";
		} else {
			blankClass = "";
		}
		table.append("<tr><td" + blankClass + ">&nbsp;</td></tr>");
	}
}

/**
 * This is just the table element that holds the Entry Lines
 */
function buildNodeTable(h, d) {
	var showLine = (d.children.length > 1);
	var table = $("<table></table>");
	var tr = $("<tr></tr>").appendTo(table);
	 
	$("<td class='n' rowspan='2'><div class='Node'>" 
			+ d.cname + "(" + d.children.length + ")</div></td>").appendTo(tr);
	var blankTr = $("<tr>").appendTo(table);
	if (showLine) {
		$("<td class='b'>&nbsp;</td>").appendTo(tr);
		$("<td>&nbsp;</td>").appendTo(blankTr);
	}
	table.appendTo(h);
}
function buildNodeTable_REAL(htmlElement, dataElement) {
	// create the base table, TODO determine the rowspans, and padding tr if needed
	var table = $("<table></table>").appendTo(htmlElement);
	var div = $('<div id="node-6691" class="Node"></div>')
				.appendTo($('<td class="n" rowspan="2"></td>'))
				.appendTo($("<tr></tr>")).appendTo(table);

	// TODO walk down the list of nodes, add the <br>
	buildNodeEntryLine(div, dataElement);
	
	// append any padding <tr>s TODO confirm this is necessary
	table.append("<tr></tr>");
}

function buildNodeEntryLine(h, e) {
	var span = $('<span class="EntryLine EntryLineTop"></span>').appendTo(h);
	// detail button
	span.append('<a title="Go to Details" href="search.detail/' + e.href + 
			'"><img src="' + iconPath() + '/detail_first.png" class="tree-detail_first" alt="search.detail" /></a>');
	// image and link
	span.append('<a class="preview" name="' + e.id + '" href="search.detail/' + e.href + '">' 
		 + e.cname + '<img alt="' + e.alt + '" height="' + e.height + '" width="' + e.width + '" src="' + 
		 	imagePath() + '/tiny/' + e.img + '" class="Thumb" /></a>');
	// menu button
	span.append('<a href="search.tree/TODO#' + e.id + '" name="' + e.id + '" class="opener">'
			+ '<img src="' + iconPath() + '/menu_more.png" alt="menu"></a>');
}
function imagePath() {
	return "images";
}
function iconPath() {
	return "../../../../../banyan-web/src/main/webapp/icons";
}

function linkChildren(d) {
	// create the map
	var map = {};
	//alert(d.length);
	for (var i = 0; i < d.length; i++) {
		var e = d[i];
		e.children = [];
		map[e.id] = e;
		//alert(e.id + "/" + map[e.id]);
	}
	
	for (var i = 0; i < d.length; i++) {
		var e = d[i];
		var p = map[e.parentId];
		e.parent = p;
		if (p) {
			p.children.push(e);
			//alert(e.id + "/" + p.id + "/" + p.children.length);
		}
	}
}

var data = {
	"entries": [
		// example for the full tree - I won't use this until I'm ready to implement full look
		//"6691": { "cname": "Complete Metamorphosis Insects", "parentId": "6692", "alt": "Endopterygota", 
		//	"img": "15/Endopterygota.jpg", "href": "Complete_Metamorphosis_Insects_Endopterygota_6691", "height": 16, "width": 20},
		{"id": "1", "cname": "Tree of Life", "parentId": "0" },
		{"id": "2", "cname": "Mammals", "parentId": "1" },
		{"id": "3", "cname": "Plants", "parentId": "1" },
		{"id": "21", "cname": "Dog", "parentId": "2" },
		{"id": "22", "cname": "Cat", "parentId": "2" }
	]
};
var db = data.entries;
