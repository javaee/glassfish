dojo.provide("dojo.widget.Editor2Plugin.DialogCommands");
dojo.require("dojo.i18n.common");
dojo.requireLocalization("dojo.widget", "common");
dojo.requireLocalization("dojo.widget", "Editor2");

dojo.require("dojo.widget.FloatingPane");
dojo.widget.defineWidget(
	"dojo.widget.Editor2Dialog",
	[dojo.widget.HtmlWidget, dojo.widget.FloatingPaneBase, dojo.widget.ModalDialogBase],
	{
		// summary:
		//		Provides a Dialog which can be modal or normal for the Editor2.

		templatePath: dojo.uri.moduleUri("dojo", "widget/templates/Editor2/EditorDialog.html"),

		// modal: Boolean: Whether this is a modal dialog. True by default.
		modal: true,

//		refreshOnShow: true, //for debug for now

		// width: String: Width of the dialog. None by default.
		width: "",

		// height: String: Height of the dialog. None by default.
		height: "",

		// windowState: String: startup state of the dialog
		windowState: "minimized",

		displayCloseAction: true,

		// contentFile: String
		//	TODO
		contentFile: "",
		
		// contentClass: String
		//	TODO
		contentClass: "",

		fillInTemplate: function(args, frag){
			this.fillInFloatingPaneTemplate(args, frag);
			dojo.widget.Editor2Dialog.superclass.fillInTemplate.call(this, args, frag);
		},
		postCreate: function(){
			if(this.contentFile){
				dojo.require(this.contentFile);
			}

			with(this.domNode.style) {
				zIndex = 999;
				display = "none";
			}

			dojo.widget.FloatingPaneBase.prototype.postCreate.apply(this, arguments);
			dojo.widget.Editor2Dialog.superclass.postCreate.call(this);
			if(this.width && this.height){
				with(this.domNode.style){
					width = this.width;
					height = this.height;
				}
			}
		},
		createContent: function(){
			if(!this.contentWidget && this.contentClass){
				this.contentWidget = dojo.widget.createWidget(this.contentClass,{parent:this});
				//buggy IE: if the dialog is hidden, the button widgets
				//in the dialog can not be shown, so append this to
				//body first (off screen of course), and then add it back
				this.contentWidget.domNode.style.top="-10000px";
				this.contentWidget.domNode.style.position="absolute";
				dojo.body().appendChild(this.contentWidget.domNode);
				this.addChild(this.contentWidget);
				this.contentWidget.domNode.style.top="";
				this.contentWidget.domNode.style.position="";
			}
		},
		show: function(){
			if(!this.contentWidget){
				this.createContent();
			}

			if(!this.contentWidget || !this.contentWidget.loadContent()){
				return;
			}
			this.showFloatingPane();
			dojo.widget.Editor2Dialog.superclass.show.apply(this, arguments);
			if(this.modal){
				this.showModalDialog();
				//place the background div under this modal pane
				this.bg.style.zIndex = this.domNode.style.zIndex-1;
			}
		},
		onShow: function(){
			dojo.widget.Editor2Dialog.superclass.onShow.call(this);
			this.onFloatingPaneShow();
		},
		closeWindow: function(){
			this.hide();
			dojo.widget.Editor2Dialog.superclass.closeWindow.apply(this, arguments);
		},
		hide: function(){
			if(this.modal){
				this.hideModalDialog();
			}
			dojo.widget.Editor2Dialog.superclass.hide.call(this);
		},
		//modified from ModalDialogBase.checkSize to call _sizeBackground conditionally
		checkSize: function(){
			if(this.isShowing()){
				if(this.modal){
					this._sizeBackground();
				}
				this.placeModalDialog();
				this.onResized();
			}
		}
	}
);

dojo.widget.defineWidget(
	"dojo.widget.Editor2DialogContent",
	dojo.widget.HtmlWidget,
{
	// summary:
	//		dojo.widget.Editor2DialogContent is the actual content of a Editor2Dialog.
	//		This class should be subclassed to provide the content.

	widgetsInTemplate: true,

	postMixInProperties: function(){
		dojo.widget.HtmlWidget.superclass.postMixInProperties.apply(this, arguments);
		this.editorStrings = dojo.i18n.getLocalization("dojo.widget", "Editor2", this.lang);
		this.commonStrings = dojo.i18n.getLocalization("dojo.widget", "common", this.lang);
	},

	loadContent:function(){
		// summary: Load the content. Called by Editor2Dialog when first shown
		return true;
	},
	cancel: function(){
		// summary: Default handler when cancel button is clicked.
		this.parent.hide();
	}
});

dojo.lang.declare("dojo.widget.Editor2DialogCommand", dojo.widget.Editor2BrowserCommand,
	function(editor, name, dialogParas){
		// summary:
		//		Provides an easy way to popup a dialog when
		//		the command is executed.
		this.dialogParas = dialogParas;
	},
{
	execute: function(){
		if(!this.dialog){
			if(!this.dialogParas.contentFile || !this.dialogParas.contentClass){
				alert("contentFile and contentClass should be set for dojo.widget.Editor2DialogCommand.dialogParas!");
				return;
			}
			this.dialog = dojo.widget.createWidget(this.dialogParas.dialogClass?this.dialogParas.dialogClass:"Editor2Dialog", this.dialogParas);

			dojo.body().appendChild(this.dialog.domNode);

			dojo.event.connect(this, "destroy", this.dialog, "destroy");
		}
		this.dialog.show();
	},
	getText: function(){
		return this.dialogParas.title || dojo.widget.Editor2DialogCommand.superclass.getText.call(this);
	}
});