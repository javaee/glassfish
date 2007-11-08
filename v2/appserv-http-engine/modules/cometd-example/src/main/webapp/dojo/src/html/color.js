dojo.require("dojo.html.style");
dojo.provide("dojo.html.color");

dojo.require("dojo.gfx.color");
dojo.require("dojo.lang.common");

dojo.html.getBackgroundColor = function(/* HTMLElement */node){
	//	summary
	//	returns the background color of the passed node as a 32-bit color (RGBA)
	node = dojo.byId(node);
	var color;
	do{
		color = dojo.html.getStyle(node, "background-color");
		// Safari doesn't say "transparent"
		if(color.toLowerCase() == "rgba(0, 0, 0, 0)") { color = "transparent"; }
		if(node == document.getElementsByTagName("body")[0]) { node = null; break; }
		node = node.parentNode;
	}while(node && dojo.lang.inArray(["transparent", ""], color));
	if(color == "transparent"){
		color = [255, 255, 255, 0];
	}else{
		color = dojo.gfx.color.extractRGB(color);
	}
	return color;	//	array
}
