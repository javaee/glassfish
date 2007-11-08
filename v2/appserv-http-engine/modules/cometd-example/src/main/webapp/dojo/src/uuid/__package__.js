dojo.kwCompoundRequire({
	common: [
		"dojo.uuid.Uuid",
		"dojo.uuid.LightweightGenerator",
		"dojo.uuid.RandomGenerator",
		"dojo.uuid.TimeBasedGenerator",
		"dojo.uuid.NameBasedGenerator",
		"dojo.uuid.NilGenerator"
	]
});
dojo.provide("dojo.uuid.*");

