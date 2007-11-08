dojo.provide("dojo.data.DeliciousStore");
dojo.require("dojo.data.core.RemoteStore");
dojo.require("dojo.lang.declare");
dojo.require("dojo.io.ScriptSrcIO");

dojo.declare("dojo.data.DeliciousStore", dojo.data.core.RemoteStore, null, {
	/* Summary:
	 *	  DeliciousStore implements the dojo.data.core.Read API. 
	 */	
	_setupQueryRequest: function(result, requestKw) { 
		var count = result.count || 1;
		var sourceUrl = "http://del.icio.us/feeds/json/" + result.query + "?count=" + count;

		requestKw.url = sourceUrl;
		requestKw.transport = "ScriptSrcTransport";
		requestKw.mimetype = "text/json";
		requestKw.jsonParamName = 'callback';
	},
		 
	_resultToQueryMetadata: function(json) { 
		return json.ResultSet; 
	},
	
	_resultToQueryData: function(json) {
		var data = {}
		for (var i = 0; i < json.length; ++i) {
			var record = json[i];
			var item = {};
			item["Bookmark"] = [record.u];
			item["Description"] = [record.d];
			item["Tags"] = [record.t];
			data[i] = item;
		}
		return data;
	}
});

