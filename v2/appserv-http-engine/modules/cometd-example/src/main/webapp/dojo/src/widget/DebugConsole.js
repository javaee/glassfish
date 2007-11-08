dojo.provide("dojo.widget.DebugConsole");
dojo.require("dojo.widget.Widget");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.FloatingPane");

dojo.widget.defineWidget(
	"dojo.widget.DebugConsole",
	dojo.widget.FloatingPane,
{
	// summary: opens a floating pane that collects and display debug messages (from dojo.debug(), etc.)

	fillInTemplate: function() {
		dojo.widget.DebugConsole.superclass.fillInTemplate.apply(this, arguments);
		this.containerNode.id = "debugConsoleClientPane";
		djConfig.isDebug = true;
		djConfig.debugContainerId = this.containerNode.id;
	}
});
