dojo.require("dojo.validate");
dojo.kwCompoundRequire({
	common:		["dojo.validate.check", 
						"dojo.validate.datetime", 
						"dojo.validate.de", 
						"dojo.validate.jp", 
						"dojo.validate.us", 
						"dojo.validate.web" 
	]
});
dojo.provide("dojo.validate.*");
