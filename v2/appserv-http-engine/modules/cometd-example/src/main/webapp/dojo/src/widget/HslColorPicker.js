dojo.provide("dojo.widget.HslColorPicker");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.math");
dojo.require("dojo.svg");
dojo.require("dojo.gfx.color");
dojo.require("dojo.gfx.color.hsl");
dojo.require("dojo.experimental");
dojo.experimental("dojo.widget.svg.HslColorPicker");


dojo.widget.defineWidget(
	"dojo.widget.svg.HslColorPicker",
	dojo.widget.HtmlWidget,
	function(){
		dojo.debug("warning: the HslColorPicker is not a finished widget, and is not yet ready for general use");
		this.filterObject = {};
	},
{
	hue: "0",
	saturation: "0",
	light: "0",
	storedColor: "#0054aa",

	//	widget props
	templatePath: dojo.uri.moduleUri("dojo", "widget/templates/HslColorPicker.svg"),
	fillInTemplate: function() {
		this.height = "131px";
		this.svgDoc = this.hueNode.ownerDocument;
		this.leftGradientColorNode = this.hueNode.ownerDocument.getElementById("leftGradientColor");
		this.rightGradientColorNode = this.hueNode.ownerDocument.getElementById("rightGradientColor");
		this.hueNode.setAttributeNS(dojo.dom.xmlns.xlink, "href", dojo.uri.moduleUri("dojo", "widget/templates/images/hue.png"));
		var hsl = dojo.gfx.color.hex2hsl(this.storedColor);
		this.hue = hsl[0];
		this.saturation = hsl[1];
		this.light = hsl[2];
		this.setSaturationStopColors();
		//this.setHueSlider();
		//this.setSaturationLightSlider();
	},
	setSaturationStopColors: function() {
		//this.leftGradientStopColor = "rgb(" + dojo.gfx.color.hsl2rgb(this.hue, 20, 50).join(", ") + ")";
		//this.rightGradientStopColor = "rgb(" + dojo.gfx.color.hsl2rgb(this.hue, 100, 50).join(", ") + ")";
		//this.leftGradientStopColor = dojo.gfx.color.hsl2hex(this.hue, 20, 50);
		//this.rightGradientStopColor = dojo.gfx.color.hsl2hex(this.hue, 100, 50);
		this.leftGradientStopColor = dojo.gfx.color.rgb2hex(this.hsl2rgb(this.hue, 0, 50));
		this.rightGradientStopColor = dojo.gfx.color.rgb2hex(this.hsl2rgb(this.hue, 100, 50));
		this.leftGradientColorNode.setAttributeNS(null,'stop-color',this.leftGradientStopColor);
		this.rightGradientColorNode.setAttributeNS(null,'stop-color',this.rightGradientStopColor);
	},
	setHue: function(hue) {
		this.hue = hue;
	},
	setHueSlider: function() {
		// FIXME: need to add some padding around the picker so you can see the slider at the top and bottom of the picker)
		this.hueSliderNode.setAttribute("y", parseInt((this.hue/360) * parseInt(this.height) - 2) + "px" );
	},
	setSaturationLight: function(saturation, light) {
		this.saturation = saturation;
		this.light = light;
	},
	setSaturationLightSlider: function() {
		// TODO
	},
	onHueClick: function(evt) {
		// get the position that was clicked on the element
		// FIXME: handle document scrolling, offset
		var yPosition = parseInt(evt.clientY) - parseInt(evt.target.getAttribute("y"));
		this.setHue( 360 - parseInt(yPosition*(360/parseInt(this.height))) );
		this.setSaturationStopColors();
		this.setStoredColor(dojo.gfx.color.hsl2hex(this.hue, this.saturation, this.light));
	},
	onHueDrag: function(evt) {
		// TODO
	},
	onSaturationLightClick: function(evt) {
		// get the position that was clicked on the element
		// FIXME: handle document scrolling, offset
		var xPosition = parseInt(evt.clientX) - parseInt(evt.target.getAttribute("y"));
		var yPosition = parseInt(evt.clientY) - parseInt(evt.target.getAttribute("y"));
		var saturation = parseInt(parseInt(xPosition)*(101/106));
		var light = parseInt(parseInt(yPosition)*(101/106));
		this.setSaturationLight(saturation, light);
		this.setStoredColor(dojo.gfx.color.hsl2hex(this.hue, this.saturation, this.light));
	},
	onSaturationLightDrag: function(evt) {
		// TODO
	},
	getStoredColor: function() {
		return this.storedColor;
	},
	setStoredColor: function(rgbHexColor) {
		this.storedColor = rgbHexColor;
		dojo.event.topic.publish("/" + this.widgetId + "/setStoredColor", this.filterObject);
	},
	hsl2rgb: function(hue, saturation, light)
	{
		// hsl2rgb in dojo.gfx.color did not behave hte way I expected, so 
		// I'm using some old code I wrote until I figure out what the issue is
		// first, check to see if saturation = 0
		function rgb(q1,q2,hue) {
			if (hue>360) hue=hue-360;
			if (hue<0) hue=hue+360;
			if (hue<60) return (q1+(q2-q1)*hue/60);
			else if (hue<180) return(q2);
			else if (hue<240) return(q1+(q2-q1)*(240-hue)/60);
			else return(q1);
		}
		this.rgb = rgb
	
		if (saturation==0) {
			return [Math.round(light*255/100), Math.round(light*255/100), Math.round(light*255/100)];
		} else {
			light = light/100;
			saturation = saturation/100;
			// check to see if light > 0.5
			if ((light)<0.5) {
				var temp2 = (light)*(1.0+saturation)
			} else {
				var temp2 = (light+saturation-(light*saturation))
			}
			var temp1 = 2.0*light - temp2;
			var rgbcolor = [];
			rgbcolor[0] = Math.round(rgb(temp1,temp2,parseInt(hue)+120)*255);
			rgbcolor[1] = Math.round(rgb(temp1,temp2,hue)*255);
			rgbcolor[2] = Math.round(rgb(temp1,temp2,parseInt(hue)-120)*255);
			return rgbcolor;
		}
	}
});
