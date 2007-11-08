dojo.provide("dojo.storage.browser");

dojo.require("dojo.storage");
dojo.require("dojo.flash");
dojo.require("dojo.json");
dojo.require("dojo.uri.*");



dojo.storage.browser.FileStorageProvider = function(){
	// summary:
	//		Storage provider that uses the native file system as
	//		a storage back end, across Internet Explorer, Firefox,
	//		Safari, and Opera.
	// description: 
	//		This storage provider will autodetect if access
	//		to the native file system is available. This is
	//		only available if a page is loaded through a file://
	//		or chrome:// URL. 
	//		
	//		We support three different browsers for native file storage:
	//			* Firefox - Uses XPCOM
	//			* Internet Explorer - Uses ActiveX
	//			* Safari/Opera - Uses Java and LiveConnect
	//
	//		This storage provider saves all files in the same directory
	//		as the HTML file it is invoked from. Each key
	//		is a separate file, with the file's contents being
	//		the value for that key and the filename being the
	//		key plus ".txt". A special file named __dojoAllKeys.txt is also
	//		created, which is a list of all the keys available.
	//
	//		The technique to create this storage provider was learned
	//		by studying TiddlyWiki (http://tiddlywiki.com). 
	//		Thanks for figuring out how to do this Jeremy Ruston!
	//
	//		TODO: Automagically create a directory named .dojo_storage in
	//		the user's home directory, cross-platform, and then create a
	//		subdirectory based off the HTML filename of this page, such as
	//		myapp.html/ to store all of our key files into. Saving the files
	//		into the same directry as the HTML file is dangerous and messy,
	//		and might not be allowed based on directory permissioning for some
	//		scenarios.
	//
	//		Authors of this storage provider-	
	//			Brad Neuberg, bkn3@columbia.edu 
}

dojo.inherits(dojo.storage.browser.FileStorageProvider, dojo.storage);

// The filename to the index file that stores all available keys
dojo.storage.browser.FileStorageProvider._KEY_INDEX_FILENAME = "__dojoAllKeys";

// The ID of the hidden applet we use for file operations on Safari and Opera
dojo.storage.browser.FileStorageProvider._APPLET_ID = "__dojoFileJavaObj";

