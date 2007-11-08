dojo.provide("dojo.widget.DateTextbox");

dojo.require("dojo.widget.ValidationTextbox");
dojo.require("dojo.date.format");
dojo.require("dojo.validate.datetime");

// summary: A TextBox which tests for a valid date
//TODO: combine date and time widgets?
dojo.widget.defineWidget(
	"dojo.widget.DateTextbox",
	dojo.widget.ValidationTextbox,
	{
		// displayFormat: String
		//	optional pattern used format date.  Uses locale-specific format by default.  See dojo.date.format.
		displayFormat: "",

		// formatLength: String
		//	alternate to displayFormat, to format date as short/medium/long.  see dojo.date.format
		formatLength: "short",

//TODO: add date, saveFormat attributes like DropdownDatePicker?

		isValid: function(){ 
			// summary: see dojo.widget.ValidationTextbox
			return dojo.date.parse(this.textbox.value, {formatLength:this.formatLength, selector:'dateOnly', locale:this.lang, datePattern: this.displayFormat});
		}
	}
);

// summary: A TextBox which tests for a valid time
dojo.widget.defineWidget(
	"dojo.widget.TimeTextbox",
	dojo.widget.ValidationTextbox,
	{
		// displayFormat: String
		//	optional pattern used format time.  Uses locale-specific format by default.  See dojo.time.format.
		displayFormat: "",

		// formatLength: String
		//	alternate to displayFormat, to format time as short/medium/long.  see dojo.time.format
		formatLength: "short",

		isValid: function(){ 
			// summary: see dojo.widget.ValidationTextbox
			return dojo.date.parse(this.textbox.value, {formatLength:this.formatLength, selector:'timeOnly', locale:this.lang, timePattern: this.displayFormat});
		}
	}
);
