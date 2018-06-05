function testFunction() {
	buildTree($("#tree"), dbMap["1"]);
}
function addChildTestFunction() {
	addEntriesToMasterMap(data2.entries);
	insertChildTree(dbMap["4"]);
}

function insertChildTree(e) {
	// locate the correct parent html element for the parent we will add it to
	var table = $("tree-" + e.parentId);
	
	// locate the insertion point - i.e. we are not appending this child, we are inserting it
	var pos = dbMap[e.parentId].children.indexOf(e) * 2;
	
	// render the html and add to that element
	
	
	// adjust the rowspan for the proper parent elements
	
	// adjust the style for the linking cells (remove "l", etc)
}

// see https://stackoverflow.com/questions/3562493/jquery-insert-div-as-certain-index
function insertAt(expression, index, element) {
	var lastIndex = expression.children().size();
	// TODO I probably don't need this check
	if (index < 0) {
		index = Math.max(0, lastIndex + 1 + index);
	}
	expression.append(element);
	if (index < lastIndex) {
		expression.children().eq(index).before(expression.children().last());
	}
}

/**
 * @param h root Html Element
 * @param e root data Entry
 */
function buildTree(h, e) {
	var table = $("<table id='tree-" + e.id + "'></table>").appendTo(h);
	buildRowsForTree(table, e);
}
// TODO add an index, and instead of append to, insert them at that position
function buildRowsForTree(table, e) {
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

/**
 * This is just the table element that holds the Entry Lines
 */
function buildNodeTable(h, d) {
	var showLine = (d.children.length > 1);
	var table = $("<table></table>");
	var tr = $("<tr></tr>").appendTo(table);
	 
	$("<td class='n' rowspan='2'><div id='node-" + d.id + "' class='Node'>" 
			+ d.cname + "</div></td>").appendTo(tr);
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

function addEntriesToMasterMap(entries) {
	
	// add each item to the map
	for (var i = 0; i < entries.length; i++) {
		var e = entries[i];
		e.children = [];
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
			// TODO - need to insert in the correct position right now
			for (var j = 0; j < p.children.length; j++) {
				var c = p.children[j];
				var dname2 = getEntryDisplayName(c);
				if (dname < dname2) {
					p.children.splice(j, 0, e);
					foundPos = true;
					break;
				}
			}
			if (!foundPos) {
				p.children.push(e);
			}
		}
	}
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
			{"id": "1", "cname": "Tree of Life", "parentId": "0" },
			{"id": "2", "cname": "Mammals", "parentId": "1" },
			{"id": "3", "cname": "Plants", "parentId": "1" },
			{"id": "21", "cname": "Dog", "parentId": "2" },
			{"id": "22", "cname": "Cat", "parentId": "2" }
		]
	};
var dbMap = {};
addEntriesToMasterMap(data.entries);


// represents additional data retrieved from server
var data2 = {
		"entries": [
			{"id": "4", "cname": "Birds", "parentId": "1" },
			{"id": "41", "cname": "Canary", "parentId": "4" },
			{"id": "42", "cname": "Eagle", "parentId": "4" }
		]
	};

