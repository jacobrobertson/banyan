javascript: (function() {
	var value = prompt("Next page", "");
	var obj = JSON.parse(value);
	var name = obj.latin;
	var url = "https://species.wikimedia.org/w/index.php?action=edit&title=" + name;
	window.location.href = url + "#" + value;
}());