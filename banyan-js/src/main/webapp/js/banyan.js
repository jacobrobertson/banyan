$(document).ready(function() {
	$(window).on('hashchange', onHashChange);
	initContextMenu();
	initLazyButtons();
	$(document).ready(initData);
	setCanonicalLinkOnReady();
});
function initData() {
	loadPartitionIndex(function() {
		loadJsonOnly([defaultTree], function() {
			loadCommandFromURL();
			initPageElements();
		});
	});
	loadRootSearchIndex();
}
$(document).ready(function() {
	$("#textfield").focus(function() {
		this.select();
	});
	$("#textfield").autocomplete({
		source : function(request, callback) {
			return findSearchSuggestions(request.term, callback);
		},
		select : function(event, ui) {
			event.preventDefault();
			var value = $(this).val();
			var searched = submitSearchQuery(event, ui);
			$(this).val(value);
			return searched;
		},
		focus : function(event) {
			event.preventDefault();
		},
		change : function(event) {
			event.preventDefault();
		},
		minLength : minSearchKeyLength,
		autoFocus: true
	}).focus(function () {
	    $(this).autocomplete("search");
	}).autocomplete("instance")._renderItem = function( ul, item ) {
		// $(ul).addClass("Node");
      	return $( "<li class='searchSuggestion'>" )
        	.append( createSuggestionHtml(item) )
        	.appendTo( ul );
    };
	$("#textfield").focus();
	$("*").mousemove(function() {areMenusAllowed = true;});
	$(".navQueryLink").click(queryLinkEvent);
});
function queryLinkEvent(e) {
    e.preventDefault();
    var href = this.href;
    loadCommandFromQuery(href);
}

//------ Global vars
function __GlobalVars(){}
var S3_BASE_URL = "https://d1w3fa9nlzlb3n.cloudfront.net";
var dbMap = {};
var dbPartitions;
var dbChildIdsToParents = {};
var dbFileIds = {};
var dbRandomFileKeys = false;
var dbRandomFileKeysToFileNames = false;
var partitionSymbols = "0123456789abcdefghijklmnopqrstuvwxyz";
var defaultTree = "e:welcome-to-banyan";
var examplesIndexLoaded = false;

var maxWidthHide = 106;
var maxWidthClose = 58;
var maxWidthFocus = 60;
var maxWidthDetail = 102;
var maxWidthShowChildren = 163;
var maxWidthShowMore = 159;
var digitWidth = 9;

var minSearchKeyLength = 3;
var maxSearchKeyLength = 12;
var dbLocalSearchIndex = {};
var dbRemoteSearchIndex = new Set(["@root"]);

var isMenuActive = false;
var cancelerEvent = null;
var areMenusAllowed = true;
var detailedId = false;

var menuData = [
	{"id": "mShowChildren", "png": "open_children", 	"caption": "Show More Children"},
	{"id": "mShowMore", 	"png": "show-interesting", 	"caption": "Show More Species"},
	{"id": "mHide", 		"png": "close-children", 	"caption": "Hide Children"},
	{"id": "mClose", 		"png": "close", 			"caption": "Close"},
	{"id": "mFocus", 		"png": "focus", 			"caption": "Focus"},
	{"id": "mPin", 			"png": "pin_image", 		"caption": "Pin Image"},
	{"id": "mUnpin", 		"png": "unpin_image", 		"caption": "Unpin Image"},
	{"id": "mDetail", 		"png": "goto-details", 		"caption": "Go to Details"},
	{"id": "mBack",			"png": "back", 				"caption": "Back to your #Tree"}
];

