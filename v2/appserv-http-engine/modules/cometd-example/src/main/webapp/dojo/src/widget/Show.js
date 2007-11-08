dojo.provide("dojo.widget.Show");

dojo.require("dojo.lang.common");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.uri.Uri");
dojo.require("dojo.event.*");
dojo.require("dojo.lfx.*");
dojo.require("dojo.math.curves");
dojo.require("dojo.lang.common");
dojo.require("dojo.lang.func");
dojo.require("dojo.html.display");
dojo.require("dojo.html.layout");
//dojo.require("dojo.animation.Animation");
dojo.require("dojo.gfx.color");

dojo.widget.defineWidget(
	"dojo.widget.Show",
	dojo.widget.HtmlWidget,
	function(){
		this._slides=[];
	},
	{
	isContainer: true,
	_slide: -1,

	body: null,
	nav: null,
	hider: null,
	select: null,
	option: null,
	inNav: false,
	debugPane: null,
	noClick: false,
	templatePath: dojo.uri.moduleUri("dojo", "widget/templates/Show.html"),
	templateCssPath: dojo.uri.moduleUri("dojo", "widget/templates/Show.css"),
	fillInTemplate: function(args, frag){
		if(args.debugPane){
			var dp = this.debugPane = dojo.widget.byId(args.debugPane);
			dp.hide();
			dojo.event.connect(dp, "closeWindow", dojo.lang.hitch(this, function(){ this.debugPane = false; }));
		}
		var source = this.getFragNodeRef(frag);
		this.sourceNode = dojo.body().appendChild(source.cloneNode(true));
		for(var i = 0, child; child = this.sourceNode.childNodes[i]; i++){
			if(	(child.tagName) && 
				(child.elementType == 1) &&
				(child.getAttribute("dojotype").toLowerCase() == "showslide")){
				child.className = "dojoShowPrintSlide";
				child.innerHTML = "<h1>" + child.title + "</h1>" + child.innerHTML;
			}
		}
		this.sourceNode.className = "dojoShowPrint";
		this.sourceNode.style.display = "none";
		
		dojo.event.connect(document, "onclick", this, "gotoSlideByEvent");
		if(dojo.render.html.ie) {
			dojo.event.connect(document,"onkeydown",this, "gotoSlideByEvent");
		} else {
			// while keydown works, keypress allows rapid successive key presses
			// to be handled correctly
			dojo.event.connect(document,"onkeypress",this, "gotoSlideByEvent");
		}
		dojo.event.connect(window, "onresize", this, "resizeWindow");
		dojo.event.connect(this.nav, "onmousemove", this, "popUpNav");
	},
	postCreate: function(){
		this._slides = [];
		for(var i = 0, child; child = this.children[i]; i++){
			if(child.widgetType == "ShowSlide"){
				this._slides.push(child);
				this.option.text = child.title+" ("+(i+1)+")";
				this.option.parentNode.insertBefore(this.option.cloneNode(true), this.option);
			}
		}
		this.option.parentNode.removeChild(this.option);
		this.domNode.style.display = "block";
		this.resizeWindow();

		this.gotoSlide(0, true);

		// check to see if we're initialized from a particular slide
		dojo.addOnLoad(dojo.lang.hitch(this, 
			function(){
				var th = window.location.hash;
				if(th.length){
					var parts = (""+window.location).split(this.widgetId+"_SlideNo_");
					if(parts.length > 1){
						setTimeout(dojo.lang.hitch(this, function(){
							this.gotoSlide(parseInt(parts[1]), true);
						}), 300);
					}
				}
			})
		);
	},
	gotoSlide: function(/*int*/ slide, /*Boolean*/preventSetHash){
		if(slide == this._slide){
			return;
		}

		if(!this._slides[slide]){
			// slide: string
			for(var i = 0, child; child = this._slides[i]; i++){
				if(child.title == slide){
					slide = i;
					break;
				}
			}
		}
		
		if(!this._slides[slide]){
			return;
		}

		if(this.debugPane){
			if(this._slides[slide].debug){
				this.debugPane.show();
			}else{
				this.debugPane.hide();
			}
		}
		
		if(this._slide != -1){
			while(this._slides[this._slide].previousAction()){}
		}

		if(!preventSetHash){
			window.location.href = "#"+this.widgetId+"_SlideNo_"+slide;
		}
		if(this._slides[this._slide]){
			this._slides[this._slide].hide();
		}
		
		this._slide = slide;
		this.select.selectedIndex = slide;
		var cn = this.contentNode;
		while(cn.firstChild){ cn.removeChild(cn.firstChild); }
		cn.appendChild(this._slides[slide].domNode);
		this._slides[slide].show();
	},
	gotoSlideByEvent: function(/*Event*/ event){
		var node = event.target;
		var type = event.type;
		if(type == "click"){
			if(node.tagName == "OPTION" && node.parentNode == this.select){
				this.gotoSlide(node.index);
			}else if(node == this.select){
				this.gotoSlide(node.selectedIndex);
			}else{
				this.nextSlide(event);
			}
		}else if (type=="keydown" || type=="keypress") {
			var key = event.keyCode;
			var ch = event.charCode;
			if(key == 63234 || key == 37){
				this.previousSlide(event);
			}else if(key == 63235 || key == 39 || ch == 32){
				this.nextSlide(event);
			}
		}
	},
	nextSlide: function(/*Event?*/ event){
		if(!this.stopEvent(event)){
			return false;
		}
		if(!this._slides[this._slide].nextAction(event)){
			if((this._slide + 1) != this._slides.length){
				this.gotoSlide(this._slide + 1);
				return true; // boolean
			}
			return false; // boolean
		}
	},
	previousSlide: function(/*Event?*/ event){
		if(!this.stopEvent(event)){
			return false;
		}
		if(!this._slides[this._slide].previousAction(event)){
			if((this._slide - 1) != -1){
				this.gotoSlide(this._slide - 1);
				return true; // boolean
			}
			return false; // boolean
		}
	},

	stopEvent: function(/*Event*/ ev){
		if(!ev){
			return true;
		}
	
		if (ev.type == "click" && (this._slides[this._slide].noClick || this.noClick)) {
			return false;
		}	
		var target = ev.target;
		// Check to see if the target is below the show domNode
		while(target != null){
			if(target == this.domNode){
				target = ev.target;
				break;
			}
			target = target.parentNode;
		}
		// Now that we know it's below this widget's domNode, we bubble up until we get to our domNode
		if(!dojo.dom.isDescendantOf(target, this.nav)){
			while(target && target != this.domNode){
				if(target.tagName == "A" || target.tagName == "INPUT" || target.tagName == "TEXTAREA" || target.tagName == "SELECT"){
					return false;
				}
				if(typeof target.onclick == "function" || typeof target.onkeypress == "function"){
					return false;
				}
				target = target.parentNode;
			}
		}
		
		if(window.event){
			ev.returnValue = false;
			ev.cancelBubble = true;
		}else{
			ev.preventDefault();
			ev.stopPropagation();
		}
		
		return true;
	},
	popUpNav: function(){
		if(!this.inNav){
			// dojo.widget.Show.node = this.nav;
			dojo.lfx.propertyAnimation(this.nav, {
				"height": { start: 5, end: 30 }
			}, 250).play();
		}
		clearTimeout(this.inNav);
		this.inNav = setTimeout(dojo.lang.hitch(this, "hideNav"), 2000);
	},
	hideNav: function(){
		clearTimeout(this.inNav);
		this.inNav = false;
		// dojo.widget.Show.node = this.nav;
		dojo.lfx.propertyAnimation(this.nav, {
			"height": { start: 30, end: 5 }
		}, 250).play();
	},
	resizeWindow: function(/*Event*/ ev){
		dojo.body().style.height = "auto";
		var h = Math.max(
			document.documentElement.scrollHeight || dojo.body().scrollHeight,
			dojo.html.getViewport().height);
		dojo.body().style.height = h + "px";
	}
});

