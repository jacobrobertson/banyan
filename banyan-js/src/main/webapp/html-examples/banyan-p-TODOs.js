// --- Separating this out to future functions so I can focus on getting something done


function testFunction() {
	buildTree($("#tree"), dbMap["1"]);
}
function addChildTestFunction() {
	addEntriesToMasterMap(data2.entries);
	// insertChildTree(dbMap["4"]);
}
function testFunction2() {
	addBranchDataToTree(dbMap["1"]);
}
function addBranchDataToTree(e) {
	// walk the data element, calling insert one at a time
	insertBranch(e);
	for (var i = 0; i < e.children.length; i++) {
		addBranchDataToTree(e.children[i]);
	}
}


function insertBranch(e) {

	// render the html
	var newRoot = buildBranchRoot(e);

	var parent = dbMap[e.parentId];
	if (!parent) {
		// this is the root element of the whole tree
		$("#tree").append(newRoot);
		return;
	}
	
	// locate the insertion point - i.e. we are not appending this child, we are inserting it
	var childPos = dbMap[e.parentId].children.indexOf(e);
	var trPos = childPos * 2;
	
	if (trPos == 0) {
		// in this case we don't append a new row, we add to first row
		
	}
	
	// insert cells into the correct row.  locate the sibling row and add after
	var siblingTr = $("#tree-" + e.parentId + " tr").eq(trPos);
	var tr = $("<tr></tr>");
	siblingTr.after(tr);
	
	
	// add a blank cell TODO get correct class name
	var blankClass = "b"; // plus l in some cases
	var blankTd =$("<td class='" + blankClass + "'>&nbsp</td>");
	tr.append(blankTd);
	
	// add the cell to hold the new root
	var childTd = $("<td rowspan='2'></td>");
	tr.append(childTd);
	childTd.append(newRoot);
	
	
	// adjust the rowspan for the proper parent elements
	
	// adjust the style for the linking cells (remove "l", etc)
}

/**
 * Only builds the root table of a new branch
 */
function buildBranchRoot(e) {
	var table = $("<table id='tree-" + e.id + "'></table>");
	var tr = $("<tr></tr>").appendTo(table);
	// assign the default rowspan at this time, but the children will be inserted later
	var td = $("<td rowspan='" + (e.children.length * 2) + "'></td>").appendTo(tr);
	// this is the tree root's Entry Lines
	var entryTable = buildEntryLinesElement(e);
	td.append(entryTable);
	// add one blank row afterwards, but do not assign the class yet
	$("<tr><td></td></tr>").appendTo(table);
	return table;
}

/**
 * @param h root Html Element
 * @param e root data Entry
 */
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
function appendEntryLinesElement(h, d) {
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
function appendEntryLinesElement_REAL(htmlElement, dataElement) {
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
			{"id": "1", "cname": "FSA", "parentId": "0" },
			{"id": "2", "cname": "OTC", "parentId": "1" },
			{"id": "3", "cname": "AMC", "parentId": "1" },
			{"id": "4", "cname": "ADC", "parentId": "1" },
			{"id": "41", "cname": "AFAO", "parentId": "4" },
			{"id": "42", "cname": "PSCAO", "parentId": "4" },
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