// ------ GUI Events/Behavior/Menus
function __EventsBehaviorMenus() {}
function onHashChange() {
	loadCommandFromURL();
}
function hideContextMenu() {
	isMenuActive = false;
	$("#menu").hide();
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
function initLazyButtons() {
	$(".lazyButton").each(function () {
		// <img class="faqButton" src="icons/focus.png"/>
		var id = $(this).attr("id");
		$(this).attr("src", "icons/" + id + ".png");
	});
}
function initContextMenu() {
	renderContextMenu();
	$("#menu").mouseenter(function() {
		isMenuActive = true;
	});
	$("#menu").mouseleave(function() {
		hideContextMenu();
	});
	$("#menu").mousemove(function() {
		areMenusAllowed = true;
		isMenuActive = true;
	});
	
	$(".mButton").bind("click", function() {
		contextMenuClicked(this);
		return false;
	});
}
function renderContextMenu() {
	var menuDiv = $('<div id="menu"></div>');
	for (var i = 0; i < menuData.length; i++) {
		var d = menuData[i];
		var rowAnchor = $('<a class="mButton" id="' + d.id + '" href="TBD" style="display: inline;"></a>');
		var rowDiv = $('<div class="MenuRow"></div>');
		var rowImage = $('<img alt="' + d.caption + '" src="icons/' + d.png + '.png" style="display: none"></img>');
		var rowCaption = $('<span id="' + d.id + 'Caption">' + d.caption + '</span>');
		
		menuDiv.append(rowAnchor);
		rowAnchor.append(rowDiv);
		rowDiv.append(rowImage);
		rowDiv.append("&nbsp;");
		rowDiv.append(rowCaption);
	}
	$(":root").append(menuDiv);
}
function initPageElements() {
	// FAQ items that need to come from entries
	var img = $("#faqImage img");
	var e = getRootEntry();
	img.attr("src", getImageTinySrcPath(e));
	img.attr("alt", e.alt);
	img.attr("height", e.tHeight);
	img.attr("width", e.tWidth);
	initTreeEvents();
}
function getImageTinySrcPath(e) {
	if (e.imgData) {
		return "data:image;base64," + e.imgData;
	} else {
		return getImagesPath("tiny") + '/' + e.img;
	}
}
function contextMenuClicked(aTag) {
	var action = aTag.id;
	var pos = aTag.href.indexOf('#');
	var id = aTag.href.substring(pos + 3);
	hideContextMenu();
	
	if (action == "mDetail" || action == "mBack") {
		setUrlForDetail(aTag.href.substring(pos + 1));
//	} else if (action == "mBack") {
		// we want to browse to that
	} else {
		performMenuAction(action, id);
	}
}
function performMenuAction(action, id) {
	if (action == "mClose") {
		closeNode(id);
	} else if (action == "mHide") {
		hideChildren(id);
	} else if (action == "mShowChildren") {
		var e = getMapEntry(id);
		if (!e.mBack) {
			loadAllChildren(id);
		}
	} else if (action == "mShowMore") {
		loadAllShowMore(id);
	} else if (action == "mFocus") {
		focusOnNode(id);
	} else if (action == "mPin") {
		pinNode(id, true);
	} else if (action == "mUnpin") {
		pinNode(id, false);
	}
	var jqueryElement = $("#" + id);
	setMenuButtonLink(id, jqueryElement);
}
function renderCrawlCommand(id) {
	var e = getMapEntry(id);
	var action;
	if (e.mFocus) {
		action = "mFocus";
	} else if (e.mShowChildren) {
		action = "mShowChildren";
	} else if (e.mShowMore) {
		action = "mShowMore";
	} else {
		action = "mClose";
	}
	performMenuAction(action, id);
}
function closeNode(id) {
	var parentId = getMapEntry(id).parentId;
	markIdAsShown(id, false);
	renderCurrentTree();
	highlightNodes(parentId);
}
function pinNode(id, pinned) {
	getMapEntry(id).pinned = pinned;
	renderCurrentTree();
	highlightNodes(id);
}
function showContextMenu(e, img) {
	if (!areMenusAllowed) {
		return false;
	}
	var isDetails = $("#detailsTab").is(':visible');
	var imgId = img.id;
	// create the right menu links
	var e = getMapEntry(imgId);
	var buttons = $("#menu a");
	var rowsShownCount = 0;
	var maxWidth = 0;
	for ( var i = 0; i < buttons.length; i++) {
		button = buttons[i];
		var buttonId = button.id;
		var buttonValue = e[buttonId];
		var link = buttonId + "#!/" + imgId;
		var isDetailsButton = (buttonId == 'mBack' || buttonId == 'mDetail');
		if (buttonValue && (isDetailsButton || !isDetails)) {
			$(button).show();
			rowsShownCount++;
		} else {
			$(button).hide();
		}
		if (buttonId == 'mHide') {
			maxWidth = Math.max(maxWidth, maxWidthHide);
		} else if (buttonId == 'mClose') {
			maxWidth = Math.max(maxWidth, maxWidthClose);
		} else if (buttonId == 'mFocus') {
			maxWidth = Math.max(maxWidth, maxWidthFocus);
		} else if (buttonId == 'mDetail') {
			maxWidth = Math.max(maxWidth, maxWidthDetail);
			link = "?q=" + getEntryDetailsHash(e);
		} else if (buttonId == 'mShowChildren') {
			var showCaption = e.mShowChildrenCaption;
			if (showCaption) {
				$("#mShowChildrenCaption").text(showCaption);
				maxWidth = getVariableWidth(showCaption, 20, maxWidthShowChildren, maxWidth);
			}
		} else if (buttonId == 'mShowMore') {
			var showMoreCaption = e.mShowMoreCaption;
			if (showMoreCaption) {
				$("#mShowMoreCaption").text(showMoreCaption);
				maxWidth = getVariableWidth(showMoreCaption, 19, maxWidthShowMore, maxWidth);
			}
		} else if (buttonId == 'mBack') {
			link = $("#treeLink").attr("href");
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
	
	$("#menu").detach().appendTo(":root");
	$(".mButton img").show();
	$("#menu").show().css( {
		"top" : top,
		"left" : left
	});
}
function hidePreviewPanel() {
	var previewE = $("#preview");
	if (previewE) { // not sure why I have this check
		previewE.hide();
	 	$("#previewCaption").hide();
 		$("#previewImage").hide();
	}
}
function showPreviewPanel(e) {
	if (!areMenusAllowed) {
		return false;
	}
	hidePreviewPanel();
	var img = e.currentTarget;
	var iwidth = getImageWidth(img);
	var iheight = getImageHeight(img);
	var src = getPreviewImageSrc(img); // this.href;

	var previewImage = $("#previewImage");
	var same = (src == previewImage.attr("src"));
	
	if (!same) {
		previewImage.attr("src", "");
		previewImage.css("width", iwidth + "px").css("height", iheight + "px"); 
		previewImage.attr("src", src);
	}
	previewImage.show();

	var previewCaption = $("#previewCaption");
	var caption = getPreviewImageCaption(img);
	previewCaption.html(caption);
	previewCaption.show();
	
	var previewOuter = $("#preview");
	var previewWidth = previewOuter.width();
	
	previewOuter.css("top", getPreviewTop(e, img) + "px").css(
			"left", getPreviewLeftForWidth(e, previewWidth) + "px"); 
	previewOuter.show();
}
function getPreviewImageCaption(img) {
	var e = getImageEntry(img);
	var latinNameCaption = "<span id='previewLatin'>(" + e.lname + ")</span>";
	var names = e.cnames || [];
	var commonNamesCaption = "";
	for (var i = 0; i < names.length; i++) {
		var name = getCaptionNameWithIndicator(names[i]);
		commonNamesCaption = commonNamesCaption + "<b>" + name + "</b><br/>";
	}
	return "<div id='previewCaptions'>" + 
		commonNamesCaption + latinNameCaption + "</span><br/>";
}
function getCaptionNameWithIndicator(name) {
	if (name.indexOf("...") >= 0) {
		return "<i>(" + name + ")</i>";
	} else {
		return name;
	}
}
function initTreeEvents() {
	$("a.preview").off(".preview");
	$("a.preview").on({
		"mouseenter.preview": function(e) {
			showPreviewPanel(e);
		},
		"mouseout.preview": function() {
 			hidePreviewPanel();
		},
		"mousemove.preview": function(e) {
			areMenusAllowed = true;
			showPreviewPanel(e);
		}
	});

	$(".menuopener").off(".preview");
	$(".menuopener").on({
		"click.preview mouseenter.preview": function(e) {
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
		}
	});
	$(".treeQueryLink").click(queryLinkEvent);
}
function hideChildren(id, skipUrl, skipHighlight) {
	markChildrenAsShown(id, false);
	renderCurrentTree(skipUrl);
	if (!skipHighlight) {
		highlightNodes(id);
	}
}
// do not call directly - call from wrapper only
function setUrlInner(afterHash) {
	var href = window.location.href;
	var pos = href.indexOf("#");
	if (pos > 0) {
		href = href.substring(0, pos);
	}
	// this will cause a page refresh, but it is necessary to fix issues with
	// the wrong canonical link, etc.  We double check that it only happens "when needed"
	pos = href.indexOf("?q=");
	if (pos > 0) {
		var query = href.substring(pos + 3);
		if (query != afterHash) {
			href = href.substring(0, pos);
		}
	}
	window.location.href = href + "#!/" + afterHash;
	setCanonicalLink(afterHash);
}
// this should only get called
function setCanonicalLinkOnReady() {
	var afterHash = getHashFromUrl(window.location.href);
	setCanonicalLink(afterHash);
}
function setCanonicalLink(afterHash) {
	// now also change the canonical version of the page's url
	var firstColon = afterHash.indexOf(':');
	var param;
	if (firstColon > 0) {
		param = afterHash.substring(0, firstColon) + "/" + afterHash.substring(firstColon + 1);
	} else {
		param = afterHash;
	}
	var link = $('link[rel="canonical"]');
	if (! link.length) {
		link = $('<link rel="canonical" href="" />').appendTo($("head"));
	}
	link.attr("href", "http://jacobrobertson.com/banyan/q/" + param);	
}
function setUrlToAllVisibleIds() {
	var ids = getAllVisibleNodeIds();
	var pids = getAllPinnedNodeIds();
	setUrlsToIdsArray(ids, pids, true, true);
}
function setUrlsToIdsArray(ids, pinnedIds, setWindowUrl, setLinkUrls) {
	// set global var
	var cids = crunch(ids);
	var idsString = "c:" + cids;
	if (pinnedIds && pinnedIds.length > 0) {
		idsString = idsString + ":p:" + crunch(pinnedIds);
	}
	if (setLinkUrls) {
		setTreeLinksToValue(idsString);
	}
	if (setWindowUrl) {
		setUrlInner(idsString);
	}
}
function setTreeLinksToValue(value) {
	$("#treeLink,#treeLinkDetails").attr("href", "?q=" + value);
}
function setUrlForDetail(detailParams) {
	// does not change #tree links
	loadCommandFromQuery(detailParams);
}
function setTreeLinksForFile(file) {
	setTreeLinksToValue(file);
}
// ------ General Utils
function __GeneralUtils() {}
function log(m, level) {
	var minLogLevel = 5;
	if (level >= minLogLevel) {
		$("#log").append("<div>" + m + "</div>");
	}
}
function getPreviewLeft(e, img) {
	var imageWidth = getImageWidth(img);
	return getPreviewLeftForWidth(e, imageWidth);
}
function getPreviewLeftForWidth(e, imageWidth) {
	var w = $("body").width();
	var x = e.pageX;

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
	var textBaseHeight = 75;
	return getTop(e, imageHeight, textBaseHeight);
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
function getPreviewImageSrc(img) {
	return getImagesPath("preview") + "/" + getImageEntry(img).img;
}
function getImageWidth(img) {
	return getImageEntry(img).pWidth;
}
function getImageHeight(img) {
	return getImageEntry(img).pHeight;
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
	if (!entryOrId.id) {
		entryOrId = getMapEntry(entryOrId);
	}
	if (!entryOrId) {
		// sometimes we're checking if an entry that isn't loaded yet is shown, which it isn't
		return false;
	} else {
		return (entryOrId.show == true);
	}
}
function setEntryShownAs(entryOrId, shown) {
	if (!entryOrId.id) {
		entryOrId = getMapEntry(entryOrId);
	}
	entryOrId.show = shown;
	if (!shown) {
		entryOrId.pinned = false;
	}
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
function getMapEntry(key) {
	if (key.id) {
		return key;
	}
	return dbMap[key];
}
function focusOnNode(id) {
	var e = getMapEntry(id);
	var p = e.parent;
	focusOnNodeParent(p, e);
	e.pinned = true;
	renderCurrentTree();
	highlightNodes(id);
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
function highlightNodes(ids) {
	if (!Array.isArray(ids)) {
		ids = [ ids ];
	}
	var entries = $();
	for (var i = 0; i < ids.length; i++) {
		var id = ids[i];
		var entry = $("#entry-" + id);
		while (entry.length == 0) {
			id = getMapEntry(id).parentId;
			entry = $("#entry-" + id);
		}
		entries = entries.add(entry);
	}
	var nodes = entries.closest(".Node");
	var topNode = $(nodes.get(0));
	var scrollPos = topNode.offset().top - $(window).height() + topNode.height() + 10;
	$('html, body').animate({ scrollTop: scrollPos }, 200);
	nodes
		.animate({opacity: .2}, 100)
		.animate({opacity: 1}, 500)
	;
}
function hideAllNodes() {
	if (getRootEntry()) {
		markEntryChildrenAsShown(getRootEntry(), false);
	}
}
function markAllIdsAsUnpinned(e) {
	e.pinned = false;
	for (var i = 0; i < e.children.length; i++) {
		markAllIdsAsUnpinned(e.children[i]);
	}
}
function markOnlyTheseIdsAsPinned(ids) {
	markAllIdsAsUnpinned(getRootEntry());
	for (var i = 0; i < ids.length; i++) {
		getMapEntry(ids[i]).pinned = true;
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
		setEntryShownAs(entryOrId, show);
		// also show all ancestors - this is for the case (for example) where we search
		//	for something that is already loaded, but isn't shown
		if (show) {
			var e = getMapEntry(entryOrId).parent;
			while (e) {
				setEntryShownAs(e, true);
				e = e.parent;
			}
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
	
	// add each item to the map, plus any init needed
	for (i = 0; i < entries.length; i++) {
		e = entries[i];
		e.children = [];
		initEntry(e);
		dbMap[e.id] = e;
		// add the g-children to that map
		for (j = 0; j < e.childrenIds.length; j++) {
			dbChildIdsToParents[e.childrenIds[j]] = e;
		}
	}
	
	
	// link each child to its parent
	for (i = 0; i < entries.length; i++) {
		e = entries[i];
		var p = dbMap[e.parentId];
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
function prepareNodesForRender(e, isDetails) {
	buildShownNodes(e);
	cleanNodes(e);
	collapseNodesForChildrenToShow(e);
	hideLongCollapsed(e);
	collapseNodesForSiblingsToShow(e);
	prepareEntryForContextMenu(e, isDetails);
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
		if (c.childrenToShow.length == 0 && !c.pinned) {
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
 * Always reduce any list of more than 5 (with chain root) to 5
 *	1 - Root keep
	2 - First in chain - keep and mark as +
	3 - remove
	4 - remove
	5 - center piece Math.ceil(totalLen / 2) - keep and leave as -
	6 - next after center piece - keep and mark as +
	7 - remove
	8 - remove
	9 - Tail keep
	
	These are already split up such that there won't be any pinned nodes in this "smaller" chain.
 */
function hideLongCollapsed(e) {
	
	// check the last in the chain only, we don't care if the pin is before, only after
	// TODO actually, let's collapse @3 if there is even one pinned child, not just
	//		if there is only one pinned child
	var len = e.collapsed.length + 1;
	var isPinnedAfterChain = false;
	var maxLen = 6;
	if (e.collapsed.length > 0) {
		var last = e.collapsed[e.collapsed.length - 1];
		if (last.childrenToShow.length == 1 && last.childrenToShow[0].pinned) {
			isPinnedAfterChain = true;
			maxLen = 3;
		}
	}

	// the collapse strategy is different depending on the pin proximity

	if (len >= maxLen) {
		var collapsed = [];
		var mid = Math.ceil(len / 2) - 1;
		if (isPinnedAfterChain) {
			// loop over the nodes to find which one is the most interesting
			// but keep the last one no matter what
			var score = 0;
			var chosenNode;
			var chosenPos;
			for (var i = 0; i < e.collapsed.length - 1; i++) {
				var c = e.collapsed[i];
				// we will keep the most interesting one, but break ties by which is closer to center
				if (c.interestingScore > score || 
					(c.interestingScore == score && Math.abs(mid - i) < Math.abs(mid - chosenPos))) {
					score = c.interestingScore;
					chosenNode = c;
					chosenPos = i;
				}
			}
			collapsed.push(chosenNode);
			chosenNode.collapsedPinned = true;
			collapsed.push(e.collapsed[e.collapsed.length - 1]);
		} else {
			// this strategy pushes the exact mid and the two outer nodes, no other math
			for (var i = 0; i < e.collapsed.length; i++) {
				var c = e.collapsed[i];
				if (i == 0 || i == mid + 1) {
					collapsed.push(c);
					c.collapsedPinned = true;
				} else if (i == mid || i == e.collapsed.length - 1) {
					collapsed.push(c);
				}
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
	if (e.childrenToShow.length != 1 || e.pinned) {
		// don't do anything for this node, but recurse
		for (var i = 0; i < e.childrenToShow.length; i++) {
			collapseNodesForChildrenToShow(e.childrenToShow[i]);
		}
	} else {
		var r = e;
		while (r.childrenToShow.length == 1) {
			var c = r.childrenToShow[0];
			r = c;
			if (c.pinned) {
				break;
			}
			e.collapsed.push(c);
			c.collapsedPinned = false;
		}
		collapseNodesForChildrenToShow(r);
	}
}
function getChildrenIdsShownCountingSiblings(e) {
	var childrenIds = [];
	for (var i = 0; i < e.childrenToShow.length; i++) {
		var c = e.childrenToShow[i];
		childrenIds.push(c.id);
		if (c.siblings) {
			for (var j = 0; j < c.siblings.length; j++) {
				childrenIds.push(c.siblings[j].id);
			}
		}
	}
	return childrenIds;
}
function prepareEntryForContextMenu(e, isDetails) {
	var childrenShownIds = getChildrenIdsShownCountingSiblings(e);
	var hiddenCount = e.childrenIds.length - childrenShownIds.length;
	if (hiddenCount == 0) {
		e.mShowChildren = false;
		e.mShowChildrenCaption = false;
	} else {
		e.mShowChildren = true;
		e.mShowChildrenCaption = getShowCaption(e.childrenToShow.length, hiddenCount, "Child", "Children");
	}

	e.mHide = (e.childrenToShow.length > 0) && !isDetails;
	e.mClose = !isDetails;
	e.mBack = isDetails;
	
	var visibleShowMoreIds = getVisibleIds(e.showMoreLeafIds);
	var showMoreVisible = visibleShowMoreIds.length;
	var showMoreHidden = e.showMoreLeafIds.length - showMoreVisible;
	var isSame = isShowMoreAndShowChildrenSame(childrenShownIds, e.childrenIds, visibleShowMoreIds, e.showMoreLeafIds);
	if (showMoreHidden == 0 || isSame) {
		e.mShowMore = false;
		e.mShowMoreCaption = false;
	} else {
		e.mShowMore = true;
		e.mShowMoreCaption = getShowCaption(showMoreVisible, showMoreHidden, "Species", "Species");
	}

	// focus doesn't show any additional nodes, it just hides everything not up or downstream of this node
	e.mFocus = isFocusNeeded(e);
	
	// recurse
	var i;
	for (i = 0; i < e.childrenToShow.length; i++) {
		prepareEntryForContextMenu(e.childrenToShow[i]);
	}
	for (i = 0; i < e.siblings.length; i++) {
		prepareEntryForContextMenu(e.siblings[i]);
	}
	
	var pinnable = (e.img);
	e.mPin = (!e.pinned && pinnable);
	e.mUnpin = (e.pinned && pinnable);
}
function getArrayOfFirstMinusSecond(first, second) {
	var a = [];
	for (var i = 0; i < first.length; i++) {
		if (second.indexOf(first[i]) < 0) {
			a.push(first[i]);
		}
	}
	return a;
}
function isShowMoreAndShowChildrenSame(childrenShownIds, childrenIds, visibleShowMoreIds, showMoreLeafIds) {
	var childrenToShow = getArrayOfFirstMinusSecond(childrenIds, childrenShownIds);
	var moreToShow = getArrayOfFirstMinusSecond(showMoreLeafIds, visibleShowMoreIds);
	if (childrenToShow.length != moreToShow.length) {
		return false;
	}
	childrenToShow.sort(sortIntCompare);
	moreToShow.sort(sortIntCompare);
	for (var i = 0; i < childrenToShow.length; i++) {
		if (childrenToShow[i] != moreToShow[i]) {
			return false;
		}
	}
	return true;
}
// are there any non-descendant leafs
function isFocusNeeded(e) {
	var p = e.parent;
	while (p) {
		if (p.childrenToShow && p.childrenToShow.length > 1) {
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
function getVisibleIds(ids) {
	var visibleIds = [];
	for (var i = 0; i < ids.length; i++) {
		if (isEntryShown(ids[i])) {
			visibleIds.push(ids[i]);
		}
	}
	return visibleIds;
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
	e.mDetail = true;
	e.childrenIds = getInitIds(e.childrenIds);
	e.showMoreLeafIds = getInitIds(e.showMoreLeafIds);
	e.showMoreOtherIds = getInitIds(e.showMoreOtherIds);
	e.mClose = true;
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
	e.interestingScore = getInterestingScore(e);
}
function getInterestingScore(e) {
	var score = 0;
	if (e.cnames) {
		score += 10;
		var paren = e.cname.indexOf('(');
		if (paren < 0) {
			score += 4;
		}
		// this is an extremely simplified version of "is common name boring"
		if (e.cname.toLowerCase().indexOf(e.lname.toLowerCase()) < 0) {
			score += 5;
		}
	}
	if (e.img) {
		score += 20;
	}
	return score;
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
		return;
	}
	var p = e.parent;
	if (!p) {
		e.partitionPath = "0";
	} else {
		if (!p.partitionPath) {
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
		if (e.cname.indexOf("...") >= 0) {
			return getCaptionNameWithIndicator(e.cname);
		} else if (e.cnames && e.cnames.length > 1) {
			return e.cname + "...";
		} else {
			var pos = e.cname.indexOf("(");
			if (pos > 0) {
				return e.cname.substring(0, pos) + "...";
			} else {
				return e.cname;
			}
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
function getAllPinnedNodeIds() {
	var ids = [];
	getPinnedNodeIds(getRootEntry(), ids);
	ids.sort(sortIntCompare);
	return ids;
}
function sortIntCompare(a, b) {
	return a - b;
}
function removeSecondFromFirst(array1, array2) {
	return array1.filter(function(x) { 
  		return array2.indexOf(x) < 0;
	});
}
function getVisibleNodeIds(e, ids) {
	if (isEntryShown(e.id)) {
		ids.push(e.id);
		for (var i = 0; i < e.children.length; i++) {
			getVisibleNodeIds(e.children[i], ids);
		}
	}
}
function getPinnedNodeIds(e, ids) {
	if (isEntryShown(e.id)) {
		if (e.pinned) {
			ids.push(e.id);
		}
		for (var i = 0; i < e.children.length; i++) {
			getPinnedNodeIds(e.children[i], ids);
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
	var rootId = getRootEntry().id;
	markEntryAsShown(rootId, true);
	renderTree(rootId, true);
	if (!skipUrl) {
		setUrlToAllVisibleIds();
	}
	hideContextMenu();
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
	renderTreeAndRows($("#treeTab"), e);
	initTreeEvents();
	showTreeTab();
}
function getPinnedNodeFromCollapsed(e) {
	for (var i = 0; i < e.collapsed.length; i++) {
		if (e.collapsed[i].pinned) {
			return e.collapsed[i];
		}
	}
	return e;
}
function initExamplesStructure(data, structure, groupNumber) {
	var e = structure;
	if (!e.children) {
		e.children = [];
	}
	e.childrenToShow = e.children;
	e.siblings = [];
	e.collapsed = [];
	e.example = true;
	e.id = -1;
	if (e.type == "example") {
		if (e.root) {
			e.pWidth = 187;
			e.pHeight = 141;
			e.caption = structure.caption;
			e.image = structure.image;
		} else {
			var example = data.groups[groupNumber].examples[e.number];
			e.caption = example.caption;
			e.file = example.file;
			e.pWidth = example.width;
			e.pHeight = example.height;
			e.image = example.image;
		}
	} else if (e.type == "question") {
		groupNumber = e.number;
		e.caption = data.groups[groupNumber].caption;
	}
	for (var i = 0; i < e.children.length; i++) {
		initExamplesStructure(data, e.children[i], groupNumber);
	}
}
function renderExamplesTab(data, structure) {
	// walk over the structure and set all helper properties
	initExamplesStructure(data, structure);
	renderTreeAndRows($("#examplesTab"), structure);
	$(".exampleQueryLink").click(queryLinkEvent);
}
function renderTreeAndRows(h, e) {
	var table = $("<table class='NodeTable' id='tree-" + e.id + "'></table>").appendTo(h);
	var tr = $("<tr></tr>").appendTo(table);
	var children = e.childrenToShow;
	if (e.collapsed.length > 0) {
		children = e.collapsed[e.collapsed.length - 1].childrenToShow;
	}
	// we won't display the root, etc, if there is one pinned
	e = getPinnedNodeFromCollapsed(e);
	// this td is for the root element's info
	var td = $("<td rowspan='" + (children.length * 2) + "'></td>").appendTo(tr);
	var showLine = (children.length > 1);
	
	renderEntryLinesElement(td, e, showLine);

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
		renderTreeAndRows(childTd, children[index]);
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
function renderEntryLinesElement(h, e, showLine) {
	var table = $("<table></table>");
	var tr = $("<tr></tr>").appendTo(table);
	var td = $("<td class='n' rowspan='2'></td>").appendTo(tr);
	
	var nodeClass = "Node";
	if (e.pinned) {
		nodeClass += " PinnedNode";
	}
	
	var div = $("<div id='node-" + e.id + "' class='" + nodeClass + "'></div>").appendTo(td); 
	
	renderNodeEntryLine(div, e, 0);
	if (!e.pinned) {
		for (var i = 0; i < e.siblings.length; i++) {
			div.append($("<br/>"));
			renderNodeEntryLine(div, e.siblings[i], 0);
		}
	
		// this will always be empty if we are rendering children
		// renderFlatTaxonNode(div, e, e.collapsed);
		//* JUST FOR FUN
		for (var i = 0; i < e.collapsed.length; i++) {
			div.append($("<br/>"));
			renderNodeEntryLine(div, e.collapsed[i], i + 1);
		}
		//*/
	}
	
	var blankTr = $("<tr></tr>").appendTo(table);
	if (showLine) {
		$("<td class='b'>&nbsp;</td>").appendTo(tr);
		$("<td>&nbsp;</td>").appendTo(blankTr);
	}
	table.appendTo(h);
}

function renderExampleNode(h, e) {
	if (e.type == "example") {
		var pinnedScaling = .50;
		var height = pinnedScaling * e.pHeight;
		var width = pinnedScaling * e.pWidth;
		var pimg = '<img alt="' + e.alt + '" height="' + height + '" width="' + width + '" src="' + 
			getImagesPath("preview") + '/' + e.image + '" class="PinnedImage" />';
		h.append(pimg);
		
		var exampleLink;
		if (!e.root) {
			exampleLink = $('<a href="?q=e:' + e.file + '" class="exampleQueryLink"></a>').appendTo(h);
		} else {
			exampleLink = $('<div></div>').appendTo(h);
		}
		var captionLines = e.caption.split("/");
		for (var k = 0; k < captionLines.length; k++) {
			var cls = "ExampleInnerCaption";
			if (k == 0) {
				cls += " ExampleInnerCaptionFirstLine";
			}
			var span = $("<div class='" + cls + "'>" + captionLines[k] + "</div>").appendTo(exampleLink);
			if (k == 0 && !e.root) {
				span.append('<img class="ExampleGotoIcon" src="icons/show-interesting.png" />');
			}
		}
		
	} else if (e.type == "question") {
		var titleDiv = $('<div class="ExampleTitle"></div>').appendTo(h);
		titleDiv.append('<img alt="question" class="question" src="icons/question.png" />');
		titleDiv.append(e.caption);
	}
}
function renderNodeEntryLine(h, e, depth) {
	if (e.example) {
		renderExampleNode(h, e);
		return;
	}
	var spanClass = "EntryLine";
	if (e.pinned) {
		spanClass += " PinnedImageEntryLine";
		if (e.img) {
			var pinnedScaling = .75;
			var height = pinnedScaling * e.pHeight;
			var width = pinnedScaling * e.pWidth;
			var pimg = '<img alt="' + e.alt + '" height="' + height + '" width="' + width + '" src="' + 
				getImagesPath("preview") + '/' + e.img + '" class="PinnedImage" />';
			h.append(pimg);
			h.append("<br/>");
		}
	} else if (depth == 0) {
		spanClass += " EntryLineTop";
	}
	var span = $('<span class="' + spanClass + '"></span>').appendTo(h);
	// detail button
	var detailIcon = "detail.png";
	var isExpandIcon = false;
	var detailClass = "tree-detail_first";
	if (e.collapsedPinned) {
		detailIcon = "open_children.png";
		detailClass = "tree-open_children";
		isExpandIcon = true;
	} else if (depth > 0) {
		detailIcon = "detail.png";
		detailClass = "tree-detail_indented";
	}
	if (!e.parentId) {
		detailIcon = "banyan-icon.png";
		detailClass = "tree-tree_root";
	}
	// the green button on the left of the line
	var pad = getNbsps(depth);
	var detailsHash = getEntryDetailsHash(e);
	if (!e.pinned) {
		var iconLink;
		var iconTitle;
		if (isExpandIcon) {
			// http://localhost:8080/banyan-js/mShowChildren#!/37805
			iconTitle = "Expand / Show Children";
			iconLink = 'mShowChildren#!/' + e.id;
		} else {
			iconTitle = "Go to Details";
			iconLink = '?q=' + detailsHash;
		}
		span.append(pad + '<a title="' + iconTitle + '" href="' + iconLink + '" class="treeQueryLink"' +
			'><img src="' + iconPath() + '/' + detailIcon + '" class="' +
			detailClass + '" alt="search.detail" /></a>');
		if (isExpandIcon) {
			span.bind("click", function() {
				performMenuAction('mShowChildren', e.id);
				return false;
			});
		}
	}
	// image and link
	var name = getEntryDisplayName(e);
	var img;
	var linkTitle;
	var imgClass;
	if (e.img) {
		if (!e.pinned) {
			var imgSrc = getImageTinySrcPath(e);
			img = '<img alt="' + e.alt + '" height="' + e.tHeight + '" width="' + e.tWidth + '" src="' + 
				imgSrc + '" class="Thumb" />';
		} else {
			img = "";
		}
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
	span.append('<a class="' + imgClass + ' treeQueryLink"' + linkTitle + ' id="entry-' + e.id + '" href="?q=' + detailsHash + '">' 
			 + name + img + '</a>');
	
	addMenuButtonToEntryLine(e, span);
}
function addMenuButtonToEntryLine(e, span) {
	// menu button style/image
	var canShowMore = (e.mShowChildren || e.mShowMore);
	var menuMore = "menu_more.png";
	if (!canShowMore) {
		menuMore = "menu_less.png";
	}
	var menuButtonLink = $('<a href="?temp" id="' + e.id + '" class="menuopener">'
			+ '<img src="' + iconPath() + '/' + menuMore + '" alt="menu button"></a>');
	setMenuButtonLink(e.id, menuButtonLink);
	span.append(menuButtonLink);
}
function setMenuButtonLink(id, jqueryElement) {
	var href = window.location.href;
	href = getHashFromUrl(href);
	href = "m:" + id + ":" + href;
	jqueryElement.attr("href", "?q=" + href);
}
function getNbsps(count) {
	var pad = "";
	for (var i = 0; i < count; i++) {
		pad = pad + "&nbsp";
	}
	return pad;
}
function getImagesPath(key) {
	if (isLocalhost()) { // images too hard to track locally
		return "/banyan-images/" + key;
	} else {
		return S3_BASE_URL + "/banyan-images/" + key;
	}
}
function iconPath() {
	return "icons";
}
function getRenderDetailsTaxoDisplayName(e) {
	var name = "<i>(" + e.lname + ")</i>";
	if (e.cname) {
		name = e.cname + " " + name;
	}
	return name;
}
function renderDetails(id) {
	var e = getMapEntry(id);

	var displayName = getEntrySimpleDisplayName(e);
	$("#DetailTitle").html(displayName);
	if (e.cname) {
		$("#DetailLatinTitle").html(e.lname);
		$("#DetailLatinTitle").show();
	} else {
		$("#DetailLatinTitle").hide();
	}
	$('.DetailTitleName').html(displayName);
	
	var div = $(".DetailImageHolder");
	if (e.img) {
		var img = $("#DetailImage");
		img.attr("alt", "");
		img.attr("height", e.dHeight);
		img.attr("width", e.dWidth);
		img.attr("src", getImagesPath("detail") + "/" + e.img);
		div.show();
	} else {
		div.hide();
	}

	var taxoEntry = $("#TaxonomyCell");
	taxoEntry.empty();

	renderFlatTaxonNode(taxoEntry, e);
	
	// if there are no children, we should hide that area
	var subTableNode = $("#SubSpeciesTableNode");
	if (!e.children || e.children.length == 0) {
		subTableNode.hide();
		$('#SubSpeciesLine').hide();
	} else {
		var subRank = getRankOfChildren(e);
		$('.DetailSubRank').html(subRank);
		
		$('#SubSpeciesLine').show();
		subTableNode.show();
		var subTable = $("#SubSpeciesTable");
		// divide them up into columns
		subTable.empty();
		for (var i = 0; i < e.children.length; i++) {
			var tr = $("<tr></tr>").appendTo(subTable);
			var td = $("<td class='DetailsRow'></td>").appendTo(tr);
			renderDetailsEntryPreviewPart(td, e.children[i], "child");
		}
	}
	
	// double-check the treeLinkDetails, it might not be populated
	var treeLinkDetails = $("#treeLinkDetails");
	var treeLinkHref = treeLinkDetails.attr("href");
	if (treeLinkHref == "#NONE") {
		var linkIds = [];
		var p = e;
		while (p) {
			linkIds.push(p.id);
			p = p.parent;
		}
		linkIds = linkIds.concat(e.childrenIds);
		linkIds.sort(sortIntCompare);
		setUrlsToIdsArray(linkIds, false, false, true);
	}
	
	initTreeEvents();
	
}
function renderFlatTaxonNode(cell, e, collapsed) {
	
	var toShow = [];
	var allEntries = [];
	var p = e;
	while (p) {
		allEntries.push(p);
		if (collapsed == null) {
			toShow.push(p);
		}
		p = p.parent;
	}
	
	if (collapsed != null) {
		for (var i = collapsed.length - 1; i >= 0; i--) {
			toShow.push(collapsed[i]);
		}
	}

	// prepare all the menu buttons
	var children = e.children; // we show all whether they were visible or not
	if (children) {
		for (var i = 0; i < children.length; i++) {
			allEntries.push(children[i]);
		}
	}
	for (var i = 0; i < allEntries.length; i++) {
		prepareNodesForRender(allEntries[i], true);
	}
	
	var table = $("<table></table>").appendTo(cell);
	for (var i = toShow.length - 1; i >= 0; i--) {
		var a = toShow[i];
		var tr = $("<tr class='DetailsRow'><td class='Rank'><span>" + a.rank + "</span></td></tr>").appendTo(table);
		var td = $("<td></td>").appendTo(tr);
		renderDetailsEntryPreviewPart(td, a, "taxo");
	}

}
function getRankOfChildren(e) {
	if (e.children && e.children.length > 0) {
		return e.children[0].rank;
	} else {
		// doesn't matter
		return null;
	}
}
function renderDetailsEntryPreviewPart(td, e, idPrefix) {
	var href = getEntryDetailsHash(e);
	$("<a href='?q=" + href 
		+ "' class='treeQueryLink'><img src='icons/detail.png' class='detail-button'></a>").appendTo(td);
	var taxoName = getRenderDetailsTaxoDisplayName(e);
	var previewClass = "preview";
	var linkTitle;
	if (!e.img) {
		previewClass = "no-preview";
		var titleName;
		if (e.cname) {
			titleName = e.cname;
		} else {
			titleName = e.lname;
		}
		linkTitle = ' title="' + titleName + '"';
	} else {
		linkTitle = "";
	}
	var previewA = $("<a id='" + idPrefix + "-" + e.id + "'"
			+ linkTitle + " class='" + previewClass + " treeQueryLink' href='?q=" + href + "'>" + taxoName + "</a>").appendTo(td);
	if (e.img) {
		$("<img height='" + e.tHeight + "' width='" + e.tWidth 
			+ "' class='Thumb' src='" + getImageTinySrcPath(e) + "'></img>").appendTo(previewA);
	}
	
	addMenuButtonToEntryLine(e, td);

}

// ------ JSON Functions
function __JsonFunctions() {}
function getJsonUrl(relativePath) {
	var baseUrl = "";
	if (!isLocalhost()) {
		baseUrl = S3_BASE_URL + "/banyan-website/";
	} else {
		baseUrl = "/banyan-json/";
	}
	return (baseUrl + relativePath);
}
function isLocalhost() {
	return window.location.href.startsWith("http://localhost");
}
function loadCommandFromQuery(url) {
	var hash = getHashFromUrl(url);
	setUrlInner(hash);
}
function getHashFromUrl(url) {
	var index = url.indexOf("#");
	var hashValue = "";
	if (index >= 0) {
		hashValue = url.substring(index + 1);
		if (hashValue.charAt(0) == '!') {
			hashValue = hashValue.substring(2);
		}
	} else {
		index = url.indexOf("?q=");
		if (index >= 0) {
			hashValue = url.substring(index + 3);
		} else {
			hashValue = "";
		}
	}
	return hashValue;
}
function loadCommandFromURL() {
	var hashValue = getHashFromUrl(window.location.href);

	areMenusAllowed = false;
	hidePreviewPanel();
	hideContextMenu();

	if (hashValue.length == 0) {
		// we will never just load nothing
		hashValue = defaultTree;
	}
	
	// handle the "menu button" scenario first, as it simply wraps the other behavior
	var isMenuButton = hashValue.startsWith("m:");
	var menuButtonId = false;
	if (isMenuButton) {
		hashValue = hashValue.substring(2);
		var colonIndex = hashValue.indexOf(":");
		menuButtonId = hashValue.substring(0, colonIndex);
		hashValue = hashValue.substring(colonIndex + 1);
	}
	
	// split the command if needed
	var colon = hashValue.charAt(1);
	var command;
	var value;
	var commandParam = false;
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
	
	if (command == "e") {
		loadExampleFile(hashValue);
	} else if (command == "r") {
		loadRandomFile(hashValue);
	} else if (command == "t") {
		if (value == "random") {
			loadRandomFile(commandParam);
		} else if (value == "startOver") {
			loadExampleFile(defaultTree);
		} else if (value == "blankTree") {
			loadBlankTree();
		} else if (value == "details") {
			loadDetails(commandParam);
		} else if (value == "examplesTab") {
			loadExamplesTab();
		} else {
			// other tab-commands can respond to simple tab change
			showTab(value);
		}
	} else if (command == "i") {
		var ids = value.split(",");
		loadAndShowNewIds(ids, false, false);
	} else if (command == "c") {
		var pinnedIds = false;
		if (commandParam) {
			var pinType = commandParam.charAt(0);
			if (pinType == "p") {
				pinnedIds = uncrunch(commandParam.substring(2));
			} else {
				pinnedIds = commandParam.substring(2).split(",");
			}
		}
		var ids = uncrunch(value);
		loadAndShowNewIds(ids, pinnedIds, menuButtonId);
	}
}

/* ------------------- Search Methods */
function loadRootSearchIndex() {
	loadRemoteIndexEntry("@root");
}
function submitSearchQuery(_event, ui) {
	// (ui.item.value + " // " + ui.item.label + " // " + ui.item.extra);
	if (ui.item.notFound) {
		return;
	}
	var id = ui.item.id;
	var ids = uncrunch(ui.item.ids);
	// make sure we're on the tree page, (not the detail page)
	if (isTabShown("detailsTab")) {
		showTreeTabFromDetailsTab();
	}
	loadJsonThenMarkNewIdsVisible(ids, function() {
		if (getMapEntry(id).img) {
			pinNode(id, true);
		}
	});
}
function isTabShown(name) {
	return $("#" + name).is(":visible");
}
// needed because on details, all nodes get marked as hidden
function showTreeTabFromDetailsTab() {
	var url = $("#treeLink").attr("href");
	loadCommandFromQuery(url);
}
function findSearchSuggestions(originalQuery, callback) {
	var term = cleanIndexKey(originalQuery);
	doFindSearchSuggestions(term, originalQuery, 1, callback);
}
function doFindSearchSuggestions(term, originalQuery, subTermLength, callback) {
	// if the root term is loaded, we are done
	var suggestions = dbLocalSearchIndex[term];
	if (suggestions) {
		// TODO filter out any that don't actually match the term - the term could be longer than the max key length
		callback(suggestions);
		return true;
	}
	
	var subTerm = term.substring(0, subTermLength);
	//console.log("doFindSearchSuggestions >>> " + term + "." + subTerm);
	if (dbRemoteSearchIndex.has(subTerm)) {
		// load that remote, and callback to this method again
		var innerCallback = function() {
			doFindSearchSuggestions(term, originalQuery, subTermLength + 1, callback);
		};
		loadRemoteIndexEntry(subTerm, innerCallback);
	} else if (subTermLength < term.length) {
		doFindSearchSuggestions(term, originalQuery, subTermLength + 1, callback);
	} else {
		// we can't find it
		//console.log("doFindSearchSuggestions.notFound." + term + "." + subTerm);
		var suggestion = {
			notFound: true,
			common: "No results found for - " + originalQuery,
			value: originalQuery
		};
		callback([suggestion]);
	}
}
function loadRemoteIndexEntry(key, callback) {
	var url = buildRemoteIndexUrl(key);
	url = getJsonUrl(url);
	$.getJSON(url, function (data) {
		buildRemoteIndexEntry(data, callback);
	});
}
function buildRemoteIndexUrl(key) {
	// apple -> ap/app/apple.json
	// app   -> ap/app.json
	var path = "";
	var fileName;
	if (key.length == 0 || key == "@root") {
		fileName = "/@root.json";
	} else {
		// from ABCDEF to A/AB/ABC/ABCDE/ABCDEF
		var pos = 1;
		while (pos <= maxSearchKeyLength && pos < key.length) {
			var left = key.substring(0, pos);
			path += ("/" + left);
			pos++;
		}
		fileName = "/" + key + ".json";
	}
	var url = "json/s" + path + fileName;
	return url;
}
function buildRemoteIndexEntry(data, callback) {
	if (data.remote) {
		data.remote.forEach( remoteKey => dbRemoteSearchIndex.add(remoteKey));
	}
	if (data.local) {
		var map = new Map(Object.entries(data.local));
		map.forEach((ids, localKey) => {
			// only add locals if the array has values
			if (ids && ids.length > 0) {
				var entries = [];
				ids.forEach(id => {
					var entry = data.entries[id];
					entry.id = id;
					entries.push(entry);
				});
				// handle the "crassulaaq|uatica" pattern
				var pos = localKey.indexOf("|");
				if (pos > 0) {
					var left = localKey.substring(0, pos);
					var right = localKey.substring(pos + 1);
					for (var i = 0 ; i <= right.length ; i++) {
						var key = left + right.substring(0, i);
						dbLocalSearchIndex[key] = entries;
					}
				} else {
					dbLocalSearchIndex[localKey] = entries;
				}
			} 
		});
	}
	if (callback) {
		callback(data);
	}
}
function createSuggestionHtml(entry) {
  var imgSrc = entry.image
    ? ("data:image;base64," + entry.image)
    : "icons/search-placeholder.svg";
  var html = "<div class='autocomplete-suggestion-row'>";
  html += "<img class='autocomplete-suggestion-img' src='" + imgSrc + "' alt='' />";
  html += "<span class='autocomplete-suggestion-text'>";
  if (entry.common && entry.common.length > 0) {
    html += "<span class='searchCommon'>" + entry.common + "</span>";
  }
  if (entry.latin) {
    html += "<span class='searchLatin'>(" + entry.latin + ")</span>";
  }
  html += "</span></div>";
  return html;
}
function getPreviewImageCaption(img) {
	var e = getImageEntry(img);
	var latinNameCaption = "<span id='previewLatin'>(" + e.lname + ")</span>";
	var names = e.cnames || [];
	var commonNamesCaption = "";
	for (var i = 0; i < names.length; i++) {
		var name = getCaptionNameWithIndicator(names[i]);
		commonNamesCaption = commonNamesCaption + "<b>" + name + "</b><br/>";
	}
	return "<div id='previewCaptions'>" + 
		commonNamesCaption + latinNameCaption + "</span><br/>";
}

function cleanIndexKey(key) {
	key = key.substring(0, maxSearchKeyLength);
	key = key.toLowerCase();
	return key.replace(/[^a-z@]/g, "");
}

/* ---------------------- */
function loadBlankTree() {
	hideChildren(getRootEntry().id, true, true);
}
function loadExamplesTab() {
	if (!examplesIndexLoaded) {
		var url = getJsonUrl("json/e/examples-index.json");
		var callback = build_loadExamplesTab_callback1();
		$.getJSON(url, callback);
	} else {
		showTab("examplesTab");
	}		
}
function build_loadExamplesTab_callback1() {
	return function(data) {
		var url = getJsonUrl("json/e/examples-structure.json");
		var callback = build_loadExamplesTab_callback2(data);
		$.getJSON(url, callback);
	}
}
function build_loadExamplesTab_callback2(data) {
	return function(structure) {
		renderExamplesTab(data, structure);
		examplesIndexLoaded = true;
		loadExamplesTab();
	}
}
// should be from "t:details:id,crunchedId"
function loadDetails(params) {

	// need to hide the current tree so we can rebuild it under certain conditions
	hideAllNodes();

	var comma = params.indexOf(",");
	var id = params.substring(0, comma);
	var otherIds = params.substring(comma + 1);
	otherIds = uncrunch(otherIds);
	loadJsonOnly(otherIds, build_loadDetails_callback(id));
}
function build_loadDetails_callback(id) {
	return function() {
		renderDetails(id);
		showTab("detailsTab");
	};
}
function showTreeTab() {
	showTab("treeTab");
}
function showTab(id) {
	$(".tabCommand").hide();
	$("#" + id).show();
}
function getRandomKeyFromFileName(name) {
	// convert "name-name2-12321" to "name-name2"
	var pos = name.lastIndexOf("-");
	return name.substring(0, pos);
}
function loadRandomFile(command) {
	if (!dbRandomFileKeys) {
		loadRandomFileIndexFromJson(command);
	} else {
		setRandomLinkIndex();
		var next = getRandomFileFromCommand(command);
		loadExampleFile(next);
	}
}
function getRandomFileFromCommand(command) {
	var index = parseInt(command);
	var key;
	if (isNaN(index)) {
		key = command.substring(2);
		index = key.lastIndexOf("-");
		if (index > 0) {
			// could be "r:word-word-123"
			var test = key.substring(index + 1);
			test = parseInt(test);
			if (!isNaN(test)) {
				key = key.substring(0, index);
			}
		}
	} else {
		var key = dbRandomFileKeys[index];
	}
	var file = dbRandomFileKeysToFileNames[key];
	return "r:" + file;
}
function setRandomLinkIndex() {
	var link = $("#RandomLink");
	// choose a random number, because that way pressing back, etc will keep that number random
	var index = Math.floor(Math.random() * dbRandomFileKeys.length);
	link.attr("href", "?q=r:" + dbRandomFileKeys[index]);
}
function loadRandomFileIndexFromJson(command) {
	var url = getJsonUrl("json/r/random-index.json");
	$.getJSON(url, function(data) {
		dbRandomFileKeys = [];
		dbRandomFileKeysToFileNames = {};
		for (var i = 0; i < data.files.length; i++) {
			var e = data.files[i];
			var key = getRandomKeyFromFileName(e);
			dbRandomFileKeys.push(key);
			dbRandomFileKeysToFileNames[key] = e;
		}
		shuffleArray(dbRandomFileKeys);
		loadRandomFile(command);
	});
}
function shuffleArray(array) {
    for (var i = array.length - 1; i > 0; i--) {
        var j = Math.floor(Math.random() * (i + 1));
        var temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
function loadExampleFile(fileName) {
	hideAllNodes();
	setTreeLinksForFile(fileName);
	
	var fileEntry = dbFileIds[fileName];
	if (fileEntry) {
		// no need to load file
		// set visible
		markEntriesAsShown(fileEntry.ids, true);
		// set pinned
		markOnlyTheseIdsAsPinned(fileEntry.pinnedIds);
		// render current tree
		renderCurrentTree(true);
	} else {
		loadJsonThenAddEntries([fileName], false, false, build_loadExampleFile_callback(fileName));
	}
}
function build_loadExampleFile_callback(fileName) {
	return function() {
		loadExampleFile(fileName);
	};
}
function loadAllChildren(id) {
	var allChildrenIds = getMapEntry(id).childrenIds;
	var visible = [];
	getVisibleNodeIds(getMapEntry(id), visible);
	var newChildrenIds = removeSecondFromFirst(allChildrenIds, visible);
	loadJsonThenMarkNewIdsVisible(newChildrenIds, function() {highlightNodes(newChildrenIds)});
}
function loadAllShowMore(id) {
	var e = getMapEntry(id);
	// build the full list
	var allShowMoreIds = e.showMoreLeafIds.concat(e.showMoreOtherIds);
	var visibleIds = [];
	getVisibleNodeIds(e, visibleIds);
	var newIds = removeSecondFromFirst(allShowMoreIds, visibleIds);
	loadJsonThenMarkNewIdsVisible(newIds, function() {highlightNodes(newIds)});
}

function loadAndShowNewIds(fileNamesOrIds, pinnedIds, menuButtonId) {
	var currentVisibleIds = getAllVisibleNodeIds();
	var same = areArraysSame(currentVisibleIds, fileNamesOrIds);
	if (same && pinnedIds) {
		var currentPinnedIds = getAllPinnedNodeIds();
		same = areArraysSame(pinnedIds, currentPinnedIds);
	}
	
	if (!same) {
		hideAllNodes();
		var callback = false;
		if (menuButtonId) {
			callback = function() {
				renderCrawlCommand(menuButtonId);
			};
		}
		
		loadJsonThenAddEntries(fileNamesOrIds, pinnedIds, true, callback);
	} else {
		showTreeTab();
	}
}
function areArraysSame(a1, a2) {
	var same = (a1.length == a2.length);
	if (same) {
		var foundCount = 0;
		for (var i = 0; i < a1.length; i++) {
			var found = (a2.indexOf(a1[i]) >= 0);
			if (found) {
				foundCount++;
			}
		}
		same = (foundCount == a1.length);
	}
	return same;
}
// these are the master "load ids" methods, and should be altered to accomodate the one or two scenarios we have
// - load these nodes/files exactly, and then mark exactly those nodes visible in addition to current tree, then render tree
// - load a brand new tree (? maybe already handled by calling method)
// - load these nodes, but don't do anything else (? not sure that's a valid scenario)
function loadJsonOnly(fileNamesOrIds, callback) {
	loadJsonThenAddEntries(fileNamesOrIds, false, false, callback);
}
function loadJsonThenMarkNewIdsVisible(fileNamesOrIds, callback) {
	loadJsonThenAddEntries(fileNamesOrIds, false, true, callback);
}
function loadJsonThenAddEntries(fileNamesOrIds, pinnedIds, showTree, outerCallback) {
	var callback = build_loadJsonThenAddEntries_callback(fileNamesOrIds, pinnedIds, showTree, outerCallback);
	loadJson(fileNamesOrIds, callback);
}
function build_loadJsonThenAddEntries_callback(newIds, pinnedIds, showTree, callback) {
	return function(entries) {
		if (showTree) {
			if (newIds.length > 0 && isFileName(newIds[0])) {
				// then we need to mark all new entries shown instead
				markEntriesAsShown(entries, true);
			} else {
				markEntriesAsShown(newIds, true);
			}
			if (pinnedIds) {
				markOnlyTheseIdsAsPinned(pinnedIds);
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
		var thisCallback = build_loadJsonInner_callbackChain(idsToProcess[j], parentCallback);
		currentCallback = thisCallback;
	}
	// call the last function, it will cascade up
	currentCallback(entries);
}
function isFileName(name) {
	name = "" + name;
	if (name.length < 3) {
		return false;
	}
	var code = name.charAt(0);
	var fileTypes = "er";
	return (fileTypes.indexOf(code) >= 0 && name.charAt(1) == ":");
}
function buildLoadJsonNextEntriesCallback(idsWithoutParents, callback, outerEntries) {
	return function() {
		loadJson(idsWithoutParents, callback, outerEntries);
	};
}
// this is a callback in the sense that it is part of a callback chain, 
// even though this method itself will call json and need a callback
function build_loadJsonInner_callbackChain(id, parentCallback) {
	return function(entries) {
		return loadOneJsonDocument(id, entries, parentCallback);
	};
}
function loadOneJsonDocument(jsonId, entries, callback) {
	jsonId = (jsonId + "");
	var url = getJsonUrl("json/");
	var loadNeeded = true;
	if (isFileName(jsonId)) {
		url = url + jsonId.charAt(0) + "/" + jsonId.substring(2) + ".json";
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
		dbFileIds[fileName] = {
			ids: [],
			pinnedIds: []
		};
		var fileEntry = dbFileIds[fileName];
		for (var i = 0; i < entries.length; i++) {
			fileEntry.ids.push(entries[i].id);
			if (entries[i].pinned) {
				fileEntry.pinnedIds.push(entries[i].id);
			}
		}
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
	var url = getJsonUrl("json/p/index.json");
	return $.getJSON(url, 
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
