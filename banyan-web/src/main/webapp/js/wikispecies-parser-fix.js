{"id":"1459536651847","latin":"Microhierax_caerulescens", "image":"[[File:Microhierax caerulescens.jpg|thumb|250px|''Microhierax caerulescens'']]"}


javascript: (function() {
	var value = prompt("Edits", "");
	var obj = JSON.parse(value);
	var editor = document.getElementById("wpTextbox1");
	var current = editor.innerHTML;
	var newText = obj.image + "\n" + current;
	editor.innerHTML = newText;
	var button = document.getElementById("wpPreview");
	button.click();
}());