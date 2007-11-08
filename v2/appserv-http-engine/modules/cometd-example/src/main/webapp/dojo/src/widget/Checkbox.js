dojo.provide("dojo.widget.Checkbox");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.event.*");
dojo.require("dojo.html.style");
dojo.require("dojo.html.selection");

dojo.widget.defineWidget(
	"dojo.widget.Checkbox",
	dojo.widget.HtmlWidget,
	{
		// summary
		//	Same as an HTML checkbox, but with fancy styling

		templatePath: dojo.uri.moduleUri("dojo", "widget/templates/Checkbox.html"),
		templateCssPath: dojo.uri.moduleUri("dojo", "widget/templates/Checkbox.css"),

		// class: String
		//	CSS class name for widget
		"class": "dojoCheckbox",

		//	Value of "type" attribute for <input>, and waiRole attribute also.
		//	User probably shouldn't adjust this.
		_type: "checkbox",

		// name: String
		//	name used when submitting form; same as "name" attribute or plain HTML elements
		name: "",

		// id: String
		//	id attached to the checkbox, used when submitting form
		id: "",

		// checked: Boolean
		//	if true, checkbox is initially marked turned on;
		//	in markup, specified as "checked='checked'" or just "checked"
		checked: false,
		
		// tabIndex: Integer
		//	order fields are traversed when user hits the tab key
		tabIndex: "",

		// value: Value
		//	equivalent to value field on normal checkbox (if checked, the value is passed as
		//	the value when form is submitted)
		value: "on",

		// This shared object keeps track of all widgets, grouped by name
		_groups: { },

		postMixInProperties: function(){
			dojo.widget.Checkbox.superclass.postMixInProperties.apply(this, arguments);
			
			// set tabIndex="0" because if tabIndex=="" user won't be able to tab to the field
			if(!this.disabled && this.tabIndex==""){ this.tabIndex="0"; }		
		},

		fillInTemplate: function(){
			this._setValue(this.checked);
		},

		postCreate: function(){
			// find any associated label and create a labelled-by relationship
			// assumes <label for="inputId">label text </label> rather than
			// <label><input type="xyzzy">label text</label>
			var notcon = true;
			this.id = this.id !="" ? this.id : this.widgetId;
			if(this.id != ""){
				var labels = document.getElementsByTagName("label");
				if (labels != null && labels.length > 0){
					for(var i=0; i<labels.length; i++){
						if (labels[i].htmlFor == this.id){
							labels[i].id = (labels[i].htmlFor + "label");
							this._connectEvents(labels[i]);
							dojo.widget.wai.setAttr(this.domNode, "waiState", "labelledby", labels[i].id);
							break;
						}
					}
				}
			}
			this._connectEvents(this.domNode);
			// this is needed here for IE
			this.inputNode.checked=this.checked;
			this._register();
		},

		uninitialize: function(){
			this._deregister();
		},

		_connectEvents: function(/*DomNode*/ node){
			dojo.event.connect(node, "onmouseover", this, "mouseOver");
			dojo.event.connect(node, "onmouseout", this, "mouseOut");
			dojo.event.connect(node, "onkey", this, "onKey");
			dojo.event.connect(node, "onclick", this, "_onClick");
			dojo.html.disableSelection(node);
		},

		_onClick: function(/*Event*/ e){
			if(this.disabled == false){
				this.setValue(!this.checked);
			}
			e.stopPropagation();
			this.onClick();
		},

		_register: function(){
			// summary: add this widget to _groups
			if(this._groups[this.name] == null){
				this._groups[this.name]=[];
			}
			this._groups[this.name].push(this);
		},

		_deregister: function(){
			// summary: remove this widget from _groups
			var idx = dojo.lang.find(this._groups[this.name], this, true);
			this._groups[this.name].splice(idx, 1);
		},

		setValue: function(/*boolean*/ bool){
			// summary: set the checkbox state
			this._setValue(bool);
		},

		onClick: function(){
			// summary: user overridable callback function for checkbox being clicked
		},

		onKey: function(/*Event*/ e){
			// summary: callback when user hits a key
			var k = dojo.event.browser.keys;
			if(e.key == " "){
	 			this._onClick(e);
	 		}
		},

		mouseOver: function(/*Event*/ e){
			// summary: callback when user moves mouse over checkbox
			this._hover(e, true);
		},

		mouseOut: function(/*Event*/ e){
			// summary: callback when user moves mouse off of checkbox
			this._hover(e, false);
		},

		_hover: function(/*Event*/ e, /*Boolean*/ isOver){
			if (this.disabled == false){
				var state = this.checked ? "On" : "Off";
				var style = this["class"] + state + "Hover";
				if (isOver){
					dojo.html.addClass(this.imageNode, style);
				}else{
					dojo.html.removeClass(this.imageNode,style);
				}
			}
		},

		_setValue: function(/*Boolean*/ bool){
			// summary:
			//	sets checkbox to given value
			//	set state of hidden checkbox node to correspond to given value.
			//	also set CSS class string according to checked/unchecked and disabled/enabled state
			this.checked = bool;
			var state = this["class"] + (this.disabled ? "Disabled" : "") + (this.checked ? "On" : "Off");
			dojo.html.setClass(this.imageNode, this["class"] + " " + state);
			this.inputNode.checked = this.checked;
			if(this.disabled){
				this.inputNode.setAttribute("disabled",true);
			}else{
				this.inputNode.removeAttribute("disabled");
			}
			dojo.widget.wai.setAttr(this.domNode, "waiState", "checked", this.checked);
		}
	}
);

