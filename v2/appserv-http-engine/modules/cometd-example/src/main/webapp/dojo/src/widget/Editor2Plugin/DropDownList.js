dojo.provide("dojo.widget.Editor2Plugin.DropDownList");

dojo.require("dojo.widget.Editor2");
dojo.require("dojo.widget.PopupContainer");
dojo.declare("dojo.widget.Editor2ToolbarDropDownButton", dojo.widget.Editor2ToolbarButton, {
	// summary: dojo.widget.Editor2ToolbarDropDownButton extends the basic button with a dropdown list

	onClick: function(){
		if(this._domNode && !this._domNode.disabled && this._parentToolbar.checkAvailability()){
			if(!this._dropdown){
				this._dropdown = dojo.widget.createWidget("PopupContainer", {});
				this._domNode.appendChild(this._dropdown.domNode);
			}
			if(this._dropdown.isShowingNow){
				this._dropdown.close();
			}else{
				this.onDropDownShown();
				this._dropdown.open(this._domNode, null, this._domNode);
			}
		}
	},
	destroy: function(){
		this.onDropDownDestroy();
		if(this._dropdown){
			this._dropdown.destroy();
		}
		dojo.widget.Editor2ToolbarDropDownButton.superclass.destroy.call(this);
	},
	enableToolbarItem: function(){
		this._domNode.disabled = false;
		dojo.html.removeClass(this._domNode, 'dojoE2TB_SCFieldDisabled');
	},

	disableToolbarItem: function(){
		this._domNode.disabled = true;
		dojo.html.addClass(this._domNode, 'dojoE2TB_SCFieldDisabled');
	},
	onDropDownShown: function(){},
	onDropDownDestroy: function(){}
});

dojo.declare("dojo.widget.Editor2ToolbarComboItem", dojo.widget.Editor2ToolbarDropDownButton,{
	// summary: dojo.widget.Editor2ToolbarComboItem provides an external loaded dropdown list

	href: null,
	create: function(node, toolbar){
		dojo.widget.Editor2ToolbarComboItem.superclass.create.apply(this, arguments);
		//do not use lazy initilization, as we need the local names in refreshState()
		if(!this._contentPane){
			dojo.require("dojo.widget.ContentPane");
			this._contentPane = dojo.widget.createWidget("ContentPane", {preload: 'true'});
			this._contentPane.addOnLoad(this, "setup");
			this._contentPane.setContent(dojo.uri.cache.get(this.href));
		}
	},

	onMouseOver: function(e){
		if(this._lastState != dojo.widget.Editor2Manager.commandState.Disabled){
			dojo.html.addClass(e.currentTarget, this._parentToolbar.ToolbarHighlightedSelectStyle);
		}
	},
	onMouseOut:function(e){
		dojo.html.removeClass(e.currentTarget, this._parentToolbar.ToolbarHighlightedSelectStyle);
	},

	onDropDownShown: function(){
		if(!this._dropdown.__addedContentPage){
			this._dropdown.addChild(this._contentPane);
			this._dropdown.__addedContentPage = true;
		}
	},

	setup: function(){
		// summary: overload this to connect event
	},

	onChange: function(e){
		if(this._parentToolbar.checkAvailability()){
			var name = e.currentTarget.getAttribute("dropDownItemName");
			var curInst = dojo.widget.Editor2Manager.getCurrentInstance();
			if(curInst){
				var _command = curInst.getCommand(this._name);
				if(_command){
					_command.execute(name);
				}
			}
		}
		this._dropdown.close();
	},

	onMouseOverItem: function(e){
		dojo.html.addClass(e.currentTarget, this._parentToolbar.ToolbarHighlightedSelectItemStyle);
	},

	onMouseOutItem: function(e){
		dojo.html.removeClass(e.currentTarget, this._parentToolbar.ToolbarHighlightedSelectItemStyle);
	}
});

