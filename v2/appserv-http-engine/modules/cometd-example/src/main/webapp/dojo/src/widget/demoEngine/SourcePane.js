dojo.provide("dojo.widget.demoEngine.SourcePane");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.io.*");

dojo.widget.defineWidget("my.widget.demoEngine.SourcePane", 
	dojo.widget.HtmlWidget, 
	{
		templatePath: dojo.uri.moduleUri("dojo", "widget/demoEngine/templates/SourcePane.html"),
		templateCssPath: dojo.uri.moduleUri("dojo", "widget/demoEngine/templates/SourcePane.css"),
		postCreate: function() {
			dojo.html.addClass(this.domNode,this.domNodeClass);
			dojo.debug("PostCreate");
		},
	
		getSource: function() {
			if (this.href) {
				dojo.io.bind({
					url: this.href,
					load: dojo.lang.hitch(this, "fillInSource"),
					mimetype: "text/plain"
				});
			}
		},	

		fillInSource: function(type, source, e) {
			this.sourceNode.value=source;
		},

		setHref: function(url) {
			this.href = url;
			this.getSource();
		}
	},
	"",
	function() {
		dojo.debug("SourcePane Init");
		this.domNodeClass="sourcePane";
		this.sourceNode = "";
		this.href = "";
	}
);
