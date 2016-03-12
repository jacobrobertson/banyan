jQuery(document).ready(function() {
	jQuery(".Button") 
           .bind('mouseover focus',function(event){ 
        	   jQuery(this).addClass("ButtonOver");
           } ) 
           .bind('mouseout blur',function(event){ 
        	   jQuery(this).removeClass("ButtonOver");
           } );
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
this.getTop = function(e, img) {
	var imageHeight = getImageHeight(img);
	var docViewTop = jQuery(window).scrollTop();
	var mouseY = e.pageY - docViewTop;
	var top = e.pageY - imageHeight; // default - put offset in relation to mouse
	if (top < docViewTop) {
		top = docViewTop; // if it's too tall, put at top of page
	} else {
		// test for bottom of page
		var textHeight = 75; // to account for two rows of text
		var docViewBottom = docViewTop + jQuery(window).height() - textHeight;
		var lowest = docViewBottom - imageHeight;
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
	return this.getImageAttribute(img).caption;
};
this.getImageAttribute = function(img, name) {
	// for example http://bi.robestone.com/tiny/we/Homininae.jpg
	var src = jQuery(img).find(".Thumb").attr("src");
	// alert('src=' + src);
	if (!src) {
		return 0;
	}
	var pos = src.indexOf("/tiny/");
	var url = src.substring(pos + 6);
	return imageAttributes[url];
};
this.imagePreview = function() {	
	jQuery("a.preview").hover(function(e){
		var imageBorderWidth = 2; // represents the two borders, left/right
		var width = getImageWidth(this) + imageBorderWidth;
		var href = getHref(this); // this.href;
		var caption = getImageCaption(this);
		var c = (caption != "") ? "<br/>" + caption : "";
		jQuery("body").append("<p id='preview'><img src='" + href + "'/>"+ c +"</p>");								 
		jQuery("#preview")
			.hide()
			.css("top",getTop(e, this) + "px")
			.css("left",getLeft(e, this) + "px")
			.css("width", width + "px")
			.show("fast");					
    },
	function(){
		jQuery("#preview").remove();
    });	
	jQuery("a.preview").mousemove(function(e){
		jQuery("#preview")
		.css("top",getTop(e, this) + "px")
		.css("left",getLeft(e, this) + "px")
	});			
};
this.toggleMenuShow = function(e) {
	toggleMenu(e, "inline", "none");
};
this.toggleMenuHide = function(e) {
	toggleMenu(e, "none", "inline");
};
this.toggleMenu = function(e, menu, opener) {
	 var parent = jQuery(e).closest(".Node");
	 var id = parent[0].id;
	 jQuery("#" + id + " .menuitem").css({"display": menu});
	 jQuery("#" + id + " .opener").css({"display": opener});
};

this.setupMenus = function() {
	 jQuery(".opener").mouseenter(function () {
		 toggleMenuShow(this);
	 });
	 jQuery(".line").mouseleave(function () {
		 toggleMenuHide(this);
	 });
};
// starting the script on page load
jQuery(document).ready(function(){
	imagePreview();
	setupMenus();
});