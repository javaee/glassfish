dojo.provide("dojo.validate.jp");
dojo.require("dojo.validate.common");

dojo.validate.isJapaneseCurrency = function(/*String*/value) {
	//summary: checks to see if 'value' is a valid representation of Japanese currency
	var flags = {
		symbol: "\u00a5",
		fractional: false
	};
	return dojo.validate.isCurrency(value, flags); // Boolean
}


