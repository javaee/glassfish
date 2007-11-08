dojo.provide("dojo.widget.demoEngine.DemoPane");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");

dojo.widget.defineWidget("my.widget.demoEngine.DemoPane", 
	dojo.widget.HtmlWidget, 
	{
		templatePath: dojo.uri.moduleUri("dojo", "widget/demoEngine/templates/DemoPane.html"),
		templateCssPath: dojo.uri.moduleUri("dojo", "widget/demoEngine/templates/DemoPane.css"),
		postCreate: function() {
			dojo.html.addClass(this.domNode,this.domNodeClass);
			dojo.debug("PostCreate");
			this._launchDemo();
		},
		
		_launchDemo: function() {
			dojo.debug("Launching Demo");
			dojo.debug(this.demoNode);
			this.demoNode.src=this.href;
		},

		setHref: function(url) {
			this.href = url;
			this._launchDemo();
		}
	},
	"",
	function() {
		dojo.debug("DemoPane Init");
		this.domNodeClass="demoPane";
		this.demoNode = "";
		this.href = "";
	}
);
