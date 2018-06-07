// ------ Document Init Methods
$(document).ready(function() {
	initContextMenu();
});

/* TODO we need to put these back later - for now we don't have these
$(document).ready(function() {
	$("#textfield").focus(function() {
		this.select();
	});
	$("#textfield").focus();
	$(".Button").bind('mouseover focus', function(event) {
		$(this).addClass("ButtonOver");
	}).bind('mouseout blur', function(event) {
		$(this).removeClass("ButtonOver");
	});
});
	*/
//------ Global vars
var dbMap = {};
var dbEntryIdsToShow = {}; // key-based map, but "false" could also mean don't show

var maxWidthHide = 106;
var maxWidthClose = 58;
var maxWidthFocus = 60;
var maxWidthDetail = 102;
var maxWidthShowChildren = 163;
var maxWidthShowMore = 159;
var digitWidth = 9;

var isMenuActive = false;
var cancelerEvent = null;

// ------ GUI Events/Behavior/Menus
function __EventsBehaviorMenus() {}
function hideContextMenu() {
	isMenuActive = false;
	$("#controlpanel").hide();
}
function checkContextMenuActive(e) {
	if (!isMenuActive && (cancelerEvent == null || cancelerEvent == e)) {
		hideContextMenu();
	}
	if (e != null) {
		e.preventDefault();
		return false;
	}
}
function initContextMenu() {
	$("#controlpanel").mouseenter(function(e) {
		isMenuActive = true;
	});
	$("#controlpanel").mouseleave(function(e) {
		hideContextMenu();
	});
	$("#controlpanel").mousemove(function(e) {
		isMenuActive = true;
	});
	
	// <a shape="rect" class="cpButton" id="cpHide" href="TBD">
	$(".cpButton").bind("click", function(e) {
		contextMenuClicked(this);
		return false;
	});
}
/**
 * TODO many operations will need to load from the server
 */
function contextMenuClicked(aTag) {
	var action = aTag.id;
	var pos = aTag.href.indexOf('#');
	var id = aTag.href.substring(pos + 1);
	hideContextMenu();
	
	if (action == "cpClose") {
		markNodeAsShown(id, false);
		renderCurrentTree();
	} else if (action == "cpHide") {
		markChildrenAsShown(id, false);
		renderCurrentTree();
	} else if (action == "cpShow") {
		markChildrenAsShown(id, true);
		renderCurrentTree();
	} else if (action == "cpFocus") {
		focusOnNode(id, true);
		renderCurrentTree();
	}
}
function showContextMenu(e, img) {
	var imgId = img.name;
	// create the right control panel links
	var e = getMapEntry(imgId);
	var buttons = $("#controlpanel a");
	var rowsShownCount = 0;
	var maxWidth = 0;
	for ( var i = 0; i < buttons.length; i++) {
		button = buttons[i];
		var buttonId = button.id;
		var buttonValue = e[buttonId];
		if (buttonValue) {
			button.href = buttonValue + "#" + imgId;
			$(button).show();
			rowsShownCount++;
		} else {
			$(button).hide();
		}
		if (buttonId == 'cpHide') {
			maxWidth = Math.max(maxWidth, maxWidthHide);
		} else if (buttonId == 'cpClose') {
			maxWidth = Math.max(maxWidth, maxWidthClose);
		} else if (buttonId == 'cpFocus') {
			maxWidth = Math.max(maxWidth, maxWidthFocus);
		} else if (buttonId == 'cpDetail') {
			maxWidth = Math.max(maxWidth, maxWidthDetail);
		} else if (buttonId == 'cpShow') {
			var showCaption = e.cpShowCaption;
			if (showCaption) {
				$("#cpShowChildrenCaption").text(showCaption);
				maxWidth = getVariableWidth(showCaption, 20, maxWidthShowChildren, maxWidth);
			}
		} else if (buttonId == 'cpShowMore') {
			var showMoreCaption = e.cpShowMoreCaption;
			if (showMoreCaption) {
				$("#cpShowMoreCaption").text(showMoreCaption);
				maxWidth = getVariableWidth(showMoreCaption, 19, maxWidthShowMore, maxWidth);
			}
		}
	}
	// one row = 168 x 19
	// 112 = total height = 5 * 19 + 17 = rowheight * numrows + ?
	var popupHeight = rowsShownCount * 22 + 2;
	
	var p = $(img).position();
	var t = p.top;
	var top = t - 5;
	
	// see if the popup will go below the screen
	var docViewBottom = getWindowHeight() + getWindowTop();
	if (top + popupHeight > docViewBottom) {
		top = docViewBottom - popupHeight;
	}

	var left = p.left + 1;
	
	// see if the popup will go beyond the right-hand side
	// this is a best-guess based on the messages
	var docViewRight = getWindowWidth() + getWindowLeft();
	if (left + maxWidth > docViewRight) {
		left = docViewRight - maxWidth;
	}
	
	$("#controlpanel").show().css( {
		"top" : top,
		"left" : left
	});

}
function initAllImagePreviewEvents() {
	$("a.preview").hover(function(e) {
		var imageBorderWidth = 2; // represents the two borders, left/right
			var width = getImageWidth(this) + imageBorderWidth;
			var href = getHref(this); // this.href;
			var caption = getImageCaption(this);
			var c = (caption != "") ? "<br/>" + caption : "";
			$("body").append(
					"<p id='preview'><img src='" + href + "'/>" + c + "</p>");
			$("#preview").hide().css("top", getPreviewTop(e, this) + "px").css(
					"left", getLeft(e, this) + "px").css("width", width + "px")
					.show("fast");
		}, function() {
			$("#preview").remove();
		});
	$("a.preview").mousemove(
			function(e) {
				$("#preview").css("top", getPreviewTop(e, this) + "px").css(
						"left", getLeft(e, this) + "px");
			});
	$(".opener").bind("click mouseenter", function(e) {
		cancelerEvent = e;
		e.preventDefault();
		var target = this;
		var timeout = 5000;
		if (e.type == "mouseenter") {
			timeout = 50;
		}
		setTimeout(function() {
			showContextMenu(e, target);
		}, 5);
		setTimeout(function() {
			checkContextMenuActive(e);
		}, timeout);
    });
}

