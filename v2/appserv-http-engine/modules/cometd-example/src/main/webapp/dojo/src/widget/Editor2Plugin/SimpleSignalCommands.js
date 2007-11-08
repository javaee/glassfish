/*
 * This plugin adds save() and insertImage() to Editor2 widget, and two commands for each
 * of them. When the corresponding button is clicked in the toolbar, the added function in the
 * Editor2 widget is called. This mimics the original Editor2 behavior. If you want to have other
 * signals on the Editor2 widget, add them to dojo.widget.Editor2Plugin.SimpleSignalCommands.signals
 * NOTE: Please consider writing your own Editor2 plugin rather than using this backward compatible
 * plugin
 * ATTENTION: This plugin overwrites the new built-in insertImage dialog. (If this is not desired, set
 * dojo.widget.Editor2Plugin.SimpleSignalCommands.signals to not contain insertImage)
 */

//uncomment this line to add save only (do not overwrite the new built-in insertImage dialog
//this line should present before require dojo.widget.Editor2Plugin.SimpleSignalCommands
//dojo.widget.Editor2Plugin['SimpleSignalCommands'] = {signals: ['save']};

dojo.provide("dojo.widget.Editor2Plugin.SimpleSignalCommands");

dojo.require("dojo.widget.Editor2");

dojo.declare("dojo.widget.Editor2Plugin.SimpleSignalCommand", dojo.widget.Editor2Command,
	function(editor, name){
		if(dojo.widget.Editor2.prototype[name] == undefined){
			dojo.widget.Editor2.prototype[name] = function(){ /*dojo.debug("Editor2::"+name);*/ };
		}
	},
{
	execute: function(){
		this._editor[this._name]();
	}
});

if(dojo.widget.Editor2Plugin['SimpleSignalCommands']){
	dojo.widget.Editor2Plugin['_SimpleSignalCommands']=dojo.widget.Editor2Plugin['SimpleSignalCommands'];
}

dojo.widget.Editor2Plugin.SimpleSignalCommands = {
	signals: ['save', 'insertImage'],
	Handler: function(name){
		if(name == 'save'){
			return new dojo.widget.Editor2ToolbarButton('Save');
		}else if(name == 'insertimage'){
			return new dojo.widget.Editor2ToolbarButton('InsertImage');
		}
	},
	getCommand: function(editor, name){
		var signal;
		dojo.lang.every(this.signals,function(s){
			if(s.toLowerCase() == name){
				signal = s;
				return false;
			}
			return true;
		});
		if(signal){
			return new dojo.widget.Editor2Plugin.SimpleSignalCommand(editor, signal);
		}
	}
};

if(dojo.widget.Editor2Plugin['_SimpleSignalCommands']){
	dojo.lang.mixin(dojo.widget.Editor2Plugin.SimpleSignalCommands, dojo.widget.Editor2Plugin['_SimpleSignalCommands']);
}
dojo.widget.Editor2Manager.registerHandler(dojo.widget.Editor2Plugin.SimpleSignalCommands, 'getCommand');
dojo.widget.Editor2ToolbarItemManager.registerHandler(dojo.widget.Editor2Plugin.SimpleSignalCommands.Handler);