// instance methods and properties
dojo.lang.extend(dojo.storage.browser.FileStorageProvider, {
	namespace: "default",
	initialized: false,
	
	_available: null,
	_statusHandler: null,
	_keyIndex: new Array(),
	
	initialize: function(){
		if(djConfig["disableFileStorage"] == true){
			return;
		}
		
		// write out applet if Java is needed
		// for file operations
		/*
		if(this._isAvailableJava()){
			this._writeApplet();
		}*/
		
		// load our keys
		this._loadKeyIndex();
		
		// indicate that this storage provider is now loaded
		this.initialized = true;
		dojo.storage.manager.loaded();	
	},
	
	isAvailable: function(){
		// see if we are a file:// or chrome:// URL
		this._available = false;
		var protocol = window.location.protocol;
		if(protocol.indexOf("file") != -1 || protocol.indexOf("chrome") != -1){
			// try each of the file access types
			this._available = this._isAvailableXPCOM();
			
			if(this._available == false){
				this._available = this._isAvailableActiveX();
			}
			
			/*if(this._available == false){
				this._available = this._isAvailableJava();
			}*/
		}
		
		return this._available;
	},

	put: function(key, value, resultsHandler){
		if(this.isValidKey(key) == false){
			dojo.raise("Invalid key given: " + key);
		}			
		
		this._statusHandler = resultsHandler;
		
		// try to save the value	as a file
		try{
			this._save(key, value);
			// indicate we succeeded
			resultsHandler.call(null, dojo.storage.SUCCESS, key);
		}catch(e){
			// indicate we failed
			this._statusHandler.call(null, dojo.storage.FAILED, 
									key, e.toString());
		}
	},

	get: function(key){
		if(this.isValidKey(key) == false){
			dojo.raise("Invalid key given: " + key);
		}
		
		// FIXME: what to do with underlying file exceptions?
		// pass to caller?
		var results = this._load(key);
		
		return results;
	},

	getKeys: function(){
		return this._keyIndex;
	},
	
	hasKey: function(key){
		if(this.isValidKey(key) == false){
			dojo.raise("Invalid key given: " + key);
		}	
		
		// reload the index in case someone hand edited
		// it in the file system
		this._loadKeyIndex();
		
		// make sure this key exists
		var exists = false;
		for(var i = 0; i < this._keyIndex.length; i++){
			if(this._keyIndex[i] == key){
				exists = true;
			}	
		}
		
		return exists;
	},

	clear: function(){
		// reload the index in case someone hand edited
		// it in the file system
		this._loadKeyIndex();
		
		// make a copy of the keyIndex, since we will be changing
		// it while looping
		var keyIndex = new Array();
		
		for(var i = 0; i < this._keyIndex.length; i++){
			keyIndex[keyIndex.length] = new String(this._keyIndex[i]);	
		}
		
		// now wipe everything out
		for(var i = 0; i < keyIndex.length; i++){
			this.remove(keyIndex[i]);	
		}
	},
	
	remove: function(key){
		if(this.isValidKey(key) == false){
			dojo.raise("Invalid key given: " + key);
		}
		
		// first, remove the key from the index
		
		// load the index in case it has been changed
		// by hand in the file system
		this._loadKeyIndex();
		
		// find and delete this key
		for(var i = 0; i < this._keyIndex.length; i++){
			if(this._keyIndex[i] == key){
				this._keyIndex.splice(i, 1);
				break;
			}
		}
		
		// resave the index
		this._save(dojo.storage.browser.FileStorageProvider._KEY_INDEX_FILENAME,
					this._keyIndex,
					false);
		
		// now delete the underlying key's file
		
		// the filename for this key entry
		var fullPath = this._getPagePath() + key + ".txt";
		
		if(this._isAvailableXPCOM()){
			this._removeXPCOM(fullPath);
		}else if(this._isAvailableActiveX()){
			this._removeActiveX(fullPath);
		} /*else if(this._isAvailableJava()){
			this._removeJava(fullPath);
		}*/
	},
	
	isPermanent: function(){
		return true;
	},

	getMaximumSize: function(){
		return dojo.storage.SIZE_NO_LIMIT;
	},

	hasSettingsUI: function(){
		return false;
	},
	
	showSettingsUI: function(){
		dojo.raise(this.getType() + " does not support a storage settings user-interface");
	},
	
	hideSettingsUI: function(){
		dojo.raise(this.getType() + " does not support a storage settings user-interface");
	},
	
	getType: function(){
		return "dojo.storage.browser.FileStorageProvider";
	},
	
	_save: function(key, value, updateKeyIndex){
		if(typeof updateKeyIndex == "undefined"){
			updateKeyIndex = true;	
		}
		
		// serialize the value;
		// handle strings differently so they have better performance;
		// add a comment at the top of the JSON so we can differentiate
		// JSONed script blocks from a giant string for better
		// performance
		if(dojo.lang.isString(value) == false){
			value = dojo.json.serialize(value);
			value = "/* JavaScript */\n" + value + "\n\n";
		}
		
		// the filename for this key/value entry
		var fullPath = this._getPagePath() + key + ".txt";
		
		if(this._isAvailableXPCOM()){
			this._saveFileXPCOM(fullPath, value);	
		}else if(this._isAvailableActiveX()){
			this._saveFileActiveX(fullPath, value);
		} /*else if(this._isAvailableJava()){
			this._saveFileJava(fullPath, value);
		}*/
		
		if(updateKeyIndex){
			this._updateKeyIndex(key);
		}
	},
	
	_load: function(key){
		// the filename for this key/value entry
		var fullPath = this._getPagePath() + key + ".txt";
		
		var results = null;
		if(this._isAvailableXPCOM()){
			results = this._loadFileXPCOM(fullPath);	
		}else if(this._isAvailableActiveX()){
			results = this._loadFileActiveX(fullPath);
		}else if(this._isAvailableJava()){
			results = this._loadFileJava(fullPath);
		}
		
		if(results == null){
			return null;
		}
		
		// destringify the content back into a 
		// real JavaScript object;
		// handle strings differently so they have better performance
		if(!dojo.lang.isUndefined(results) && results != null 
			 && /^\/\* JavaScript \*\//.test(results)){
			results = dojo.json.evalJson(results);
		}
		
		return results;
	},
	
	_updateKeyIndex: function(key){
		// reload the key index, in case someone
		// has changed it by hand on disk
		this._loadKeyIndex();
		
		// now add our new key if it's not there yet
		var alreadyAdded = false;
		for(var i = 0; i < this._keyIndex.length; i++){
			if(this._keyIndex[i] == key){
				alreadyAdded = true;
				break;
			}
		}
		
		if(alreadyAdded == false){
			this._keyIndex[this._keyIndex.length] = key;
		}
		
		this._save(dojo.storage.browser.FileStorageProvider._KEY_INDEX_FILENAME,
					this._keyIndex,
					false);
	},
	
	_loadKeyIndex: function(){
		var indexContents = this._load(
								dojo.storage.browser.FileStorageProvider._KEY_INDEX_FILENAME);
		
		// turn the file from a JSONed array back into
		// a real object
		if(indexContents == null){
			this._keyIndex = new Array();
		}else{
			this._keyIndex = indexContents;
		}
	},
	
	_saveFileXPCOM: function(filename, value){ // Mozilla
		try{
			// indicate we are a privileged code block
			netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
			
			// get the file to work with
			var f = Components.classes["@mozilla.org/file/local;1"]
						.createInstance(Components.interfaces.nsILocalFile);
			f.initWithPath(filename);
			
			// open an output stream
			var ouputStream = Components.classes["@mozilla.org/network/file-output-stream;1"]
						.createInstance(Components.interfaces.nsIFileOutputStream);
			// void init(nsIFile file, int ioFlags, int perm, int behaviorFlags)
			// 		Arguments:
    			//			file: - file to write to
			//		    ioFlags: - file open flags 
			//				0x20 = PR_TRUNCATE - If the file exists, its length is truncated to 0
			// 				0x04 = R_RDWR - Read and Write access
			//				0x08 = PR_CREATE_FILE - Create file if it doesn't exist
			//		    perm: - file mode bits (Applied on Unix only)
			//				00400   Read by owner.
			//		    		00200   Write by owner.
			//		    behaviorFlags: flags specifying various behaviors of the 
			//				class (currently none supported)
			 
			// FIXME: BUG: We don't work on Parallels when we are on Windows trying
			// to update a SMB share on a Mac; .allKeys.txt gets zeroed out
			
			ouputStream.init(f, 0x20 | 0x04 | 0x08, 00400 + 00200, null);
			ouputStream.write(value, value.length);
			ouputStream.close();
		}catch(e){
			var msg = e.toString();
			if(e.name && e.message){
				msg = e.name + ": " + e.message;
			}
			dojo.raise("dojo.storage.browser.FileStorageProvider._saveFileXPCOM(): " + msg);
		}
	},
	
	_loadFileXPCOM: function(filename){ // Mozilla
		try{
			// indicate we are a privileged code block
			netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
		
			// get the file to work with
			var f = Components.classes["@mozilla.org/file/local;1"]
						.createInstance(Components.interfaces.nsILocalFile);
			f.initWithPath(filename);
			if(f.exists() == false){
				return null;
			}
		
			var inp = Components.classes["@mozilla.org/network/file-input-stream;1"]
						.createInstance(Components.interfaces.nsIFileInputStream);
			// void init(nsIFile file, int ioFlags, int perm, int behaviorFlags)
			//	Arguments:
			//	    file: file to read from
			//	    ioFlags: file open flags
			//			0x01 = PR_RDONLY - Open for reading only
			//	    perm: file mode bits
			//			00004 = Read by others (applied on UNIX only)
			//	    behaviorFlags: flags specifying various behaviors of the class
			inp.init(f, 0x01, 00004, null);
			var inputStream = Components.classes["@mozilla.org/scriptableinputstream;1"]
								.createInstance(Components.interfaces.nsIScriptableInputStream);
			inputStream.init(inp);
			var results = inputStream.read(inputStream.available());
			return results;
		}catch(e){
			var msg = e.toString();
			if(e.name && e.message){
				msg = e.name + ": " + e.message;
			}
			dojo.raise("dojo.storage.browser.FileStorageProvider._loadFileXPCOM(): " + msg);
		}
		
		return null;
	},
	
	_saveFileActiveX: function(filename, value){ // Internet Explorer
		try{
			var fileSystem = new ActiveXObject("Scripting.FileSystemObject");
			
			// object.OpenTextFile(filename[, iomode[, create[, format]]])
			//
			//	Arguments
			//		filename - String expression that identifies the file to open.
			//		iomode - Can be one of three constants: ForReading (1), 
			//				ForWriting (2), or ForAppending (8).
			//		create - Boolean value that indicates whether a new file 
			//				can be created if the specified filename doesn't exist. 
			//				The value is True if a new file is created, False if it isn't created. 
			//				If omitted, a new file isn't created.
			//		format - One of three Tristate values used to indicate the format of the 
			//		opened file. If omitted, the file is opened as ASCII. 
			
			var f = fileSystem.OpenTextFile(filename, 2 /* ForWriting */, 
											true /* Create File */);
			f.Write(value);
			f.Close();
		}catch(e){
			var msg = e.toString();
			if(e.name && e.message){
				msg = e.name + ": " + e.message;
			}
			dojo.raise("dojo.storage.browser.FileStorageProvider._saveFileActiveX(): " + msg);
		}
	},
	
	_loadFileActiveX: function(filename){ // Internet Explorer
		try{
			var fileSystem = new ActiveXObject("Scripting.FileSystemObject");
			
			if(fileSystem.FileExists(filename) == false){
				return null;
			}
			
			// object.OpenTextFile(filename[, iomode[, create[, format]]])
			//
			//	Arguments
			//		filename - String expression that identifies the file to open.
			//		iomode - Can be one of three constants: ForReading (1), 
			//				ForWriting (2), or ForAppending (8).
			//		create - Boolean value that indicates whether a new file 
			//				can be created if the specified filename doesn't exist. 
			//				The value is True if a new file is created, False if it isn't created. 
			//				If omitted, a new file isn't created.
			//		format - One of three Tristate values used to indicate the format of the 
			//		opened file. If omitted, the file is opened as ASCII. 
			
			var f = fileSystem.OpenTextFile(filename, 1 /* ForReading */);
			var results = f.ReadAll();
			f.Close();
			
			return results;
		}catch(e){
			var msg = e.toString();
			if(e.name && e.message){
				msg = e.name + ": " + e.message;
			}
			dojo.raise("dojo.storage.browser.FileStorageProvider._loadFileActiveX(): " + msg);
		}
	},
	
	_saveFileJava: function(filename, value){ // Safari and Opera
		//dojo.debug("saveFileJava, filename="+filename+", value="+value);
		try{
			var applet = dojo.byId(dojo.storage.browser.FileStorageProvider._APPLET_ID);
			applet.save(filename, value);
		}catch(e){
			var msg = e.toString();
			if(e.name && e.message){
				msg = e.name + ": " + e.message;
			}
			dojo.raise("dojo.storage.browser.FileStorageProvider._saveFileJava(): " + msg);
		}
	},
	
	_loadFileJava: function(filename){ // Safari and Opera
		//dojo.debug("loadFileJava, filename="+filename);
		try{
			var applet = dojo.byId(dojo.storage.browser.FileStorageProvider._APPLET_ID);
			var results = applet.load(filename);
			return results;
		}catch(e){
			var msg = e.toString();
			if(e.name && e.message){
				msg = e.name + ": " + e.message;
			}
			dojo.raise("dojo.storage.browser.FileStorageProvider._loadFileJava(): " + msg);
		}
	},
	
	_isAvailableActiveX: function(){ // Internet Explorer
		try{
			if(window.ActiveXObject){
				var fileSystem = new window.ActiveXObject("Scripting.FileSystemObject");
				return true;
			}
		}catch(e){
			dojo.debug(e);
		}
		
		return false;
	},
	
	_isAvailableXPCOM: function(){ // Mozilla
		try{
			if(window.Components){
				netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
				Components.classes["@mozilla.org/file/local;1"]
					.createInstance(Components.interfaces.nsILocalFile);
				return true;
			}
		}catch(e){
			dojo.debug(e);
		}
		
		return false;
	},
	
	_isAvailableJava: function(){ // Safari and Opera
		try{
			if(dojo.render.html.safari == true || dojo.render.html.opera == true()){
				if(navigator.javaEnabled() == true){
					return true;
				}
			}
		}catch(e){
			dojo.debug(e);
		}
		
		return false;
	},
	
	_getPagePath: function(){
		var path = window.location.pathname;
		
		// strip off any potential filename
		// FIXME: it is very difficult to truly differentiate
		// a directory from a filename; we just assume that if it
		// ends with .html or .htm that it is a filename
		if(/\.html?$/i.test(path)){
			path = path.replace(/(?:\/|\\)?[^\.\/\\]*\.html?$/, "");
		}
		
		if(/^\/?[a-z]+\:/i.test(path)){ // Windows disk name, such as a:
			// Mozilla adds a leading slash
			path = path.replace(/^\/?/, "");
			// convert slashes to Windows back slashes
			path = path.replace(/\//g, "\\"); 
		}else if(/^[\/\\]{2,3}[^\/]/.test(path)){ // SMB share, such as \\foobar\someshare\dir
			// strip off leading slashes
			// Mozilla adds three slashes, so optionally slice that off as well
			path = path.replace(/^[\/\\]{2,3}/, "");
			// convert slashes to Windows back slashes
			path = path.replace(/\//g, "\\");
			// add leading slashes back on 
			path = "\\\\" + path; /* becomes just \\ */
		}
		
		// end the path with a slash
		if(/\/$/.test(path) == false 
			&& /\\$/.test(path) == false){
			// add slash or backslash?
			if(/\//.test(path)){
				path += "/";
			}else{
				path += "\\";
			}
		}
		
		// some platforms will escape these values
		path = unescape(path);
		
		return path;
	},
	
	_removeXPCOM: function(filename){ // Mozilla
		try{
			// indicate we are a privileged code block
			netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
		
			// get the file to work with
			var f = Components.classes["@mozilla.org/file/local;1"]
						.createInstance(Components.interfaces.nsILocalFile);
			f.initWithPath(filename);
			if(f.exists() == false || f.isDirectory()){
				return;
			}
			
			if(f.isFile()){
				f.remove(false); // false = don't recursively delete	
			}
		}catch(e){
			// FIXME: Should we be raising an exception on a 
			// dojo.storage.remove()?
			dojo.raise("dojo.storage.browser.FileStorageProvider.remove(): " + e.toString());
		}	
	},
	
	_removeActiveX: function(filename){ // Internet Explorer
		try{
			var fileSystem = new ActiveXObject("Scripting.FileSystemObject");
			fileSystem.DeleteFile(filename);
		}catch(e){
			// FIXME: Should we be raising an exception on a 
			// dojo.storage.remove()?
			dojo.raise("dojo.storage.browser.FileStorageProvider.remove(): " + e.toString());
		}	
	},
	
	_removeJava: function(filename){ // Safari and Opera
		try{
			var applet = dojo.byId(dojo.storage.browser.FileStorageProvider._APPLET_ID);
			applet.remove(filename);
		}catch(e){
			var msg = e.toString();
			if(e.name && e.message){
				msg = e.name + ": " + e.message;
			}
			dojo.raise("dojo.storage.browser.FileStorageProvider._removeJava(): " + msg);
		}
	},
	
	_writeApplet: function(){
		var archive = dojo.uri.moduleUri("dojo", "../DojoFileStorageProvider.jar").toString();
		var tag = "<applet "
					+ "id='" + dojo.storage.browser.FileStorageProvider._APPLET_ID + "' "
					+ "style='position: absolute; top: -500px; left: -500px; width: 1px; height: 1px;' "
					+ "code='DojoFileStorageProvider.class' "
					+ "archive='" + archive + "' "
					+ "width='1' "
					+ "height='1' "
					+ ">"
					+ "</applet>";
		document.writeln(tag);
	}
});



dojo.storage.browser.WhatWGStorageProvider = function(){
	// summary:
	//		Storage provider that uses WHAT Working Group features in Firefox 2 
	//		to achieve permanent storage.
	// description: 
	//		The WHAT WG storage API is documented at 
	//		http://www.whatwg.org/specs/web-apps/current-work/#scs-client-side
	//
	//		You can disable this storage provider with the following djConfig
	//		variable:
	//		var djConfig = { disableWhatWGStorage: true };
	//		
	//		Authors of this storage provider-	
	//			JB Boisseau, jb.boisseau@eutech-ssii.com
	//			Brad Neuberg, bkn3@columbia.edu 
}

dojo.inherits(dojo.storage.browser.WhatWGStorageProvider, dojo.storage);

// instance methods and properties
dojo.lang.extend(dojo.storage.browser.WhatWGStorageProvider, {
	namespace: "default",
	initialized: false,
	
	_domain: null,
	_available: null,
	_statusHandler: null,
	
	initialize: function(){
		if(djConfig["disableWhatWGStorage"] == true){
			return;
		}
		
		// get current domain
		this._domain = location.hostname;
		
		// indicate that this storage provider is now loaded
		this.initialized = true;
		dojo.storage.manager.loaded();	
	},
	
	isAvailable: function(){
		try{
			var myStorage = globalStorage[location.hostname];
		}catch(e){
			this._available = false;
			return this._available;
		}
		
		this._available = true;	
		return this._available;
	},

	put: function(key, value, resultsHandler){
		if(this.isValidKey(key) == false){
			dojo.raise("Invalid key given: " + key);
		}			
		
		this._statusHandler = resultsHandler;
		
		// serialize the value;
		// handle strings differently so they have better performance
		if(dojo.lang.isString(value)){
			value = "string:" + value;
		}else{
			value = dojo.json.serialize(value);
		}
		
		// register for successful storage events
		window.addEventListener("storage", function(evt){
			// indicate we succeeded
			resultsHandler.call(null, dojo.storage.SUCCESS, key);
		}, false);
		
		// try to store the value	
		try{
			var myStorage = globalStorage[this._domain];
			myStorage.setItem(key,value);
		}catch(e){
			// indicate we failed
			this._statusHandler.call(null, dojo.storage.FAILED, 
									key, e.toString());
		}
	},

	get: function(key){
		if(this.isValidKey(key) == false){
			dojo.raise("Invalid key given: " + key);
		}
		
		var myStorage = globalStorage[this._domain];
		
		var results = myStorage.getItem(key);

		if(results == null){
			return null;
		}
	
		results = results.value;
		
		// destringify the content back into a 
		// real JavaScript object;
		// handle strings differently so they have better performance
		if(!dojo.lang.isUndefined(results) && results != null 
			 && /^string:/.test(results)){
			results = results.substring("string:".length);
		}else{
			results = dojo.json.evalJson(results);
		}
		
		return results;
	},

	getKeys: function(){
		var myStorage = globalStorage[this._domain];
		var keysArray = new Array();
		for(i=0; i<myStorage.length;i++){
			keysArray[i] = myStorage.key(i);
		}
		
		return keysArray;
	},

	clear: function(){
		var myStorage = globalStorage[this._domain];
		var keys = new Array();
		for(var i = 0; i < myStorage.length; i++){
			keys[keys.length] = myStorage.key(i);
		}
		
		for(var i = 0; i < keys.length; i++){
			myStorage.removeItem(keys[i]);
		}
	},
	
	remove: function(key){
		var myStorage = globalStorage[this._domain];
		myStorage.removeItem(key);
	},
	
	isPermanent: function(){
		return true;
	},

	getMaximumSize: function(){
		return dojo.storage.SIZE_NO_LIMIT;
	},

	hasSettingsUI: function(){
		return false;
	},
	
	showSettingsUI: function(){
		dojo.raise(this.getType() + " does not support a storage settings user-interface");
	},
	
	hideSettingsUI: function(){
		dojo.raise(this.getType() + " does not support a storage settings user-interface");
	},
	
	getType: function(){
		return "dojo.storage.browser.WhatWGProvider";
	}
});




dojo.storage.browser.FlashStorageProvider = function(){
	// summary: Storage provider that uses features in Flash to achieve permanent storage
	// description:
	//		Authors of this storage provider-
	//			Brad Neuberg, bkn3@columbia.edu	
}

dojo.inherits(dojo.storage.browser.FlashStorageProvider, dojo.storage);

// instance methods and properties
dojo.lang.extend(dojo.storage.browser.FlashStorageProvider, {
	namespace: "default",
	initialized: false,
	_available: null,
	_statusHandler: null,
	
	initialize: function(){
		if(djConfig["disableFlashStorage"] == true){
			return;
		}
		
		// initialize our Flash
		var loadedListener = function(){
			dojo.storage._flashLoaded();
		}
		dojo.flash.addLoadedListener(loadedListener);
		var swfloc6 = dojo.uri.moduleUri("dojo", "../Storage_version6.swf").toString();
		var swfloc8 = dojo.uri.moduleUri("dojo", "../Storage_version8.swf").toString();
		dojo.flash.setSwf({flash6: swfloc6, flash8: swfloc8, visible: false});
	},
	
	isAvailable: function(){
		if(djConfig["disableFlashStorage"] == true){
			this._available = false;
		}else{
			this._available = true;
		}
		
		return this._available;
	},

	put: function(key, value, resultsHandler){
		if(this.isValidKey(key) == false){
			dojo.raise("Invalid key given: " + key);
		}
			
		this._statusHandler = resultsHandler;
		
		// serialize the value;
		// handle strings differently so they have better performance
		if(dojo.lang.isString(value)){
			value = "string:" + value;
		}else{
			value = dojo.json.serialize(value);
		}
		
		dojo.flash.comm.put(key, value, this.namespace);
	},

	get: function(key){
		if(this.isValidKey(key) == false){
			dojo.raise("Invalid key given: " + key);
		}
		
		var results = dojo.flash.comm.get(key, this.namespace);

		if(results == ""){
			return null;
		}
    
		// destringify the content back into a 
		// real JavaScript object;
		// handle strings differently so they have better performance
		if(!dojo.lang.isUndefined(results) && results != null 
			 && /^string:/.test(results)){
			results = results.substring("string:".length);
		}else{
			results = dojo.json.evalJson(results);
		}
    
		return results;
	},

	getKeys: function(){
		var results = dojo.flash.comm.getKeys(this.namespace);
		
		if(results == ""){
			return [];
		}

		// the results are returned comma seperated; split them
		return results.split(",");
	},

	clear: function(){
		dojo.flash.comm.clear(this.namespace);
	},
	
	remove: function(key){
		// summary: 
		//		Note- This one method is not implemented on the
		// 		FlashStorageProvider yet
		
		dojo.unimplemented("dojo.storage.browser.FlashStorageProvider.remove");
	},
	
	isPermanent: function(){
		return true;
	},

	getMaximumSize: function(){
		return dojo.storage.SIZE_NO_LIMIT;
	},

	hasSettingsUI: function(){
		return true;
	},

	showSettingsUI: function(){
		dojo.flash.comm.showSettings();
		dojo.flash.obj.setVisible(true);
		dojo.flash.obj.center();
	},

	hideSettingsUI: function(){
		// hide the dialog
		dojo.flash.obj.setVisible(false);
		
		// call anyone who wants to know the dialog is
		// now hidden
		if(dojo.storage.onHideSettingsUI != null &&
			!dojo.lang.isUndefined(dojo.storage.onHideSettingsUI)){
			dojo.storage.onHideSettingsUI.call(null);	
		}
	},
	
	getType: function(){
		return "dojo.storage.browser.FlashStorageProvider";
	},
	
	/** Called when the Flash is finished loading. */
	_flashLoaded: function(){
		this._initialized = true;

		// indicate that this storage provider is now loaded
		dojo.storage.manager.loaded();
	},
	
	//	Called if the storage system needs to tell us about the status
	//	of a put() request. 
	_onStatus: function(statusResult, key){
		var ds = dojo.storage;
		var dfo = dojo.flash.obj;
		
		if(statusResult == ds.PENDING){
			dfo.center();
			dfo.setVisible(true);
		}else{
			dfo.setVisible(false);
		}
		
		if((!dj_undef("_statusHandler", ds))&&(ds._statusHandler != null)){
			ds._statusHandler.call(null, statusResult, key);		
		}
	}
});

// register the existence of our storage providers
dojo.storage.manager.register("dojo.storage.browser.FileStorageProvider",
								new dojo.storage.browser.FileStorageProvider());
dojo.storage.manager.register("dojo.storage.browser.WhatWGStorageProvider",
								new dojo.storage.browser.WhatWGStorageProvider());
dojo.storage.manager.register("dojo.storage.browser.FlashStorageProvider",
								new dojo.storage.browser.FlashStorageProvider());

// now that we are loaded and registered tell the storage manager to initialize
// itself
dojo.storage.manager.initialize();