dojo.widget.defineWidget(
	"dojo.widget.a11y.Checkbox",
	dojo.widget.Checkbox,
	{
		// summary
		//	variation on Checkbox widget to be display on monitors in high-contrast mode (that don't display CSS background images)

		templatePath: dojo.uri.moduleUri("dojo", "widget/templates/CheckboxA11y.html"),

		postCreate: function(args, frag){
			this.inputNode.checked=this.checked;
			//only set disabled if true since FF interprets any value for disabled as true
			if (this.disabled){
				this.inputNode.setAttribute("disabled",true);
			}
			this._register();
		},

		_setValue: function(/*Boolean*/ bool){
			// summary:
			//	internal function to set checkbox value
			//	set state of hidden checkbox node to correspond to given value.
			//	also set CSS class string according to checked/unchecked and disabled/enabled state
			this.checked = bool;
			this.inputNode.checked = bool;
		}
	}
);

dojo.declare(
	"dojo.widget.RadioButtonBase",
	null,
	{
		// summary
		//	Base class for radio button widgets

		// class: String
		//	CSS class name for widget
		"class": "dojoRadioButton",

		_type: "radio",
		
		_onClick: function(/*Event*/ e){
			if(!this.disabled && !this.checked){
				this.setValue(true);
			}
			e.stopPropagation();
			this.onClick();
		},

		setValue: function(/*boolean*/ bool){
			this._setValue(bool);

			// if turning this widget on, then turn others in same group off
			if(bool){
				dojo.lang.forEach(this._groups[this.name], function(widget){
					if(widget != this){
						widget._setValue(false);
					}
				}, this);
			}
		}
	}
);

dojo.widget.defineWidget(
	"dojo.widget.RadioButton",
	[dojo.widget.Checkbox, dojo.widget.RadioButtonBase],
	{
		// summary
		//	Same as an HTML radio button, but with fancy styling
	}
);

dojo.widget.defineWidget(
	"dojo.widget.a11y.RadioButton",
	[dojo.widget.a11y.Checkbox, dojo.widget.RadioButtonBase],
	{
		// summary
		//	variation on RadioButton widget to be display on monitors in high-contrast mode (that don't display CSS background images)

	}
);