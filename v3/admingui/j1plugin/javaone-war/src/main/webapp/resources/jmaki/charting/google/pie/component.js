jmaki.namespace("jmaki.widgets.jmaki.charting.google.pie");

/**
* Google pie chart jMaki widget 
* 
* Most of the implementation is realready refactored into base class
*
* @constructor
* @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
*/
jmaki.widgets.jmaki.charting.google.pie.Widget = function(wargs) {
    //Note: This chart extends from jmaki.widgets.jmaki.charting.google.base (base.js)
    jmaki.widgets.jmaki.charting.extend(this,jmaki.widgets.jmaki.charting.google.base);
    var self = this;
    this.prototype.chartType = "pie";
    this.prototype.shouldFill = true;
    this.prototype.publish = "/jmaki/charting/google/pie";
    this.prototype.subscribe = ["/jmaki/charting/google/pie", "/chart"];

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
