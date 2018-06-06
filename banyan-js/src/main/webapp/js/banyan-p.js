// global vars and their functions
var dbMap = {};
var dbEntryIdsToShow = {}; // key-based map, but "false" could also mean don't show

function getGlobalMap() {
	return dbMap;
}
function getMapEntry(key) {
	return dbMap[key];
}
function isEntryShown(id) {
	return (dbEntryIdsToShow[id] == true);
}
function markNodeAsShown(id, show) {
	dbEntryIdsToShow[id] = show;
}
// -------

// ----- TEST functions -----
var testFile = "1-1528250141737"; // "6691";
function testLoadJson() {
	log("Starting Test Function", 4);
	loadJson(testFile, {}, true);
}
function testRenderTree(testId, keepOnlyNew) { // from button
	renderTree(testId, keepOnlyNew);
}
function testDeleteTreeNode(id) {
	markNodeAsShown(id, false);
	var testId = $("#renderId").val();
	testRenderTree(testId, true);
}
// util mostly for testing phase
function addAllToRenderMap(e) {
	dbEntryIdsToShow[e.id] = true;
	for (var i = 0; i < e.children.length; i++) {
		addAllToRenderMap(e.children[i]);
	}
}
function addChildTestFunction() {
	addEntriesToMap(data2.entries);
	testFunction();
}
// ------- end test functions
function renderTree(id, keepOnlyNew) { // from button
	$("#tree").empty();
	// for this test, we flag every entry as being rendered
	if (!keepOnlyNew) {
		addAllToRenderMap(getMapEntry(id));
	}
	var e = getMapEntry(id);
	// we need to collapse nodes only once we know the map is done
	prepareNodesForRender(e);
	buildTree($("#tree"), e);
	addMenusToButtons();
}
function addNodesToSelect() {
	$("#renderId").empty();
	$("#deleteId").empty();
	for (var id in dbMap) {
		$("#renderId").append("<option value='" + id + "'>" + id + "</option>");
		$("#deleteId").append("<option value='" + id + "'>" + id + "</option>");
	}
}

