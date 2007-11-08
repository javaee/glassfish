dojo.provide("dojo.lfx.toggle");
dojo.require("dojo.lfx.*");

dojo.lfx.toggle.plain = {
	show: function(node, duration, easing, callback){
		dojo.html.show(node);
		if(dojo.lang.isFunction(callback)){ callback(); }
		return null;
	},
	
	hide: function(node, duration, easing, callback){
		dojo.html.hide(node);
		if(dojo.lang.isFunction(callback)){ callback(); }
		return null;
	}
}

dojo.lfx.toggle.fade = {
	show: function(node, duration, easing, callback){
		return dojo.lfx.fadeShow(node, duration, easing, callback).play();
	},

	hide: function(node, duration, easing, callback){
		return dojo.lfx.fadeHide(node, duration, easing, callback).play();
	}
}

dojo.lfx.toggle.wipe = {
	show: function(node, duration, easing, callback){
		return dojo.lfx.wipeIn(node, duration, easing, callback).play();
	},

	hide: function(node, duration, easing, callback){
		return dojo.lfx.wipeOut(node, duration, easing, callback).play();
	}
}

dojo.lfx.toggle.explode = {
	show: function(node, duration, easing, callback, explodeSrc){
		return dojo.lfx.explode(explodeSrc||{x:0,y:0,width:0,height:0}, node, duration, easing, callback).play();
	},

	hide: function(node, duration, easing, callback, explodeSrc){
		return dojo.lfx.implode(node, explodeSrc||{x:0,y:0,width:0,height:0}, duration, easing, callback).play();
	}
}
