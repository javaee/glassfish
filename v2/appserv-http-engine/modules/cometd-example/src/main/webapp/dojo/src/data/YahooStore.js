dojo.provide("dojo.data.YahooStore");
dojo.require("dojo.data.core.RemoteStore");
dojo.require("dojo.lang.declare");
dojo.require("dojo.io.ScriptSrcIO");

dojo.declare("dojo.data.YahooStore", dojo.data.core.RemoteStore, null, {
	/* Summary:
	 *	  The YahooStore implements the dojo.data.core.Read API. 
	 */	
	_setupQueryRequest: function(result, requestKw) { 
		var start = 1;
		var count = 1;
		if (result) {
			start = result.start || start;
			count = result.count || count;
		}
		var sourceUrl = "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=dojo&language=en&query=" + 
				result.query + "&start=" + start + "&results=" + count + "&output=json";
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
		for (var i = 0; i < json.ResultSet.totalResultsReturned; ++i) {
			var record = json.ResultSet.Result[i];
			var item = {};
			item["Url"] = [record.Url];
			item["Title"] = [record.Title];
			item["Summary"] =[ record.Summary];
			var arrayIndex = (json.ResultSet.firstResultPosition - 1) + i;
			data[ arrayIndex.toString() ] = item;
		}
		return data;
	},

	getFeatures: function() {
		// summary: See dojo.data.core.Read.getFeatures()
		 var features = {
			 'dojo.data.core.Read': true
		 };
		 return features;
	}

});

