dojo.provide("dojo.widget.Toggler");
dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");

dojo.widget.defineWidget(
	"dojo.widget.Toggler",
	dojo.widget.HtmlWidget,
{
	// summary:
	//		clicking on this widget shows/hides another widget

	// targetId: String
	//		Id of widget to show/hide when this widget is clicked
	targetId: '',
	
	fillInTemplate: function() {
		dojo.event.connect(this.domNode, "onclick", this, "onClick");
	},
	
	onClick: function() {
		var pane = dojo.widget.byId(this.targetId);
		if(!pane){ return; }
		pane.explodeSrc = this.domNode;
		pane.toggleShowing();
	}
});
