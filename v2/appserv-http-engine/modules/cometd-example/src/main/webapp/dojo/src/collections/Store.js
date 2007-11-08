dojo.provide("dojo.collections.Store");
dojo.require("dojo.lang.common");

/*	Store
 *	Designed to be a simple store of data with access methods...
 *	specifically to be mixed into other objects (such as widgets).
 */
dojo.collections.Store = function(/* array? */jsonArray){
	//	summary
	//	Data Store with accessor methods.
	var data = [];
	var items = {};	//	for key-based lookups.
	this.keyField = "Id";

	this.get = function(){
		//	summary
		//	Get the internal data array, should not be used.
		return data;	//	array
	};
	this.getByKey = function(/* string */key){
		//	summary
		//	Find the internal data object by key.
		return items[key];	//	object
	};
	this.getByIndex = function(/*number*/idx){ 
		//	summary
		//	Get the internal data object by index.
		return data[idx]; 	// object
	};
	this.getIndexOf = function(/* string */key){
		//	summary
		//	return the index of the object at key
		for(var i=0; i<data.length; i++){
			if(data[i].key==key){
				return i;	//	number
			}
		}
		return -1;	//	number
	};
	
	this.getData = function(){
		//	summary
		//	Get an array of source objects.
		var arr = [];
		for(var i=0; i<data.length; i++){
			arr.push(data[i].src);
		}
		return arr;	//	array
	};
	this.getDataByKey = function(/*string*/key){
		//	summary
		//	Get the source object by key.
		if(items[key]!=null){
			return items[key].src;	//	object
		}
		return null;	//	null
	};
	this.getIndexOfData = function(/* object */obj){
		//	summary
		//	return the internal index of obj, if it exists in the store.
		for(var i=0; i<data.length; i++){
			if(data[i].src==obj){
				return i;
			}
		}
		return -1;	//	number
	};
	this.getDataByIndex = function(/*number*/idx){ 
		//	summary
		//	Get the source object at index idx.
		if(data[idx]){
			return data[idx].src;	//	object
		}
		return null; 	//	object
	};

	this.update = function(/* Object */obj, /* string */fieldPath, /* Object */val, /* boolean? */bDontFire){
		var parts=fieldPath.split("."), i=0, o=obj, field;
		if(parts.length>1) {
			field = parts.pop();
			do{ 
				if(parts[i].indexOf("()")>-1){
					var temp=parts[i++].split("()")[0];
					if(!o[temp]){
						dojo.raise("dojo.collections.Store.getField(obj, '" + field + "'): '" + temp + "' is not a property of the passed object.");
					} else {
						//	this *will* throw an error if the method in question can't be invoked without arguments.
						o = o[temp]();
					}
				} else {
					o = o[parts[i++]];
				}
			} while (i<parts.length && o != null);
		} else {
			field = parts[0];
		}

		obj[field] = val;
		if(!bDontFire){
			this.onUpdateField(obj, fieldPath, val);
		}
	};

	this.forEach = function(/* function */fn){
		//	summary
		//	Functional iteration directly on the internal data array.
		if(Array.forEach){
			Array.forEach(data, fn, this);
		}else{
			for(var i=0; i<data.length; i++){
				fn.call(this, data[i]);
			}
		}
	};
	this.forEachData = function(/* function */fn){
		//	summary
		//	Functional iteration on source objects in internal data array.
		if(Array.forEach){
			Array.forEach(this.getData(), fn, this);
		}else{
			var a=this.getData();
			for(var i=0; i<a.length; i++){
				fn.call(this, a[i]);
			}
		}
	};

	this.setData = function(/*array*/arr, /* boolean? */bDontFire){
		//	summary
		//	Set up the internal data.
		data = []; 	//	don't fire onClearData
		for(var i=0; i<arr.length; i++){
			var o={ key: arr[i][this.keyField], src:arr[i] };
			data.push(o);
			items[o.key]=o;
		}
		if(!bDontFire){
			this.onSetData();
		}
	};
	
	this.clearData = function(/* boolean? */bDontFire){
		//	summary
		//	Clears the internal data array.
		data = [];
		items={};
		if(!bDontFire){
			this.onClearData();
		}
	};

	this.addData = function(/* object */obj, /*string?*/key, /* boolean? */bDontFire){ 
		//	summary
		//	Add an object with optional key to the internal data array.
		var k = key || obj[this.keyField];
		if(items[k]!=null){
			var o = items[k];
			o.src = obj;
		} else {
			var o={ key:k, src:obj };
			data.push(o);
			items[o.key] = o;
		}
		if(!bDontFire){
			this.onAddData(o);
		}
	};
	this.addDataRange = function(/*array*/arr, /* boolean? */bDontFire){
		//	summary
		//	Add a range of objects to the internal data array.
		var objects=[];
		for(var i=0; i<arr.length; i++){
			var k = arr[i][this.keyField];
			if(items[k]!=null){
				var o = items[k];
				o.src = obj;
			} else {
				var o = { key:k, src:arr[i] };
				data.push(o);
				items[k] = o;
			}
			objects.push(o);
		}
		if(!bDontFire){
			this.onAddDataRange(objects);
		}
	};
	this.addDataByIndex = function(/* object */obj, /* number */idx, /* string? */key, /* boolean? */bDontFire){
		//	summary
		//	Add an object with optional key to the internal data array at index idx.
		//	If the key exists in the store, it is removed and reinserted into the data array.
		var k = key || obj[this.keyField];
		if(items[k]!=null){
			var i=this.getIndexOf(k);
			var o=data.splice(i, 1);
			o.src = obj;
		} else {
			var o={ key:k, src:obj };
			items[k] = o;
		}
		data.splice(idx, 0, o);
		if(!bDontFire){
			this.onAddData(o);
		}
	};
	this.addDataRangeByIndex = function(/* array */arr, /* number */idx, /* boolean? */bDontFire){
		//	summary
		//	Add a range of objects to the internal data array, beginning at idx.  If any of
		//	the objects already exist in the store, they are removed and reinserted into the data array.
		var objects=[];
		for(var i=0; i<arr.length; i++){
			var k = arr[i][this.keyField];
			if(items[k]!=null){
				var j=this.getIndexOf(k);
				var o=data.splice(j, 1);
				o.src=arr[i];
			} else {
				var o={ key:k, src:arr[i] };
				items[k]=o;
			}
			objects.push(o);
		}
		data.splice(idx, 0, objects);
		if(!bDontFire){
			this.onAddDataRange(objects);
		}
	};
	
	this.removeData = function(/*obj*/obj, /* boolean? */bDontFire){
		//	summary
		//	remove the passed object from the internal data array.
		var idx=-1;
		var o=null;
		for(var i=0; i<data.length; i++){
			if(data[i].src==obj){
				idx=i;
				o=data[i];
				break;
			}
		}
		if(!bDontFire){
			this.onRemoveData(o);
		}
		if(idx>-1){
			data.splice(idx,1);
			delete items[o.key];
		}
	};
	this.removeDataRange = function(/* number */idx, /* number */range, /* boolean? */bDontFire){
		//	summary
		//	Remove a range of objects from the internal array, beginning at idx and removing range objects.
		var ret = data.splice(idx, range);
		for(var i=0; i<ret.length; i++){
			delete items[ret[i].key];
		}
		if(!bDontFire){
			this.onRemoveDataRange(ret);
		}
		return ret;	//	array
	};
	this.removeDataByKey = function(/*string*/key, /* boolean? */bDontFire){
		//	summary
		//	remove the object at key from the internal data array.
		this.removeData(this.getDataByKey(key), bDontFire);
	};
	this.removeDataByIndex = function(/*number*/idx, /* boolean? */bDontFire){
		//	summary
		//	remove the object at idx from the internal data array.
		this.removeData(this.getDataByIndex(idx), bDontFire);
	};

	if(jsonArray && jsonArray.length && jsonArray[0]){
		this.setData(jsonArray, true);
	}
};

