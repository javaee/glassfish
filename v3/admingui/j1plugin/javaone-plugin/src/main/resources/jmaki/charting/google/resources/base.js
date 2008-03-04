jmaki.namespace("jmaki.widgets.jmaki.charting.google");

/**
*  This is the base class for all Google charts
*  jMaki widgets.
*
*  Google charts API: http://code.google.com/apis/chart/
*  @constructor
*  @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
*/
jmaki.widgets.jmaki.charting.google.base = function() {
    var self = this;
    var autoSizeH = true;
    var autoSizeW = true;
    var pmarkers = null;
    var xAxis;
    var yAxis;
    var xTicks;
    var data;
    var wargs = null;
    this.COLORS = [
        "76A4FB", /* light blue */
        "FF0000", /* red */
        "00FF00", /* green */
        "0000FF", /* blue */
        "808080", /* gray */
        "000000" /* black */
    ];
    this.cfg = {
        legend : false,
        height : 250,
        width : 700,
        _pr : 30,
        _pl : 40,
        _pt : 30,
        _pb : 30,
        backgroundColor : 0,
        axisLabelColor : 0,
        axisColor : 0,
        axisLineWidth : 0.1,
        drawXAxis : true,
        drawYAxis : true,
        colorScheme : 0
    };
    
    /**
    * Process widget arguments
    */
    this.processArgs = function(wargs) {
        self.wargs = wargs;
        if (wargs.publish) {
            self.publish = wargs.publish;
        }
        if (wargs.subscribe){
            if (typeof wargs.subscribe == "string") {
                self.subscribe = [];
                self.subscribe.push(wargs.subscribe);
            } else
            {
                self.subscribe = wargs.subscribe;
            }
        }
        if (wargs.args) {
            var args = wargs.args;
            if (args.legend) {
                this.cfg.legend = args.legend;
            }
            if (args.height) {
                this.cfg.height = args.height;
                autoSizeH = false;
            }
            if (args.width) {
                this.cfg.width = args.width;
                autoSizeW = false;
            }
            if (args.paddingRight) {
                //not implemented
                this.cfg._pr = args.paddingRight;
            }
            if (args.paddingLeft) {
                //not implemented
                this.cfg._pl = args.paddingLeft;
            }
            if (args.paddingBottom) {
                //not implemented
                this.cfg._pb = args.paddingBottom;
            }
            if (args.xTicks) {
                //not implemented
                xTicks = args.xTicks;
            }
            if (args.backgroundColor) {
                //not implemented
                this.cfg.backgroundColor = args.backgroundColor;
            }
            if (args.axisLabelColor) {
                //not implemented
                this.cfg.axisLabelColor = args.axisLabelColor;
            }
            if (args.colorScheme) {
                //not implemented
                this.cfg.colorScheme = args.colorScheme;
            }
            if (args.axisColor) {
                //not implemented
                this.cfg.axisColor = args.axisColor;
            }
            if (args.axisLabelColor) {
                //not implemented
                this.cfg.axisLabelColor = args.axisLabelColor;
            }
            if (args.axisLineWidth) {
                //not implemented
                this.cfg.axisLineWidth = args.axisLineWidth;
            }
            if (args.drawXAxis) {
                //not implemented
                this.cfg.drawXAxis = args.drawXAxis;
            }
            if (args.drawYAxis) {
                //not implemented
                this.cfg.drawYAxis = args.drawYAxis;
            }
        }
    };
    
    /**
    * Returns your chart div container
    */
    this.getContainer = function(wargs) {
        if (!this.container) {
            this.container = document.getElementById(wargs.uuid);
        }
        return this.container;
    };
    
    /**
    * Returns null (since Google has no JS object)
    * @return null
    */
    this.getPlotter = function() {
        return null;
    };
    
    /**
    * Clears current chart
    */
    this.clear = function() {
        //and clear data
        data = [];
        var imgEl = self.getContainer();
        if(imgEl) {
            imgEl.src = self.createChartDataFromModel(xAxis,data);
        }
    };
    
    /**
    * helper function for an action to subscribe to a topic
    * @param aData {Object} the dataset to be plotted
    */
    this.doSubscribe = function(topic, handler) {
        if (!self.subs) {
            self.subs = [];
        }
        var i = jmaki.subscribe(topic, handler);
        self.subs.push(i);
    };
    
    /**
    * Called on destruction?
    * // gmurray71 : yes. Destory is called on all widgets upon distruction.
    */
    this.destroy = function() {
        for (var i=0; self.subs && i < self.subs.length; i++) {
            jmaki.unsubscribe(self.subs[i]);
        }
        for (var i=0; markers && i< markers.length; i++) {
            markers[i].divs[0].parentNode.removeChild(markers[i].divs[0]);
            markers[i].divs[1].parentNode.removeChild(markers[i].divs[1]);
        }
    };
    
    /**
    * Plot Datasets
    * @param aData {Object} the dataset(s) to be plotted
    */
    this.plotDatasets = function(data) {
        var imgEl = self.getContainer();
        if(imgEl) {
            imgEl.src = self.createChartDataFromModel(xAxis,data);
        }
    };
    
    
    /**
    * Plot Pie Datasets (used only by *.pie)
    * @param obj {Object} the data model object for updating axis
    */
    this.updateAxes = function(obj) {
        //TODO implement once functionality is available
        jmaki.log("'updateAxes' is not currently implemented");
    };
    
    /**
    * Returns current data set count
    * @return {Number} data set count
    */
    function getDatasetCount() {
        return data.length;
    }
    
    /**
    * Update a  dataset
    * @param payload {Object} the payload with the dataset in its message
    */
    this.updateDataset = function(payload) {
        self.addDataset(payload, true);
    };
    
    /**
    * Add a new datasets
    * @param payload {Object} the payload with the dataset in its message
    */
    this.addDataset = function(payload, update) {
        var message = (typeof payload.message == "object") ? payload.message : payload;
        var found = false;
        for(var i = 0; i < data.length; i++) {
            if(message.id && message.id === data[i].id) {
                found = true;
                break;
            }
        }
        var isUpdate = (typeof update == 'boolean' && update == true);
        // account for updates
        if((!isUpdate && !found) || (isUpdate && found)) {
            // remove the dataset
            if (isUpdate) {
                self.removeDataset({targetId : message.id});
            }
            //add the dataset right now
            if("pie" == self.chartType) {
                data = [message];
            } else
            {
                data.push(message);
            }
            var imgEl = self.getContainer();
            if(imgEl) {
                imgEl.src = self.createChartDataFromModel(xAxis,data);
            }
            
        } else if (isUpdate && !found)
        {
            jmaki.log("dataset with id '" + message.id + "' not found in chart");
        } else
        {
            jmaki.log("dataset with id '" + message.id + "' is already in chart");
        }
        
    };
    
    /**
    * Remove a dataset
    * @param payload {Object} the payload with the 'dataset id' in its message
    */
    this.removeDataset = function(payload) {
        var message = (typeof payload.message == "object") ? payload.message : payload;
        var found = false;
        var i;
        for(i = 0; i < data.length; i++) {
            if(message.targetId && message.targetId === data[i].id) {
                found = true;
                break;
            }
        }
        if(found) {
            //remove the dataset right now
            data.splice(i,1);
            var imgEl = self.getContainer();
            if(imgEl) {
                imgEl.src = self.createChartDataFromModel(xAxis,data);
            }
        } else
        {
            jmaki.log("dataset with id '" + message.id + "' is not found");
        }
        
    };
    
    /**
    * Process widget arguments and initialize chart from value/service/other
    * @param {Object} wargs widget arguments
    */
    this.init = function(wargs) {
        var v;
        this.processArgs(wargs);
        
        // init the container sizing
        //TODO proper container resizing like yahoo.flashmap...
        
        //Google restriction: maximum width or height must be less than 10000
        if(self.cfg.width > 1000) {
            self.cfg.width = 1000;
        }
        if(self.cfg.height > 1000) {
            self.cfg.height = 1000;
        }
        
        // get the data and intialize
        if (wargs.value) {
            v = wargs.value;
            if (v.xTicks) {
                xTicks = v.xTicks;
            }
            if (v.data) {
                data = v.data;
            }
            if (v.xAxis) {
                xAxis = v.xAxis;
            }
            if (v.yAxis) {
                yAxis = v.yAxis;
            }
            if (v.markers) {
                pmarkers = v.markers;
            }
            self.initPlotter(wargs);
            
        } else if (wargs.service)
        {
            jmaki.doAjax({url: wargs.service, callback: function(req) {
                var response = req.responseText;
                if (response === '') {
                    jmaki.log("Widget Error: Service " + wargs.service + " returned no data");
                    return;
                }
                v = eval( "(" + response + ")");
                if(v) {
                    if (v.xTicks) {
                        xTicks = v.xTicks;
                    }
                    if (v.xAxis) {
                        xAxis = v.xAxis;
                    }
                    if (v.yAxis) {
                        yAxis = v.yAxis;
                    }
                    if (v.data) {
                        data = v.data;
                    }
                    if (v.markers) {
                        pmarkers = v.markers;
                    }
                    self.initPlotter(wargs);
                }
            }});
        } else
        {
            jmaki.doAjax({url: wargs.widgetDir + "/self.json", callback: function(req) {
                var response = req.responseText;
                if (response) {
                    jmaki.log("Widget Error: Service " + wargs.service + " returned no data");
                    return;
                }
                var config = jmaki.JSON.deserialize(response);
                if (config.value && config.value.defaultValue) {
                    v = config.value.defaultValue;
                    if(v) {
                        if (v.xTicks) {
                            xTicks = v.xTicks;
                        }
                        if (v.data) {
                            data = v.data;
                        }
                        if (v.markers) {
                            pmarkers = v.markers;
                        }
                    }
                }
                self.initPlotter(wargs);
            }});
        }
        
        self.subs = [];
        for (var _i=0; _i < self.subscribe.length; _i++) {
            self.doSubscribe(self.subscribe[_i] + "/plot", self.plot);
            self.doSubscribe(self.subscribe[_i]  + "/addDataset", self.addDataset);
            self.doSubscribe(self.subscribe[_i]  + "/removeDataset", self.removeDataset);
            self.doSubscribe(self.subscribe[_i]  + "/updateAxes", self.updateAxes);
            self.doSubscribe(self.subscribe[_i]  + "/updateDataset", self.updateDataset);
            self.doSubscribe(self.subscribe[_i] + "/clear", self.clear);
            self.doSubscribe(self.subscribe[_i]  + "/addMarker", self.addMarker);
            self.doSubscribe(self.subscribe[_i]  + "/removeMarker", self.removeMarker);
        }
    };
    
    /**
    * Create Datasource and chart series from model
    * @param xAxis {Object} chart X-axis  (see model)
    * @param data {Object} chart datasets (see model)
    */
    this.createChartDataFromModel = function(xAxis,data) {
        var cData = [];
        var cSchema = {fields:[]};
        var cSeries = [];
        var i,j;
        var url = "http://chart.apis.google.com/chart?";
        
        //chart type
        if("area" == self.chartType) {
            url += "cht=lc";
        } else if("bar" == self.chartType) 
        {
            url += "cht=bvg";
        } else if("line" == self.chartType)
        {
            url += "cht=lc";
        } else if("pie" == self.chartType)
        {
            url += "cht=p";
        } else
        {
            url += "cht=p3";
        }
        
        //chart size
        url += "&chs=" + self.cfg.width + "x" + self.cfg.height;
        
        //only x & y axis
        url += "&chxt=x,y";
        
        var xUrl = "";
        if(xAxis && xAxis.labels) {
            var labels = xAxis.labels;
            for(i = 0; i < labels.length; i++) {
                if(i == 0) {
                    xUrl += "0:|";
                }
                xUrl += labels[i].label;
                if(i < labels.length - 1) {
                    xUrl += "|";
                }
            }
        } else
        {
            jmaki.log("incompatible data model");
        }
        var yUrl = "";
        if(yAxis && yAxis.labels) {
            var labels = yAxis.labels;
            for(i = 0; i < labels.length; i++) {
                if(i == 0) {
                    yUrl += "1:|";
                }
                yUrl += labels[i].label;
                if(i < labels.length - 1) {
                    yUrl += "|";
                }
            }
        } else
        {
            jmaki.log("incompatible data model");
        }
        //combine x and y-axis
        url += "&chxl=" + xUrl + "|" + yUrl;
        
        var datasetCount = 0;
        var min = Number.MAX_VALUE;
        var max = Number.MIN_VALUE;
        if(data) {
            var l = "&chm=";
            var colorUrl = "&chco=";
            var legendUrl = "&chdl=";
            for(i = 0; i < data.length; i++) {
                var dataset = data[i];
                if(i == 0) {
                    url += "&chd=t:";
                }
                var datasetLen = dataset.values.length;
                for(j = 0; j < datasetLen; j++) {
                    var v = dataset.values[j]; 
                    url += v;
                    if(max < v) {
                        max = v;
                    }
                    if(min > v) {
                        min = v;
                    }
                    if(j < datasetLen - 1) {
                        url += ",";
                    }
                }
                datasetCount+=datasetLen;
                if("area" == self.chartType) {
                    l += "b," + self.COLORS[i] + "," + i + "," + (i+1) + ",0";
                } 
                colorUrl += self.COLORS[i];
                legendUrl += (dataset.label) ? dataset.label : "";
                if(i < data.length - 1) {
                    url += "|";
                    colorUrl += ",";
                    legendUrl += "|";
                    l += "|";
                }
            }
            url += colorUrl;
            url += legendUrl;
            if("area" == self.chartType) {
                url += l;
            }
            
        } else
        {
            jmaki.log("incompatible data model");
        }
        if("bar" == self.chartType) {
            //calculate bar width to avoid clipping... yup it is not auto-calculated atm
            var barWidth = Math.round((datasetCount > 0) ? (self.cfg.width-30) / (datasetCount * 1.4) : 10);
            url += "&chbh="+barWidth + ",0";
        }
        
        //specify y-axis ranges...
        if("bar" == self.chartType) {
            min = 0;
        }
        url += "&chxr=1," + min + "," + max;
        return url;
    };
    
    /**
    * Initialize plotter (aka chart)
    * @param wargs {Object} widget arguments
    */
    this.initPlotter = function(wargs) {
        self.container = this.getContainer(wargs);
        
        // plot data if we have it otherwise wait for a potential push
        if (data) {
            self.plot(data);
        }
        if (self.cfg.legend) {
            self.showLegend(self.cfg.legend);
        }
        if (pmarkers) {
            for (i = 0; i < pmarkers.length; i++) {
                self.addMarker(pmarkers[i]);
            }
        }
    };
    
    this.getLegendInfo = function() {
        //TODO implement once functionality is available
        jmaki.log("'getLegendInfo' is not currently implemented");
    };
    
    this.showLegend = function(location) {
        //TODO implement once functionality is available
        jmaki.log("'showLegend' is not currently implemented");
    };
    
    this.addMarker = function (target) {
        //TODO implement once functionality is available
        jmaki.log("'addMarker' is not currently implemented");
    };
    
    this.clearMarkers = function() {
        //TODO implement once functionality is available
        jmaki.log("'clearMarkers' is not currently implemented");
    };
    
    this.getMarker = function(markerId) {
        //TODO implement once functionality is available
        jmaki.log("'getMarker' is not currently implemented");
    };
    
    this.respositionMarkersAtIndex = function(index) {
        //TODO implement once functionality is available
        jmaki.log("'respositionMarkersAtIndex' is not currently implemented");
    };
    
    this.refreshMarkers = function() {
        //TODO implement once functionality is available
        jmaki.log("'refreshMarkers' is not currently implemented");
    };
    
    this.removeMarker = function(target) {
        //TODO implement once functionality is available
        jmaki.log("'removeMarker' is not currently implemented");
    };
    
};

/*
*  An updated extend function to allow for copying of base resources.
* @param {Object} src
* @param {Object} Parent
*/
jmaki.widgets.jmaki.charting.extend = function(src, Parent) {
    src.prototype = new Parent();
    src.prototype.constructor = src;
    src.superclass = Parent.prototype;
    for (var i in src.prototype) {
        src[i] = src.prototype[i];
    }
};