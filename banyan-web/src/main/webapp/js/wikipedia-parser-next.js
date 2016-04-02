// pages['1459536651847'] = {id:'1459536651847',latin:'Microhierax_caerulescens', image:"[[File:Microhierax caerulescens.jpg|thumb|250px|''Microhierax caerulescens'']]"};

{"id":"1459536651847","latin":"Microhierax_caerulescens", "image":"[[File:Microhierax caerulescens.jpg|thumb|250px|''Microhierax caerulescens'']]"}

javascript: (function() {
	var value = prompt("Next page", "");
	var obj = JSON.parse(value);
	var name = obj.latin;
	var url = "https://species.wikimedia.org/w/index.php?action=edit&title=" + name;
	window.location.href = url;
}());