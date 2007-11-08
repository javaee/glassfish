
dojo.provide("dojo.widget.TreeExpandToNodeOnSelect");

dojo.require("dojo.widget.HtmlWidget");

/**
 * when a node is selected, expands tree to make it visible
 * useful for program expansion
 */
dojo.widget.defineWidget(
	"dojo.widget.TreeExpandToNodeOnSelect",
	dojo.widget.HtmlWidget,
{
	selector: "",
	controller: "",
	
	/**
	* if true, then selected node will be expanded too
	*/
	withSelected: false,
	
	initialize: function() {
		this.selector = dojo.widget.byId(this.selector);
		this.controller = dojo.widget.byId(this.controller);
		
		dojo.event.topic.subscribe(this.selector.eventNames.select, this, "onSelectEvent");	
	},

	
	onSelectEvent: function(message) {
		this.controller.expandToNode(message.node, this.withSelected)		
	}
	
	
	

});