dojo.declare("dojo.widget.Editor2ToolbarFormatBlockSelect", dojo.widget.Editor2ToolbarComboItem, {
	// summary: dojo.widget.Editor2ToolbarFormatBlockSelect is an improved format block setting item

	href: dojo.uri.cache.allow(dojo.uri.moduleUri("dojo", "widget/templates/Editor2/EditorToolbar_FormatBlock.html")),

	setup: function(){
		dojo.widget.Editor2ToolbarFormatBlockSelect.superclass.setup.call(this);

		var nodes = this._contentPane.domNode.all || this._contentPane.domNode.getElementsByTagName("*");
		this._blockNames = {};
		this._blockDisplayNames = {};
		for(var x=0; x<nodes.length; x++){
			var node = nodes[x];
			dojo.html.disableSelection(node);
			var name=node.getAttribute("dropDownItemName")
			if(name){
				this._blockNames[name] = node;
				var childrennodes = node.getElementsByTagName(name);
				this._blockDisplayNames[name] = childrennodes[childrennodes.length-1].innerHTML;
			}
		}
		for(var name in this._blockNames){
			dojo.event.connect(this._blockNames[name], "onclick", this, "onChange");
			dojo.event.connect(this._blockNames[name], "onmouseover", this, "onMouseOverItem");
			dojo.event.connect(this._blockNames[name], "onmouseout", this, "onMouseOutItem");
		}
	},

	onDropDownDestroy: function(){
		if(this._blockNames){
			for(var name in this._blockNames){
				delete this._blockNames[name];
				delete this._blockDisplayNames[name];
			}
		}
	},

	refreshState: function(){
		dojo.widget.Editor2ToolbarFormatBlockSelect.superclass.refreshState.call(this);
		if(this._lastState != dojo.widget.Editor2Manager.commandState.Disabled){
			var curInst = dojo.widget.Editor2Manager.getCurrentInstance();
			if(curInst){
				var _command = curInst.getCommand(this._name);
				if(_command){
					var format = _command.getValue();
					if(format == this._lastSelectedFormat && this._blockDisplayNames){
						return this._lastState;
					}
					this._lastSelectedFormat = format;
					var label = this._domNode.getElementsByTagName("label")[0];
					var isSet = false;
					if(this._blockDisplayNames){
						for(var name in this._blockDisplayNames){
							if(name == format){
								label.innerHTML = 	this._blockDisplayNames[name];
								isSet = true;
								break;
							}
						}
						if(!isSet){
							label.innerHTML = "&nbsp;";
						}
					}
				}
			}
		}

		return this._lastState;
	}
});

dojo.declare("dojo.widget.Editor2ToolbarFontSizeSelect", dojo.widget.Editor2ToolbarComboItem,{
	// summary: dojo.widget.Editor2ToolbarFontSizeSelect provides a dropdown list for setting fontsize

	href: dojo.uri.cache.allow(dojo.uri.moduleUri("dojo", "widget/templates/Editor2/EditorToolbar_FontSize.html")),

	setup: function(){
		dojo.widget.Editor2ToolbarFormatBlockSelect.superclass.setup.call(this);

		var nodes = this._contentPane.domNode.all || this._contentPane.domNode.getElementsByTagName("*");
		this._fontsizes = {};
		this._fontSizeDisplayNames = {};
		for(var x=0; x<nodes.length; x++){
			var node = nodes[x];
			dojo.html.disableSelection(node);
			var name=node.getAttribute("dropDownItemName")
			if(name){
				this._fontsizes[name] = node;
				this._fontSizeDisplayNames[name] = node.getElementsByTagName('font')[0].innerHTML;
			}
		}
		for(var name in this._fontsizes){
			dojo.event.connect(this._fontsizes[name], "onclick", this, "onChange");
			dojo.event.connect(this._fontsizes[name], "onmouseover", this, "onMouseOverItem");
			dojo.event.connect(this._fontsizes[name], "onmouseout", this, "onMouseOutItem");
		}
	},

	onDropDownDestroy: function(){
		if(this._fontsizes){
			for(var name in this._fontsizes){
				delete this._fontsizes[name];
				delete this._fontSizeDisplayNames[name];
			}
		}
	},

	refreshState: function(){
		dojo.widget.Editor2ToolbarFormatBlockSelect.superclass.refreshState.call(this);
		if(this._lastState != dojo.widget.Editor2Manager.commandState.Disabled){
			var curInst = dojo.widget.Editor2Manager.getCurrentInstance();
			if(curInst){
				var _command = curInst.getCommand(this._name);
				if(_command){
					var size = _command.getValue();
					if(size == this._lastSelectedSize && this._fontSizeDisplayNames){
						return this._lastState;
					}
					this._lastSelectedSize = size;
					var label = this._domNode.getElementsByTagName("label")[0];
					var isSet = false;
					if(this._fontSizeDisplayNames){
						for(var name in this._fontSizeDisplayNames){
							if(name == size){
								label.innerHTML = 	this._fontSizeDisplayNames[name];
								isSet = true;
								break;
							}
						}
						if(!isSet){
							label.innerHTML = "&nbsp;";
						}
					}
				}
			}
		}
		return this._lastState;
	}
});

dojo.declare("dojo.widget.Editor2ToolbarFontNameSelect", dojo.widget.Editor2ToolbarFontSizeSelect, {
	// summary: dojo.widget.Editor2ToolbarFontNameSelect provides a dropdown list for setting fontname
	href: dojo.uri.cache.allow(dojo.uri.moduleUri("dojo", "widget/templates/Editor2/EditorToolbar_FontName.html"))
});
