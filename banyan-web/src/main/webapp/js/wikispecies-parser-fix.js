javascript: (function() {
	var url = window.location.href;
	var hash = url.indexOf("#");
	var value;
	if (hash > 0) {
		value = url.substring(hash + 1);
	} else {
		value = prompt("Edits", "");
	}
	var obj = JSON.parse(value);
	var editor = document.getElementById("wpTextbox1");
	var current = editor.innerHTML;
	var newText = obj.image + "\n" + current;
	editor.innerHTML = newText;
	var button = document.getElementById("wpPreview");
	button.click();
}());