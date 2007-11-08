
dojo.provide("dojo.widget.TreeExtension");

dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.widget.TreeCommon");

dojo.widget.defineWidget(
	"dojo.widget.TreeExtension",
	[dojo.widget.HtmlWidget, dojo.widget.TreeCommon],
	function() {
		this.listenedTrees = {};
	},
	{}
);
