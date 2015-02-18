jQuery.noConflict();
jQuery(document).ready(function() {
	jQuery("#textfield").focus(function() {
		this.select();
	});
	jQuery("#textfield").focus();
	jQuery(".Button").bind('mouseover focus', function(event) {
		jQuery(this).addClass("ButtonOver");
	}).bind('mouseout blur', function(event) {
		jQuery(this).removeClass("ButtonOver");
	});
});

this.getLeft = function(e, img) {
	var w = jQuery("body").width();
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
	var docViewTop = jQuery(window).scrollTop();
	var top = e.pageY - popupHeight; // default - put offset in relation to mouse
	if (top < docViewTop) {
		top = docViewTop; // if it's too tall, put at top of page
	} else {
		// test for bottom of page
		var docViewBottom = docViewTop + jQuery(window).height() - bottomFudge;
		var lowest = docViewBottom - popupHeight;
		if (top > lowest) {
			top = lowest;
		}
	}
	return top;
};
this.getHref = function(e) {
	// for example http://bi.robestone.com/tiny/we/Homininae.jpg
	var src = jQuery(e).find(".Thumb").attr("src");
	var pos = src.indexOf("/tiny/");
	var left = src.substring(0, pos);
	var right = src.substring(pos + 6);
	var href = left + "/preview/" + right;
	return href;
};
this.getImageWidth = function(img) {
	return this.getImageAttribute(img).width;
};
this.getImageHeight = function(img) {
	return this.getImageAttribute(img).height;
};
this.getImageCaption = function(img) {
	var caption = this.getImageAttribute(img).caption;
	caption = caption.replace(/\[/g, '<');
	caption = caption.replace(/\]/g, '>');
	return caption;
};
this.getImageAttribute = function(img) {
	var src = img.name;
	if (!src) {
		return 0;
	}
	return imageAttributes[src];
};
this.imagePreview = function() {
	jQuery("a.preview").hover(function(e) {
		var imageBorderWidth = 2; // represents the two borders, left/right
			var width = getImageWidth(this) + imageBorderWidth;
			var href = getHref(this); // this.href;
			var caption = getImageCaption(this);
			var c = (caption != "") ? "<br/>" + caption : "";
			jQuery("body").append(
					"<p id='preview'><img src='" + href + "'/>" + c + "</p>");
			jQuery("#preview").hide().css("top", getPreviewTop(e, this) + "px").css(
					"left", getLeft(e, this) + "px").css("width", width + "px")
					.show("fast");
		}, function() {
			jQuery("#preview").remove();
		});
	jQuery("a.preview").mousemove(
			function(e) {
				jQuery("#preview").css("top", getPreviewTop(e, this) + "px").css(
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
	var buttonValues = controlPanel[imgId];
	var buttons = jQuery("#controlpanel a");
	var rowsShownCount = 0;
	var maxWidth = 0;
	for ( var i = 0; i < buttons.length; i++) {
		button = buttons[i];
		var buttonId = button.id;
		var buttonValue = buttonValues[buttonId];
		if (buttonValue) {
			button.href = buttonValue + "#" + imgId;
			jQuery(button).show();
			rowsShownCount++;
		} else {
			jQuery(button).hide();
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
				jQuery("#cpShowChildrenCaption").text(showCaption);
				maxWidth = getVariableWidth(showCaption, 20, maxWidthShowChildren, maxWidth);
			}
		} else if (buttonId == 'cpShowMore') {
			var showMoreCaption = buttonValues['showMoreCaption'];
			if (showMoreCaption) {
				jQuery("#cpShowMoreCaption").text(showMoreCaption);
				maxWidth = getVariableWidth(showMoreCaption, 19, maxWidthShowMore, maxWidth);
			}
		}
	}
	// one row = 168 x 19
	// 112 = total height = 5 * 19 + 17 = rowheight * numrows + ?
	var popupHeight = rowsShownCount * 22 + 2;
	
	var p = jQuery(img).position();
	var t = p.top;
	var top = t - 5;
	
	// see if the popup will go below the screen
	var docViewBottom = jQuery(window).height() + jQuery(window).scrollTop();
	if (top + popupHeight > docViewBottom) {
		top = docViewBottom - popupHeight;
	}

	var left = p.left + 1;
	
	// see if the popup will go beyond the right-hand side
	// this is a best-guess based on the messages
	var docViewRight = jQuery(window).width() + jQuery(window).scrollLeft();
	if (left + maxWidth > docViewRight) {
		left = docViewRight - maxWidth;
	}
	
	jQuery("#controlpanel").show().css( {
		"top" : top,
		"left" : left
	});

};
this.hideMenu = function() {
	isMenuActive = false;
	jQuery("#controlpanel").hide();
};
var isMenuActive = false;
this.checkMenuActive = function() {
	if (!isMenuActive) {
		hideMenu();
	}
};
this.setupMenus = function() {
	jQuery(".opener").mouseenter(function(e) {
		showMenu(e, this);
		setTimeout(checkMenuActive, 500);
	});
	jQuery("#controlpanel").mouseenter(function(e) {
		isMenuActive = true;
	});
	jQuery("#controlpanel").mouseleave(function(e) {
		hideMenu();
	});
	jQuery("#controlpanel").mousemove(function(e) {
		isMenuActive = true;
	});
};
// starting the script on page load
jQuery(document).ready(function() {
	imagePreview();
	setupMenus();
});