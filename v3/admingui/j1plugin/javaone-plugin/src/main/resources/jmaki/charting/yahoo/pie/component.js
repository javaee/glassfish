jmaki.namespace("jmaki.widgets.jmaki.charting.yahoo.pie");

/**
* Y! pie chart jMaki widget 
* 
* Most of the implementation is realready refactored into base class
*
* @constructor
* @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
*/
jmaki.widgets.jmaki.charting.yahoo.pie.Widget = function(wargs) {
    //Note: This chart extends from jmaki.widgets.jmaki.charting.yahoo.base (base.js)
    jmaki.widgets.jmaki.charting.extend(this,jmaki.widgets.jmaki.charting.yahoo.base);
    var self = this;
    this.prototype.chartType = "pie";
    this.prototype.shouldFill = true;
    this.prototype.publish = "/jmaki/charting/yahoo/pie";
    this.prototype.subscribe = ["/jmaki/charting/yahoo/pie", "/chart"];

    // set the plot to be the plot series
    this.prototype.plot = this.plotDatasets;

    /**
    * Called later once jmaki has been loaded
    */
    var initOnPageLoad = function() {
        self.init(wargs);
    }
    jmaki.subscribe("/jmaki/runtime/loadComplete", initOnPageLoad);
};
