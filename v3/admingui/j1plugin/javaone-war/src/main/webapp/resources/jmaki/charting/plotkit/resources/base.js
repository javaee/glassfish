jmaki.namespace("jmaki.widgets.jmaki.charting.plotkit");

/**
 *  This is the base class for all Plotkit jMaki widgets.
 *
 */
jmaki.widgets.jmaki.charting.plotkit.base = function() {

    this.idMappings = {};
    var autoSizeH = true;
    var autoSizeW = true;
    var data;
    var datasetMappings = {};
    var xTicks;
    var yAxis;
    var yTicks;
    var xAxis;
    var _widget = this;
    var wargs;
    var markers;
    var pmarkers;
    // used to track duplicate markers on the same point
    var markerPoints = {};
    
    this.defaults = {
       height : 300,
       width : 300,
      _pr : 30,
      _pl : 40,
      _pt : 30,
      _pb : 30,
      backgroundColor : new MochiKit.Color.Color.fromString("rgb(255,255,255)"),
      axisLabelColor : new MochiKit.Color.Color.fromString("rgb(0,0,0)"),
      axisColor : new MochiKit.Color.Color.fromString("rgb(255,255,255)"),
      axisLineWidth : .1,
      drawXAxis : true,
      drawYAxis : true,
      colorScheme : 0
    };

    this.processArgs = function(wargs) {    
        _widget.wargs = wargs;       
        if (wargs.publish) _widget.publish = wargs.publish;
        if (wargs.subscribe){
            if (typeof wargs.subscribe == "string") {
                _widget.subscribe = [];
                _widget.subscribe.push(wargs.subscribe);
            } else {
                _widget.subscribe = wargs.subscribe;
            }
        }     
        if (wargs.args) {
            if (wargs.args.legend) {
                this.defaults.legend = wargs.args.legend;
            }
            if (wargs.args.height) {
                this.defaults.height = wargs.args.height;
                autoSizeH = false;
            }
            if (wargs.args.width) {
                this.defaults.width = wargs.args.width;
                autoSizeW = false;
            }
            if (wargs.args.paddingRight) {
                this.defaults._pr = wargs.args.paddingRight;
            }
            if (wargs.args.paddingLeft) {
                this.defaults._pl = wargs.args.paddingLeft;
            }
            if (wargs.args.paddingBottom) {
                this.defaults._pb = wargs.args.paddingBottom;
            }
            if (wargs.args.xTicks) {
                xTicks = wargs.args.xTicks;
            }
            if (wargs.args.backgroundColor) {
                 this.defaults.backgroundColor = new MochiKit.Color.Color.fromString(wargs.args.backgroundColor);
            }
            if (wargs.args.axisLabelColor) {
                 this.defaults.axisLabelColor = new MochiKit.Color.Color.fromString(wargs.args.axisLabelColor);
            }
            if (wargs.args.colorScheme) {                
                this.defaults.colorScheme = wargs.args.colorScheme;               
            }
            if (wargs.args.axisColor) {
                 this.defaults.axisColor = new MochiKit.Color.Color.fromString(wargs.args.axisColor);
            }
            if (wargs.args.axisLabelColor) {
                 this.defaults.axisLabelColor = new MochiKit.Color.Color.fromString(wargs.args.axisLabelColor);
            }            
            if (wargs.args.axisLineWidth) {
                 this.defaults.axisLineWidth = wargs.args.axisLineWidth;
            }
            if (wargs.args.drawXAxis) {
                 this.defaults.drawXAxis = wargs.args.drawXAxis;
            }
             if (wargs.args.drawYAxis) {
                 this.defaults.drawYAxis = wargs.args.drawYAxis;
            }                
        }     
    };

    this.getContainer = function(wargs) {
          if (!this.container) this.container = document.getElementById(wargs.uuid);
          return this.container;
    };

    this.getChart = function(wargs) {
          if (!this.chart) this.chart = document.getElementById(wargs.uuid + "_chart");
          return this.chart;
    };

    this.getPlotter = function() {
        return this.plotter;
    };

    this.getLegendInfo = function() {
        var count = _widget.layout.options.colorScheme.length;
        var colorScheme = _widget.layout.options.colorScheme;
        var plotNames = MochiKit.Base.keys(_widget.plotter.layout.datasets);
        var colors = [];
        for (var i=0; i < plotNames.length; i++) {
           var c = colorScheme[i];
           colors.push({ label : plotNames[i], color : c.toHexString()});
        }
        return colors;
    };

    this.showLegend = function(location) {
        var _legend = _widget.getLegendInfo();
        var _ul = document.createElement("ul");
        _ul.className = "jmakiChartingLegend";

        for (var _i = 0; _i < _legend.length; _i++) {
            var _li = document.createElement("li");
            _li.style.listStyle = "none";
            _li.className = "jmakiChartingLegendItem";
            var _cs = document.createElement("div");
            _cs.className = "jmakiChartingLegendColorSwatch";
            _cs.style.backgroundColor = _legend[_i].color;
            _li.appendChild(_cs);
            var _lbl = document.createElement("div");
            var _cl = document.createTextNode( _legend[_i].label);
            _lbl.className = "jmakiChartingLegendLabel";
            _lbl.appendChild(_cl);
            _li.appendChild(_lbl);
            _ul.appendChild(_li);
        }
        if (location && location.appendChild) {
            location.appendChild(_ul);
        } else if (document.getElementById(location)){
            document.getElementById(location).appendChild(_ul);
        } else {
            document.body.appendChild(_ul);
        }
    };

    this.clear = function() {
        var plotNames = MochiKit.Base.keys(_widget.plotter.layout.datasets);
        for (var i=0; i < plotNames.length; i++) {
            _widget.layout.removeDataset(plotNames[i]);
        }
        _widget.plotter.clear();
        _widget.plotter.render();
        datasetMappings = {};
        _widget.clearMarkers();
    };

    this.clearPie = function() {       
        _widget.plotter.clear();
        _widget.layout.plotPieDataset("empty", [[0]]);
        _widget.layout.evaluate();
        _widget.plotter.render();
    };

    this.removeDataset = function(id) {
        var name;
        if (id.message) id = id.message;
        if (id.targetId) name = datasetMappings[id.targetId];
        else name = id;
        if (name) {
            _widget.layout.removeDataset(name);
            _widget.layout.evaluate();
            _widget.plotter.clear();
            _widget.plotter.render();
            if (datasetMappings[name])datasetMappings[name] = undefined;
        }
    };

    this.doSubscribe = function(topic, handler) {
        if (!_widget.subs) _widget.subs = [];
        var i = jmaki.subscribe(topic, handler);
        _widget.subs.push(i);
    }

    this.destroy = function() {
        for (var i=0; _widget.subs && i < _widget.subs.length; i++) {
            jmaki.unsubscribe(_widget.subs[i]);
        }
       for (var i=0; markers && i< markers.length; i++) {
           markers[i].divs[0].parentNode.removeChild(markers[i].divs[0]);
           markers[i].divs[1].parentNode.removeChild(markers[i].divs[1]);
       }
    };

    this.plotDatasets = function(_data) {
        this.plotter.clear();
        var dsCount = getDatasetCount();
        for (var ol=0; ol < _data.length; ol++) {
            // now apply the dataset to the xTicks
            var _pdata = [];
            for (var i=0; i < _data[ol].values.length; i++) {
                var da = [];
                da.push(i);
                da.push(_data[ol].values[i]);
               _pdata.push(da);
            }
           var _id = "Set " + dsCount++;
           if (_data[ol].id) {
               _id = _data[ol].id;
           }

           var dsName = _id;
           if (_data[ol].label) dsName = _data[ol].label;
           this.layout.addDataset(dsName, _pdata);
           // alias the id so we can track it internally
           datasetMappings[_id] = _data[ol].label;

        }
        this.layout.evaluate();
        this.plotter.render();
    };

    this.updateAxes = function(obj) {
        if (obj.value) obj = obj.value;

       if ( obj.xAxis && obj.xAxis.labels) {          
        xAxis.labels = obj.xAxis.labels;
       var xTicks = [];
        // add values to the xTicks if they are not provided
        for (var i=0; i < xAxis.labels.length; i++) {
            if (typeof xAxis.labels[i].value == 'number') xAxis.labels[i].v =  xAxis.labels[i].value;
            else if (!xAxis.labels[i].v) xAxis.labels[i].v = i;
            // add the mappings to the table so we can track it
            xTicks.push(xAxis.labels[i]);
        }
        _widget.layout.options.xTicks = xTicks;
        }
    };
    
    function getDatasetCount() {
        var _counter = 0;
        for (var _z in datasetMappings) {
            if (typeof _z != 'function') _counter++;
        }
        return _counter;
    };
    
    this.addDataset = function(_data) {
        _widget.plotter.clear();
        if (_data.value) _data = _data.value;
        var  _id = "Set " + getDatasetCount();   
        if (_data.id) _id = _data.id;
        else if (_data.label) _id = _data.label;
        // gotta remove the previous first
        _widget.removeDataset(_id);

        var _pdata = [];
        for (var i=0; i < _data.values.length; i++) {
            var da = [];
            da.push(i);
            da.push(_data.values[i]);
            _pdata.push(da);
        }
        var xTicksLen = _widget.layout.options.xTicks;

        // check the ticks to see if we have any % based tickes
        for (var i=0; xAxis && xAxis.labels && i < xAxis.labels.length; i++) {
            if (typeof xAxis.labels[i].value == 'string'){
                if (xAxis && xAxis.labels[i].value.indexOf("%") != -1) {
                    var pa = xAxis.labels[i].value.split("%");
                    var percentage = new Number(pa[0]) / 100;
                    var index = Math.floor(_pdata.length * percentage)
                   _widget.layout.options.xTicks[i].v =  index;
                }
            }
        }
        for (var i=0; yAxis && yAxis.labels && i < yAxis.labels.length; i++) {
            if (typeof yAxis.labels[i].value == 'string'){
                if (yAxis.labels[i].value.indexOf("%") != -1) {
                    var pa = yAxis.labels[i].value.split("%");
                    var percentage = new Number(pa[0]) / 100;
                    var index = Math.floor(_pdata.length * percentage)
                   _widget.layout.options.yTicks[i].v =  index;
                }
            }
        }
        var dsName = _id;
        if (_data.label) dsName = _data.label;
        _widget.layout.addDataset(dsName, _pdata);
        _widget.layout.evaluate();
        _widget.plotter.clear();
        _widget.plotter.render();
        datasetMappings[_id] = dsName;
    };

    this.plotPieDataset = function(_data) {

        if (_data.message)_data = _data.message;

         if (_data.length > 0 && _data[0] instanceof Array) {     
            _widget.plotPie(_data[0]);
         } else {
   
             if (_data.value) {
                 _data = _data.value;
                 }          
             _widget.plotPie( [_data]);
         }
    };

    this.plotPie = function(_data) {
        if (!_data[0])return;
        _widget.clear();
        // now apply the dataset to the xTicks
        var _id = "Set " + _widget.plotter.layout.datasets.length;
        if (_data[0].message) _data[0] = _data.message;

       var _mid = _id;
       if (_data[0].label) _id = _data[0].label;
       if (_data[0].id) _mid = _data[0].id;
       datasetMappings[_mid] = _data[0].label;

       var _pdata = [_data[0].values.length];

        // format is [[0, 1], [1, 1], [2, 1], [3, 1], [4, 2]]
        for (var i=0; i < _data[0].values.length; i++) {
            var d = this.idMappings[_data[0].values[i].id];
            v = i;
            if (d) {
                v = d.v;
            }
            var da = [];
            da.push(v);
            da.push(_data[0].values[i]);
            _pdata[v] = da;
        }
        _widget.plotter.clear();
        _widget.layout.addDataset(_id, _pdata);
        _widget.layout.evaluate();
        _widget.plotter.render();
    };

    this.init = function(wargs) {
        this.processArgs(wargs);
        // init the container sizing
        if (autoSizeH || autoSizeW) {
            var _tNode = this.getContainer(wargs).parentNode;
            while(_tNode != null &&
            (_tNode.clientHeight == 0 ||
            typeof _tNode.clientWidth == 'undefined')) {
                _tNode = _tNode.parentNode;
            }
            if (_tNode != null) {
                if(autoSizeW)_widget.defaults.width = _tNode.clientWidth;
                if (autoSizeH)_widget.defaults.height = _tNode.clientHeight;
            }
        }
        // get the data and intialize
        if (wargs.value) {
            var inVal = wargs.value;
            if (inVal.xTicks) xTicks = inVal.xTicks;
            if (inVal.data) data = inVal.data;
            if (inVal.xAxis) xAxis = inVal.xAxis;
            if (inVal.yAxis) yAxis = inVal.yAxis;
            if (inVal && inVal.markers) pmarkers = inVal.markers;
            _widget.initPlotter(wargs);
        } else if (wargs.service) {
             jmaki.doAjax({url: wargs.service, callback: function(req) {
                 if (req.responseText == '') {
                     jmaki.log("Widget Error: Service " + wargs.service + " returned no data");
                     return;
                 }
                 var inVal = eval('(' + req.responseText + ')');
                 if (inVal && inVal.xTicks) xTicks = inVal.xTicks;
                 if (inVal.xAxis) xAxis = inVal.xAxis;
                 if (inVal.yAxis) yAxis = inVal.yAxis;                 
                 if (inVal && inVal.data) data = inVal.data;
                 if (inVal && inVal.makers) pmarkers = inVal.markers;
                 _widget.initPlotter(wargs);
          }});
        } else {
             jmaki.doAjax({url: wargs.widgetDir + "/_widget.json", callback: function(req) {
                 if (req.responseText == '') {
                     jmaki.log("Widget Error: Service " + wargs.service + " returned no data");
                     return;
                 }
                 var config = eval('(' + req.responseText + ')');
                 if (config.value && config.value.defaultValue) inVal = config.value.defaultValue;
                 if (inVal && inVal.xTicks) xTicks = inVal.xTicks;
                 if (inVal && inVal.data) data = inVal.data;
                 if (inVal && inVal.makers) pmarkers = inVal.markers;
                 _widget.initPlotter(wargs);
          }});
        }
    };

    this.initPlotter = function(wargs) {
        if (xTicks) {
            // add values to the xTicks if they are not provided
            for (var i=0; i < xTicks.length; i++) {
                if (xTicks[i].value) xTicks[i].v =  xTicks[i].value;
                else if (!xTicks[i].v) xTicks[i].v = i;
                if (!xTicks[i].id) xTicks[i].id = xTicks.v;
                // add the mappings to the table so we can track it
                _widget.idMappings[xTicks[i].id] = xTicks[i];
            }
        }      
        var xTickCount = undefined;
        var yTickCount = undefined;
        var xRange = undefined;;
        var yRange = undefined;;
        var xOriginIsZero = true;
        var yOriginIsZero = true;
        if (xAxis) {
            if (xAxis.tickCount) xTickCount = xAxis.tickCount;
            // change the orgin automatically if range set
            if (xAxis.range) {
                 xRange = xAxis.range;
                 if (xRange[0] != 0) xOriginIsZero = false;
            }
            if (typeof xAxis.xOriginIsZero != 'undefined') xOriginIsZero = xAxis.xOriginIsZero;
            xTicks = [];
            // add values to the xTicks if they are not provided
            for (var i=0; i < xAxis.labels.length; i++) {
                if (typeof xAxis.labels[i].value == 'number') xAxis.labels[i].v =  xAxis.labels[i].value;
                else if (!xAxis.labels[i].v) xAxis.labels[i].v = i;
                // add the mappings to the table so we can track it
                xTicks.push(xAxis.labels[i]);
            }
        }      
        if (yAxis) {
            if (yAxis.tickCount) yTickCount = yAxis.tickCount;
            // change the orgin automatically if range set
            if (yAxis.range) {
                yRange = yAxis.range;
                if (yRange[0] != 0) yOriginIsZero = false;
            }
            if (typeof yAxis.yOriginIsZero != 'undefined') yOriginIsZero = yAxis.yOriginIsZero;
            if (yAxis.labels) {
                yTicks = [];
                // add values to the xTicks if they are not provided
                for (var i=0; yAxis.labels && i < yAxis.labels.length; i++) {
                    if (yAxis.labels[i].value) yAxis.labels[i].v =  yAxis.labels[i].value;
                    else if (!yAxis.labels[i].v) yAxis.labels[i].v = i;
                    // add the mappings to the table so we can track it
                    yTicks.push(yAxis.labels[i]);
                }
            }
        }        
        _widget.container = this.getContainer(wargs);
        this.getContainer(wargs).style.width = this.defaults.width  + "px";
        this.getContainer(wargs).style.height = this.defaults.height + "px";
        this.getChart(wargs).height = this.defaults.height -2;
        this.getChart(wargs).width = this.defaults.width -2;

        var options = {
           colorScheme: PlotKit.Base.palette(PlotKit.Base.baseColors()[this.defaults.colorScheme]),
           backgroundColor : this.defaults.backgroundColor,
           axisLabelColor : this.defaults.axisLabelColor,
           axisLineColor : this.defaults.axisColor,
           axisLineWidth : this.defaults.axisLineWidth,
           drawXAxis : this.defaults.drawXAxis,
           drawYAxis : this.defaults.drawYAxis,
           padding : {left: this.defaults._pl,
                      right: this.defaults._pr,
                      top: this.defaults._pt,
                      bottom: this.defaults._pb},
           xTicks : xTicks,
           yTicks : yTicks,
           pieRadius : 0.45,
           yOriginIsZero : yOriginIsZero,
           xOriginIsZero : xOriginIsZero,
           yAxis : yRange,
           xAxis : xRange,
           strokeWidth : .1,
           xNumberOfTicks : xTickCount,
           yNumberOfTicks : yTickCount
        };

        if (typeof this.shouldFill != 'undefined') options.shouldFill = this.shouldFill;
        _widget.layout = new PlotKit.Layout(this.chartType, options);
        this.plotter = new PlotKit.SweetCanvasRenderer(this.getChart(), _widget.layout, options);
        // plot data if we have it otherwise wait for a potential push
        if (data)_widget.plot(data);
        if (_widget.defaults.legend)_widget.showLegend(_widget.defaults.legend);
        if (pmarkers) {
            for (var i=0;i < pmarkers.length; i++) {
                _widget.addMarker(pmarkers[i]);
            }
        }
    };

    function getPosition(_e){
        var pX = 0;
        var pY = 0;
        if(_e.offsetParent) {
            while(true){
                pY += _e.offsetTop;
                pX += _e.offsetLeft;
                if(_e.offsetParent == null){
                    break;
                }
                _e = _e.offsetParent;
            }
        } else if(_e.y) {
                pY += _e.y;
                pX += _e.x;
        }
        return {x: pX, y: pY};
    }

    function clone(t) {
       var obj = {};
       for (var i in t) {
            obj[i] = t[i];
       }
       return obj;
    }

    function processActions(_t, _pid, _type, _value) {
        if (_t) {
            var _topic = _widget.publish;
            var _m = {widgetId : _widget.wargs.uuid, type : _type, targetId : _pid};
            if (typeof _value != "undefined") _m.value = _value;
            var action = _t.action;
            if (!action) _topic = _topic + "/" + _type;
            if (action && action instanceof Array) {
              for (var _a=0; _a < action.length; _a++) {
                  var payload = clone(_m);
                  if (action[_a].topic) payload.topic = action[_a].topic;
                  else payload.topic = publish;
                  if (action[_a].message) payload.message = action[_a].message;
                  jmaki.publish(payload.topic,payload);
              }
            } else {
              if (action && action.topic) {
                  _topic = _m.topic = action.topic;
              }
              if (action && action.message) _m.message = action.message;
              jmaki.publish(_topic,_m);
            }
        }
    }

    /**
     *  Add a marker to a given chart taking into account the different chart types.
     * Marker format is expected to be in format like:
     *
     * { targetId : 'gray', label : 'I am {value}', index : 8}
     * 
     *  targetId refers to the data set id.
     *  index is the index in the dataset where to place the marker
     *  labels is the label to be used. This may include markup. If a @{value} or {value} is encountered it will be replaced with the value at the given index.
     */
    this.addMarker = function (target) {      
        if (typeof target == 'undefined') return;
        if (target.message) target = target.message;
        var targetValue;
        var targetPoint;
        var targetIndex = target.index;

        var targetId = target.targetId;             
        targetId =  datasetMappings[targetId];
        var _set = _widget.layout.points;
        // for bar charts use bars
        if (_widget.chartType == "bar") _set = _widget.layout.bars;
        //  slices 
        if (_widget.chartType == "pie") _set = _widget.layout.slices;
           
        if (targetIndex < _set.length &&
            _set.length > 0) {       
            for (var i=0; i < _set.length; i++ ) {
                if ((_set[i].name == targetId || _widget.chartType == "pie") &&
                   _set[i].xval == targetIndex) {                      
                   targetPoint = _set[i];
                   break;
                }
            }
            if (targetPoint)targetValue = targetPoint.yval;
        } else {
            jmaki.log("jMaki Charting addMarker " + target.index + " not found.");
        }     
        if (!targetPoint) {
            return;
        }
        var loc = getPosition(_widget.container);

        var mDiv = document.createElement("div");
        // give the marker a uuid so we can remove it
        if (target.id) {
            mDiv.markerId = target.id;
            // prevent
            if (_widget.getMarker(target.id)) {              
                jmaki.log("jMaki Charting: addMaker. Can't add marker with duplicate id");
                return;
            }
        } else mDiv.markerId = jmaki.genId();
        mDiv.markerValue = targetValue;
        if (target.value) mDiv.action = target.value.action;
        else if (target.action) mDiv.action = target.action;
        mDiv.className = "jmaki-charting-marker-label";
        var label;
        if (target.value) label = target.value.label;
        else if (target.label) label = target.label;
        label = label.replace('@{value}', targetValue, 'g');
        label = label.replace('{value}', targetValue, 'g');
        mDiv.appendChild(document.createTextNode(label));

        var mADiv = document.createElement("div");
        mADiv.className = "jmaki-charting-marker-pointer"; 
        _widget.container.parentNode.appendChild(mDiv);
        _widget.container.parentNode.appendChild(mADiv);
        var markerCount = 0;
        if (typeof markerPoints[target.index] == 'undefined') {
            markerPoints[target.index] = markerCount;
        } else if (markerPoints[target.index] != 0) {    
            markerCount = markerPoints[target.index] +1;
            markerPoints[target.index] = markerCount;                     
        }
        var mx;
        var my;
        if (_widget.chartType == "pie") {
            // find coordinates on a pie charts
            var _291=_widget.plotter.area.x+_widget.plotter.area.w*0.5;
            var _292=_widget.plotter.area.y+_widget.plotter.area.h*0.5;
            var _293=Math.min(_widget.plotter.area.w*_widget.plotter.options.pieRadius+10,_widget.plotter.area.h*_widget.layout.options.pieRadius+10);
            var _294=_widget.layout.options.axisLabelWidth;
            _295=targetPoint;
            var _296=(_295.startAngle+_295.endAngle)/2;
            var _297=_296;
            if(_297>Math.PI*2){
            _297=_297-Math.PI*2;
            }else{
            if(_297<0){
            _297=_297+Math.PI*2;
            }
            }
            mx=_291+Math.sin(_297)*(_293+10);
            my=_292-Math.cos(_297)*(_293+10);     
        } else {
        // find coordinates on bar / line / area chart 
            mx =  (targetPoint.x  *  _widget.plotter.area.w )  + _widget.layout.options.padding.left -1;
            my = (targetPoint.y *  _widget.plotter.area.h) - _widget.plotter.options.axisLabelFontSize + _widget.plotter.area.y ;
        }
        // end pie charts
        
        mDiv.style.left = loc.x  + mx + "px";
        mDiv.style.top = (loc.y -27 + my) - (markerCount * 15) + "px";
        mADiv.style.left = loc.x + mx + "px";
        mADiv.style.top = loc.y  -14 + my - (markerCount * 15) + "px";
        mDiv.onclick = function() {
            processActions(this, this.markerId, 'onClick', this.markerValue);
        }
        mDiv.style.visibility = "visible";
        mADiv.style.visibility = "visible";
        // track the markers so they can be removed
        if (!markers) markers = [];
        markers.push({id : mDiv.markerId, divs : [mDiv, mADiv], index : targetIndex});
   };

  this.clearMarkers = function() {
       for (var i=0; markers && i< markers.length; i++) {
           markers[i].divs[0].parentNode.removeChild(markers[i].divs[0]);
           markers[i].divs[1].parentNode.removeChild(markers[i].divs[1]);
       }
       markers = [];
       markerPoints = {};
   };
   
   this.getMarker = function(markerId) {
       for (var i=0; markers && i< markers.length; i++) {
           if (markers[i].id == markerId) {
             return markers[i];
           }
       }
   };
   
   this.respositionMarkersAtIndex = function(index) {    
        var markerCount = 0;
        delete markerPoints[index];
        var markerPoint
        var count;
        // find the point on the chart at this point
        var targetPoint;
         
        if (index < _widget.layout.points.length &&
            _widget.layout.points.length > 0) {
            for (var i=0; i < _widget.layout.points.length; i++ ) {               
                if (_widget.layout.points[i].xval == index) {
                   targetPoint = _widget.layout.points[i];
                   break;
                }
            }
            if (targetPoint)targetValue = targetPoint.yval;
        }       
        if (!targetPoint) {
            return;
        }       
        var mx =  (targetPoint.x  *  _widget.plotter.area.w )  + _widget.layout.options.padding.left -1;
        var my = (targetPoint.y *  _widget.plotter.area.h) - _widget.plotter.options.axisLabelFontSize + _widget.plotter.area.y ;
        var loc = getPosition(_widget.container);
        
       for (var i=0; markers && i< markers.length; i++) {
           if (index == markers[i].index) {               
             markers[i].divs[0].style.left = loc.x  + mx + "px";
             markers[i].divs[0].style.top = (loc.y -27 + my) - (markerCount * 15) + "px";
             markers[i].divs[1].style.left = loc.x + mx + "px";
             markers[i].divs[1].style.top = loc.y  -14 + my - (markerCount * 15) + "px";
             markerCount++;
           }
       }
       // now reset the marker count
       markerPoints[index] = markerCount; 
   }; 

   this.refreshMarkers = function() {
       for (var i in markerPoints) {
           if (typeof markerPoints[i] == "number") {           
             _widget.respositionMarkersAtIndex(i);
           }
       }
   }; 

   this.removeMarker = function(target) {
       if (!markers) jmaki.log("jMaki Charting::remove no markers");
       var makerId;
       if (target.message) target = message;
       if (target.targetId) markerId = target.targetId;
       else markerId = target;
       for (var i=0; markers && i< markers.length; i++) {
           if (markers[i].id == markerId) {
             var tIndex = markers[i].index;
             markers[i].divs[0].parentNode.removeChild(markers[i].divs[0]);
             markers[i].divs[1].parentNode.removeChild(markers[i].divs[1]);           
             markers.splice(i,1);
             markerPoints[tIndex] = 0;
             _widget.respositionMarkersAtIndex(tIndex);
             break;             
           }
       }
   };
};

/*
*  An updated extend function to allow for copying of base resources.
*/
jmaki.widgets.jmaki.charting.extend = function(_src, _par) {
        _src.prototype = new _par();
        _src.prototype.constructor = _src;
        _src.superclass = _par.prototype;
        for (i in _src.prototype) {
            _src[i] = _src.prototype[i];
        }
    };