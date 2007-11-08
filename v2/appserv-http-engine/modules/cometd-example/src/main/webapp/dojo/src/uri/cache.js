dojo.provide("dojo.uri.cache");

dojo.uri.cache = {
	_cache: {},
	set: function(/*dojo.uri.Uri*/uri, /*String*/content){
		//summary: Sets the cache value for a URI.
		this._cache[uri.toString()] = content;
		return uri; //dojo.uri.Uri
	},

	remove: function(/*dojo.uri.Uri*/uri){
		//summary: Removes a cache value for a URI.
		delete this._cache[uri.toString()];
	},

	get: function(/*dojo.uri.Uri*/uri){
		//summary: Removes a cache value for a URI.
		var key =  uri.toString();
		var value = this._cache[key];
		if(!value){
			value = dojo.hostenv.getText(key);
			if(value){
				this._cache[key] = value;
			}
		}
		return value; //String
	},

	allow: function(/*dojo.uri.Uri*/uri){
		//summary: A marker that indicates a URI can be interned by the
		//intern-strings build step.
		return uri;
	}
}