// ------ General Utils
function __GeneralUtils() {}
function log(m, level) {
	var minLogLevel = 3;
	if (level >= minLogLevel) {
		$("#log").append("<div>" + m + "</div>");
	}
}
function getLeft(e, img) {
	var w = $("body").width();
	var x = e.pageX;

	var imageWidth = getImageWidth(img);

	var left;
	if (x > w / 2) {
		var pointerOffset = 30; // so pointer isn't on top of it
		left = x - (imageWidth + pointerOffset);
	} else {
		var pointerOffset = 20; // so pointer isn't on top of it
		left = x + pointerOffset;
	}
	return left;
}
function getPreviewTop(e, img) {
	var imageHeight = getImageHeight(img);
	// to account for two rows of text
	var textHeight = 75; 
	return getTop(e, imageHeight, textHeight);
}
function getTop(e, popupHeight, bottomFudge) {
	var docViewTop = $(window).scrollTop();
	var top = e.pageY - popupHeight; // default - put offset in relation to mouse
	if (top < docViewTop) {
		top = docViewTop; // if it's too tall, put at top of page
	} else {
		// test for bottom of page
		var docViewBottom = docViewTop + $(window).height() - bottomFudge;
		var lowest = docViewBottom - popupHeight;
		if (top > lowest) {
			top = lowest;
		}
	}
	return top;
}
function getHref(img) {
	// TODO I won't need to do this because the attribute is already in the entry
	// for example http://bi.robestone.com/tiny/we/Homininae.jpg
	/*
	var src = $(e).find(".Thumb").attr("src");
	var pos = src.indexOf("/tiny/");
	var left = src.substring(0, pos);
	var right = src.substring(pos + 6);
	var href = left + "/preview/" + right;
	*/
	return imagePath() + "/preview/" + this.getImageAttribute(img).img;
}
function getImageWidth(img) {
	return this.getImageAttribute(img).pWidth;
}
function getImageHeight(img) {
	return this.getImageAttribute(img).pHeight;
}
function getImageCaption(img) {
	var e = this.getImageAttribute(img);
	var caption = "<span class='PopupLatin'>(" + e.lname + ")</span>";
	if (e.cname) {
		// TODO fix when we get multiple cnames
		caption = "<b>" + e.cname + "</b><br/>" + caption;
	}
	return caption;
}
function getImageAttribute(img) {
	return getMapEntry(img.name);
}
function getVariableWidth(text, minLength, baseWidth, maxWidth) {
	var len = text.length;
	var extraDigits = len - minLength;
	var thisWidth = baseWidth + extraDigits * digitWidth;
	return Math.max(maxWidth, thisWidth);
}
function getImagePosition(img) {
	return  $(img).position(); // jquery 1.3.1
}
function getWindowHeight() {
	return  $(window).height(); // jquery 1.3.1
}
function getWindowTop() {
	return  $(window).scrollTop(); // jquery 1.3.1
}
function getWindowWidth() {
	return  $(window).width(); // jquery 1.3.1
}
function getWindowLeft() {
	return  $(window).scrollLeft(); // jquery 1.3.1
}

// ------ Entry Functions
function __EntryFunctions() {}

function getGlobalMap() {
	return dbMap;
}
function getMapEntry(key) {
	return dbMap[key];
}
function isEntryShown(id) {
	return (dbEntryIdsToShow[id] == true);
}
function focusOnNode(id) {
	var e = getMapEntry(id);
	var p = e.parent;
	focusOnNodeParent(p, e);
}
function focusOnNodeParent(p, e) {
	for (var i = 0; i < p.children.length; i++) {
		var c = p.children[i];
		if (c != e) {
			markEntryAsShown(c, false);
		}
	}
	if (p.parent) {
		focusOnNodeParent(p.parent, p);
	}
}
function markNodeAsShown(id, show) {
	markEntryAsShown(getMapEntry(id), show);
}
function markEntryAsShown(e, show) {
	dbEntryIdsToShow[e.id] = show;
	if (!show) {
		markEntryChildrenAsShown(e, false);
	}
}
function markLeavesAsShown(ids) {
	// set each leaf and parent to visible
	for (var i = 0; i < ids.length; i++) {
		
	}
}

