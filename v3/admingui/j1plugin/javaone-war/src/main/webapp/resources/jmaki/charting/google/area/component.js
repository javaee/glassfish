jmaki.namespace("jmaki.widgets.jmaki.charting.google.area");

/**
* Google area chart jMaki widget 
* 
* Most of the implementation is refactored into base class
*
* @constructor
* @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
*/
jmaki.widgets.jmaki.charting.google.area.Widget = function(wargs) {
    //Note: This chart extends from jmaki.widgets.jmaki.charting.google.base (base.js)
    jmaki.widgets.jmaki.charting.extend(this,jmaki.widgets.jmaki.charting.google.base);
    var self = this;
    this.prototype.shouldFill = true;         
    this.prototype.chartType = "area";
    this.prototype.publish = "/jmaki/charting/google/area";    
    this.prototype.subscribe = ["/jmaki/charting/google/area", "/chart"]; 
    
    // set the plot to be the plot seriese
    this.prototype.plot = this.plotDatasets;
    
    
    /**
    * Called later once jmaki has been loaded
    */
    var initOnPageLoad = function() {
        self.init(wargs);
    }
    jmaki.subscribe("/jmaki/runtime/loadComplete", initOnPageLoad);
    
};