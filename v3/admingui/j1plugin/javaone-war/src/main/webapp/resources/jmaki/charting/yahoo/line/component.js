jmaki.namespace("jmaki.widgets.jmaki.charting.yahoo.line");

/**
* Y! line chart jMaki widget 
* 
* Most of the implementation is refactored into base class
*
* @constructor
* @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
*/
jmaki.widgets.jmaki.charting.yahoo.line.Widget = function(wargs) {
    //Note: This chart extends from jmaki.widgets.jmaki.charting.yahoo.base (base.js)    
    jmaki.widgets.jmaki.charting.extend(this,jmaki.widgets.jmaki.charting.yahoo.base);
    var self = this;
    this.prototype.shouldFill = false;
    this.prototype.chartType = "line";
    this.prototype.publish = "/jmaki/charting/yahoo/line";
    this.prototype.subscribe = ["/jmaki/charting/yahoo/line", "/chart"];
    
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