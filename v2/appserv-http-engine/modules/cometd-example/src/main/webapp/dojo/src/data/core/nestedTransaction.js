dojo.provide("dojo.data.core.nestedTransaction");
dojo.require("dojo.data.core.Write");
dojo.require("dojo.experimental");

dojo.experimental("dojo.data.core.nestedTransaction");

dojo.data.core.nestedTransaction.beginTransaction = function() {
	if (!this._countOfNestedTransactions) {
		this._countOfNestedTransactions = 0;
	}
	this._countOfNestedTransactions += 1;
};

dojo.data.core.nestedTransaction.endTransaction = function() {
	this._countOfNestedTransactions -= 1;
	dojo.lang.assert(this._countOfNestedTransactions >= 0);
	if (this._countOfNestedTransactions === 0) {
		return this.save(); // save() is defined on the dojo.data.core.Write API
	}
};

dojo.data.core.nestedTransaction.rollbackTransaction = function() {
	this._countOfNestedTransactions = 0;
	this.revert(); // revert() is defined on the dojo.data.core.Write API
};


