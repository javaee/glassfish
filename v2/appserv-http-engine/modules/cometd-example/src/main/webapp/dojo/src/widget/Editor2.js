dojo.provide("dojo.widget.Editor2");

dojo.require("dojo.io.*");
dojo.require("dojo.widget.RichText");
dojo.require("dojo.widget.Editor2Toolbar");
dojo.require("dojo.i18n.common");
dojo.require("dojo.uri.cache");
dojo.requireLocalization("dojo.widget", "Editor2");
dojo.requireLocalization("dojo.widget", "Editor2BrowserCommand");

dojo.widget.Editor2Manager = new dojo.widget.HandlerManager();

dojo.lang.mixin(dojo.widget.Editor2Manager,
{
	// summary: Manager of current focused Editor2 Instance and available editor2 commands

	_currentInstance: null,

	// commandState: Object: state a command may be in
	commandState: {Disabled: 0, Latched: 1, Enabled: 2},

	getCurrentInstance: function(){
		// summary: Return the current focused Editor2 instance
		return this._currentInstance;
	},
	setCurrentInstance: function(/*Widget*/inst){
		// summary: Set current focused Editor2 instance
		this._currentInstance = inst;
	},
	getCommand: function(/*dojo.widget.Editor2*/editor,/*String*/name){
		// summary: Return Editor2 command with the given name
		// name: name of the command (case insensitive)
		var oCommand;
		name = name.toLowerCase();
		for(var i=0;i<this._registeredHandlers.length;i++){
			oCommand = this._registeredHandlers[i](editor, name);
			if(oCommand){
				return oCommand;
			}
		}
		if(name == 'createlink' || name == 'insertimage'){
			if(!dojo.widget['Editor2Plugin'] || !dojo.widget.Editor2Plugin['DialogCommands']){
				dojo.deprecated('Command '+name+" is now defined in plugin dojo.widget.Editor2Plugin.DialogCommands. It shall be required explicitly", "0.6");
				dojo['require']("dojo.widget.Editor2Plugin.DialogCommands"); //avoid loading by the build
			}
		}
		var resources = dojo.i18n.getLocalization("dojo.widget", "Editor2", this.lang);
		switch(name){
			case 'htmltoggle':
				//Editor2 natively provide the htmltoggle functionalitity
				//and it is treated as a builtin command
				oCommand = new dojo.widget.Editor2BrowserCommand(editor, name);
				break;
//			case 'formatblock':
//				oCommand = new dojo.widget.Editor2FormatBlockCommand(editor, name);
//				break;
			case 'anchor':
				oCommand = new dojo.widget.Editor2Command(editor, name);
				break;

			//dialog command
			case 'createlink':
				oCommand = new dojo.widget.Editor2DialogCommand(editor, name,
						{contentFile: "dojo.widget.Editor2Plugin.CreateLinkDialog",
							contentClass: "Editor2CreateLinkDialog",
							title: resources.createLinkDialogTitle, width: "300px", height: "200px",
							lang: this.lang});
				break;
			case 'insertimage':
				oCommand = new dojo.widget.Editor2DialogCommand(editor, name,
						{contentFile: "dojo.widget.Editor2Plugin.InsertImageDialog",
							contentClass: "Editor2InsertImageDialog",
							title: resources.insertImageDialogTitle, width: "400px", height: "270px",
							lang: this.lang});
				break;
			// By default we assume that it is a builtin simple command.
			default:
				var curtInst = this.getCurrentInstance();
				if((curtInst && curtInst.queryCommandAvailable(name)) ||
					(!curtInst && dojo.widget.Editor2.prototype.queryCommandAvailable(name))){
					oCommand = new dojo.widget.Editor2BrowserCommand(editor, name);
				}else{
					dojo.debug("dojo.widget.Editor2Manager.getCommand: Unknown command "+name);
					return;
				}
		}
		return oCommand;
	},
	destroy: function(){
		// summary: Cleaning up. This is called automatically on page unload.
		this._currentInstance = null;
		dojo.widget.HandlerManager.prototype.destroy.call(this);
	}
});

dojo.addOnUnload(dojo.widget.Editor2Manager, "destroy");