dojo.widget.defineWidget(
	"dojo.widget.ShowAction",
	dojo.widget.HtmlWidget,
{
	on: "",
	action: "fade",
	duration: 350,
	from: "",
	to: "",
	auto: "false",
	postMixInProperties: function(){ 
		// fix for very strange Opera 9 bug
		if(dojo.render.html.opera){
			this.action = this.action.split("/").pop();
		}
	}
});

dojo.widget.defineWidget(
	"dojo.widget.ShowSlide",
	dojo.widget.HtmlWidget,
{
	title: "",
	_action: -1,
	isContainer: true,
	_components: {},
	_actions: [],

	gotoAction: function(/*int*/ action){
		this._action = action;
	},
	_nextAction: function(/*Event?*/ event){
		if((this._action + 1) != this._actions.length){
			++this._action;
			return true; // boolean
		}
		return false; // boolean
	},
	_previousAction: function(/*Event?*/ event){
		if((this._action - 1) != -1){
			--this._action;
			return true; // boolean
		}
		return false; // boolean
	},

	htmlTitle: null,
	debug: false,
	noClick: false,
	templatePath: dojo.uri.moduleUri("dojo", "widget/templates/ShowSlide.html"),
	templateCssPath: dojo.uri.moduleUri("dojo", "widget/templates/ShowSlide.css"),
	postCreate: function(){
		this.htmlTitle.innerHTML = this.title;

		var actions = this.getChildrenOfType("ShowAction", false);
		var atypes = {};
		dojo.lang.forEach(actions, function(act){ atypes[act.on] = true; });

		this._components = {};
		var cn = this.containerNode;
		var nodes = dojo.render.html.ie ? cn.all : cn.getElementsByTagName('*');
		dojo.lang.forEach(nodes, function(node){
			var as = node.getAttribute("as");
			if(as){
				if(!this._components[as]){
					this._components[as] = [];
				}
				this._components[as].push(node);
				if(!atypes[as]){
					var tmpAction = dojo.widget.createWidget("ShowAction", { on: as });
					this.addChild(tmpAction);
					atypes[as] = true;
				}
			}
		}, this);

		this._actions = [];
		actions = this.getChildrenOfType("ShowAction", false);
		dojo.lang.forEach(actions, function(child){
			this._actions.push(child);
			var components = this._components[child.on];
			for(var j = 0, component; component = components[j]; j++){
				if(child["action"] && (
					(child.action != "remove")&&
					(child.action != "fadeout")&&
					(child.action != "wipeout")
				) ){
					this.hideComponent(component);
				}
			}
		}, this);
	},

	previousAction: function(/*Event?*/ event){
		if(!this.parent.stopEvent(event)){
			return false;
		}

		var action = this._actions[this._action];
		if(!action){
			return false;
		}

		var on = action.on;
		while(action.on == on){
			var components = this._components[on];
			for(var i = 0, component; component = components[i]; i++){
				if(
					(action.action == "remove")||
					(action.action == "fadeout")||
					(action.action == "wipeout")
				){
					if(component.style.display == "none"){
						component.style.display = "";
						component.style.visibility = "visible";
						var exits = true;
					}
					dojo.html.setOpacity(component, 1);
				}else if(action.action){
					this.hideComponent(component);
				}
			}

			--this._action;

			if(exits){
				return true;
			}	

			if(action.auto == "true"){
				on = this._actions[this._action].on;
			}

			action = this._actions[this._action];
			if(!action){
				return false;
			}
		}
		return true;
	},
	hideComponent: function(/*Node*/ component){
		component.style.visibility = "hidden";
		component.style.backgroundColor = "transparent";
		var parent = component.parentNode;
		if((parent)&&(parent.tagName.toLowerCase() == "li")){
			parent.oldType = parent.style.listStyleType;
			parent.style.listStyleType = "none";
		}
	},
	nextAction: function(/*Event?*/ event){
		if(!this.parent.stopEvent(event)){
			return false;
		}

		if(!this._nextAction(this)){
			return false;
		}

		var action = this._actions[this._action];
		if(!action){
			return false;
		}
		var tmpAction = action["action"];
		
		var components = this._components[action.on];
		for(var i = 0, component; component = components[i]; i++){
			if(tmpAction){
				var duration = action.duration || 1000;
				if((tmpAction == "fade")||(tmpAction == "fadeIn")){
					dojo.html.setOpacity(component, 0);
					dojo.lfx.html.fadeShow(component, duration).play(true);
				}else if(tmpAction == "fadeout"){
					dojo.lfx.html.fadeHide(component, duration).play(true);
				}else if(tmpAction == "fly"){
					var width = dojo.html.getMarginBox(component).width;
					var position = dojo.html.getAbsolutePosition(component);
					// alert(position);
					component.style.position = "relative";
					component.style.left = -(width + position.x) + "px";
					dojo.lfx.html.slideBy(component, { top: 0, left: (width + position.x)}, duration, -1, this.callWith).play(true);
				}else if((tmpAction == "wipe")||(tmpAction == "wipein")){
					dojo.lfx.html.wipeIn(component, duration).play();
				}else if(tmpAction == "wipeout"){
					dojo.lfx.html.wipeOut(component, duration).play();
				}else if(tmpAction == "color"){
					var from = new dojo.gfx.color.Color(action.from).toRgb();
					var to = new dojo.gfx.color.Color(action.to).toRgb();
					var anim = new dojo.animation.Animation(new dojo.math.curves.Line(from, to), duration, 0);
					var node = component;
					dojo.event.connect(anim, "onAnimate", function(e) {
						node.style.color = "rgb(" + e.coordsAsInts().join(",") + ")";
					});
					anim.play(true);
				}else if(tmpAction == "bgcolor"){
					dojo.lfx.html.unhighlight(component, action.to, duration).play();
				}else if(tmpAction == "remove"){
					component.style.display = "none";
				}

				if(tmpAction == "hide"){
					component.style.visibility = "hidden";
				}else{
					component.style.visibility = "visible";
				}
			}
		}
		
		action = this._actions[this._action + 1];
		if(action && action.auto == "true"){
			this.nextAction();
		}

		return true;
	},
	callWith: function(/*Node*/ node){
		if(!node){ return; }
		if(dojo.lang.isArray(node)){
			dojo.lang.forEach(node, arguments.callee);
			return;
		}
		var parent = node.parentNode;
		if((parent)&&(parent.tagName.toLowerCase() == "li")){
			parent.style.listStyleType = parent.oldType;
		}
	}
});
