dojo.provide("dojo.gfx.common");

dojo.require("dojo.gfx.color");
dojo.require("dojo.lang.declare");
dojo.require("dojo.lang.extras");
dojo.require("dojo.dom");

dojo.lang.mixin(dojo.gfx, {
	// summary: defines constants, prototypes, and utility functions
	
	// default shapes, which are used to fill in missing parameters
	defaultPath:     {type: "path",     path: ""},
	defaultPolyline: {type: "polyline", points: []},
	defaultRect:     {type: "rect",     x: 0, y: 0, width: 100, height: 100, r: 0},
	defaultEllipse:  {type: "ellipse",  cx: 0, cy: 0, rx: 200, ry: 100},
	defaultCircle:   {type: "circle",   cx: 0, cy: 0, r: 100},
	defaultLine:     {type: "line",     x1: 0, y1: 0, x2: 100, y2: 100},
	defaultImage:    {type: "image",    x: 0, y: 0, width: 0, height: 0, src: ""},
	defaultText:     {type: "text",     x: 0, y: 0, text: "",
		align: "left", decoration: "none", rotated: false, kerning: true },
	defaultTextPath: {type: "textpath", text: "",
		align: "left", decoration: "none", rotated: false, kerning: true },

	// default geometric attributes
	defaultStroke: {color: "black", width: 1, cap: "butt", join: 4},
	defaultLinearGradient: {type: "linear", x1: 0, y1: 0, x2: 100, y2: 100, 
		colors: [{offset: 0, color: "black"}, {offset: 1, color: "white"}]},
	defaultRadialGradient: {type: "radial", cx: 0, cy: 0, r: 100, 
		colors: [{offset: 0, color: "black"}, {offset: 1, color: "white"}]},
	defaultPattern: {type: "pattern", x: 0, y: 0, width: 0, height: 0, src: ""},
	defaultFont: {type: "font", style: "normal", variant: "normal", weight: "normal", 
		size: "10pt", family: "serif"},

	normalizeColor: function(/*Color*/ color){
		// summary: converts any legal color representation to normalized dojo.gfx.color.Color object
		return (color instanceof dojo.gfx.color.Color) ? color : new dojo.gfx.color.Color(color); // dojo.gfx.color.Color
	},
	normalizeParameters: function(existed, update){
		// summary: updates an existing object with properties from an "update" object
		// existed: Object: the "target" object to be updated
		// update:  Object: the "update" object, whose properties will be used to update the existed object
		if(update){
			var empty = {};
			for(var x in existed){
				if(x in update && !(x in empty)){
					existed[x] = update[x];
				}
			}
		}
		return existed;	// Object
	},
	makeParameters: function(defaults, update){
		// summary: copies the original object, and all copied properties from the "update" object
		// defaults: Object: the object to be cloned before updating
		// update:   Object: the object, which properties are to be cloned during updating
		if(!update) return dojo.lang.shallowCopy(defaults, true);
		var result = {};
		for(var i in defaults){
			if(!(i in result)){
				result[i] = dojo.lang.shallowCopy((i in update) ? update[i] : defaults[i], true);
			}
		}
		return result; // Object
	},
	formatNumber: function(x, addSpace){
		// summary: converts a number to a string using a fixed notation
		// x:			Number:		number to be converted
		// addSpace:	Boolean?:	if it is true, add a space before a positive number
		var val = x.toString();
		if(val.indexOf("e") >= 0){
			val = x.toFixed(4);
		}else{
			var point = val.indexOf(".");
			if(point >= 0 && val.length - point > 5){
				val = x.toFixed(4);
			}
		}
		if(x < 0){
			return val; // String
		}
		return addSpace ? " " + val : val; // String
	},
	// font operations
	makeFontString: function(font){
		// summary: converts a font object to a CSS font string
		// font:	Object:	font object (see dojo.gfx.defaultFont)
		return font.style + " " + font.variant + " " + font.weight + " " + font.size + " " + font.family; // Object
	},
	splitFontString: function(str){
		// summary: converts a CSS font string to a font object
		// str:		String:	a CSS font string
		var font = dojo.lang.shallowCopy(dojo.gfx.defaultFont);
		var t = str.split(/\s+/);
		do{
			if(t.length < 5){ break; }
			font.style  = t[0];
			font.varian = t[1];
			font.weight = t[2];
			var i = t[3].indexOf("/");
			font.size = i < 0 ? t[3] : t[3].substring(0, i);
			var j = 4;
			if(i < 0){
				if(t[4] == "/"){
					j = 6;
					break;
				}
				if(t[4].substr(0, 1) == "/"){
					j = 5;
					break;
				}
			}
			if(j + 3 > t.length){ break; }
			font.size = t[j];
			font.family = t[j + 1];
		}while(false);
		return font;	// Object
	},
	
	// a constant used to split a SVG/VML path into primitive components
	pathRegExp: /([A-Za-z]+)|(\d+(\.\d+)?)|(\.\d+)|(-\d+(\.\d+)?)|(-\.\d+)/g
});

dojo.declare("dojo.gfx.Surface", null,
	function(){
		// summary: a surface object to be used for drawings
		
		// underlying node
		this.rawNode = null;
	},
{
	getEventSource: function(){
		// summary: returns a node, which can be used to attach event listeners
		
		return this.rawNode; // Node
	}
});

dojo.declare("dojo.gfx.Point", null, {
	// summary: a hypothetical 2D point to be used for drawings - {x, y}
	// description: This object is defined for documentation purposes.
	//	You should use a naked object instead: {x: 1, y: 2}.
});

dojo.declare("dojo.gfx.Rectangle", null, {
	// summary: a hypothetical rectangle - {x, y, width, height}
	// description: This object is defined for documentation purposes.
	//	You should use a naked object instead: {x: 1, y: 2, width: 100, height: 200}.
});
