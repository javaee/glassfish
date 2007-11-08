dojo.provide("dojo.lfx.toggler");
dojo.require("dojo.lfx.*");
dojo.require("dojo.lang.common");

dojo.lfx.toggler.plain = function(){
	// summary: implements a toggler interface using simple "show/hide a node"
	//		functionality
	this.stop = function(){
		// summary: stops animation, if it was active (ignored)
	};
	this.show = function(node, duration, easing, callback){
		// summary: shows a node
		// description: ignores all animation settings, shows node instantaniously
		// node: DOMNode: a node to be shown
		// duration: Integer: duration of the animation in milliseconds (ignored)
		// easing: Function: an easing function or null (ignored)
		// callback: Function: a function to be called when animation is finished
		dojo.html.show(node);
		if(dojo.lang.isFunction(callback)){ callback(); }
	};
	this.hide = function(node, duration, easing, callback){
		// summary: hides a node
		// description: ignores all animation settings, hides node instantaniously
		// node: DOMNode: a node to be hidden
		// duration: Integer: duration of the animation in milliseconds (ignored)
		// easing: Function: an easing function or null (ignored)
		// callback: Function: a function to be called when animation is finished
		dojo.html.hide(node);
		if(dojo.lang.isFunction(callback)){ callback(); }
	};
};

dojo.lfx.toggler.common = {
	// summary: implements common methods of a toggler interface
	stop: function() {
		// summary: stops animation, if it was active
		if(this.anim && this.anim.status() != "stopped"){
			this.anim.stop();
		}
	},
	_act: function(action, node, duration, easing, callback, explodeSrc){
		// summary: launches an animation
		// node: DOMNode: a node to be animated
		// duration: Integer: duration of the animation in milliseconds
		// easing: Function: an easing function or null
		// callback: Function: a function to be called when animation is finished
		// explodeSrc: DOMNode: a reference node for some animations (like explode/implode)
		this.stop();
		this.anim = dojo.lfx[action](node, duration, easing, callback).play();
	},
	show: function(node, duration, easing, callback, explodeSrc){
		// summary: shows a node using "show" animation
		// node: DOMNode: a node to be animated
		// duration: Integer: duration of the animation in milliseconds
		// easing: Function: an easing function or null
		// callback: Function: a function to be called when animation is finished
		// explodeSrc: DOMNode: a reference node for some animations (like explode/implode)
		this._act(this.show_action, node, duration, easing, callback, explodeSrc);
	},
	hide: function(node, duration, easing, callback, explodeSrc){
		// summary: hides a node using "hide" animation
		// node: DOMNode: a node to be animated
		// duration: Integer: duration of the animation in milliseconds
		// easing: Function: an easing function or null
		// callback: Function: a function to be called when animation is finished
		// explodeSrc: DOMNode: a reference node for some animations (like explode/implode)
		this._act(this.hide_action, node, duration, easing, callback, explodeSrc);
	}
};

dojo.lfx.toggler.fade = function(){
	// summary: fadeShow/fadeHide toggler
	this.anim = null;
	this.show_action = "fadeShow";
	this.hide_action = "fadeHide";
};
dojo.extend(dojo.lfx.toggler.fade, dojo.lfx.toggler.common);

dojo.lfx.toggler.wipe = function(){
	// summary: wipeIn/wipeOut toggler
	this.anim = null;
	this.show_action = "wipeIn";
	this.hide_action = "wipeOut";
};
dojo.extend(dojo.lfx.toggler.wipe, dojo.lfx.toggler.common);

dojo.lfx.toggler.explode = function(){
	// summary: explode/implode toggler
	this.anim = null;
	this.show_action = "explode";
	this.hide_action = "implode";
	this.show = function(node, duration, easing, callback, explodeSrc){
		// summary: specialized implementation to "explode" a node
		// node: DOMNode: a node to be animated
		// duration: Integer: duration of the animation in milliseconds
		// easing: Function: an easing function or null
		// callback: Function: a function to be called when animation is finished
		// explodeSrc: DOMNode: a start reference node for explode
		this.stop();
		this.anim = dojo.lfx.explode(explodeSrc||{x:0,y:0,width:0,height:0}, node, duration, easing, callback).play();
	};
	this.hide = function(node, duration, easing, callback, explodeSrc){
		// summary: specialized implementation to "implode" a node
		// node: DOMNode: a node to be animated
		// duration: Integer: duration of the animation in milliseconds
		// easing: Function: an easing function or null
		// callback: Function: a function to be called when animation is finished
		// explodeSrc: DOMNode: an end reference node for implode
		this.stop();
		this.anim = dojo.lfx.implode(node, explodeSrc||{x:0,y:0,width:0,height:0}, duration, easing, callback).play();
	};
};
dojo.extend(dojo.lfx.toggler.explode, dojo.lfx.toggler.common);
