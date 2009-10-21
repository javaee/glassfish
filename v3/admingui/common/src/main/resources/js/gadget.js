
if (typeof(admingui) == 'undefined') {
    admingui = {};
}
admingui.gadget = {
    noop: function() {
    },

    setResponse: function(response, rawData) {
	admingui.gadget.response = response;
	admingui.gadget.responseRaw = rawData;
    },

    /**
     *	handler - The name of the handler to invoke.
     *	args - An object containing properties / values for the parameters.
     *	callback - A JS function that should be notified.
     */
    invoke: function(handler, args, callback) {
	if ((callback == null) || (typeof(callback) === 'undefined')) {
	    callback = admingui.gadget.setResponse;
	}
	//return window.top.admingui.ajax.invoke(handler, args, callback, 3, false);
	//For now pass in true (asynchronous) b/c JSF2 Ajax is broken
	window.top.admingui.ajax.invoke(handler, args, callback, 3, true);
	return false;
    },

    getResponse: function() {
	return admingui.gadget.response;
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
		    var resp = admingui.gadget.getResponse()
		    if (typeof(resp) != 'undefined') {
			resp = resp.value;
		    }
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
