function testFunction() {
	$("#tree").empty();
	addEntriesToMasterMap(data2.entries);
	buildTree($("#tree"), dbMap["1"]);
}
function addChildTestFunction() {
	addEntriesToMasterMap(data2.entries);
	testFunction();
}
function testFunction2() {
	// TODO
}
function buildTree(h, e) {
	var table = $("<table id='tree-" + e.id + "'></table>").appendTo(h);
	buildRowsForTree(table, e);
}
function buildRowsForTree(table, e) {
	var tr = $("<tr></tr>").appendTo(table);
	// this td is for the root element's info
	var td = $("<td rowspan='" + (e.children.length * 2) + "'></td>").appendTo(tr);
	appendEntryLinesElement(td, e);
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
		var childTd = $("<td rowspan='2'></td>").appendTo(tr);
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

function buildEntryLinesElement(e) {
	var showLine = (e.children.length > 1);
	var table = $("<table></table>");
	var tr = $("<tr></tr>").appendTo(table);
	 
	$("<td class='n' rowspan='2'><div id='node-" + e.id + "' class='Node'>" 
			+ e.cname + "</div></td>").appendTo(tr);
	
	// add the necessary blank cells, but do not set the class (b, l) here
	tr.append($("<td>&nbsp;</td>")); // this is the cell that will need the class TODO add an id?
	table.append($("<tr><td>&nbsp;</td></tr>"));
	
	return table;
}

/**
 * This is just the table element that holds the Entry Lines
 */
function appendEntryLinesElement(h, e) {
	var showLine = (e.children.length > 1);
	var table = $("<table></table>");
	var tr = $("<tr></tr>").appendTo(table);
	var td = $("<td class='n' rowspan='2'></td>").appendTo(tr);
	var div = $("<div id='node-" + e.id + "' class='Node'></div>").appendTo(td); 
	buildNodeEntryLine(div, e, 0);
	
	if (e.collapsed) {
		for (var i = 0; i < e.collapsed.length; i++) {
			div.append($("<br/>"));
			buildNodeEntryLine(div, e.collapsed[i], i + 1);
		}
	}
	
	var blankTr = $("<tr>").appendTo(table);
	if (showLine) {
		$("<td class='b'>&nbsp;</td>").appendTo(tr);
		$("<td>&nbsp;</td>").appendTo(blankTr);
	}
	table.appendTo(h);
}

function buildNodeEntryLine(h, e, depth) {
	var span = $('<span class="EntryLine EntryLineTop"></span>').appendTo(h);
	// detail button
	var detailIcon = "detail_first.png";
	var detailClass = "tree-detail_first";
	if (depth > 0) {
		detailIcon = "detail_indented.png";
		detailClass = "tree-detail_indented";
	}
	var pad = getNbsps(depth);
	span.append(pad + '<a title="Go to Details" href="search.detail/' + e.href + 
			'"><img src="' + iconPath() + '/' + detailIcon + '" class="' +
			detailClass + '" alt="search.detail" /></a>');
	// image and link
	span.append('<a class="preview" name="' + e.id + '" href="search.detail/' + e.href + '">' 
		 + e.cname + '<img alt="' + e.alt + '" height="' + e.height + '" width="' + e.width + '" src="' + 
		 	imagePath() + '/tiny/' + e.img + '" class="Thumb" /></a>');
	// menu button
	span.append('<a href="search.tree/TODO#' + e.id + '" name="' + e.id + '" class="opener">'
			+ '<img src="' + iconPath() + '/menu_more.png" alt="menu"></a>');
}
function getNbsps(count) {
	var pad = "";
	for (var i = 0; i < count; i++) {
		pad = pad + "&nbsp";
	}
	return pad;
}
function imagePath() {
	return "images";
}
function iconPath() {
	return "icons";
}

function addEntriesToMasterMap(entries) {
	
	// add each item to the map
	for (var i = 0; i < entries.length; i++) {
		var e = entries[i];
		e.children = [];
		enhanceEntryTemp(e);
		// TODO this might not be a simple replacement, depending on the operation
		dbMap[e.id] = e;
	}
	
	// link each child to its parent
	for (var i = 0; i < entries.length; i++) {
		var e = entries[i];
		var p = dbMap[e.parentId];
		e.parent = p;
		if (p) {
			var dname = getEntryDisplayName(e);
			var foundPos = false;
			var foundSame = false;
			// TODO - need to insert in the correct position right now
			for (var j = 0; j < p.children.length; j++) {
				var c = p.children[j];
				var dname2 = getEntryDisplayName(c);
				if (dname < dname2) {
					p.children.splice(j, 0, e);
					foundPos = true;
					break;
				} else if (e.id == c.id) {
					foundSame = true;
					p.children[j] = e;
					break;
				}
			}
			if (!foundPos && !foundSame) {
				p.children.push(e);
			}
		}
	}
	
	collapseNodes(getRoot(entries[0]));
}
function getRoot(e) {
	while (true) {
		var p = dbMap[e.parentId];
		if (p) {
			e = p;
		} else {
			break;
		}
	}
	return e;
}
// collapse nodes into the "chains" - whenever there is a child with just one child, etc, roll it up
// TODO add the "..." behavior for long chains
function collapseNodes(e) {
	if (e.children.length == 0) {
		return;
	} else if (e.children.length > 1) {
		// don't do anything for this node, but recurse
		for (var i = 0; i < e.children.length; i++) {
			collapseNodes(e.children[i]);
		}
	} else {
		e.collapsed = [];
		var r = e;
		while (r.children.length == 1) {
			var c = r.children[0];
			e.collapsed.push(c);
			r.children = [];
			r = c;
		}
	}
}
//function collapseNode(e) {
//	for (var i = 0; i < entries.length; i++) {
//		var e = entries[i];
//	}
//}

// this is temp until I start using actual data
function enhanceEntryTemp(e) {
	e.alt = "Endopterygota"; 
	e.img = "15/Endopterygota.jpg";
	e.href = "Complete_Metamorphosis_Insects_Endopterygota_6691";
	e.height = 16;
	e.width = 20;
}

function getEntryDisplayName(e) {
	// TODO we will care about parens, and boring, etc...
	return e.cname;
}

var data = {
		"entries": [
			// example for the full tree - I won't use this until I'm ready to implement full look
			//"6691": { "cname": "Complete Metamorphosis Insects", "parentId": "6692", "alt": "Endopterygota", 
			//	"img": "15/Endopterygota.jpg", "href": "Complete_Metamorphosis_Insects_Endopterygota_6691", "height": 16, "width": 20},
			{"id": "1", "cname": "FSA", "parentId": "0" },
			{"id": "2", "cname": "OTC", "parentId": "1" },
			{"id": "3", "cname": "AMC", "parentId": "1" },
			{"id": "4", "cname": "ADC", "parentId": "1" },
			{"id": "41", "cname": "AFAO", "parentId": "4" },
			{"id": "42", "cname": "PSCAO", "parentId": "4" },
			{"id": "421", "cname": "SFG", "parentId": "42" },
			{"id": "4211", "cname": "TTPP", "parentId": "421" },
			{"id": "43", "cname": "PARMO", "parentId": "4" },
			{"id": "21", "cname": "IPUSO", "parentId": "2" },
			{"id": "22", "cname": "DBMO", "parentId": "2" }
		]
	};
var dbMap = {};
addEntriesToMasterMap(data.entries);


// represents additional data retrieved from server
var data2 = {
		"entries": [
			{"id": "31", "cname": "AO", "parentId": "3" },
			{"id": "32", "cname": "CITSO", "parentId": "3" }
		]
	};

