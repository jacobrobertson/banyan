//--- Taking the original JS and trying to integrate with banyan-js

// jQuery.noConflict(); // TODO shouldn't need this for banyan-js
/* TODO we can put these back later
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

this.getLeft = function(e, img) {
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
};
this.getPreviewTop = function(e, img) {
	var imageHeight = getImageHeight(img);
	// to account for two rows of text
	var textHeight = 75; 
	return getTop(e, imageHeight, textHeight);
};
this.getTop = function(e, popupHeight, bottomFudge) {
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
};
this.getHref = function(img) {
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
};
this.getImageWidth = function(img) {
	return this.getImageAttribute(img).width;
};
this.getImageHeight = function(img) {
	return this.getImageAttribute(img).height;
};
this.getImageCaption = function(img) {
	var e = this.getImageAttribute(img);
	var caption = "<span class='PopupLatin'>(" + e.lname + ")</span>";
	if (e.cname) {
		// TODO fix when we get multiple cnames
		caption = "<b>" + e.cname + "</b><br/>" + caption;
	}
	return caption;
};
this.getImageAttribute = function(img) {
	var id = img.name;
	/* ???
	if (!src) {
		return 0;
	}
	*/
	return getMapEntry(id);
};
this.imagePreview = function() {
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
};
var maxWidthHide = 106;
var maxWidthClose = 58;
var maxWidthFocus = 60;
var maxWidthDetail = 102;
var maxWidthShowChildren = 163;
var maxWidthShowMore = 159;
var digitWidth = 9;

this.getVariableWidth = function(text, minLength, baseWidth, maxWidth) {
	var len = text.length;
	var extraDigits = len - minLength;
	var thisWidth = baseWidth + extraDigits * digitWidth;
	return Math.max(maxWidth, thisWidth);
};

this.showMenu = function(e, img) {
	var imgId = img.name;
	// create the right control panel links
	var buttonValues = getMapEntry(imgId);
	var buttons = $("#controlpanel a");
	var rowsShownCount = 0;
	var maxWidth = 0;
	for ( var i = 0; i < buttons.length; i++) {
		button = buttons[i];
		var buttonId = button.id;
		var buttonValue = buttonValues[buttonId];
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
			var showCaption = buttonValues['showCaption'];
			if (showCaption) {
				$("#cpShowChildrenCaption").text(showCaption);
				maxWidth = getVariableWidth(showCaption, 20, maxWidthShowChildren, maxWidth);
			}
		} else if (buttonId == 'cpShowMore') {
			var showMoreCaption = buttonValues['showMoreCaption'];
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
	var docViewBottom = $(window).height() + $(window).scrollTop();
	if (top + popupHeight > docViewBottom) {
		top = docViewBottom - popupHeight;
	}

	var left = p.left + 1;
	
	// see if the popup will go beyond the right-hand side
	// this is a best-guess based on the messages
	var docViewRight = $(window).width() + $(window).scrollLeft();
	if (left + maxWidth > docViewRight) {
		left = docViewRight - maxWidth;
	}
	
	$("#controlpanel").show().css( {
		"top" : top,
		"left" : left
	});

};
this.hideMenu = function() {
	isMenuActive = false;
	// TODO put back $("#controlpanel").hide();
};
var isMenuActive = false;
var cancelerEvent = null;
this.checkMenuActive = function(e) {
	if (!isMenuActive && (cancelerEvent == null || cancelerEvent == e)) {
		hideMenu();
	}
	if (e != null) {
		e.preventDefault();
		return false;
	}
};
this.setupMenus = function() {
	$(".opener").bind("click mouseenter", function(e) {
		cancelerEvent = e;
		e.preventDefault();
		var target = this;
		var timeout = 5000;
		if (e.type == "mouseenter") {
			timeout = 50;
		}
		setTimeout(function() {
			showMenu(e, target);
		}, 5);
		setTimeout(function() {
			checkMenuActive(e);
		}, timeout);
    });
	$("#controlpanel").mouseenter(function(e) {
		isMenuActive = true;
	});
	$("#controlpanel").mouseleave(function(e) {
		hideMenu();
	});
	$("#controlpanel").mousemove(function(e) {
		isMenuActive = true;
	});
	
	// <a shape="rect" class="cpButton" id="cpHide" href="TBD">
	$(".cpButton").click(function(e) {
		controlPanelClicked(this);
		return false;
	});
	
};
function controlPanelClicked(aTag) {
	var action = aTag.id;
	var pos = aTag.href.indexOf('#');
	var id = aTag.href.substring(pos + 1);
	
	if (action == "cpClose") {
		// this logic doesn't really make any sense yet
		markNodeAsShown(id, false);
		// TODO I need to fix the premise that I'm only rendering a sub-tree.
		//		in the test code, change it from rendering a sub-tree, to marking all as hide, and just marking those nodes to show
		//		and then when you show a node, always walk up the tree to mark parent nodes to show too
		renderTree(id, true);
	}
}
function addMenusToButtons() {
	imagePreview();
	setupMenus();
}
// starting the script on page load
/* We now perform these on demand, not at load
$(document).ready(function() {
	imagePreview();
	setupMenus();
});
*/