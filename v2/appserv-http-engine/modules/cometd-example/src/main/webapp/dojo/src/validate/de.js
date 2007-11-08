dojo.provide("dojo.validate.de");
dojo.require("dojo.validate.common");

dojo.validate.isGermanCurrency = function(/*String*/value) {
	//summary: checks to see if 'value' is a valid representation of German currency (Euros)
	var flags = {
		symbol: "\u20AC",
		placement: "after",
		signPlacement: "begin", //TODO: this is really locale-dependent.  Will get fixed in v0.5 currency rewrite. 
		decimal: ",",
		separator: "."
	};
	return dojo.validate.isCurrency(value, flags); // Boolean
}