function markChildrenAsShown(id, show) {
	markEntryChildrenAsShown(getMapEntry(id), show);
}
function markEntryChildrenAsShown(e, show) {
	for (var i = 0; i < e.children.length; i++) {
		var c = e.children[i];
		dbEntryIdsToShow[c.id] = show;
		if (!show) {
			// in this case we want to hide all children now,
			// it makes other logic simpler
			markEntryChildrenAsShown(c, false);
		}
	}
}
function addEntriesToMap(entries) {
	var map = getGlobalMap();
	var e;
	var i;
	// add each item to the map, plus any init needed
	for (i = 0; i < entries.length; i++) {
		e = entries[i];
		e.children = [];
		if (!e.childrenIds) {
			e.childrenIds = [];
		}
		enhanceEntryTemp(e);
		// TODO this might not be a simple replacement, depending on the operation
		map[e.id] = e;
	}
	
	// link each child to its parent
	for (i = 0; i < entries.length; i++) {
		e = entries[i];
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
	updateEntryForControlPanel(e);
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
		collapseNodes(r);
	}
}
function updateEntryForControlPanel(e) {
	var showChildrenCount = e.childrenIds.length - e.childrenToShow.length;
	if (showChildrenCount == 0) {
		e.cpShow = false;
		e.cpShowCaption = false;
	} else {
		e.cpShow = true;
		if (showChildrenCount == 1) {
			e.cpShowCaption = "Show 1 Child";
		} else {
			e.cpShowCaption = "Show " + showChildrenCount + " Children";
		}
	}

	e.cpHide = (e.childrenToShow.length > 0);
	
	// TODO just hiding this since it's not ready yet
	e.cpShowMore = false;

	// TODO this logic is more complex - if focusing wouldn't accomplish anything, then set to false
	e.cpFocus = true;
	
	// recurse
	for (var i = 0; i < e.childrenToShow.length; i++) {
		updateEntryForControlPanel(e.childrenToShow[i]);
	}
}
// this is temp until I start using actual data
function enhanceEntryTemp(e) {
	e.alt = "Endopterygota"; 
	//e.img = "15/Endopterygota.jpg";
	e.href = e.id;// "Complete_Metamorphosis_Insects_Endopterygota_6691";
	//e.height = 16; // TODO these need to be tinyHeight and tinyWidth
	//e.width = 20;
	e.cpShowMore = "? TODO";
	e.cpShowMoreCaption = "Show More ... TODO";
	e.cpDetail = "TODO";
}

function getEntryDisplayName(e) {
	// TODO we will care about parens, and boring, etc...
	return e.cname || e.lname;
}
// ----- TEST functions -----
function __TestFunctions() {}
var testFile = "1-1528339515448";
function testLoadJson() {
//	log("Starting Test Function", 4);
//	loadJson(testFile, {}, true);
	loadJson("1", {}, false);
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
	addAllToRenderMapDownstream(e);
	var p = e;
	while (p) {
		dbEntryIdsToShow[p.id] = true;
		e = p;
		p = p.parent;
	}
	return e.id;
}
function addAllToRenderMapDownstream(e) {
	dbEntryIdsToShow[e.id] = true;
	for (var i = 0; i < e.children.length; i++) {
		addAllToRenderMapDownstream(e.children[i]);
	}
}
function addChildTestFunction() {
	addEntriesToMap(data2.entries);
	testFunction();
}
function addNodesToSelect() {
	$("#renderId").empty();
	$("#deleteId").empty();
	for (var id in dbMap) {
		var e = getMapEntry(id);
		if (e.childrenIds && e.childrenIds.length > 1) {
			$("#renderId").append("<option value='" + id + "'>" + id + "</option>");
		}
	}
	testRenderTree($("#renderId").val(), false);
}
// ------- end test functions

// ------ Tree/HTML Rendering
function __TreeHtmlRendering() {}
function renderCurrentTree() {
	// TODO the "1" is dumb - need better way
	renderTree("1", true);
}
function renderTreeFromLeaves(ids) {
	// set each leaf and parent to visible
//	for (var i = 0; i < e.collapsed.length; i++) {
}
function renderTree(id, keepOnlyNew) {
	$("#tree").empty();
	// for this test, we flag every entry as being rendered
	if (!keepOnlyNew) {
		id = addAllToRenderMap(getMapEntry(id));
	}
	var e = getMapEntry(id);
	// we need to collapse nodes only once we know the map is done
	prepareNodesForRender(e);
	buildTree($("#tree"), e);
	initAllImagePreviewEvents();
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
		 + name + '<img alt="' + e.alt + '" height="' + e.tHeight + '" width="' + e.tWidth + '" src="' + 
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
	return "icons"; // "http://jacobrobertson.com/banyan/icons"; // "icons";
}

// ------ JSON Functions
function __JsonFunctions() {}
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
