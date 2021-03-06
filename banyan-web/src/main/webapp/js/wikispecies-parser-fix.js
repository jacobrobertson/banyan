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
	var newText = editor.innerHTML;
	
	if (obj.image != null) {
		newText = obj.image + "\n" + newText;	
	}
	if (obj.commonName != null) {
		var vnToken = "{{VN";
		var titleToken = "==Vernacular names==";
		var vnPos = newText.indexOf(vnToken);
		var appendPos = -1;
		var titleNeeded = true;
		if (vnPos > 0) {
			var enPos = newText.indexOf("|en=", vnPos);
			if (enPos > 0) {
				vnPos = -1;
			}
		} else {
			var titlePos = newText.indexOf(titleToken);
			if (titlePos > 0) {
				appendPos = titlePos + titleToken.length;
				titleNeeded = false;
			} else {
				var tokens = [
					"{{commonscat|",
					"[[Category:",
					"{{Commons category|",
					"{{commons|Category:",
					"{{stub}}"
					];
				var lastTokenPos = 0;
				for (var i = 0; i < tokens.length; i++) {
					var tokenPos = newText.indexOf(tokens[i], lastTokenPos);
					if (tokenPos > 0) {
						lastTokenPos = tokenPos;
					}
				}
				if (lastTokenPos > 0) {
					appendPos = lastTokenPos;
				}
			}
		}
		if (vnPos > 0) {
			var left = newText.substring(0, vnPos + vnToken.length);
			var right = newText.substring(vnPos + vnToken.length);
			var vn = "\n|en=" + obj.commonName;
			newText = left + vn + right;
		} else {
			var vn = "\n{{VN\n|en=" + obj.commonName + "\n}}\n";
			if (titleNeeded) {
				vn = "\n" + titleToken + vn;
			}
			if (appendPos < 0) {
				newText = newText + vn;
			} else {
				var left = newText.substring(0, appendPos);
				var right = newText.substring(appendPos);
				newText = left.trim() + "\n" + vn + "\n" + right.trim();
			}
		}
	}
	
	editor.innerHTML = newText;
	var button = document.getElementById("wpPreview");
	button.click();
}());