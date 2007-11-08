dojo.provide("dojo.widget.ResizableTextarea");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.LayoutContainer");
dojo.require("dojo.widget.ResizeHandle");
dojo.require("dojo.i18n.common");
dojo.requireLocalization("dojo.widget", "ResizableTextarea");

dojo.widget.defineWidget(
	"dojo.widget.ResizableTextarea",
	dojo.widget.HtmlWidget,
{
	// summary
	//	A resizable textarea.
	//	Takes all the parameters (name, value, etc.) that a vanilla textarea takes.
	// usage
	//	<textarea dojoType="ResizableTextArea">...</textarea>

	templatePath: dojo.uri.moduleUri("dojo", "widget/templates/ResizableTextarea.html"),
	templateCssPath: dojo.uri.moduleUri("dojo", "widget/templates/ResizableTextarea.css"),

	postMixInProperties: function(){
		dojo.widget.HtmlWidget.superclass.postMixInProperties.apply(this, arguments);
		this.messages = dojo.i18n.getLocalization("dojo.widget", "ResizableTextarea", this.lang);
	},

	fillInTemplate: function(args, frag){
		this.textAreaNode = this.getFragNodeRef(frag).cloneNode(true);

		// FIXME: Safari apparently needs this!
		dojo.body().appendChild(this.domNode);

		this.rootLayout = dojo.widget.createWidget(
			"LayoutContainer",
			{
				minHeight: 50,
				minWidth: 100
			},
			this.rootLayoutNode
		);

		// TODO: all this code should be replaced with a template
		// (especially now that templates can contain subwidgets)
		this.textAreaContainer = dojo.widget.createWidget(
			"LayoutContainer",
			{ layoutAlign: "client" },
			this.textAreaContainerNode
		);
		this.rootLayout.addChild(this.textAreaContainer);

		this.textAreaContainer.domNode.appendChild(this.textAreaNode);
		with(this.textAreaNode.style){
			width="100%";
			height="100%";
		}

		this.statusBar = dojo.widget.createWidget(
			"LayoutContainer",
			{ 
				layoutAlign: "bottom", 
				minHeight: 28
			},
			this.statusBarContainerNode
		);
		this.rootLayout.addChild(this.statusBar);

		this.statusLabel = dojo.widget.createWidget(
			"LayoutContainer",
			{ 
				layoutAlign: "client", 
				minWidth: 50
			},
			this.statusLabelNode
		);
		this.statusBar.addChild(this.statusLabel);

		this.resizeHandle = dojo.widget.createWidget(
			"ResizeHandle", 
			{ targetElmId: this.rootLayout.widgetId },
			this.resizeHandleNode
		);
		this.statusBar.addChild(this.resizeHandle);
	}
});
