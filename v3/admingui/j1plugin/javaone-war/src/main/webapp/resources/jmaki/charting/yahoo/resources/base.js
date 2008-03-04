jmaki.namespace("jmaki.widgets.jmaki.charting.yahoo");

/**
*  This is the base class for all yahoo charts
*  jMaki widgets.
*
*  @constructor
*  @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
*/
jmaki.widgets.jmaki.charting.yahoo.base = function() {
    var YUI_SWF_URL = "/../resources/yahoo/v2.4.1/charts/assets/charts.swf";
    var YUI_EXPRESS_INSTALL = "/../resources/expressinstall.swf";
    var self = this;
    var plotter = null;
    var dataSource = null;
    var YDom = YAHOO.util.Dom;
    var autoSizeH = true;
    var autoSizeW = true;
    var pmarkers = null;
    var xAxis;
    var yAxis;
    var xTicks;
    var data;
    var counter = 0;
    
    this.cfg = {
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
                //not implemented
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
    * Returns your plotter
    * @return {Object} can be a YAHOO.widget.[Bar|Line|Pie]Chart object
    */
    this.getPlotter = function() {
        return self.plotter;
    };
    
    /**
    * Clears current chart
    */
    this.clear = function() {
        if(!self.plotter) {
            jmaki.log("Plotter is not initialized");
            return;
        }
        //and clear data
        data = [];
        var cd = self.createChartDataFromModel(xAxis,data);
        self.plotter.set("series",cd.cSeries);
        self.plotter.set("dataSource", cd.dataSource);
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
        //for (var i=0; markers && i< markers.length; i++) {
        //    markers[i].divs[0].parentNode.removeChild(markers[i].divs[0]);
        //    markers[i].divs[1].parentNode.removeChild(markers[i].divs[1]);
        //}
    };
    
    /**
    * Plot Datasets
    * @param aData {Object} the dataset(s) to be plotted
    */
    this.plotDatasets = function(data) {
        if(!self.plotter) {
            jmaki.log("Plotter is not initialized");
            return;
        }
        
        var cd = self.createChartDataFromModel(xAxis,data);
        self.plotter.set("series",cd.cSeries);
        self.plotter.set("dataSource",cd.dataSource);
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
        if(!self.plotter) {
            jmaki.log("Plotter is not initialized");
            return;
        }
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
            var cd = self.createChartDataFromModel(xAxis,data);
            self.plotter.set("series",cd.cSeries);
            self.plotter.set("dataSource",cd.dataSource);
        } else if (isUpdate && !found) {
            jmaki.log("dataset with id '" + message.id + "' not found in chart");
        } else {
            jmaki.log("dataset with id '" + message.id + "' is already in chart");
        }
        
    };
    
    /**
    * Remove a dataset
    * @param payload {Object} the payload with the 'dataset id' in its message
    */
    this.removeDataset = function(payload) {
        if(!self.plotter) {
            jmaki.log("Plotter is not initialized");
            return;
        }
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
            var cd = self.createChartDataFromModel(xAxis,data);
            self.plotter.set("series",cd.cSeries);
            self.plotter.set("dataSource",cd.dataSource);
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
        if(xAxis && xAxis.labels) {
            var labels = xAxis.labels;
            for(i = 0; i < labels.length; i++) {             
                cData.push({x:labels[i].label});
            }
            cSchema.fields.push("x");
            cSeries.push({xField:"x",displayName:(xAxis.title) ? xAxis.title : ""});
        } else
        {
            jmaki.log("incompatible data model");
        }
       
        if(data) {        
            for(var i = 0; i < data.length; i++) {             
                var dataset = data[i];
                cSeries.push({yField:"y"+i,displayName:(dataset.label) ? dataset.label : ""});
                cSchema.fields.push("y" + i);                
                for(j = 0; j < dataset.values.length; j++) {
                    var rec = cData[j];
                    rec["y" + i] = dataset.values[j];
                }
            }
        } else
        {
            jmaki.log("incompatible data model");
        }
        var dataSource = new YAHOO.util.DataSource( cData );
        dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        dataSource.responseSchema = cSchema;
        return {dataSource:dataSource,cSeries:cSeries};
    };
    
    /**
    * Create (Bar/Line/Pie) Chart
    * @param wargs {Object} widget arguments
    * @param dataSource {Object} YAHOO.util.DataSource object
    * @param cSeries {Array} chart series array
    */
    var createChart = function(wargs,dataSource,cSeries) {
        var chart = null;
        //--- chart
        if("bar" == self.chartType) {
            //Bar chart
            chart = new YAHOO.widget.ColumnChart( wargs.uuid, dataSource, {
                xField: "x",
                series: cSeries,
                expressInstall: wargs.widgetDir + YUI_EXPRESS_INSTALL
            });
        } else if("line" == self.chartType)
        {
            //Line chart
            chart = new YAHOO.widget.LineChart( wargs.uuid, dataSource,{
                xField: "x",
                series: cSeries,
                expressInstall: wargs.widgetDir + YUI_EXPRESS_INSTALL
            });
        } else if("pie" == self.chartType)
        {
            //Pie chart
            chart = new YAHOO.widget.PieChart( wargs.uuid, dataSource, {
                dataField: "y0",
                categoryField: "x",
                series: [{}],
                expressInstall: wargs.widgetDir + YUI_EXPRESS_INSTALL
            });
            
        } else
        {
            jmaki.log("chartType '" + self.chartType + "' is not implemented");
            return;
        }
        return chart;
    };
    
    
    /**
    * Initialize plotter (aka chart)
    * @param wargs {Object} widget arguments
    */
    this.initPlotter = function(wargs) {
        self.container = this.getContainer(wargs);
        YDom.setStyle(wargs.uuid, "width", this.cfg.width  + "px");
        YDom.setStyle(wargs.uuid, "height", this.cfg.height  + "px");
        
        YAHOO.widget.Chart.SWFURL = wargs.widgetDir + YUI_SWF_URL;

        //---data
        
        var cd = self.createChartDataFromModel(xAxis,data);
        self.plotter = createChart(wargs,cd.dataSource, cd.cSeries);
        
        
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