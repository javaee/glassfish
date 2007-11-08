var _globalScope = this;
if (!jmaki) {
    var jmaki = new Jmaki();  
}
if (!jmaki.widgets) {
    jmaki.widgets = {};
}

function Jmaki() {
    this.version = '.9';
    this.debug = true;
    var self = this;
    var libraries = [];
    var widgets = [];
    this.loaded = false;
    this.initialized = false;
    this.webRoot = "";
    this.resourcesRoot = "resources";
    
    this.Map = function() {
        /**
         * This class provides a hash table like utility
         */
        var map = {};
        /**
         * Get a list of the keys to check
         */
        this.keys = function() {
            var o = {};
            var _keys = [];
            
            for (var _i in map){
                // make sure we don't return prototype properties.
                if (typeof o[_i] == 'undefined') _keys.push(_i);
            }
            return _keys;
        }
        /**
         * Put stores the value in the table
         * @param key the index in the table where the value will be stored
         * @param value the value to be stored 
         */
        this.put = function(key,value) {
            map[key] = value;
        }
        
        /**
         * Return the value stored in the table
         * @param key the index of the value to retrieve
         */
        this.get = function(key) {
            return map[key];
        }
        
        /**
         * Remove the value from the table
         * @param key the index of the value to be removed
         */
        this.remove =  function(key) {
            delete map[key];
        }
        /**
         *  Clear the table
         */
        this.clear = function() {
            delete map;
            map = {};
        }
    }
    
    this.attributes = new this.Map();	
	var topics = new this.Map();

    /**
     * Subscribe to a new topic
     * @param name Name of the topic to subscribe to
     * @param listener Callback listener
     */
    this.subscribe = function(name, _l) {
        var topic = topics.get(name);
        // create the topic if it has not been created yet
        if (!topic) {
            topic = [];
            topics.put(name, topic);
        }
        // make sure that a listener is only added once
        for (var i in topic) {
            if (i == _l) {
                return;
            }
        }
        topic.push(_l);
    }
	
    /**
     *  Unsubscribe a listener from a topic
     *  @param name Name of the topic 
     *  @param listener 
     */
    this.unsubscribe = function(name, listener) {
        var topic = topics.get(name);
        // create the topic if it has not been created yet
        if (topic) {
            for (var i = 0; i < topic.length; i++) {
                if (topic[i] == listener) {
                    topic.splice(i,1);
                    break;
                }
            }
        }
    }  
	
    /**
     *  Publish an event to a topic
     *  @param name of the topic to be published to
     *  @param args any object
     */
    this.publish = function(name, args, bubbleDown, bubbleUp) {
        if (typeof name == 'undefined' || typeof args == 'undefined') return;

        var topic = topics.get(name);
        // create the topic if it does not exist
        if (!topic) {
            topic = [];
            topics.put(name, topic);
        }
        // notify the listeners
        for (var index=0;index < topic.length;index++) {
            if (typeof topic[index] =='function')
                topic[index](args);
        }
        // check the glue for listeners
        if (jmaki.config  &&
            jmaki.config.glue &&
            jmaki.config.glue.listeners){
            
            for (var _l=0; _l <jmaki.config.glue.listeners.length;_l++ ) {
                var _listener = jmaki.config.glue.listeners[_l];
                var rgex = new RegExp(_listener.topic);
                if (_listener.topic == name || rgex.test(name)) {
                    if (_listener.action == 'call' &&
                    _listener.target != 'undefined' &&
                    _listener.target.object != 'undefined' &&
                    _listener.target.functionName != 'undefined'
                    ) {
                        // get the top level object                   
                        var _obj = jmaki.findObject(_listener.target.object);
                        // create an instance of the object if needed.
                        if (_obj && typeof _obj == 'function') {
                            myo = new _obj;
                        } else if (_obj) {
                            myo = _obj;
                        }
                        if (typeof myo != 'undefined' &&
                            typeof myo[_listener.target.functionName] == 'function'){
                            myo[_listener.target.functionName](args);
                        }
                    }
                } else if (jmaki.config.glue.listeners[_l].action == 'forward') {
                    var _topics = jmaki.config.glue.listeners[_l].topics;
                    // now multiplex the event
                    for (var ti = 0; ti < _topics.length; ti++){
                        // don't cause a recursive loop if the topic is this one
                        // TODO: Care needs to be taken to prevent re-entrant calls
                        if (_topics[ti] != name) {
                            self.publish(_topics[ti], args);
                        }
                    }
                }
            }
        }
        // publish to subframes with a global context appended
        var bd = true;
        if (typeof bubbleDown != 'undefined') bd = bubbleDown;
      
        if ( bd && window.frames.length > 0){
            for (var i=0; i < window.frames.length; i++){
              if (window.frames[i].jmaki){
                  window.frames[i].jmaki.publish("/global" + name, args, true, false);
              }
            }
        }
        //  publish to parent frame if we are a sub-frame. This will prevent duplicate events
        if (window != window.top){
            var bu = true;
            if (typeof bubbleUp != 'undefined') bu = bubbleUp;
              if (bu && window.parent.jmaki){
                  window.parent.jmaki.publish("/global" + name, args, false, true);
            }
        }        
    }

    /**
     * Load a set of libraries in order
     */
    this.addLibraries = function(_libs, _cb, _inprocess) {
        if (_libs.length <= 0) {
            if (typeof _cb == 'function') {
                _cb();
                return;
            }
        }
        if (typeof _inprocess == 'undefined') {
            _inprocess = new jmaki.Map();
        }
        var _uuid = new Date().getMilliseconds();
        var _lib = _libs[_libs.length-1];
        if (self.blocked) {
            // check the global block list
            for (var b=0; b < self.blocked.length; b++) {
                if (_lib.indexOf(self.blocked[b]) != -1) {
                    return _lib;
                }
            }
        }
        var _s_uuid = "c_script_" + _libs.length + "_" + _uuid;
        var head = document.getElementsByTagName("head")[0];
        var e = document.createElement("script");
        e.start = _uuid;
        e.id =  _s_uuid;
        e.type = 'text/javascript';
        head.appendChild(e);
        var se = document.getElementById(_s_uuid);
        _inprocess.put(_s_uuid,_lib);
        var loadHandler = function (_id) {
            var t = document.getElementById(_id);
            if (t.timeoutHandler) {
               clearInterval(t.timeoutHandler.interval);
               delete t.timeoutHandler;
            }
            _inprocess.remove(_id);
            var _cbk = _cb;
            if (_libs.length-1 > 0) {
                _libs.pop();
                jmaki.addLibraries(_libs, _cb,_inprocess);
             /**  rather than check length check for inprocess **/
            } 
            if (_inprocess.keys().length == 0) {
                if (typeof _cb != 'undefined'){
                    var timout = 0;
                    delete _inprocess;
                    setTimeout(function(){_cbk();}, 0);
                }
            }
        }
        if (/MSIE/i.test(navigator.userAgent)) {
            se.onreadystatechange = function () {
                if (this.readyState == 'loaded') {
                    var _id = _s_uuid;
                    loadHandler(_id);
                }
            }; 
            document.getElementById(_s_uuid).src = _lib;
        } else {   
            if (se.addEventListener) {
               var loader = function(_id) {
                 var _c = 0;
                 var self = this;
                 this.interval = setInterval(function() {
                 if (_c>2){
                   clearInterval(self.interval);
                   loadHandler(_id);
                 } else {
                   _c++;
                 }
                   }, 500);
                }
              se.timeoutHandler = new loader(_s_uuid);
              se.addEventListener("load", function(){var _id = _s_uuid;loadHandler(_id)}, true);
            }
            setTimeout(function(){document.getElementById(_s_uuid).src = _lib;}, 0);
        }
        se = null;
        head = null;
    }
    
    /**
     * Checks wheter a script has been loaded yet
     */
    this.isScriptLoaded = function(_name) {
        var exists = false;
        var gscripts = document.getElementsByTagName("script");
        for (var _ii = 0; _ii  < gscripts.length; _ii ++) {
            if (typeof gscripts[_ii].src) {
                if (gscripts[_ii ].src == _name) {
                    exists = true;
                    break;
                }
            }
        }
        return exists;
    }
    
    /**
     *  Dynamically load a type using the parameters in the config.json
     *  @param name of the type to load 
     */
    this.addType = function(name) {
        var _type = null;
        if (jmaki.config && 
            jmaki.config.types) {
            for (var _i =0; _i < jmaki.config.types.length; _i++) {
                if (name == jmaki.config.types[_i].id) {
                   _type = jmaki.config.types[_i];
                   break;
                }
            }
            if (_type) {
                if (_type.preload) {
                    _globalScope.eval(_type.preload);
                }
                // don't include scripts already loaded.
                var _libs = [];
                for (var _i=0; _i < _type.libs.length; _i++) {
                    if (!this.isScriptLoaded(this.webRoot + _type.libs[_i])) {
                        _libs.push(this.webRoot + _type.libs[_i]);
                    }
                }
                // call the post load
                var _cb;
                if (_type.postload) {
                    _cb = function() {
                        var postLoad = _type.postload;
                        _globalScope.eval(postLoad);
                    }
                }
                this.addLibraries(_libs, _cb);
            }
        }
    }
    
    /**
     *  Library name is added as a script element which will be loaded when the page is rendered
     *  @param lib library to add 
     */
    this.addLibrary = function(lib) {
        var se = document.createElement("script");
        se.src = lib;
        if (/WebKit/i.test(navigator.userAgent)) {
            document.body.appendChild(se);
        } else {
            var head = document.getElementsByTagName("head")[0];
            head.appendChild(se);
        }
    }
    
    /**
     * Register widget with jMaki 
     * @param widget Object respresenting the widget
     */
    this.addWidget = function(widget) {
        widgets.push(widget);
        if (this.loaded){this.loadWidget(widget);}
    }
    
    /**
     * Bootstrap or load all registered widgets
     */
    this.bootstrapWidgets = function() {
        this.loaded = true;
        for (var l=0; l < widgets.length; l++) {
            this.loadWidget(widgets[l]);
        }
    }

    /**
     *  Get the XMLHttpRequest object
     *
     */
    this.getXHR = function () {
        if (window.XMLHttpRequest) {
            return new XMLHttpRequest();
        } else if (window.ActiveXObject) {
            return new ActiveXObject("Microsoft.XMLHTTP");
        }
    }
    
    /**
    * Generalized XMLHttpRequest which can be used from evaluated code. Evaluated code is not allowed to make calls.
    * @param args is an object literal containing configuration parameters including method[get| post, get is default], body[bodycontent for a post], asynchronous[true is default]
    */
    this.doAjax= function(args) {
        if (typeof args == 'undefined') return;
       var _req = this.getXHR();
       var method = "GET";

       var async = true;
       var callback;
       if  (typeof args.asynchronous != 'undefined') {
            async=args.asynchronous;
       }
       if (args.method) {
            method=args.method;
       }
       if (typeof args.callback == 'function') {
           callback = args.callback;
       }
       var body = null;
       if (args.content) {
           body = "";
           for (var l in args.content) {
               if (typeof args.content[l] == "string") { 
                   body = body +  l + "=" + encodeURIComponent(args.content[l]) + "&";
               }
           }
        }
       if (async == true) _req.onreadystatechange = function() {callback(_req);};
       _req.open(method, args.url, async);
       if (args.method) {
            method=args.method;
            if (method.toLowerCase() == 'post') {
                _req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            }
       }
       _req.send(body);
       if (callback) return callback(_req);
    }
    
    /**
     * Load the script name provided 
     * @param target name of script from the web root to load 
     */
    this.loadScript = function(target) {
        if (!/http/.test(target)) target = this.webRoot + target;
        var req = this.getXHR();
        req.open("GET", target, false);
        try {
            req.send(null);
        } catch (e){
            // log error
        }
        if (req.status == 200) {
             return window.eval(req.responseText);
        }
    }
    
    /**
     * Loads the style sheet by adding a link element to the DOM 
     * @param target name of style sheet to load 
     */
    this.loadStyle = function(target) {
        var styleElement = document.createElement("link");
        styleElement.type = "text/css";
        styleElement.rel="stylesheet"
        styleElement.href = this.webRoot + target;
        if (document.getElementsByTagName('head').length == 0) {
            var headN = document.createElement("head");
            document.documentElement.insertBefore(headN, document.documentElement.firstChild);
        }
        document.getElementsByTagName('head')[0].appendChild(styleElement);
    }
    /**
     * Replace style class
     * @param root root of the oldStyle classes
     * @param oldStyle name of class or classes to replace
     * @param targetStyle name of new class or classes to use 
     */
    this.replaceStyleClass = function (root, oldStyle, targetStyle) {
        var elements = this.getElementsByStyle(oldStyle,root);
        for (var i=0; i < elements.length; i++) {
            // Handle cases where there are multiple classnames
            if (elements[i].className.indexOf(' ') != -1) {
                var classNames = elements[i].className.split(' ');
                for (var ci in classNames) {
                    if (classNames[ci] == oldStyle) {
                        classNames[ci] = targetStyle;
                    }
                }
                // now reset the styles with the replaced values
                elements[i].className = classNames.join(' ');
            } else  if (elements[i].className == oldStyle) {
                elements[i].className = targetStyle;
            }
        }
    }
    
    /**
    * Find a set of child nodes that contain the className specified
    * @param className is the targetClassName you are looking for
    * @param root  An optional root node to start searching from. The entire document will be searched if not specfied.
    *
    */
    this.getElementsByStyle = function(className, root){
        var elements = [];
        if (typeof root != 'undefined') {
            var rootNode = root;
            if (typeof root == 'string') {
                rootNode = document.getElementById(root);
            }    
            elements = this.getAllChildren(rootNode, []);
        } else {
            elements = (document.all) ? document.all : document.getElementsByTagName("*");
        }
		var found = [];
		for (var i=0; i < elements.length; i++) {
			// Handle cases where there are multiple classnames
            if (elements[i].className.indexOf(' ') != -1) {
                var classNames = elements[i].className.split(' ');
                for (var ci in classNames) {
                    if (classNames[ci] == className) {
                        found.push(elements[i]);
                    }
                }
            } else  if (elements[i].className == className) {
                found.push(elements[i]);
            }
        }
        return found;
    }
    
    /**
     * Utility Function to get children
     * @param element for which to get the children
     */
    this.getAllChildren = function(target, children) {
        var _nc = target.childNodes;
        for (var l=0; l <  _nc.length; l++) {
            if (_nc[l].nodeType == 1) {
                children.push(_nc[l]);
                if (_nc[l].childNodes.length > 0) {
                    this.getAllChildren(_nc[l], children);
                }
            }
        }
        return children;
    }
    
    /**
     * Load a widget
     * @param widget Object representing widget to load
     */
    this.loadWidget = function(_jmw) {
        // see if the widget has been defined.
        if (jmaki.attributes.get(_jmw.uuid) != null) {
            return;
        }
        var targetName ="jmaki.widgets." + _jmw.name + ".Widget";
        var con = this.findObject(targetName);
        if (typeof con != "function") {
            logError("Could not find widget constructor for: " + targetName + ". Please make sure the widget constructor is properly defined.", document.getElementById(_jmw.uuid));
        }
        var wimpl;
        // bind the value using a @{foo.obj} notation       
        if ((typeof _jmw.value == 'string') && /[^@{].*[^}]/i.test(_jmw.value)) {
            var _l = _jmw.value.indexOf("}");
            var _vw = /[^@{].*[^}]/.exec(_jmw.value);
            _jmw.value = jmaki.findObject(new String(_vw));          
        }
        // do not wrap IE with exception handler
        // because we cant' get the right line number
        var _uuid = _jmw.uuid;      
        if (/MSIE/i.test(navigator.userAgent)) {
            var _uuid = _jmw.uuid;
            var oldError = null;
            if (window.onerror) {
                oldError = window.onerror;
            }
            var eh = function(message, url, line) {
                var _puuid = _uuid;
                logWidgetError(targetName, _puuid,url, line, message, document.getElementById(_puuid));
            }
            window.onerror = eh;
            wimpl = new con(_jmw);          
            window.onerror = null;
            if (oldError) {
                window.onerror = oldError;
            }              
        } else if (typeof con == 'function'){
            try {
                wimpl = new con(_jmw);
            } catch (e){
                var line = "unknown";
                var description = null;
                if (e.lineNumber) line = e.lineNumber;
                if (e.message) description = e.message;
 
                if (jmaki.debug) {
                    logWidgetError(targetName, _jmw.uuid,_jmw.script, line, description , document.getElementById(_jmw.uuid));
                    return;
                }
            }
        } else if (typeof con == 'undefined') {          
            logError("Unable to find widget constructor " + targetName + " check log and make sure constructor is defined.", document.getElementById(_jmw.uuid));
            return;
        }
        if (typeof wimpl == 'object') {
            jmaki.attributes.put(_jmw.uuid, wimpl);
             if (typeof wimpl != 'undefined' && typeof wimpl.postLoad == 'function') {
                wimpl.postLoad();
             }
             jmaki.publish("/jmaki/runtime/widget/loaded", _jmw);
        } else {          
            logError("Unable to create an instance of " + targetName + ". See the error log for more detials.", document.getElementById(_jmw.uuid ));
        }
    }
    
    function logWidgetError(name,uuid, url, line, _m, div) {
        var message= "<span>Error loading " + name + " : id=" + uuid + "<br>" +
        "Script: "  + url + " (line:" + line + ")." +
        "<br>Message: " + _m + "</span>";
        logError(message, div);
    }
 
    function logError(message, div) {
        if (div == null || typeof div.className == 'undefined') {
            div = document.createElement("div");
            document.body.appendChild(div);
        }
        div.className = "";
        div.style.color = "red";
        div.innerHTML = message;
    }
           
    /**
     * destroy all registered widgets under the target node
     * @param _root - The _root to start at. All widgets will be removed if not specified.
     */
    this.clearWidgets = function(_root) {
        if (typeof _root == "undefined") {
            var _k = jmaki.attributes.keys();
            // call destroy on objects that were registered in the attribute map
            for (var l=0; l < _k.length; l++) {
                if (typeof jmaki.attributes.get(_k[l]).destroy != 'undefined' &&
                    typeof jmaki.attributes.get(_k[l]).destroy == 'function') {
                    jmaki.attributes.get(_k[l]).destroy();
                    jmaki.attributes.remove(_k[l]);
                }
            }
            jmaki.loaded = false;
            widgets = [];
        } else {
            var _ws = jmaki.getAllChildren(_root,[]);           
            for (var l=0; l < _ws.length; l++) {
                   if (_ws[l].id && jmaki.attributes.get(_ws[l].id) &&
                    typeof jmaki.attributes.get(_ws[l].id).destroy == 'function') {
                    jmaki.attributes.get(_ws[l].id).destroy();
                    var _p = document.getElementById(_ws[l].id);
                    _p.parentNode.removeChild(_p);
                    jmaki.attributes.remove(_ws[l].id);
                }             
            }
        }
    }
    
    /*
     * Add a glue listener programatcially. following is an example.
     *
     *{topic : "/dojo/fisheye",action: "call", target: { object: "jmaki.dynamicfaces",functionName: "fishEyeValueUpdate"}}
     *   or 
     * @param l as topic and 
     * @param t as the target object path ending with a function 
     */
    this.addGlueListener = function(l, t) {
        if (!jmaki.config) jmaki.config = {};
        if (!jmaki.config.glue) jmaki.config.glue = {};
        if (!jmaki.config.glue.listeners) jmaki.config.glue.listeners = [];
        if (typeof l == 'object') {
            if (l.topic) l.topic = l.topic.replace(/^\s+|\s+$/g, "");
            lis = l;
        } else if (typeof l == 'string' && typeof t == 'string'){    
          jmaki.config.glue.listeners.push(l);
          lis = {};
          lis.topic = l;
          lis.target = {};
          var _is = t.split('.');
          lis.action = "call";
          lis.target.functionName = _is.pop();
          lis.target.object = _is.join('.');
        }
        if (lis)jmaki.config.glue.listeners.push(lis);
    }
    
    /*
     * @param _src is the source object
     * @param _par is the class to extend
     */
    this.extend = function(_src, _par) {
        _src.prototype = new _par();
        _src.prototype.constructor = _src;
        _src.superclass = _par.prototype;
        for (i in _par) {
            _src.prototype[i] = _par[i];
        }
    }
    
    /**
     * Load a set of libraries in order
     */
    this.initializeBlocked = function() {
        // build a list of blocked scripts (ones that can dynamically reload
        if (jmaki.config && jmaki.config.types && !jmaki.blocked){
                jmaki.blocked = [];
                for (var ll=0; ll < jmaki.config.types.length;ll++) {
                    if (typeof jmaki.config.types[ll].dynamicallyLoadable != 'undefined') {
                     if (!jmaki.config.types[ll].dynamicallyLoadable) {
                         for (tl =0; tl < jmaki.config.types[ll].libs.length; tl++) {
                             jmaki.blocked.push(jmaki.config.types[ll].libs[tl]);
                         }
                    }
                }
            }
        }
    }
    
    /**
     * Initialize jMaki by loading the config.json
     *  Write in the glue by loading dependencies and
     *  Register listeners.
     */
    this.initialize = function() {     
        if (!jmaki.config){
             var req = this.getXHR();
             req.open("GET", this.webRoot + this.resourcesRoot + "/config.json", false);
             req.send(null);
             if (req.readyState == 4) {   
                 // status of 200 signifies sucessful HTTP call
                 if (req.status == 200) {
                     if (req.responseText == '') return;
                     var obj = eval('(' + req.responseText + ')');
                     jmaki.config = obj.config;          
                     // write out the dependent libraries so we have access
                     if (jmaki.config.glue && 
                         jmaki.config.glue.timers) {  
                         self.addTimers(jmaki.config.glue.timers);
                     }
                     if (jmaki.config.glue){              
                         if (jmaki.config.glue.includes){                 
                             var _libs = [];
                             for (var ll =0; ll < jmaki.config.glue.includes.length;ll++) {
                                    var _inc = jmaki.config.glue.includes[ll];
                                    if (self.webRoot != '' && !/^\//i.test(_inc)) _inc = "/" + _inc;
                                    _libs.push(self.webRoot +  _inc);
                             }
                             jmaki.addLibraries(_libs, function() {postInitialize();return;});
                         }
                     }
                 } else {
                     postInitialize();
                 }
             }
         }
    }
    
    var timers = [];
    
    /**
     * Create a namespace with the given string
     */
    this.namespace = function(_path, target) {
        // get the top level object
        var paths = _path.split('.');
        var _obj = _globalScope[paths[0]];
        for (var ii = 1; ii < paths.length; ii++) {
            if (typeof _obj[paths[ii]] != 'undefined' ) {
                _obj = _obj[paths[ii]];                                       
            } else {
                _obj[paths[ii]] = {};
                _obj = _obj[paths[ii]];
            }
        }
        // if object provided it becomes the last in the chain
        if (typeof target == 'object') {
            _obj = target;
        }
        return _obj;
    }
    
    this.findObject = function(_path) {
        var paths = _path.split('.');
        var _obj = _globalScope[paths[0]];
        var found = true;
        if (typeof _obj != 'undefined' ){
            for (var ii =1; ii < paths.length; ii++) {
                var _lp = paths[ii];
                if (_lp.indexOf('()') != -1){                  
                  var _ns = _lp.split('()');
                  if (typeof _obj[_ns[0]] == 'function'){
                      var _fn = _obj[_ns[0]];              
                      return _fn();
                  }
                }     
                if (typeof _obj[_lp] != 'undefined' ) {
                    _obj = _obj[_lp];                                       
                    found = true;
                } else {
                    found = false;
                    break;
                }
            }
            if (found) {
                return _obj;
            }
        }
        return null;
    }
    
    this.Timer = function(args, isCall) {
        var _src = this;
        this.args = args;
        var _target;
        
        this.processTopic = function() {
            for (var ti = 0; ti < args.topics.length; ti++){
                jmaki.publish(args.topics[ti], {topic: args.topics[ti],
                type:'timer',
                src:_src,
                timeout: args.to});
            }
        }
        
        this.processCall = function() {
            if (!_target) {
             var  _obj = jmaki.findObject(args.on);
                if (typeof _obj == 'function'){
                    _target = new _obj();
                } else if (typeof _obj == 'object'){
                    _target = _obj;
                }
            }
            if ((_target && typeof _target == 'object')) {
              if(typeof _target[args.fn] == 'function') {
                _target[args.fn]({type:'timer', src:_src, timeout: args.to});
              }
            }
        }
        
        this.run = function() {
            if (isCall) {
                _src.processCall();
            } else {
                _src.processTopic();
            }
            _globalScope.setTimeout(_src.run,args.to);
        }
    }
    
    this.addTimer = function(_timer){
        var timers = [];
        timers.push(_timer);
        this.addTimers(timers);
    }
    
    this.addTimers = function(_timers){
        if (typeof _timers != 'undefined'){
            for (var _l=0; _l <_timers.length;_l++ ) {
                // create a wrapper and add the timer
                var _timer = _timers[_l];              
                if (_timer.action == 'call' &&
                _timer.target != 'undefined' &&
                _timer.target.object != 'undefined' &&
                _timer.target.functionName != 'undefined' &&
                typeof _timer.timeout != 'undefined') {
                    // create the timer
                    var args = {on: _timer.target.object,
                    fn: _timer.target.functionName,
                    to: _timer.timeout
                    }
                    var timer = new jmaki.Timer(args,true);
                    timers.push(timer);
                    timer.run();
                    
                } else if (_timers[_l].action == 'publish') {
                    var args = {topics: _timers[_l].topics,
                    to: _timer.timeout
                    }
                    var timer = new jmaki.Timer(args,false);
                    timers.push(timer);
                    timer.run();
                }
            }            
        }
    }
    
    function postInitialize() {
        if (jmaki.initialized) return;
        else jmaki.initialized = true;
        jmaki.publish("/jmaki/runtime/intialized", {});
        jmaki.bootstrapWidgets();
        jmaki.publish("/jmaki/runtime/widgetsLoaded", {});
        jmaki.publish("/jmaki/runtime/loadComplete", {});
    }
}

var oldLoad  = window.onload;

/**
 * onload calls bootstrap function to initialize and load all registered widgets
 * override intial onload.
 */
window.onload = function() {
    if (!jmaki.initialized) {
        jmaki.initialize();
    } else {
       jmaki.bootstrapWidgets();
       return;
    }
    if (typeof oldLoad  == 'function') {
        oldLoad();
    }
}