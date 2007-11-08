dojo.provide("dojo.widget.demoEngine.DemoItem");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.i18n.common");
dojo.requireLocalization("dojo.widget", "DemoEngine");

dojo.widget.defineWidget("my.widget.demoEngine.DemoItem", 
	dojo.widget.HtmlWidget, 
	{
		templatePath: dojo.uri.moduleUri("dojo", "widget/demoEngine/templates/DemoItem.html"),
		templateCssPath: dojo.uri.moduleUri("dojo", "widget/demoEngine/templates/DemoItem.css"),
		postCreate: function() {
			dojo.html.addClass(this.domNode,this.domNodeClass);
			dojo.html.addClass(this.summaryBoxNode, this.summaryBoxClass);
			dojo.html.addClass(this.screenshotTdNode, this.screenshotTdClass);
			dojo.html.addClass(this.summaryContainerNode, this.summaryContainerClass);
			dojo.html.addClass(this.summaryNode, this.summaryClass);
			dojo.html.addClass(this.viewDemoLinkNode, this.viewDemoLinkClass);

			this.nameNode.appendChild(document.createTextNode(this.name));
			this.descriptionNode.appendChild(document.createTextNode(this.description));
			this.thumbnailImageNode.src = this.thumbnail;
			this.thumbnailImageNode.name=this.name;
			this.viewDemoImageNode.src = this.viewDemoImage;
			this.viewDemoImageNode.name=this.name;
		},
		onSelectDemo: function() {
			//Attach to this to do something when a demo is selected
		}
	},
	"",
	function() {
		this.demo = "";

		this.domNodeClass="demoItemWrapper";

		this.summaryBoxNode="";
		this.summaryBoxClass="demoItemSummaryBox";

		this.nameNode="";
		this.thumbnailImageNode="";
		this.viewDemoImageNode="";

		this.screenshotTdNode="";
		this.screenshotTdClass="demoItemScreenshot";

		this.summaryContainerNode="";
		this.summaryContainerClass="demoItemSummaryContainer";

		this.summaryNode="";
		this.summaryClass="demoItemSummary";

		this.viewDemoLinkNode="";
		this.viewDemoLinkClass="demoItemView";

		this.descriptionNode="";

		this.name="Some Demo";
		var bundle = dojo.i18n.getLocalization("dojo.widget", "DemoEngine");
		this.description=bundle.itemDescription;
		this.thumbnail="images/test_thumb.gif";
		this.viewDemoImage="images/viewDemo.png";
	}
);