dojo.lang.declare("dojo.widget.Editor2Command",null,
	function(editor,name){
		// summary:
		//		dojo.widget.Editor2Command is the base class for all commands in Editor2

		this._editor = editor;
		this._updateTime = 0;
		this._name = name;
	},
{
		_text: 'Unknown',
		execute: function(para){
			// summary: Execute the command. should be implemented in subclass
			// description: this function should be re-implemented in subclass
			dojo.unimplemented("dojo.widget.Editor2Command.execute");
		},
		getText: function(){
			// summary: return the text name of this command
			return this._text;
		},
		getState: function(){
			// summary:
			//		Return the state of the command. The default behavior is
			//		to always return Enabled
			return dojo.widget.Editor2Manager.commandState.Enabled;
		},
		destroy: function(){
			// summary: Destructor
		}
	}
);

dojo.lang.declare("dojo.widget.Editor2BrowserCommand", dojo.widget.Editor2Command, 
	function(editor,name){
		// summary:
		//		dojo.widget.Editor2BrowserCommand is the base class for all the browser built
		//		in commands
		var browserCommandNames = dojo.i18n.getLocalization("dojo.widget", "Editor2BrowserCommand", editor.lang);
		var text = browserCommandNames[name.toLowerCase()];
		if(text){
			this._text = text;
		}
	},
{
		execute: function(para){
			this._editor.execCommand(this._name, para);
		},
		getState: function(){
			if(this._editor._lastStateTimestamp > this._updateTime || this._state == undefined){
				this._updateTime = this._editor._lastStateTimestamp;
				try{
					if(this._editor.queryCommandEnabled(this._name)){
						if(this._editor.queryCommandState(this._name)){
							this._state = dojo.widget.Editor2Manager.commandState.Latched;
						}else{
							this._state = dojo.widget.Editor2Manager.commandState.Enabled;
						}
					}else{
						this._state = dojo.widget.Editor2Manager.commandState.Disabled;
					}
				}catch (e) {
					//dojo.debug("exception when getting state for command "+this._name+": "+e);
					this._state = dojo.widget.Editor2Manager.commandState.Enabled;
				}
			}
			return this._state;
		},
		getValue: function(){
			try{
				return this._editor.queryCommandValue(this._name);
			}catch(e){}
		}
	}
);

dojo.widget.Editor2ToolbarGroups = {
	// summary: keeping track of all available share toolbar groups
};

