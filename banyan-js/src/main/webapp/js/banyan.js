// ------ Document Init Methods
$(document).ready(function() {
	$(window).on('hashchange', onHashChange);
	initContextMenu();
	$(document).ready(initData);
});
function initData() {
	loadPartitionIndex(function() {
		loadJsonOnly([defaultTree], function() {
			loadCommandFromURL();
			initFaq();
		});
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
var dbChildIdsToParents = {};
var dbFileIds = {};
var dbRandomFiles = false;
var partitionSymbols = "0123456789abcdefghijklmnopqrstuvwxyz";
var defaultTree = "f:welcome-to-banyan";

var maxWidthHide = 106;
var maxWidthClose = 58;
var maxWidthFocus = 60;
var maxWidthDetail = 102;
var maxWidthShowChildren = 163;
var maxWidthShowMore = 159;
var digitWidth = 9;

var isHashChangeListening = true;
var isMenuActive = false;
var cancelerEvent = null;

// ------ GUI Events/Behavior/Menus
function __EventsBehaviorMenus() {}
function onHashChange() {
	if (isHashChangeListening) {
		loadCommandFromURL();
	} else {
		// we set this back because this will execute after an event
		isHashChangeListening = true;
	}
}
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
function initFaq() {
	var img = $("#faqImage img");
	var e = getRootEntry();
	img.attr("src", getImagesPath() + '/tiny/' + e.img);
	img.attr("alt", e.alt);
	img.attr("height", e.tHeight);
	img.attr("width", e.tWidth);
	initAllImagePreviewEvents();
}
function contextMenuClicked(aTag) {
	var action = aTag.id;
	var pos = aTag.href.indexOf('#');
	var id = aTag.href.substring(pos + 1);
	hideContextMenu();
	
	if (action == "cpClose") {
		closeNode(id);
	} else if (action == "cpHide") {
		hideChildren(id);
	} else if (action == "cpShowChildren") {
		loadAllChildren(id);
	} else if (action == "cpShowMore") {
		loadAllShowMore(id);
	} else if (action == "cpFocus") {
		focusOnNode(id);
	} else if (action = "cpDetail") {
		setUrl(aTag.href.substring(pos + 1), false);
	}
}
function closeNode(id) {
	markIdAsShown(id, false);
	renderCurrentTree();
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
		var link = buttonId + "#" + imgId;
		if (buttonValue) {
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
			link = "#" + getEntryDetailsHash(e);
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
		button.href = link;		
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
function hideChildren(id) {
	markChildrenAsShown(id, false);
	renderCurrentTree();
}
function setUrl(afterHash, turnOffListening) {
	var href = window.location.href;
	var pos = href.indexOf("#");
	if (pos > 0) {
		href = href.substring(0, pos);
	}
	if (turnOffListening) {
		isHashChangeListening = false;
	}
	window.location.href = href + "#" + afterHash;
}
function setUrlToAllVisibleIds() {
	var ids = getAllVisibleNodeIds();
	var cids = "c:" + crunch(ids);
	$("#treeLink,#treeLinkDetails").attr("href", "#" + cids);
	setUrl(cids, true);
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
	return getImagesPath() + "/preview/" + getImageEntry(img).img;
}
function getImageWidth(img) {
	return getImageEntry(img).pWidth;
}
function getImageHeight(img) {
	return getImageEntry(img).pHeight;
}
function getImageCaption(img) {
	var e = getImageEntry(img);
	var latinNameCaption = "<span class='PopupLatin'>(" + e.lname + ")</span>";
	var names = e.cnames || [];
	var commonNamesCaption = "";
	for (var i = 0; i < names.length; i++) {
		commonNamesCaption = commonNamesCaption + "<b>" + names[i] + "</b><br/>";
	}
	return commonNamesCaption + latinNameCaption;
}
function getImageEntry(img) {
	if (img.id == "faqImage") {
		return getRootEntry();
	}
	var id = img.id;
	var dash = id.indexOf('-');
	id = id.substring(dash + 1);
	return getMapEntry(id);
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
function isEntryShown(entryOrId) {
	if (entryOrId.id) {
		entryOrId = entryOrId.id;
	}
	return (dbEntryIdsToShow[entryOrId] == true);
}
function setEntryShownAs(entryOrId, shown) {
	if (entryOrId.id) {
		entryOrId = entryOrId.id;
	}
	dbEntryIdsToShow[entryOrId] = shown;
}
function setEntryShown(entryOrId) {
	setEntryShownAs(entryOrId, true);
}
function setEntryHidden(entryOrId) {
	setEntryShownAs(entryOrId, false);
}
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
function focusOnNode(id) {
	var e = getMapEntry(id);
	var p = e.parent;
	focusOnNodeParent(p, e);
	renderCurrentTree();
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
function hideAllNodes() {
	if (getRootEntry()) {
		markEntryChildrenAsShown(getRootEntry(), false);
	}
}
function markIdAsShown(id, show) {
	markEntryAsShown(getMapEntry(id), show);
}
function markEntryAsShown(e, show) {
	setEntryShownAs(e, show);
	if (!show) {
		markEntryChildrenAsShown(e, false);
	}
}
function markShowMoreAsShown(id) {
	var e = getMapEntry(id);
	for (var i = 0; i < e.showMoreLeafIds.length; i++) {
		markIdAsShown(e.showMoreLeafIds[i], true);
	}
	for (var i = 0; i < e.showMoreOtherIds.length; i++) {
		markIdAsShown(e.showMoreOtherIds[i], true);
	}
}
function markChildrenAsShown(id, show) {
	markEntryChildrenAsShown(getMapEntry(id), show);
}
// just these ids, nothing else
function markEntriesAsShown(entriesOrIds, show) {
	for (var i = 0; i < entriesOrIds.length; i++) {
		var entryOrId = entriesOrIds[i];
		if (entryOrId.childrenIds) {
			setEntryShownAs(entryOrId.id, show);
		} else {
			setEntryShownAs(entryOrId, show);
		}
	}
}
function markEntryChildrenAsShown(e, show) {
	for (var i = 0; i < e.children.length; i++) {
		var c = e.children[i];
		setEntryShownAs(c.id, show);
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
	var j;
	
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
		// add the g-children to that map
		for (j = 0; j < e.childrenIds.length; j++) {
			dbChildIdsToParents[e.childrenIds[j]] = e;
		}
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
			for (j = 0; j < p.children.length; j++) {
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
		initEntryDetailsCrunchedIds(e);
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
	cleanNodes(e);
	collapseNodesForChildrenToShow(e);
	hideLongCollapsed(e);
	collapseNodesForSiblingsToShow(e);
	prepareEntryForContextMenu(e);
}
function cleanNodes(e) {
	// we may have more things to do later...
	e.collapsedPinned = false;
	if (e.childrenToShow) {
		for (var i = 0; i < e.childrenToShow.length; i++) {
			var c = e.children[i];
			cleanNodes(c);
		}
	}
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
function prepareEntryForContextMenu(e) {
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
		prepareEntryForContextMenu(e.childrenToShow[i]);
	}
	for (var i = 0; i < e.siblings.length; i++) {
		prepareEntryForContextMenu(e.siblings[i]);
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
		if (isEntryShown(ids[i])) {
			count++;
		}
	}
	return count;
}
function getInitIds(ids) {
	if (!ids) {
		return [];
	} else {
		return uncrunch(ids);
	} 
}
// only those things that need to be done exactly one time when first loaded
function initEntry(e) {
	e.cpDetail = true;
	e.childrenIds = getInitIds(e.childrenIds);
	e.showMoreLeafIds = getInitIds(e.showMoreLeafIds);
	e.showMoreOtherIds = getInitIds(e.showMoreOtherIds);
	e.cpClose = true;
	if (!e.extinct) {
		e.extinct = false;
	}
	e.siblings = [];
	e.collapsed = [];
	if (e.cnames) {
		e.cname = e.cnames[0];
	}
	if (e.cname) {
		e.alt = e.cname;
	}  else {
		e.alt = e.lname;
	}
}
function initEntryDetailsCrunchedIds(e) {
	var ids = [];
	ids = ids.concat((e.childrenIds));
	ids.push(e.id);
	var p = e.parent;
	while (p) {
		ids.push(p.id);
		p = p.parent;
	}
	ids.sort(sortIntCompare);
	var cids = crunch(ids);
	e.detailCrunchedIds = cids;
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
		e.partitionPath = p.partitionPath + "" + getPartitionPathPart(index); // to ensure by string
	}
}
function getEntryDetailsHash(e) {
	return "t:details:" + e.id + "," + e.detailCrunchedIds;
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
	getVisibleNodeIds(getRootEntry(), ids);
	ids.sort(sortIntCompare);
	return ids;
}
function sortIntCompare(a, b) {
	return a - b;
}
function getVisibleNodeIds(e, ids) {
	if (isEntryShown(e.id)) {
		ids.push(e.id);
		for (var i = 0; i < e.children.length; i++) {
			getVisibleNodeIds(e.children[i], ids);
		}
	}
}
//util mostly for testing phase
function addAllToRenderMap(e) {
	addAllToRenderMapDownstream(e);
	var p = e;
	while (p) {
		setEntryShown(p.id);
		e = p;
		p = p.parent;
	}
	return e.id;
}
function addAllToRenderMapDownstream(e) {
	setEntryShown(e.id);
	for (var i = 0; i < e.children.length; i++) {
		addAllToRenderMapDownstream(e.children[i]);
	}
}
// ------ Tree/HTML Rendering
function __TreeHtmlRendering() {}
function renderCurrentTree(skipUrl) {
	renderTree(getRootEntry().id, true);
	if (!skipUrl) {
		setUrlToAllVisibleIds();
	}
}
function renderTree(id, keepOnlyNew) {
	$("#treeTab").empty();
	// for this test, we flag every entry as being rendered
	if (!keepOnlyNew) {
		id = addAllToRenderMap(getMapEntry(id));
	}
	var e = getMapEntry(id);
	// we need to collapse nodes only once we know the map is done
	prepareNodesForRender(e);
	buildTree($("#treeTab"), e);
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
	var detailsHash = getEntryDetailsHash(e);
	span.append(pad + '<a title="Go to Details" href="#' + detailsHash + 
			'"><img src="' + iconPath() + '/' + detailIcon + '" class="' +
			detailClass + '" alt="search.detail" /></a>');
	// image and link
	var name = getEntryDisplayName(e);
	var img;
	var linkTitle;
	var imgClass;
	if (e.img) {
		img = '<img alt="' + e.alt + '" height="' + e.tHeight + '" width="' + e.tWidth + '" src="' + 
			getImagesPath() + '/tiny/' + e.img + '" class="Thumb" />';
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
	span.append('<a class="' + imgClass + '"' + linkTitle + ' id="entry-' + e.id + '" href="#' + detailsHash + '">' 
			 + name + img + '</a>');
	
	// menu button
	var canShowMore = (e.cpShowChildren || e.cpShowMore);
	var menuMore = "menu_more.png";
	if (!canShowMore) {
		menuMore = "menu_less.png";
	}
	span.append('<a href="search.tree/CRUNCHED#' + e.id + '" name="' + e.id + '" class="opener">'
			+ '<img src="' + iconPath() + '/' + menuMore + '" alt="menu"></a>');
}
function getNbsps(count) {
	var pad = "";
	for (var i = 0; i < count; i++) {
		pad = pad + "&nbsp";
	}
	return pad;
}
function getImagesPath() {
	return "http://jacobrobertson.com/banyan-images"; // "images";
}
function iconPath() {
	return "icons"; // "http://jacobrobertson.com/banyan/icons"; // "icons";
}
function getRenderTaxoDisplayName(e) {
	var name = "<i>(" + e.lname + ")</i>";
	if (e.cname) {
		name = e.cname + " " + name;
	}
	return name;
}
function renderDetails(id) {
	var e = getMapEntry(id);

	var gbase = "http://www.google.com/";
	var gquery = "?q=";
	if (e.cnames) {
		for (var i = 0; i < e.cnames.length; i++) {
			gquery += e.cnames[i];
			gquery += " ";
		}
	}
	gquery += e.lname;
	gquery = gquery.replace(/ /g, '+');
	
	var displayName = getEntrySimpleDisplayName(e);
	$(".DetailTitleName").html(displayName); // TODO should list all names
	$("#DetailLatinTitle").html(e.lname);
	$("#DetailGoogleLink").attr("href", gbase + "search" + gquery);
	$("#DetailGoogleImageLink").attr("href", gbase + "images" + gquery);
	
	var searchName = e.lname;
	if (e.cname) {
		searchName = e.cname + " (" + searchName + ")";
	}
	$(".SearchTerm").html(searchName);
	
	var img = $("#DetailImage");
	img.attr("alt", "");
	img.attr("height", e.pHeight);
	img.attr("width", e.pWidth);
	img.attr("src", getImagesPath() + "/preview/" + e.img);

	var wikiLink = "http://species.wikimedia.org/wiki/File:" + e.iLink;
	$("#DetailImageWikiSpeciesLink").attr("href", wikiLink);

	var taxoEntry = $("#TaxonomyCell .Entry");
	taxoEntry.empty();
	var table = $("<table></table>").appendTo(taxoEntry);
	var ancestors = [];
	var p = e;
	while (p) {
		ancestors.push(p);
		p = p.parent;
	}
	var i;
	for (i = ancestors.length - 1; i >= 0; i--) {
		var a = ancestors[i];
		var tr = $("<tr><td class='Rank'><span>" + a.rank + "</span></td></tr>").appendTo(table);
		var td = $("<td></td>").appendTo(tr);
		renderDetailsEntryPreviewPart(td, a);
	}

	// <table id="SubSpeciesTable">	
	// divide them up into columns
	var children = e.children; // we show all whether they were visible or not
	
	// each child is the same as the tax area - make common
	
	// if there are no children, we should hide that area
	var subTableNode = $("#SubSpeciesTableNode");
	if (!children || children.length == 0) {
		subTableNode.hide();
	} else {
		subTableNode.show();
		var subTable = $("#SubSpeciesTable");
		subTable.empty();
		for (i = 0; i < children.length; i++) {
			var tr = $("<tr></tr>").appendTo(subTable);
			var td = $("<td></td>").appendTo(tr);
			renderDetailsEntryPreviewPart(td, children[i]);
		}
	}
	
	initAllImagePreviewEvents();
}
function renderDetailsEntryPreviewPart(td, e) {
	var href = getEntryDetailsHash(e);
	$("<a title='Go to Detail' href='#" + href 
		+ "'><img src='icons/green_button.png' class='detail-button'></a>").appendTo(td);
	var taxoName = getRenderTaxoDisplayName(e);
	var previewA = $("<a id='taxo-" + e.id + "' class='preview' href='#" + href + "'>" + taxoName + "</a>").appendTo(td);
	if (e.img) {
		$("<img height='" + e.tHeight + "' width='" + e.tWidth 
			+ "' class='Thumb' src='" + getImagesPath() + '/tiny/' + e.img + "'></img>").appendTo(previewA);
	}
}

// ------ JSON Functions
function __JsonFunctions() {}
function loadCommandFromURL() {
	var url = window.location.href;
	var index = url.indexOf("#");
	var hashValue = "";
	if (index > 0) {
		hashValue = url.substring(index + 1);
	} 
	if (hashValue.length == 0) {
		// we will never just load nothing
		hashValue = defaultTree;
	}
	
	// split the command if needed
	var colon = hashValue.charAt(1);
	var command;
	var value;
	var commandParam;
	if (colon == ":") {
		command = hashValue.charAt(0);
		value = hashValue.substring(2);
		var colon2Index = value.indexOf(":");
		if (colon2Index > 0) {
			commandParam = value.substring(colon2Index + 1);
			value = value.substring(0, colon2Index);
		}
	} else {
		command = "";
		value = hashValue;
	}

	// default view
	var tab = "treeTab";
	
	if (command == "f") {
		loadExampleFile(hashValue);
	} else if (command == "t") {
		if (value == "random") {
			loadRandomFile();
		} else if (value == "startOver") {
			loadExampleFile(defaultTree);
		} else if (value == "details") {
			loadDetails(commandParam);
			tab = "detailsTab";
		} else {
			// other tab-commands can respond to visual treatment
			tab = value;
		}
	} else if (command == "i") {
		var ids = value.split(",");
		loadJsonThenMarkOnlyNewVisible(ids);
	} else if (command == "c") {
		var ids = uncrunch(value);
		loadJsonThenMarkOnlyNewVisible(ids);
	}
	// check the tabs first to ensure the tree is visible
	loadTab(tab);
}
// should be from "t:details:id,crunchedId"
function loadDetails(params) {
	var comma = params.indexOf(",");
	var id = params.substring(0, comma);
	var otherIds = params.substring(comma + 1);
	otherIds = uncrunch(otherIds);
	loadJsonOnly(otherIds, build_loadDetails_callback(id));
}
function build_loadDetails_callback(id) {
	return function(entries) {
		renderDetails(id);
	}
}
function loadTab(id) {
	$(".tabCommand").hide();
	$("#" + id).show();
}
function loadRandomFile() {
	if (!dbRandomFiles) {
		loadRandomFileIndexFromJson();
	} else {
		var next = dbRandomFiles.shift();
		dbRandomFiles.push(next);
		setUrl("f:" + next, false);
	}
}
function loadRandomFileIndexFromJson() {
	$.getJSON("json/f/random.json", function(data) {
		dbRandomFiles = data.files;
		loadRandomFile();
	});
}
function loadExampleFile(file) {
	hideAllNodes();
	loadJsonThenAddEntries([file], false, build_loadExampleFile_callback());
}
function build_loadExampleFile_callback() {
	return function(entries) {
		markEntriesAsShown(entries, true);
		renderCurrentTree(true);
	}
}
function loadAllChildren(id) {
	var childrenIds = getMapEntry(id).childrenIds;
	loadJsonThenMarkNewIdsVisible(childrenIds);
}
function loadAllShowMore(id) {
	var e = getMapEntry(id);
	// build the full list
	var allShowMoreIds = e.showMoreLeafIds.concat(e.showMoreOtherIds);
	loadJsonThenMarkNewIdsVisible(allShowMoreIds);
}

function loadJsonThenMarkOnlyNewVisible(fileNamesOrIds) {
	hideAllNodes();
	loadJsonThenAddEntries(fileNamesOrIds, true);
}
// these are the master "load ids" methods, and should be altered to accomodate the one or two scenarios we have
// - load these nodes/files exactly, and then mark exactly those nodes visible in addition to current tree, then render tree
// - load a brand new tree (? maybe already handled by calling method)
// - load these nodes, but don't do anything else (? not sure that's a valid scenario)
function loadJsonOnly(fileNamesOrIds, callback) {
	loadJsonThenAddEntries(fileNamesOrIds, false, callback);
}
function loadJsonThenMarkNewIdsVisible(fileNamesOrIds, callback) {
	loadJsonThenAddEntries(fileNamesOrIds, true, callback);
}
function loadJsonThenAddEntries(fileNamesOrIds, showTree, outerCallback) {
	var callback = build_loadJsonThenAddEntries_callback(fileNamesOrIds, showTree, outerCallback);
	loadJson(fileNamesOrIds, callback);
}
function build_loadJsonThenAddEntries_callback(newIds, showTree, callback) {
	return function(entries) {
		if (showTree) {
			if (newIds.length > 0 && isFileName(newIds[0])) {
				// then we need to mark all new entries shown instead
				markEntriesAsShown(entries, true);
			} else {
				markEntriesAsShown(newIds, true);
			}
			renderCurrentTree();
		}
		if (callback) {
			callback(entries);
		}
	};
}
// ids would come from "open children" for example
function loadJson(fileNamesOrIds, callback) {
	var entries = [];
	loadJsonInner(fileNamesOrIds, callback, entries);
}
function loadJsonInner(fileNamesOrIds, callback, entries) {
	// build actual list of ids based on what we don't have already
	var idsToProcess = [];
	var idsWithoutParents = [];
	for (var i = 0; i < fileNamesOrIds.length; i++) {
		var id = fileNamesOrIds[i];
		if (isFileName(id)) {
			idsToProcess.push(id);
		} else {
			var e = getMapEntry(id);
			if (!e) {
				if (dbChildIdsToParents[id]) {
					idsToProcess.push(id);
				} else {
					idsWithoutParents.push(id);
				}
			}
		}
	}
	var currentCallback = callback;
	if (idsWithoutParents.length > 0 && idsToProcess.length > 0) {
		currentCallback = buildLoadJsonNextEntriesCallback(idsWithoutParents, callback, entries);
	}
	// build list of functions - we don't care which order
	for (var j = 0; j < idsToProcess.length; j++) {
		var parentCallback = currentCallback;
		var thisCallback = buildJsonCallback(idsToProcess[j], parentCallback);
		currentCallback = thisCallback;
	}
	// call the last function, it will cascade up
	currentCallback(entries);
}
function isFileName(name) {
	name = "" + name;
	return (name.length > 1 && name.charAt(0) == "f" && name.charAt(1) == ":");
}
function buildLoadJsonNextEntriesCallback(idsWithoutParents, callback, entries) {
	return function() {
		loadJson(idsWithoutParents, callback, entries);
	};
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
	var url = "json/";
	var loadNeeded = true;
	if (isFileName(jsonId)) {
		url = url + "f/" + jsonId.substring(2) + ".json";
	} else {
		// we might have already loaded this id in a call chain, no need to look it up
		if (dbMap[jsonId]) {
			loadNeeded = false;
		}
		url = url + buildPartitionFilePath(jsonId);
	}
	if (loadNeeded) {
		callback = buildAssignFileIdsCallback(jsonId, callback);
	}
	if (loadNeeded) {
		var innerSuccessCallback = buildInnerJsonSuccessCallback(entries, callback);
		return $.getJSON(url, innerSuccessCallback);
	} else {
		callback(entries);
	}
}
function buildAssignFileIdsCallback(fileName, callback) {
	return function(entries) {
		var ids = [];
		for (var i = 0; i < entries.length; i++) {
			ids.push(entries[i].id);
		}
		dbFileIds[fileName] = ids;
		callback(entries);
	};
}
function buildInnerJsonSuccessCallback(entries, callback) {
	return function(data) {
		// this is first thing that happens when JSON returns.
		// we add the entries immediately to the global map
		// and also add to the current list we are tracking for the callback chain
		addEntriesToMap(data.entries);
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
function buildPartitionFilePath(id) {
	var parent = dbChildIdsToParents[id];
	var pname = parent.partitionPath;
	
	var index = indexOf(parent.childrenIds, id);
	pname = pname + "" + getPartitionPathPart(index); // to ensure we concatenate
	
	var path = dbPartitions[pname]; 
	while (!path && pname.length > 0) {
		pname = pname.substring(0, pname.length - 1);
		path = dbPartitions[pname];
	}
	path = "p/" + path + ".json";
	return path;
}
function getPartitionPathPart(index) {
	var path = "";
	while (index >= partitionSymbols.length) {
		index = index - partitionSymbols.length;
		path = path + "_";
	}
	path = path + partitionSymbols.charAt(index);
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
// ------ Crunched Ids
function __CrunchedIds() {}
var crunchedMinPadSize = 3;
var crunchedSubtractionIndicator = '-';
var crunchedPadChangeDelimiter = '_';
var crunchedRebaseIndicator = '.';
var crunchedChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
var crunchedCharsMap = {};
var crunchedRadix = crunchedChars.length;
var crunchedZeroChar = "0";
var crunchedMinValuesCount = 10;
var crunchedPads = [];
var crunchedMinValues = [];
initCruncher();
function initCruncher() {
	var padding = "";
	var i;
	for (i = 0; i < crunchedMinValuesCount; i++) {
		crunchedMinValues[i] = Math.pow(crunchedRadix, i + 1);
		crunchedPads[i] = padding;
		padding += crunchedPadChangeDelimiter;
	}
	for (i = 0; i < crunchedChars.length; i++) {
		crunchedCharsMap[crunchedChars[i]] = i;
	}
}
function crunchOne(n, pad, padSize) {
	var s;
	if (n == 0) {
		s = crunchedZeroChar;
	} else {
		s = "";
		while (n > 0) {
			var next = Math.floor(n / crunchedRadix);
			var diff = (n - (next * crunchedRadix));
			var diffChar = crunchIntToChar(diff);
			s = diffChar + s;
			n = next;
		}
	}
	if (pad) {
		var padded = leftPad(s, padSize, crunchedZeroChar);
		return padded;
	} else {
		return s;
	}
}
function leftPad(s, padSize, padChar) {
	while (s.length < padSize) {
		s = padChar + s;
	}
	return s;
}
// TODO delete these two test methods
function testCrunch() {
	var val = $("#uncrunchedIds").val();
	var nums = val.split(",");
	var crunched = crunch(nums);
	$("#crunchOutput").val(crunched);
}
function testUncrunch() {
	var val = $("#crunchedIds").val();
	var uncrunched = uncrunch(val);
	$("#crunchOutput").val(uncrunched);
}
function uncrunch(s) {
	var ids = [];
	var subPos = s.indexOf(crunchedSubtractionIndicator);
	if (subPos < 0) {
		subPos = s.indexOf(crunchedRebaseIndicator);
	}
	var toAdd = 0;
	var useSub = subPos >= 0;
	var padSize = crunchedMinPadSize;
	var len = s.length;
	for (var i = 0; i < len;) {
		var c = s.charAt(i);
		if (c == crunchedRebaseIndicator) {
			if (padSize == 1) {
				padSize = crunchedMinPadSize;
			} else if (padSize >= crunchedMinPadSize) {
				padSize = 1;
			} else {
				return false;
				// throw new IllegalArgumentException("Not expecting " + rebaseIndicator + " at pos " + i + " of string " + s);
			}
			i++;
		} else if (c == crunchedSubtractionIndicator) {
			padSize--;
			i++;
		} else if (c == crunchedPadChangeDelimiter) {
			padSize++;
			i++;
		} else {
			var sub = s.substring(i, i + padSize);
			var val = uncrunchToInt(sub);
			if (useSub) {
				val += toAdd;
				toAdd = val;
			}
			ids.push(val);
			i += padSize;
		}
	}
	return ids;
}
function uncrunchToInt(s) {
	var len = s.length - 1;
	var val = 0;
	for (var i = 0; i <= len; i++) {
		var c = s.charAt(i);
		var n = uncrunchCharToInt(c);
		val += (n * Math.pow(crunchedRadix, len - i));
	}
	return val;
}
function uncrunchCharToInt(c) {
	return crunchedCharsMap[c];
}
function crunchIntToChar(i) {
	return crunchedChars.charAt(i);
}
// documentation found in Java file
function crunch(nums) {
	var usedSymbol = false;
	var last = 0;
	var buf = "";
	var currentPad = crunchedMinPadSize;
	var nsize = nums.length;
	for (var i = 0; i < nsize; i++) {
		var num = nums[i];
		var sub = num - last;
		last = num;
		num = sub;
		var s = crunchOne(num, false, crunchedMinPadSize);
		var len = s.length;
		var padDiff = currentPad - len;
		if (padDiff > 0) {
			if (padDiff == 1) {
				// first determine if we should just zero pad it instead
				var zeroPad = false;
				// if it's the last number, can't compare the next number
				if (i < nsize - 1) {
					// look at the next number to see if it will just go back up again
					var nextNum = nums[i + 1] - last;
					var maxNext = crunchedMinValues[len - 1];
					if (nextNum >= maxNext) {
						zeroPad = true;
					}
				} else {
					// the last number, so zero pad to look nicer
					zeroPad = true;
				}
				if (zeroPad) {
					buf += crunchedZeroChar;
					len++;
				} else {
					buf += crunchedSubtractionIndicator;
					usedSymbol = true;
				}
			} else if (currentPad >= crunchedMinPadSize && len == 1) {
				buf += crunchedRebaseIndicator;
				usedSymbol = true;
			} else {
				for (var j = 0; j < padDiff; j++) {
					buf += crunchedSubtractionIndicator;
				}
				usedSymbol = true;
			}
		} else if (padDiff < 0) {
			if (padDiff == -1) {
				buf += crunchedPadChangeDelimiter;
			} else if (currentPad == 1 && len == crunchedMinPadSize) {
				buf += crunchedRebaseIndicator;
				usedSymbol = true;
			} else {
				for (var j = 0; j < -padDiff; j++) {
					buf += crunchedPadChangeDelimiter;
				}
			}
		} else {
			// no action needed...
		}
		currentPad = len;
		buf += s;
	}
	var other = crunchSimple(nums);
	var olen = other.length;
	if (!usedSymbol) {
		// we need this case, because otherwise when we parse it, we won't look for subtraction
		if (buf.length + 1 < olen) { 
			buf += crunchedSubtractionIndicator;
			return buf;
		} else {
			return other;
		}
	} else if (olen <= buf.length) {
		// this covers some very rare cases, plus makes it so w prefer non-subtraction style (easier on eyes)
		return other;
	} else {
		return buf;
	}
}
function crunchSimple(nums) {
	var minPadSize = crunchedMinPadSize;
	var crunched = "";
	var minValue = crunchedMinValues[minPadSize - 1];
	for (var i = 0; i < nums.length; i++) {
		var n = nums[i];
		while (n >= minValue) {
			crunched += crunchedPadChangeDelimiter;
			minPadSize++;
			minValue = crunchedMinValues[minPadSize - 1];
		}
		var one = crunchOne(n, true, minPadSize);
		crunched += one;
	}
	return crunched;
}