function loadJson(jsonId, idsMap, recurse) {

	
	// load the json, and in the callback add to worklist and global map
	
	/// ----- this really isn't okay, because I don't render it when it's done
	
	var url = "../json/" + jsonId + ".json";
	log("Processing " + url, 2);

	$.getJSON(url, function(data) {
		log("getJson: " + data + "/" + data.entries, 1);
		addEntriesToMap(data.entries);
		for (var i = 0; i < data.entries.length; i++) {
			var e = data.entries[i];
			log("Entry " + e.id + " parent:" + e.parent + " c: " + e.cname + " l: " + e.lname, 1);
			if (e.parent) {
				log("Entry " + e.id + 
						" parentId/children = " + e.parent.id + "/" + e.parent.children.length, 1);
			}
			
		}
		//log("Map Size: " + Object.keys(getGlobalMap()).length);
		if (recurse) {
			for (var i = 0; i < data.entries.length; i++) {
				// TODO need to load parents recursively also, as we might just have one child id
				//		the problem is that would also load the other map fragments?
				//		this algorithm really isn't quite right, we should get leafs and then walk up
				//if (!getMapEntry(data.entries[i].parentId)) {
					//loadJson(data.entries[i].parentId, idsMap, true);
				//}
				var cids = data.entries[i].childrenIds || [];
				for (var j = 0; j < cids.length; j++) {
					if (!getMapEntry(cids[j])) {
						loadJson(cids[j], idsMap, true);
					}
				}
			}
		}
	});
}
function buildTree(h, e) {
	var table = $("<table id='tree-" + e.id + "'></table>").appendTo(h);
	buildRowsForTree(table, e);
}
function buildRowsForTree(table, e) {
	var tr = $("<tr></tr>").appendTo(table);
	var children = e.childrenToShow;
	if (e.collapsed.length > 0) {
		children = e.collapsed[e.collapsed.length - 1].childrenToShow;
	}
	// this td is for the root element's info
	var td = $("<td rowspan='" + (children.length * 2) + "'></td>").appendTo(tr);
	var showLine = (children.length > 1);
	appendEntryLinesElement(td, e, showLine);
	var lastIndex = children.length - 1;
	for (var index = 0; index < children.length; index++) {
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
		buildTree(childTd, children[index]);
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
function appendEntryLinesElement(h, e, showLine) {
	var table = $("<table></table>");
	var tr = $("<tr></tr>").appendTo(table);
	var td = $("<td class='n' rowspan='2'></td>").appendTo(tr);
	var div = $("<div id='node-" + e.id + "' class='Node'></div>").appendTo(td); 
	buildNodeEntryLine(div, e, 0);
	
	for (var i = 0; i < e.collapsed.length; i++) {
		div.append($("<br/>"));
		buildNodeEntryLine(div, e.collapsed[i], i + 1);
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
	var name = getEntryDisplayName(e);
	span.append('<a class="preview" name="' + e.id + '" href="search.detail/' + e.href + '">' 
		 + name + '<img alt="' + e.alt + '" height="' + e.height + '" width="' + e.width + '" src="' + 
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
	return "http://jacobrobertson.com/banyan-images"; // "images";
}
function iconPath() {
	return "http://jacobrobertson.com/banyan/icons"; // "icons";
}

function addEntriesToMap(entries) {
	var map = getGlobalMap();
	// add each item to the map
	for (var i = 0; i < entries.length; i++) {
		var e = entries[i];
		e.children = [];
		enhanceEntryTemp(e);
		// TODO this might not be a simple replacement, depending on the operation
		map[e.id] = e;
	}
	
	// link each child to its parent
	for (var i = 0; i < entries.length; i++) {
		var e = entries[i];
		var p = map[e.parentId];
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
function prepareNodesForRender(e) {
	buildShownNodes(e);
	collapseNodes(e);
}
function buildShownNodes(e) {
	e.childrenToShow = [];
	for (var i = 0; i < e.children.length; i++) {
		var c = e.children[i];
		if (isEntryShown(c.id)) {
			e.childrenToShow.push(c);
			buildShownNodes(c);
		}
	}
}

// collapse nodes into the "chains" - whenever there is a child with just one child, etc, roll it up
// TODO add the "..." behavior for long chains
function collapseNodes(e) {
	e.collapsed = [];
	if (e.childrenToShow.length == 0) {
		return;
	} else if (e.childrenToShow.length > 1) {
		// don't do anything for this node, but recurse
		for (var i = 0; i < e.childrenToShow.length; i++) {
			collapseNodes(e.childrenToShow[i]);
		}
	} else {
		var r = e;
		while (r.childrenToShow.length == 1) {
			var c = r.childrenToShow[0];
			e.collapsed.push(c);
			r = c;
		}
		prepareNodesForRender(r);
	}
}

// this is temp until I start using actual data
function enhanceEntryTemp(e) {
	e.alt = "Endopterygota"; 
	e.img = "15/Endopterygota.jpg";
	e.href = e.id;// "Complete_Metamorphosis_Insects_Endopterygota_6691";
	e.height = 16; // TODO these need to be tinyHeight and tinyWidth
	e.width = 20;
	e.cpHide = "TODO";
	e.cpFocus = "TODO";
	e.cpClose = "TODO";
	e.cpShow = "TODO";
	e.cpShowMore = "TODO";
	e.showCaption = "TODO";
	e.showMoreCaption = "TODO";
}

function getEntryDisplayName(e) {
	// TODO we will care about parens, and boring, etc...
	return e.cname || e.lname;
}

var minLogLevel = 3;
function log(m, level) {
	if (level >= minLogLevel) {
		$("#log").append("<div>" + m + "</div>");
	}
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
// addEntriesToMasterMap(data.entries);


// represents additional data retrieved from server
var data2 = {
		"entries": [
			{"id": "31", "cname": "AO", "parentId": "3" },
			{"id": "32", "cname": "CITSO", "parentId": "3" }
		]
	};

