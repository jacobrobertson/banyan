// ------ Document Init Methods
$(document).ready(function() {
	$(window).on('hashchange', function() {
		loadTreeFromURL();
	});
	initContextMenu();
	$(document).ready(function() {
		initData();
	});
});
function initData() {
	loadPartitionIndex(function() {
		loadTreeFromURL();
	});
}
/* TODO we need to put these back later - for now we don't have these
$(document).ready(function() {
	$("#textfield").focus(function() {
		this.select();
	});
	$("#textfield").focus();
	// TODO I don't actually remember what these are for
	$(".Button").bind('mouseover focus', function(event) {
		$(this).addClass("ButtonOver");
	}).bind('mouseout blur', function(event) {
		$(this).removeClass("ButtonOver");
	});
});
	*/
//------ Global vars
function __GlobalVars(){}
var dbMap = {};
var dbEntryIdsToShow = {}; // key-based map, but "false" could also mean don't show
var dbPartitions;

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
function contextMenuClicked(aTag) {
	var action = aTag.id;
	var pos = aTag.href.indexOf('#');
	var id = aTag.href.substring(pos + 1);
	hideContextMenu();
	
	if (action == "cpClose") {
		markNodeAsShown(id, false);
		renderCurrentTree();
	} else if (action == "cpHide") {
		closeNode(id);
	} else if (action == "cpShowChildren") {
		loadAllChildren(id);
	} else if (action == "cpShowMore") {
		loadAllShowMore(id);
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
		} else if (buttonId == 'cpShowChildren') {
			var showCaption = e.cpShowChildrenCaption;
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
		var img = e.currentTarget;
		var imageBorderWidth = 2; // represents the two borders, left/right
		var width = getImageWidth(img) + imageBorderWidth;
		var href = getHref(img); // this.href;
		var caption = getImageCaption(img);
		var c = (caption != "") ? "<br/>" + caption : "";
		$("body").append(
				"<p id='preview'><img src='" + href + "'/>" + c + "</p>");
		$("#preview").hide().css("top", getPreviewTop(e, img) + "px").css(
				"left", getLeft(e, img) + "px").css("width", width + "px")
				.show("fast");
		}, function() {
			$("#preview").remove();
		});
	$("a.preview").mousemove(
		function(e) {
			var img = e.currentTarget;
			$("#preview").css("top", getPreviewTop(e, img) + "px").css(
					"left", getLeft(e, img) + "px");
		});
	$(".opener").bind("click mouseenter", function(e) {
		cancelerEvent = e;
		e.preventDefault();
		var img = e.currentTarget;
		var timeout = 5000;
		if (e.type == "mouseenter") {
			timeout = 50;
		}
		setTimeout(function() {
			showContextMenu(e, img);
		}, 5);
		setTimeout(function() {
			checkContextMenuActive(e);
		}, timeout);
    });
}
function closeNode(id) {
	var ids = getAllVisibleNodeIds();
	var pos = ids.indexOf(id);
	ids = ids.splice(pos, 1);
	setUrlIds(ids);
	// this way is working
//	markChildrenAsShown(id, false);
//	renderCurrentTree();
}
function setUrlIds(ids) {
	var url = window.location.href;
	var index = url.indexOf("#");
	if (index > 0) {
		url = url.substring(0, index);
	}
	url = url + "#";
	for (var i = 0; i < ids.length; i++) {
		if (i > 0) {
			url = url + ",";
		}
		url = url + ids[i];
	}
}
// ------ General Utils
function __GeneralUtils() {}
function log(m, level) {
	var minLogLevel = 5;
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
	var latinNameCaption = "<span class='PopupLatin'>(" + e.lname + ")</span>";
	var names = e.cnames || [];
	var commonNamesCaption = "";
	for (var i = 0; i < names.length; i++) {
		commonNamesCaption = commonNamesCaption + "<b>" + names[i] + "</b><br/>";
	}
	return commonNamesCaption + latinNameCaption;
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
function getRootEntry() {
	for (var id in dbMap) {
		return getRootFromId(id);
	}
	return false;
}
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
function markShowMoreAsShown(id) {
	var e = getMapEntry(id);
	for (var i = 0; i < e.showMoreLeafIds.length; i++) {
		markNodeAsShown(e.showMoreLeafIds[i], true);
	}
	for (var i = 0; i < e.showMoreOtherIds.length; i++) {
		markNodeAsShown(e.showMoreOtherIds[i], true);
	}
}
function markChildrenAsShown(id, show) {
	markEntryChildrenAsShown(getMapEntry(id), show);
}
// just these ids, nothing else
function markEntriesAsShown(entries, show) {
	for (var i = 0; i < entries.length; i++) {
		dbEntryIdsToShow[entries[i].id] = show;
	}
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
	var e;
	var i;
	
	// skip all that we already have - we never need to load from server twice
	var temp = [];
	for (i = 0; i < entries.length; i++) {
		e = entries[i];
		if (!getMapEntry(e.id)) {
			temp.push(e);
		}
	}
	entries = temp;
	
	var map = getGlobalMap();
	// add each item to the map, plus any init needed
	for (i = 0; i < entries.length; i++) {
		e = entries[i];
		e.children = [];
		initEntry(e);
		map[e.id] = e;
	}
	
	// link each child to its parent
	for (i = 0; i < entries.length; i++) {
		e = entries[i];
		var p = map[e.parentId];
		e.parent = p;
		if (p) {
			var dname = getEntrySimpleDisplayName(e);
			var foundPos = false;
			var foundSame = false;
			for (var j = 0; j < p.children.length; j++) {
				var c = p.children[j];
				var dname2 = getEntrySimpleDisplayName(c);
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
	for (i = 0; i < entries.length; i++) {
		e = entries[i];
		initPartitionPath(e);
	}
}
function getRootFromId(id) {
	var e = getMapEntry(id);
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
	collapseNodesForChildrenToShow(e);
	hideLongCollapsed(e);
	collapseNodesForSiblingsToShow(e);
	prepareEntryForControlPanel(e);
}
// These are the visible "children" of a node, not the actual children of an entry
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
function collapseNodesForSiblingsToShow(e) {
	var collapsed = [];
	var last = false;
	for (var i = 0; i < e.childrenToShow.length; i++) {
		var c = e.childrenToShow[i];
		if (c.childrenToShow.length == 0) {
			if (!last) {
				last = c;
				c.siblings = [];
				collapsed.push(c);
			} else {
				last.siblings.push(c);
			}
		} else {
			// this one will be shown on its own
			last = false;
			collapsed.push(c);
			c.siblings = [];
		}
	}
	e.childrenToShow = collapsed;
	// recurse on just the non-collapsed siblings
	for (var i = 0; i < e.childrenToShow.length; i++) {
		var c = e.childrenToShow[i];
		if (c.siblings.length == 0) {
			collapseNodesForSiblingsToShow(c);
		}
	}
}
/**
 * Phase 1 algorithm:
 * Always reduce any list of 7 or more (with chain root) to 5
 *	1 - Root keep
	2 - First in chain - keep and mark as +
	3 - remove
	4 - remove
	5 - center piece Math.ceil(totalLen / 2) - keep and leave as -
	6 - next after center piece - keep and mark as +
	7 - remove
	8 - remove
	9 - Tail keep
 */
function hideLongCollapsed(e) {
	var len = e.collapsed.length + 1;
	if (len >= 7) {
		var collapsed = [];
		var mid = Math.ceil(len / 2) - 1;
		for (var i = 0; i < e.collapsed.length; i++) {
			var c = e.collapsed[i];
			if (i == 0 || i == mid + 1) {
				collapsed.push(c);
				c.collapsedPinned = true;
			} else if (i == mid || i == e.collapsed.length - 1) {
				collapsed.push(c);
			}
		}
		e.collapsed = collapsed;
	}
	
	// recurse
	var toRecurse;
	if (e.collapsed.length > 0) {
		toRecurse = e.collapsed[e.collapsed.length - 1].childrenToShow;
	} else {
		toRecurse = e.childrenToShow;
	}
	for (var i = 0; i < toRecurse.length; i++) {
		hideLongCollapsed(toRecurse[i]);
	}
}
function collapseNodesForChildrenToShow(e) {
	e.collapsed = [];
	if (e.childrenToShow.length == 0) {
		return;
	} else if (e.childrenToShow.length > 1) {
		// don't do anything for this node, but recurse
		for (var i = 0; i < e.childrenToShow.length; i++) {
			collapseNodesForChildrenToShow(e.childrenToShow[i]);
		}
	} else {
		var r = e;
		while (r.childrenToShow.length == 1) {
			var c = r.childrenToShow[0];
			e.collapsed.push(c);
			c.collapsedPinned = false;
			r = c;
		}
		collapseNodesForChildrenToShow(r);
	}
}
function getTotalChildrenShownCountingSiblings(e) {
	var count = 0;
	for (var i = 0; i < e.childrenToShow.length; i++) {
		var c = e.childrenToShow[i];
		count++;
		if (c.siblings) {
			count += c.siblings.length;
		}
	}
	return count;
}
function prepareEntryForControlPanel(e) {
	var hiddenCount = e.childrenIds.length - getTotalChildrenShownCountingSiblings(e);
	if (hiddenCount == 0) {
		e.cpShowChildren = false;
		e.cpShowChildrenCaption = false;
	} else {
		e.cpShowChildren = true;
		e.cpShowChildrenCaption = getShowCaption(e.childrenToShow.length, hiddenCount, "Child", "Children");
	}

	e.cpHide = (e.childrenToShow.length > 0);
	
	var showMoreVisible = countVisible(e.showMoreLeafIds);
	var showMoreHidden = e.showMoreLeafIds.length - showMoreVisible;
	if (showMoreHidden == 0) {
		e.cpShowMore = false;
		e.cpShowMoreCaption = false;
	} else {
		e.cpShowMore = true;
		e.cpShowMoreCaption = getShowCaption(showMoreVisible, showMoreHidden, "Species", "Species");
	}

	// focus doesn't show any additional nodes, it just hides everything not up or downstream of this node
	e.cpFocus = isFocusNeeded(e);
	
	// recurse
	for (var i = 0; i < e.childrenToShow.length; i++) {
		prepareEntryForControlPanel(e.childrenToShow[i]);
	}
	for (var i = 0; i < e.siblings.length; i++) {
		prepareEntryForControlPanel(e.siblings[i]);
	}
}
// are there any non-descendant leafs
function isFocusNeeded(e) {
	var p = e.parent;
	while (p) {
		if (p.childrenToShow.length > 1) {
			return true;
		}
		p = p.parent;
	}
	return false;
}
function getShowCaption(visibleCount, hiddenCount, label1, labelPlural) {
	var caption;
	var more = "";
	if (visibleCount > 0) {
		more = " More";
	}
	if (hiddenCount == 1) {
		caption = "Show 1" + more + " " + label1;
	} else {
		caption = "Show " + hiddenCount + more + " " + labelPlural;
	}
	return caption;
}
function countVisible(ids) {
	var count = 0;
	for (var i = 0; i < ids.length; i++) {
		if (dbEntryIdsToShow[ids[i]]) {
			count++;
		}
	}
	return count;
}
// only those things that need to be done exactly one time when first loaded
function initEntry(e) {
	if (e.cname) {
		// TODO will be first in array later (cnames)
		e.alt = e.cname;
	}  else {
		e.alt = e.lname;
	}
	e.href = e.id;// "Complete_Metamorphosis_Insects_Endopterygota_6691";
	e.cpDetail = "TODO"; // should just be true?
	if (!e.childrenIds) {
		e.childrenIds = [];
	}
	if (!e.showMoreLeafIds) {
		e.showMoreLeafIds = [];
	}
	if (!e.showMoreOtherIds) {
		e.showMoreOtherIds = [];
	}
	e.cpClose = true;
	if (!e.extinct) {
		e.extinct = false;
	}
	e.siblings = [];
	e.collapsed = [];
	if (e.cnames) {
		e.cname = e.cnames[0];
	}
}
function initPartitionPath(e) {
	if (e.partitionPath) {
		// TODO kind of dumb too - maybe need a way to traverse the tree instead?
		return;
	}
	var p = e.parent;
	if (!p) {
		e.partitionPath = "0";
	} else {
		if (!p.partitionPath) {
			// TODO this is a dumb way to do it
			initPartitionPath(p);
		}
		var index = indexOf(p.childrenIds, e.id);
		e.partitionPath = p.partitionPath + "" + index; // to ensure by string
	}
}
function getEntryDisplayName(e) {
	if (e.cname) {
		if (e.cnames && e.cnames.length > 1) {
			return e.cname + "...";
		} else {
			return e.cname;
		}
	} else {
		return "<i>(" + e.lname + ")</i>";
	}
}
// only used for sorting and alt text
function getEntrySimpleDisplayName(e) {
	if (e.cname) {
		return e.cname;
	} else {
		return e.lname;
	}
}
function getAllVisibleNodeIds() {
	var ids = [];
	getVisibleNodes(getRootEntry(), ids);
	return nodes;
}
function getVisibleNodeIds(e, nodes) {
	if (isEntryShown(e.id)) {
		ids.push(e.id);
		for (var i = 0; i < e.children.length; i++) {
			getVisibleNodes(e.children[i], ids);
		}
	}
}
//util mostly for testing phase
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
// ------ Tree/HTML Rendering
function __TreeHtmlRendering() {}
function renderCurrentTree() {
	renderTree(getRootEntry().id, true);
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
	// TODO I need to know right here if I can render all the siblings too
	appendEntryLinesElement(td, e, showLine);
//	if (entries.length == 1) {
		// skip this section if we decided to append all within the first siblings element
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
//	}
}
/**
 * This is just the table element that holds the Entry Lines
 */
function appendEntryLinesElement(h, e, showLine) {
	var table = $("<table></table>");
	var tr = $("<tr></tr>").appendTo(table);
	var td = $("<td class='n' rowspan='2'></td>").appendTo(tr);
	var div = $("<div id='node-" + e.id + "' class='Node'></div>").appendTo(td); 
	
	renderNodeEntryLine(div, e, 0);
	for (var i = 0; i < e.siblings.length; i++) {
		div.append($("<br/>"));
		renderNodeEntryLine(div, e.siblings[i], 0);
	}
	
	// this will always be empty if we are rendering
	for (var i = 0; i < e.collapsed.length; i++) {
		div.append($("<br/>"));
		renderNodeEntryLine(div, e.collapsed[i], i + 1);
	}
	
	var blankTr = $("<tr></tr>").appendTo(table);
	if (showLine) {
		$("<td class='b'>&nbsp;</td>").appendTo(tr);
		$("<td>&nbsp;</td>").appendTo(blankTr);
	}
	table.appendTo(h);
}

function renderNodeEntryLine(h, e, depth) {
	var span = $('<span class="EntryLine EntryLineTop"></span>').appendTo(h);
	// detail button
	var detailIcon = "detail_first.png";
	var detailClass = "tree-detail_first";
	if (e.collapsedPinned) {
		detailIcon = "open_children.png";
		detailClass = "tree-open_children";
	} else if (depth > 0) {
		detailIcon = "detail_indented.png";
		detailClass = "tree-detail_indented";
	}
	if (!e.parentId) {
		detailIcon = "tree_root.png";
		detailClass = "tree-tree_root";
	}
	var pad = getNbsps(depth);
	span.append(pad + '<a title="Go to Details" href="search.detail/' + e.href + 
			'"><img src="' + iconPath() + '/' + detailIcon + '" class="' +
			detailClass + '" alt="search.detail" /></a>');
	// image and link
	var name = getEntryDisplayName(e);
	var img;
	var linkTitle;
	var imgClass;
	if (e.img) {
		img = '<img alt="' + e.alt + '" height="' + e.tHeight + '" width="' + e.tWidth + '" src="' + 
			imagePath() + '/tiny/' + e.img + '" class="Thumb" />';
		imgClass = "preview";
		linkTitle = "";
	} else {
		img = "";
		linkTitle = ' title="' + getEntrySimpleDisplayName(e) + '"';
		imgClass = "no-preview";
	}
	if (e.extinct != false) {
		var eClass = "Extinct";
		if (e.extinct == "top") {
			eClass = "TopExtinct";
		}
		span.append('<a title="Extinct" href="#"><span class="' + eClass + '">&dagger;</span></a>');
	}
	span.append('<a class="' + imgClass + '"' + linkTitle + ' name="' + e.id + '" href="search.detail/' + e.href + '">' 
			 + name + img + '</a>');
	
	// menu button
	var canShowMore = (e.cpShowChildren || e.cpShowMore);
	var menuMore = "menu_more.png";
	if (!canShowMore) {
		menuMore = "menu_less.png";
	}
	span.append('<a href="search.tree/TODO#' + e.id + '" name="' + e.id + '" class="opener">'
			+ '<img src="' + iconPath() + '/' + menuMore + '" alt="menu"></a>');
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
function loadTreeFromURL() {
	var url = window.location.href;
	var index = url.indexOf("#");
	if (index > 0) {
		var id = url.substring(index + 1);
		if (id.length > 0) {
			var ids = id.split(",");
			loadJsonThenMarkOnlyNewVisible(ids);
		}
	}
}
// TODO this isn't working anymore because I've reworked the callbacks to gather all entries first
function loadAllChildren(id) {
	var childrenIds = getMapEntry(id).childrenIds;
	loadJsonThenMarkAllNewVisible(childrenIds);
}
function loadAllShowMore(id) { // TODO same issues here
	var e = getMapEntry(id);
	// build the full list
	var allShowMoreIds = e.showMoreLeafIds.concat(e.showMoreOtherIds);
	loadJsonThenMarkAllNewVisible(allShowMoreIds);
}

// TODO this is probably not working any more due to refactor
function loadJsonThenMarkOnlyNewVisible(fileNamesOrIds) {
	if (getRootEntry()) {
		markEntryChildrenAsShown(getRootEntry(), false);
	}
	loadJsonThenMarkAllNewVisible(fileNamesOrIds);
}
// this is the master "load ids" method, and should be altered to accomodate the one or two scenarios we have
// - load these nodes/files exactly, and then mark exactly those nodes visible in addition to current tree, then render tree
// - load a brand new tree (? maybe already handled by calling method)
// - load these nodes, but don't do anything else (? not sure that's a valid scenario)
function loadJsonThenMarkAllNewVisible(fileNamesOrIds) {
	var callback = function(entries) {
		addEntriesToMap(entries);
		markEntriesAsShown(entries, true);
		renderCurrentTree();
	};
	loadJson(fileNamesOrIds, true, callback);
}
// ids would come from "open children" for example
function loadJson(fileNamesOrIds, markNewEntriesShown, callback) {
	// build actual list of ids based on what we don't have already
	var temp = [];
	for (var i = 0; i < fileNamesOrIds.length; i++) {
		var e = getMapEntry(fileNamesOrIds[i]);
		if (!e) {
			// TODO this might be a file name - need to track that separately so we don't reload files twice
			temp.push(fileNamesOrIds[i]);
		} else if (markNewEntriesShown) {
			markEntryAsShown(e, true);
		}
	}
	fileNamesOrIds = temp;
	
	var entries = [];
	// build list of functions - we don't care which order
	var currentCallback = callback;
	for (var j = 0; j < fileNamesOrIds.length; j++) {
		var parentCallback = currentCallback;
		var thisCallback = buildJsonCallback(fileNamesOrIds[j], parentCallback);
		currentCallback = thisCallback;
	}
	// call the last function, it will cascade up
	currentCallback(entries);
}
// this is a callback in the sense that it is part of a callback chain, 
// even though this method itself will call json and need a callback
function buildJsonCallback(id, parentCallback) {
	return function(entries) {
		return loadOneJsonDocument(id, entries, parentCallback);
	};
}
function loadOneJsonDocument(jsonId, entries, callback) {
	jsonId = (jsonId + "");
	log("loadOneJsonDocument: " + jsonId, 1);
	var subfolder;
	var url = "json/"
	if (jsonId.charAt(0) == 'f') {
		 url = url + "f/" + jsonId + ".json";
	} else if (jsonId.charAt(0) == 'p') {
		url = url + "p/" + jsonId.subtring(2) + ".json";
	} else {
		// subfolder = "n" + "/" + Math.ceil(jsonId / 100);
		var e = getMapEntry(jsonId);
		subfolder = "p" + "/" + findPartitionFile(e)
	}
	var innerSuccessCallback = buildInnerJsonSuccessCallback(entries, callback);
	return $.getJSON(url, innerSuccessCallback);
}
function buildInnerJsonSuccessCallback(entries, callback) {
	return function(data) {
		// all we do is gather all the json data together into one large array
		// any other processing will be handled by the final callback in the chain
		for (var i = 0; i < data.entries.length; i++) {
			entries.push(data.entries[i]);
		}
		callback(entries);
	};
}
function loadPartitionIndex(callback) {
	return $.getJSON("json/p/index.json", 
		function(data) {
			loadPartitionIndexDb(data);
			callback();
		});
}
function loadPartitionIndexDb(data) {
	dbPartitions = data;
}
function findPartitionFile(e) {
	if (!e) {
		return false;
	}
	var pname = e.partitionPath;
	if (pname.length == 0) {
		return false;
	}
	var path = dbPartitions[pname]; 
	while (!path) {
		pname = pname.substring(0, pname.length - 1);
		path = dbPartitions[pname];
	}
	path = "p/" + path + ".json";
	return path;
}
function indexOf(array, item) {
    var i = array.length;
    while (i--) {
       if (array[i] == item) {
           return i;
       }
    }
    return -1;
}