dojo.extend(dojo.collections.Store, {
	getField:function(/*object*/obj, /*string*/field){
		//	helper to get the nested value if needed.
		var parts=field.split("."), i=0, o=obj;
		do{ 
			if(parts[i].indexOf("()")>-1){
				var temp=parts[i++].split("()")[0];
				if(!o[temp]){
					dojo.raise("dojo.collections.Store.getField(obj, '" + field + "'): '" + temp + "' is not a property of the passed object.");
				} else {
					//	this *will* throw an error if the method in question can't be invoked without arguments.
					o = o[temp]();
				}
			} else {
				o = o[parts[i++]];
			}
		} while (i<parts.length && o != null);
		
		if(i < parts.length){
			dojo.raise("dojo.collections.Store.getField(obj, '" + field + "'): '" + field + "' is not a property of the passed object.");
		}
		return o; // object
	},
	getFromHtml:function(/* array */meta, /* HTMLTableBody */body, /* function? */fnMod){
		//	summary
		//	Parse HTML data into native JSON structure for the store.
		var rows = body.rows;

		//	create a data constructor.
		var ctor=function(row){
			var obj = {};
			for(var i=0; i<meta.length; i++){
				var o = obj;
				var data = row.cells[i].innerHTML;
				var p = meta[i].getField();
				if(p.indexOf(".") > -1){
					p = p.split(".");
					while(p.length>1){
						var pr = p.shift();
						o[pr] = {};
						o = o[pr];
					}
					p = p[0];
				}

				var type = meta[i].getType();
				if(type == String){
					o[p] = data;
				} else {
					if(data){
						o[p] = new type(data);
					} else {
						o[p] = new type();
					}
				}
			}
			return obj;
		};

		//	we have initialization data, let's parse it.
		var arr=[];
		for(var i=0; i<rows.length; i++){
			var o = ctor(rows[i]);
			if(fnMod){
				fnMod(o, rows[i]);	//	apply any modifiers.
			}
			arr.push(o);
		}
		return arr;	//	array
	},
	onSetData:function(){ },
	onClearData:function(){ },
	onAddData:function(obj){ },
	onAddDataRange:function(arr){ },
	onRemoveData:function(obj){ },
	onRemoveDataRange:function(arr){ },
	onUpdateField:function(obj, field, val){ }
});
