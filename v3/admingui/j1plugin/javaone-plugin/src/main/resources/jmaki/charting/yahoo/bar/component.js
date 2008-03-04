jmaki.namespace("jmaki.widgets.jmaki.charting.yahoo.bar");

/**
* Y! bar chart jMaki widget 
* 
* Most of the implementation is refactored into base class
*
* @constructor
* @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
*/
jmaki.widgets.jmaki.charting.yahoo.bar.Widget = function(wargs) {
    //Note: This chart extends from jmaki.widgets.jmaki.charting.yahoo.base (base.js)
    jmaki.widgets.jmaki.charting.extend(this,jmaki.widgets.jmaki.charting.yahoo.base);
    var self = this;
    this.prototype.shouldFill = true;         
    this.prototype.chartType = "bar";
    this.prototype.publish = "/jmaki/charting/yahoo/bar";    
    this.prototype.subscribe = ["/jmaki/charting/yahoo/bar", "/chart"]; 
    
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