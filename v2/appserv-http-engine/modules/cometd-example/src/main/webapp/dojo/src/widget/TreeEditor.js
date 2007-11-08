dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.RichText");

dojo.provide("dojo.widget.TreeEditor");

dojo.widget.defineWidget(
	"dojo.widget.TreeEditor",
	dojo.widget.HtmlWidget,
{
	singleLineMode: false, // enter saves
	saveOnBlur: true, // blur or new edit saves current
	sync: false,  // finish editing in sync/async mode
	selectOnOpen: true,
	
	controller: null,
		
	node: null,
	
	richTextParams: {styleSheets: 'src/widget/templates/TreeEditor.css'},

	getContents: function() {
		return this.richText.getEditorContent();
	},
	
	open: function(node) {
		
		var self = this;
		var selectFunc = function(){
			if (self.selectOnOpen && self.richText.isLoaded) {
				self.richText.execCommand("selectall");
			}
		};
			
		if (!this.richText) {
			this.richText = dojo.widget.createWidget("RichText", this.richTextParams, node.labelNode);

			dojo.event.connect("around", this.richText, "onKeyDown", this, "richText_onKeyDown" );
			dojo.event.connect(this.richText, "onBlur", this, "richText_onBlur" );
			
			dojo.event.connect(this.richText, "onLoad", selectFunc );
		} else {
			this.richText.open(node.labelNode);
		}
		selectFunc();
		
		this.node = node;		
	},
	
	close: function(save) {
		
		this.richText.close(save);
		
		
		this.node = null;	
	},
	
	isClosed: function() {
		return !this.richText || this.richText.isClosed;
	},
	
	execCommand: function() {
		this.richText.execCommand.apply(this.richText, arguments);
	},
	
	richText_onKeyDown: function(invocation) {
		var e = invocation.args[0];
		if((!e)&&(this.object)) {
			e = dojo.event.browser.fixEvent(this.editor.window.event);
		}
		
		switch (e.keyCode) {
			case e.KEY_ESCAPE:
				this.finish(false);
				dojo.event.browser.stopEvent(e);		
				break;
			case e.KEY_ENTER:
				if( e.ctrlKey && !this.singleLineMode ) {
					this.execCommand( "inserthtml", "<br/>" );
							
				}
				else {
					this.finish(true);					
					//dojo.debug("finish");
				}
				dojo.event.browser.stopEvent(e);
				break;
			default:
				return invocation.proceed();
		}
	},
	
	richText_onBlur: function() {
		this.finish(this.saveOnBlur);
	},
	
	
	finish: function(save) {
		return this.controller.editLabelFinish(save, this.sync);
	}
		
		
	
});
