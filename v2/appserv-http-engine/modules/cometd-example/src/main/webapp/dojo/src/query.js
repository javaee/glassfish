dojo.provide("dojo.query");
dojo.require("dojo.lang.array");
dojo.require("dojo.experimental");
dojo.experimental("dojo.query");
(function(){
	var h = dojo.render.html;
	var d = dojo;
	// if((false)&&( h.moz || h.opera)){

	////////////////////////////////////////////////////////////////////////
	// XPath query code
	////////////////////////////////////////////////////////////////////////

	var buildPath = function(query){
		var xpath = "";
		var qparts = query.split(" ");
		var hashIdx, dotIdx, bktIdx, colIdx;
		while(qparts.length){
			var tqp = qparts.shift();
			var prefix;
			if(tqp == ">"){
				prefix = "/";
				tqp = qparts.shift();
			}else{
				prefix = "//";
			}
			hashIdx = tqp.indexOf("#");
			dotIdx = tqp.indexOf(".");
			bktIdx = tqp.indexOf("[");
			colIdx = tqp.indexOf(":");
			var tagNameEnd = getTagNameEnd(tqp, hashIdx, dotIdx, bktIdx, colIdx);

			// get the tag name (if any)
			var tagName = (tagNameEnd != 0) ? tqp.substr(0, tagNameEnd).toLowerCase() : "*";

			xpath += prefix + tagName;
			
			// check to see if it's got an id. Needs to come first in xpath.
			if(hashIdx >= 0){
				var hashEnd = Math.min(
									Math.max(0, dotIdx), 
									Math.max(0, bktIdx), 
									Math.max(0, colIdx) 
							) || query.length;
				var idComponent = tqp.substring(hashIdx+1, hashEnd);
				xpath += "[@id='"+idComponent+"']";
			}

			// check the class name component
			if(0 <= dotIdx){
				var cn = getClassName(tqp, dotIdx, bktIdx, colIdx);
				
				var padding = " ";
				if(cn.charAt(cn.length-1) == "*"){
					padding = ""; cn = cn.substr(0, cn.length-1);
					// dojo.debug(cn)
				}
				xpath += 
					"[contains(concat(' ',@class,' '), ' "+
					cn + padding + "')]";
			}

			// FIXME: need to implement attribute and pseudo-class checks!!

		};
		return xpath;
	};
	/*
	*/

	var _xpathFuncCache = {};
	var getXPathFunc = function(path){
		if(_xpathFuncCache[path]){
			return _xpathFuncCache[path];
		}

		var doc = dojo.doc();
		var parent = dojo.body(); // FIXME
		// FIXME: don't need to memoize. The closure scope handles it for us.
		var xpath = buildPath(path);

		var tf = function(){
			// XPath query strings are memoized.
			var ret = [];
			var xpathResult = doc.evaluate(xpath, parent, null, 
											// XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
											XPathResult.ANY_TYPE, null);
			var result = xpathResult.iterateNext();
			while(result){
				ret.push(result);
				result = xpathResult.iterateNext();
			}
			return ret;
		}
		return _xpathFuncCache[path] = tf;
	};

	d.xPathMatch = function(query){
		// XPath based DOM query system. Handles a small subset of CSS
		// selectors, subset is identical to the non-XPath version of this
		// function. 

		// FIXME: need to add support for alternate roots
		return getXPathFunc(query)();
	}

	////////////////////////////////////////////////////////////////////////
	// DOM query code
	////////////////////////////////////////////////////////////////////////

	var _filtersCache = {};
	var _simpleFiltersCache = {};

	var agree = function(first, second){
		if(first && second){
			return function(){
				return first.apply(window, arguments) && second.apply(window, arguments);
			}
		}else if(first){
			return first;
		}else{
			return second;
		}
	}

	var filterUp = function(elements, queryParts){
		var ret = [];
		// at each level, weed out successively
		var idx = queryParts.length-1;
		var tqp = queryParts[idx];

		// dojo.debug(tqp);
		while(tqp){
			var parent = false;
			if(tqp == ">"){
				parent = true;
				if(idx > 0){
					tqp = queryParts[--idx];
				}else{
					// FIXME!!!
				}
			}
			var ff = getFilterFunc(tqp);
			// look up the chain to see if we need to modify our look-up mode

			nextElem:
			for(var x=elements.length-1, te; x>=0, te=elements[x]; x--){
				te = te.parentNode;
				while(te){
					if(ff(te)){
						ret.push(te);
						continue nextElem;
					}
					if(!parent){
						te = te.parentNode;
					}else{
						continue nextElem;
					}
				}
			}
			elements = ret;
			ret = []; // FIXME: how can we avoid array alloc here?
			tqp = queryParts[--idx];
		}
		return elements;
	}

	var getTagNameEnd = function(query, hashIdx, dotIdx, bktIdx, colIdx){
		// spammy and verbose, but fucking fast
		if((hashIdx == 0)||(dotIdx == 0)){
			return 0;
		}else{
			var mainEnd = query.length;
			if(hashIdx >= 0){
				if(hashIdx < mainEnd){
					mainEnd = hashIdx;
				}
			}
			if(dotIdx >= 0){
				if(dotIdx < mainEnd){
					mainEnd = dotIdx;
				}
			}
			if(bktIdx >= 0){
				if(bktIdx < mainEnd){
					mainEnd = bktIdx;
				}
			}
			if(colIdx >= 0){
				if(colIdx < mainEnd){
					mainEnd = colIdx;
				}
			}
			if(mainEnd < 0){
				mainEnd = query.length;
			}
			return mainEnd;
		}
	}

	var getFilterFunc = function(query){
		// note: query can't have spaces!
		if(_filtersCache[query]){
			return _filtersCache[query];
		}
		var ff = null;
		var hashIdx = query.indexOf("#");
		var dotIdx = query.indexOf(".");
		var bktIdx = query.indexOf("[");
		var colIdx = query.indexOf(":");
		var mainEnd = getTagNameEnd(query, hashIdx, dotIdx, bktIdx, colIdx);

		// does it have a tagName component?
		if(mainEnd > 0){
			// tag name match
			var tagName = query.substr(0, mainEnd).toLowerCase();;
			// dojo.debug(query, tagName);
			// dojo.debug("tagName:", tagName, mainEnd, dotIdx, hashIdx, bktIdx, colIdx);
			ff = agree(ff, 
				function(elem){
					var isTn = (
						(elem.nodeType == 1) &&
						(tagName == elem.tagName.toLowerCase())
					);
					return isTn;
				}
			);
		}

		// does the node have an ID?
		if(hashIdx >= 0){
			var hashEnd = Math.min(
								Math.max(0, dotIdx), 
								Math.max(0, bktIdx), 
								Math.max(0, colIdx) 
						) || query.length;
			var idComponent = query.substring(hashIdx+1, hashEnd);
			ff = agree(ff, 
				function(elem){
					return (
						(elem.nodeType == 1) &&
						(elem.id == idComponent)
					);
				}
			);
		}

		if(	(dotIdx >= 0) ||
			(bktIdx >= 0) ||
			(colIdx >= 0) ){
			ff = agree(ff,
				getSimpleFilterFunc(query, dotIdx, bktIdx, colIdx)
			);
		}
		// dojo.debug(ff({ nodeType: 1, tagName: "Th", id: "thbbt" }));

		return _filtersCache[query] = ff;
	}

	var getClassName = function(query, dotIdx, bktIdx, colIdx){
		// regular expressions are for people who don't understand state machines
		if(bktIdx > dotIdx){
			// brackets come before colons
			return query.substr(dotIdx+1, bktIdx);
		}else if(colIdx > dotIdx){
			return query.substr(dotIdx+1, colIdx);
		}else{
			return query.substr(dotIdx+1);
		}
	}

	var firedCount = 0;

	var getSimpleFilterFunc = function(query, dotIdx, bktIdx, colIdx){
		var fcHit = (_simpleFiltersCache[query]||_filtersCache[query]);
		if(fcHit){ return fcHit; }

		var ff = null;

		// if there's a class in our query, generate a match function for it
		if(dotIdx >= 0){
			// get the class name
			var className = getClassName(query, dotIdx, bktIdx, colIdx);
			var isWildcard = className.charAt(className.length-1) == "*";
			if(isWildcard){
				className = className.substr(0, className.length-1);
			}
			// FIXME: need to make less spammy!!
			ff = agree(ff, function(elem){
					var spc = " ";
					var ecn = elem.className;
					var ecnl = ecn.length;
					if(ecnl == 0){ return false; }
					var cidx = ecn.indexOf(className);
					if(0 > cidx){ return false; }
					var cnl = className.length;
					if((cidx == 0)&&(ecnl == cnl)){
							return true;
					}
					if(0 == cidx){
						if(ecn.charAt(cnl) == spc){
							// it was at the front
							return true;
						}
					}else{
						var cidxcnl = cidx+cnl;
						if(ecnl == cidxcnl){
							// if it's at the end, check to see if we got a
							// full match up front
							if(ecn.charAt(cidx-1) == spc){
								return true;
							}
						}else{
							// otherwise, check both sides
							if(	
								(ecn.charAt(cidx-1) == spc) && 
								( 
									(ecn.charAt(cidxcnl) == spc)||isWildcard
								)
							){
								return true;
							}
						}
					}
					return false;
				}
			);
		}
		if(bktIdx >= 0){
			ff = agree(ff, 
				function(elem){
					return true;
				}
			);
		}
		if(colIdx >= 0){
			// NOTE: we count on the pseudo name being at the end
			var pseudoName = query.substr(colIdx+1);
			var condition = "";
			var obi = pseudoName.indexOf("(");
			var cbi = pseudoName.indexOf(")");
			if(	(0 <= obi)&&
				(0 <= cbi)&&
				(cbi > obi)){
				condition = pseudoName.substring(obi+1, cbi);
				pseudoName = pseudoName.substr(0, obi);
			}

			// NOTE: NOT extensible on purpose until I figure out
			// the portable xpath pseudos extensibility plan.

			// http://www.w3.org/TR/css3-selectors/#structural-pseudos
			switch(pseudoName){
				case "first-child":
					ff = agree(ff, 
						function(elem){
							var p = elem.parentNode;
							var fc = p.firstChild;
							if(!fc){ return false; }
							while(fc && fc.nodeType != 1){
								fc = fc.nextSibling;
							}
							return (elem === fc);
						}
					);
					break;
				case "last-child":
					ff = agree(ff, 
						function(elem){
							var p = elem.parentNode;
							var lc = p.lastChild;
							if(!lc){ return false; }
							while(lc && lc.nodeType != 1){
								lc = lc.previousSibling;
							}
							return (elem === lc);
						}
					);
					break;
				case "empty":
					ff = agree(ff, 
						function(elem){
							var cn = elem.childNodes;
							var cnl = elem.childNodes.length;
							// if(!cnl){ return true; }
							for(var x=cnl-1; x >= 0; x--){
								var nt = cn[x].nodeType;
								if((nt == 1)||(nt == 3)){ return false; }
							}
							return true;
						}
					);
					break;
				case "nth-child":
					if(condition.indexOf("n") == -1){
						var ncount = parseInt(condition);
						ff = agree(ff, 
							function(elem){
								return (elem.parentNode.childNodes[ncount-1] === elem);
							}
						);
					}else if(condition == "2n"){
						ff = agree(ff, 
							function(elem){
								var sib = elem.previousSibling;
								if(sib){
									var scount = 1;
									while(sib){
										sib = sib.previousSibling;
										scount++;
									}
									return ((scount % 2) == 0);
								}else{ 
									return false; 
								}
							}
						);
					}
					break;
				default:
					break;
			}
		}
		if(!ff){
			ff = function(){ return true; };
		}
		return _simpleFiltersCache[query] = ff;
	}

	var getElements = function(query, root){
		// NOTE: this function is in the fast path! not memoized!!!

		// dojo.debug(query);
		// the query doesn't contain any spaces, so there's only so many
		// things it could be
		if(!root){ root = document; }
		var dotIdx = query.indexOf(".");
		var bktIdx = query.indexOf("[");
		var colIdx = query.indexOf(":");
		var hashIdx = query.indexOf("#");
		var ret = [];
		var idOnly = (hashIdx == 0);

		var filterFunc;
		if(!idOnly){
			filterFunc = getSimpleFilterFunc(query, dotIdx, bktIdx, colIdx);
		}

		if(idOnly){
			// ID query. Easy.
			ret.push(document.getElementById(query.substr(1)));
			return ret;
		}else if(hashIdx >= 0){
			// we got a type-filtered ID search (e.g., "h4#thinger")
			var te = document.getElementById(query.substr(hashIdx+1));
			if(	(te.nodeType == 1)&&
				(te.tagName.toLowerCase() == query.substr(0, hashIdx).toLowerCase()) ){
				ret.push(te);
			}
		}else{
			var tret;
			if( (dotIdx >= 0) || (query == "*") ){
				// if we're the beginning of a generic class search, we need to
				// get every element in the root for filtering
				var elName = ((dotIdx == 0)||(query == "*")) ? "*" : query.substr(0, dotIdx);
				tret = root.getElementsByTagName(elName);
			}else if(0 > dotIdx){
				// otherwise we're in node-type query...go get 'em
				tret = root.getElementsByTagName(query.substr(0, 
					getTagNameEnd(query, hashIdx, dotIdx, bktIdx, colIdx)));
			}

			if(0 <= colIdx){
				var pseudoName = (0 <= colIdx) ? query.substr(colIdx+1) : "";
				switch(pseudoName){
					case "first":
						for(var x=0, te; te = tret[x]; x++){
							if(filterFunc(te)){
								return [ te ];
							}
						}
						break;
					case "last":
						for(var x=tret.length-1, te; te = tret[x]; x--){
							if(filterFunc(te)){
								return [ te ];
							}
						}
						break;
					default:
						for(var x=0, te; te = tret[x]; x++){
							if(filterFunc(te)){
								ret.push(te);
							}
						}
						break;
				}
			}else{
				for(var x=0, te; te = tret[x]; x++){
					if(filterFunc(te)){
						ret.push(te);
					}
				}
			}
		}

		return ret;
	}

	var _partsCache = {};

	////////////////////////////////////////////////////////////////////////
	// the query runner
	////////////////////////////////////////////////////////////////////////

	var _queryFuncCache = {};


	var getStepQueryFunc = function(query, sidx){
		if(_queryFuncCache[query]){ return _queryFuncCache[query]; }

		if(sidx < 0){
			_queryFuncCache[query] = function(root){
				return getElements(query, root);
			}
			return _queryFuncCache[query];
		}

		var sqf = function(root){
			var qparts = query.split(" ");
			var roots = getElements(qparts.shift());
			// var roots = getElements((qparts[0].indexOf("#") < 0) ? qparts[0] : qparts.shift());
			var qpl = qparts.length;

			// now that we have the roots, work backward from the bottom, using
			// the roots as backstops
			var stepFiltered = [];
			dojo.lang.forEach(roots, function(root){
				if(!root){ return; }
				// use the root as the jumping off spot for other rules
				var pMatches = getElements(qparts[qpl-1], root);
				// dojo.debug(pMatches);
				if(qpl > 1){
					// dojo.debug(qparts.slice(0, qpl-1));
					// pMatches = filterUp(pMatches, qparts);
					pMatches = filterUp(pMatches, (qparts.slice(0, qpl-1)));
				}
				// FIXME: is there a way to avoid this?
				stepFiltered = stepFiltered.concat(pMatches);
			});

			return stepFiltered;
		}
		_queryFuncCache[query] = sqf;
		return sqf;
	}

	var _getQueryFunc = (
		// NOTE: 
		//		XPath on the Webkit nighlies is slower than it's DOM iteration
		//		for most test cases
		(document["evaluate"] && !dojo.render.html.safari) ? 
		function(query, sidx){
			if(_queryFuncCache[query]){ return _queryFuncCache[query]; }
			// has xpath support
			var qparts = query.split(" ");
			if(document["evaluate"]&&(query.indexOf(":") == -1)){
				// kind of a lame heuristic, but it works
				var gtIdx = query.indexOf(">")
				if(	
					((qparts.length > 2)&&(query.indexOf(">") == -1))||
					(qparts.length > 3)||
					((sidx == -1)&&(query.indexOf(".")==0))

				){
					// FIXME: we might be accepting selectors that we can't handle
					return _queryFuncCache[query] = getXPathFunc(query);
				}
			}

			// getStepQueryFunc has caching built in
			return getStepQueryFunc(query, sidx);
		} : getStepQueryFunc
	);

	var getQueryFunc = function(query, sidx){
		if(_queryFuncCache[query]){ return _queryFuncCache[query]; }
		if(0 > query.indexOf(",")){
			return _queryFuncCache[query] = _getQueryFunc(query, sidx);
		}else{
			var parts = query.split(", ");
			var tf = function(root){
				var pindex = 0; // avoid array alloc for every invocation
				var ret = [];
				var tp;
				while(tp = parts[pindex++]){
					ret = ret.concat(_getQueryFunc(tp, tp.indexOf(" "))(root));
				}
				return ret;
			}
			return _queryFuncCache[query] = tf;
		}
	}

	d.query = function(query, root){
		// return is always an array
		// NOTE: elementsById is not currently supported
		// NOTE: ignores xpath-ish queries for now

		// the basic shape of the algorithm is:
		//	- apply shortcuts
		//	- prune search to container(s)
		//		- top-level node type
		//		- constraining ID
		//	- start bottom-up searches (if applicable)
		//		- last elements in descendant selector
		//		- apply filters (:last-child, attr matches, etc.)
		//	- work the filter chain backward

		// shortcut for non-chained selectors!
		return getQueryFunc(query, query.indexOf(" "))(root);

	}
})();
