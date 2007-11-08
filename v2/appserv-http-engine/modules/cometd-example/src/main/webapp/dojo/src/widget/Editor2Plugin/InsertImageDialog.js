dojo.provide("dojo.widget.Editor2Plugin.InsertImageDialog");

dojo.widget.defineWidget(
	"dojo.widget.Editor2InsertImageDialog",
	dojo.widget.Editor2DialogContent,
{
	templatePath: dojo.uri.moduleUri("dojo", "widget/templates/Editor2/Dialog/insertimage.html"),

	editableAttributes: ['src', 'alt', 'width', 'height', 'hspace', 'vspace', 'border', 'align'],
	loadContent: function(){
		var curInst = dojo.widget.Editor2Manager.getCurrentInstance();
		this.imageNode = dojo.withGlobal(curInst.window, "getSelectedElement", dojo.html.selection);
		if(!this.imageNode){
			this.imageNode = dojo.withGlobal(curInst.window, "getAncestorElement", dojo.html.selection, ['img']);
		}
		var imageAttributes = {};
		this.extraAttribText = "";
		if(this.imageNode){
			var attrs = this.imageNode.attributes;
			for(var i=0; i<attrs.length; i++) {
				if(dojo.lang.find(this.editableAttributes, attrs[i].name.toLowerCase())>-1){
					imageAttributes[attrs[i].name] = attrs[i].value;
				}else{
					this.extraAttribText += attrs[i].name + '="'+attrs[i].value+'" ';
				}
			}
		}
		for(var i=0; i<this.editableAttributes.length; ++i){
			name = this.editableAttributes[i];
			this["image_"+name].value = (imageAttributes[name] == undefined) ? "" : imageAttributes[name] ;
		}
		return true;
	},
	ok: function(){
		var curInst = dojo.widget.Editor2Manager.getCurrentInstance();
		var insertcmd = curInst.getCommand('inserthtml');
		var option = 0;

		var attstr='';
		for(var i=0; i<this.editableAttributes.length; ++i){
			name = this.editableAttributes[i];
			var value = this["image_"+name].value;
			if(value.length > 0){
				attstr += name + '="'+value+'" ';
			}
		}
		if(this.imageNode){
			dojo.withGlobal(curInst.window, "selectElement", dojo.html.selection, [this.imageNode]);
		}
		insertcmd.execute('<img '+attstr+this.extraAttribText+'/>');

		this.cancel();
	}
});