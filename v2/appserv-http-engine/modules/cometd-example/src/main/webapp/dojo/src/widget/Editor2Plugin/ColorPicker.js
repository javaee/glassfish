dojo.provide("dojo.widget.Editor2Plugin.ColorPicker")

dojo.require("dojo.widget.Editor2Plugin.DropDownList");
dojo.require("dojo.widget.ColorPalette");
dojo.declare("dojo.widget.Editor2ToolbarColorPaletteButton", dojo.widget.Editor2ToolbarDropDownButton, {
	// summary: dojo.widget.Editor2ToolbarColorPaletteButton provides a dropdown color palette picker

	onDropDownShown: function(){
		if(!this._colorpalette){
			this._colorpalette = dojo.widget.createWidget("ColorPalette", {});
			this._dropdown.addChild(this._colorpalette);

			this.disableSelection(this._dropdown.domNode);
			this.disableSelection(this._colorpalette.domNode);
			//do we need a destory to delete this._colorpalette manually?
			//I assume as it is added to this._dropdown via addChild, it
			//should be deleted when this._dropdown is destroyed

			dojo.event.connect(this._colorpalette, "onColorSelect", this, 'setColor');
			dojo.event.connect(this._dropdown, "open", this, 'latchToolbarItem');
			dojo.event.connect(this._dropdown, "close", this, 'enableToolbarItem');
		}
	},
	enableToolbarItem: function(){
		dojo.widget.Editor2ToolbarButton.prototype.enableToolbarItem.call(this);
	},

	disableToolbarItem: function(){
		dojo.widget.Editor2ToolbarButton.prototype.disableToolbarItem.call(this);
	},
	setColor: function(color){
		this._dropdown.close();
		var curInst = dojo.widget.Editor2Manager.getCurrentInstance();
		if(curInst){
			var _command = curInst.getCommand(this._name);
			if(_command){
				_command.execute(color);
			}
		}
	}
});