dojo.widget.defineWidget(
	"dojo.widget.Editor2",
	dojo.widget.RichText,
	function(){
		this._loadedCommands={};
	},
	{
		// summary:
		//		dojo.widget.Editor2 is the WYSIWYG editor in dojo with toolbar. It supports a plugin
		//		framework which can be used to extend the functionalities of the editor, such as
		//		adding a context menu, table operation etc.
		// description:
		//		Plugins are available using dojo's require syntax. Please find available built-in plugins
		//		under src/widget/Editor2Plugin.

//		// saveUrl: String: url to which save action should send content to
//		saveUrl: "",
//		// saveMethod: String: HTTP method for save (post or get)
//		saveMethod: "post",
//		saveArgName: "editorContent",
//		closeOnSave: false,

		// toolbarAlwaysVisible: Boolean: Whether the toolbar should scroll to keep it in the view
		toolbarAlwaysVisible: false,

//		htmlEditing: false,

		toolbarWidget: null,
		scrollInterval: null,

		// toolbarTemplatePath: dojo.uri.Uri
		//		to specify the template file for the toolbar
		toolbarTemplatePath: dojo.uri.cache.allow(dojo.uri.moduleUri("dojo", "widget/templates/EditorToolbarOneline.html")),

		// toolbarTemplateCssPath: dojo.uri.Uri
		//		to specify the css file for the toolbar
		toolbarTemplateCssPath: dojo.uri.cache.allow(dojo.uri.moduleUri("dojo", "widget/templates/EditorToolbar.css")),

		// toolbarPlaceHolder: String
		//		element id to specify where to attach the toolbar
		toolbarPlaceHolder: '',

		_inSourceMode: false,
		_htmlEditNode: null,

		// toolbarGroup: String: 
		//		This instance of editor will share the same toolbar with other editor with the same toolbarGroup. 
		//		By default, toolbarGroup is empty and standalone toolbar is used for this instance.
		toolbarGroup: '',

		// shareToolbar: Boolean: Whether to share toolbar with other instances of Editor2. Deprecated in favor of toolbarGroup
		shareToolbar: false,

		// contextMenuGroupSet: String: specify which context menu set should be used for this instance. Include ContextMenu plugin to use this
		contextMenuGroupSet: '',

		editorOnLoad: function(){
			// summary:
			//		Create toolbar and other initialization routines. This is called after
			//		the finish of the loading of document in the editing element
//			dojo.profile.start("dojo.widget.Editor2::editorOnLoad");

			dojo.event.topic.publish("dojo.widget.Editor2::preLoadingToolbar", this);
			if(this.toolbarAlwaysVisible){
				dojo.require("dojo.widget.Editor2Plugin.AlwaysShowToolbar");
			}

			if(this.toolbarWidget){
				this.toolbarWidget.show();
				//re-add the toolbar to the new domNode (caused by open() on another element)
				dojo.html.insertBefore(this.toolbarWidget.domNode, this.domNode.firstChild);
			}else{
				if(this.shareToolbar){
					dojo.deprecated("Editor2:shareToolbar is deprecated in favor of toolbarGroup", "0.5");
					this.toolbarGroup = 'defaultDojoToolbarGroup';
				}
				if(this.toolbarGroup){
					if(dojo.widget.Editor2ToolbarGroups[this.toolbarGroup]){
						this.toolbarWidget = dojo.widget.Editor2ToolbarGroups[this.toolbarGroup];
					}
				}
				if(!this.toolbarWidget){
						var tbOpts = {shareGroup: this.toolbarGroup, parent: this, lang: this.lang};
						tbOpts.templateString = dojo.uri.cache.get(this.toolbarTemplatePath);
						if(this.toolbarTemplateCssPath){
							tbOpts.templateCssPath = this.toolbarTemplateCssPath;
							tbOpts.templateCssString = dojo.uri.cache.get(this.toolbarTemplateCssPath);
						}
						if(this.toolbarPlaceHolder){
							this.toolbarWidget = dojo.widget.createWidget("Editor2Toolbar", tbOpts, dojo.byId(this.toolbarPlaceHolder), "after");
						}else{
							this.toolbarWidget = dojo.widget.createWidget("Editor2Toolbar", tbOpts, this.domNode.firstChild, "before");
						}
						if(this.toolbarGroup){
							dojo.widget.Editor2ToolbarGroups[this.toolbarGroup] = this.toolbarWidget;
						}
						dojo.event.connect(this, "close", this.toolbarWidget, "hide");
	
						this.toolbarLoaded();
				}
			}

			dojo.event.topic.registerPublisher("Editor2.clobberFocus", this, "clobberFocus");
			dojo.event.topic.subscribe("Editor2.clobberFocus", this, "setBlur");

			dojo.event.topic.publish("dojo.widget.Editor2::onLoad", this);
//			dojo.profile.end("dojo.widget.Editor2::editorOnLoad");
		},

		//event for plugins to use
		toolbarLoaded: function(){
			// summary:
			//		Fired when the toolbar for this editor is created.
			//		This event is for plugins to use
		},

		//TODO: provide a query mechanism about loaded plugins?
		registerLoadedPlugin: function(/*Object*/obj){
			// summary: Register a plugin which is loaded for this instance
			if(!this.loadedPlugins){
				this.loadedPlugins = [];
			}
			this.loadedPlugins.push(obj);
		},
		unregisterLoadedPlugin: function(/*Object*/obj){
			// summary: Delete a loaded plugin for this instance
			for(var i in this.loadedPlugins){
				if(this.loadedPlugins[i] === obj){
					delete this.loadedPlugins[i];
					return;
				}
			}
			dojo.debug("dojo.widget.Editor2.unregisterLoadedPlugin: unknown plugin object: "+obj);
		},

		//overload the original ones to provide extra commands
		execCommand: function(/*String*/command, argument){
			switch(command.toLowerCase()){
				case 'htmltoggle':
					this.toggleHtmlEditing();
					break;
				default:
					dojo.widget.Editor2.superclass.execCommand.apply(this, arguments);
			}
		},
		queryCommandEnabled: function(/*String*/command, argument){
			switch(command.toLowerCase()){
				case 'htmltoggle':
					return true;
				default:
					if(this._inSourceMode){ return false;}
					return dojo.widget.Editor2.superclass.queryCommandEnabled.apply(this, arguments);
			}
		},
		queryCommandState: function(/*String*/command, argument){
			switch(command.toLowerCase()){
				case 'htmltoggle':
					return this._inSourceMode;
				default:
					return dojo.widget.Editor2.superclass.queryCommandState.apply(this, arguments);
			}
		},

		onClick: function(/*Event*/e){
			dojo.widget.Editor2.superclass.onClick.call(this, e);
			//if Popup is used, call dojo.widget.PopupManager.onClick
			//manually when click in the editing area to close all
			//open popups (dropdowns)
			if(dojo.widget.PopupManager){
				if(!e){ //IE
					e = this.window.event;
				}
				dojo.widget.PopupManager.onClick(e);
			}
		},

		clobberFocus: function(){
			// summary: stub to signal other instances to clobber focus
		},
		toggleHtmlEditing: function(){
			// summary: toggle between WYSIWYG mode and HTML source mode
			if(this===dojo.widget.Editor2Manager.getCurrentInstance()){
				if(!this._inSourceMode){
					var html = this.getEditorContent();
					this._inSourceMode = true;

					if(!this._htmlEditNode){
						this._htmlEditNode = dojo.doc().createElement("textarea");
						dojo.html.insertAfter(this._htmlEditNode, this.editorObject);
					}

					this._htmlEditNode.style.display = "";
					this._htmlEditNode.style.width = "100%";
					this._htmlEditNode.style.height = dojo.html.getBorderBox(this.editNode).height+"px";
					this._htmlEditNode.value = html;

					//activeX object (IE) doesn't like to be hidden, so move it outside of screen instead
					with(this.editorObject.style){
						position = "absolute";
						left = "-2000px";
						top = "-2000px";
					}
				}else{
					this._inSourceMode = false;

					//In IE activeX mode, if _htmlEditNode is focused,
					//when toggling, an error would occur, so unfocus it
					this._htmlEditNode.blur();

					with(this.editorObject.style){
						position = "";
						left = "";
						top = "";
					}
					var html = this._htmlEditNode.value;

					dojo.lang.setTimeout(this, "replaceEditorContent", 1, html);
					this._htmlEditNode.style.display = "none";
					this.focus();
				}
				this.onDisplayChanged(null, true);
			}
		},

		setFocus: function(){
			// summary: focus is set on this instance
//			dojo.debug("setFocus: start "+this.widgetId);
			if(dojo.widget.Editor2Manager.getCurrentInstance() === this){ return; }

			this.clobberFocus();
//			dojo.debug("setFocus:", this);
			dojo.widget.Editor2Manager.setCurrentInstance(this);
		},

		setBlur: function(){
			// summary: focus on this instance is lost
//			 dojo.debug("setBlur:", this);
			//dojo.event.disconnect(this.toolbarWidget, "exec", this, "execCommand");
		},

		saveSelection: function(){
			// summary: save the current selection for restoring it
			this._bookmark = null;
			this._bookmark = dojo.withGlobal(this.window, dojo.html.selection.getBookmark);
		},
		restoreSelection: function(/*Boolean*/keepbookmark){
			// summary: restore the last saved selection
			if(this._bookmark){
				this.focus(); //require for none-activeX IE
				dojo.withGlobal(this.window, "moveToBookmark", dojo.html.selection, [this._bookmark]);
				if(keepbookmark){
					this._bookmark = null;
				}
			}else{
				dojo.debug("restoreSelection: no saved selection is found!");
			}
		},

		_updateToolbarLastRan: null,
		_updateToolbarTimer: null,
		_updateToolbarFrequency: 500,

		updateToolbar: function(/*Boolean*/force){
			// summary: update the associated toolbar of this Editor2
			if((!this.isLoaded)||(!this.toolbarWidget)){ return; }

			// keeps the toolbar from updating too frequently
			// TODO: generalize this functionality?
			var diff = new Date() - this._updateToolbarLastRan;
			if( (!force)&&(this._updateToolbarLastRan)&&
				((diff < this._updateToolbarFrequency)) ){

				clearTimeout(this._updateToolbarTimer);
				var _this = this;
				this._updateToolbarTimer = setTimeout(function() {
					_this.updateToolbar();
				}, this._updateToolbarFrequency/2);
				return;

			}else{
				this._updateToolbarLastRan = new Date();
			}
			// end frequency checker

			//IE has the habit of generating events even when this editor is blurred, prevent this
			if(dojo.widget.Editor2Manager.getCurrentInstance() !== this){ return; }

			this.toolbarWidget.update();
		},

		destroy: function(/*Boolean*/finalize){
			this._htmlEditNode = null;
			dojo.event.disconnect(this, "close", this.toolbarWidget, "hide");
			if(!finalize){
				this.toolbarWidget.destroy();
			}
			dojo.widget.Editor2.superclass.destroy.call(this);
		},

		_lastStateTimestamp: 0,
		onDisplayChanged: function(/*Object*/e, /*Boolean*/forceUpdate){
			this._lastStateTimestamp = (new Date()).getTime();
			dojo.widget.Editor2.superclass.onDisplayChanged.call(this,e);
			this.updateToolbar(forceUpdate);
		},

		onLoad: function(){
			try{
				dojo.widget.Editor2.superclass.onLoad.call(this);
			}catch(e){ // FIXME: debug why this is throwing errors in IE!
				dojo.debug(e);
			}
			this.editorOnLoad();
		},

		onFocus: function(){
			dojo.widget.Editor2.superclass.onFocus.call(this);
			this.setFocus();
		},

		//overload to support source editing mode
		getEditorContent: function(){
			if(this._inSourceMode){
				return this._htmlEditNode.value;
			}
			return dojo.widget.Editor2.superclass.getEditorContent.call(this);
		},

		replaceEditorContent: function(html){
			if(this._inSourceMode){
				this._htmlEditNode.value = html;
				return;
			}
			dojo.widget.Editor2.superclass.replaceEditorContent.apply(this,arguments);
		},
		getCommand: function(/*String*/name){
			// summary: return a command associated with this instance of editor
			if(this._loadedCommands[name]){
				return this._loadedCommands[name];
			}
			var cmd = dojo.widget.Editor2Manager.getCommand(this, name);
			this._loadedCommands[name] = cmd;
			return cmd;
		},
		// Array: Commands shortcuts. Each element can has up to 3 fields:
		//		1. String: the name of the command
		//		2. String Optional: the char for shortcut key, by default the first char from the command name is used
		//		3. Int Optional: specify the modifier of the shortcut, by default ctrl is used
		shortcuts: [['bold'],['italic'],['underline'],['selectall','a'],['insertunorderedlist','\\']],
		setupDefaultShortcuts: function(){
			// summary: setup default shortcuts using Editor2 commands
			var exec = function(cmd){ return function(){ cmd.execute(); } };
//			if(!dojo.render.html.ie){
//				this.shortcuts.push(['redo','Z']);
//			}
			var self = this;
			dojo.lang.forEach(this.shortcuts, function(item){
				var cmd = self.getCommand(item[0]);
				if(cmd){
					self.addKeyHandler(item[1]?item[1]:item[0].charAt(0), item[2]==undefined?self.KEY_CTRL:item[2], exec(cmd));
				}
			});
//			this.addKeyHandler("s", ctrl, function () { this.save(true); });
		}
		/*,
		// FIXME: probably not needed any more with new design, but need to verify
		_save: function(e){
			// FIXME: how should this behave when there's a larger form in play?
			if(!this.isClosed){
				dojo.debug("save attempt");
				if(this.saveUrl.length){
					var content = {};
					content[this.saveArgName] = this.getEditorContent();
					dojo.io.bind({
						method: this.saveMethod,
						url: this.saveUrl,
						content: content
					});
				}else{
					dojo.debug("please set a saveUrl for the editor");
				}
				if(this.closeOnSave){
					this.close(e.getName().toLowerCase() == "save");
				}
			}
		}*/
	}
);