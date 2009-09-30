
if (typeof(admingui) == 'undefined') {
    admingui = {};
}
admingui.gadget = {
    noop: function() {
    },

    /**
     *	handler - The name of the handler to invoke.
     *	args - An object containing properties / values for the parameters.
     *	callback - A JS function that should be notified.
     */
    invoke: function(handler, args, callback) {
	if (typeof(callback) == 'undefined') {
	    callback = admingui.gadget.noop;
	}
	var button = document.getElementById("execHandler");
	var params = 'h=' + handler + '&a=';
	for (var param in args) {
	    // Create a String to represent all the parameters
	    // Double escape, this will prevent the server-side from fully
	    // urldecoding it.  Allowing me to first parse the commas, then
	    // decode the content.
	    // FIXME: I think 1 escape is all that is needed?? Test this.
	    params += param + ':' + escape(escape(args[param])) + ',';
	}
	DynaFaces.fireAjaxTransaction(
	    button,
	    {
		execute:    button.id,
		inputs:	    button.id,
		parameters: params,
		render:	    'gadgetResponse',
		onComplete: callback,
		// Experimental... this doesn't seem to cause problems in the
		// browser, despite the fact that I expected it to:
		asynchronous: false
	    });
	return false;
    },

    getResponse: function() {
	var resp = document.getElementById('gadgetResponse');
	var obj = eval('(' + resp.innerHTML + ')');
	return obj;
    }
};

if (typeof(gadgets) == 'undefined') {
    // FIXME: I have to solve how to make these functions appear synchronous
    // FIXME: when they are asynchronous when backed by Ajax
    gadgets = {
	Prefs: function(moduleId) {
	    if (typeof(moduleId) == 'undefined') {
		moduleId = 'default';
	    }
	    // outputs (i.e. "value") are not used, but must be supplied
	    this.prefsHandlerOpts = {root: '/glassfish/gadget/' + moduleId, value:'gadgetPrefs'}
	    this.moduleId = (typeof(moduleId) == 'undefined') ? 'GlassFish' : moduleId;
	    this.getArray =
		function(key) {
		    // invoke Ajax to get preference
		    this.prefsHandlerOpts.key = key;
		    admingui.gadget.invoke("getPreference", this.prefsHandlerOpts);
		    var resp = admingui.gadget.getResponse().value;
		    if (typeof(resp) != 'object') {
			// Array's show up as Objects...
			resp = [resp];
		    }
		    return resp;
		};
	    this.getBool =
		/* true iff preference == 'true' */
		function(key) {
		    // invoke Ajax to get preference
		    this.prefsHandlerOpts.key = key;
		    admingui.gadget.invoke("getPreference", this.prefsHandlerOpts);
		    var resp = admingui.gadget.getResponse().value;
		    resp = (resp == 'true');
		    return resp;
		};
	    this.getCountry =
		function() {
		    // FIXME: Not implemented!
		    return "US";
		};
	    this.getFloat =
		function(key) {
		    // invoke Ajax to get preference
		    this.prefsHandlerOpts.key = key;
		    admingui.gadget.invoke("getPreference", this.prefsHandlerOpts);
		    var resp = admingui.gadget.getResponse().value;
		    resp = parseFloat(resp);
		    return resp;
		};
	    this.getInt =
		function(key) {
		    // invoke Ajax to get preference
		    this.prefsHandlerOpts.key = key;
		    admingui.gadget.invoke("getPreference", this.prefsHandlerOpts);
		    var resp = admingui.gadget.getResponse().value;
		    resp = parseInt(resp);
		    alert(typeof(resp));
		    return resp;
		};
	    this.getLang =
		function() {
		    // FIXME: Not implemented!
		    return "en";
		};
	    this.getModuleId =
		function() {
		    return this.moduleId;
		};
	    this.getMsg =
		function(key) {
		    alert('Prefs.getMsg() tbd...');
		    // FIXME: TBD...
		    // invoke Ajax to get the message
		    return key;
		};
	    this.getString =
		function(key) {
		    // invoke Ajax to get preference
		    this.prefsHandlerOpts.key = key;
		    admingui.gadget.invoke("getPreference", this.prefsHandlerOpts);
		    var resp = admingui.gadget.getResponse().value;
		    return resp;
		};
	    this.set =
		function(key, val) {
		    // invoke Ajax to set preference
		    this.prefsHandlerOpts.key = key;
		    this.prefsHandlerOpts.value = val;
		    admingui.gadget.invoke("setPreference", this.prefsHandlerOpts);
		};
	    this.setArray =
		function(key, val) {
		    // invoke Ajax to get preference
		    if (typeof(val) != 'object') {
			val = [val];
		    }
		    this.prefsHandlerOpts.key = key;
		    this.prefsHandlerOpts.value = val;
		    admingui.gadget.invoke("setPreference", this.prefsHandlerOpts);
		};
	}
    };
}
