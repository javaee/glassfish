jmaki.namespace("jmaki.widgets.jmaki.charting.plotkit.line");

jmaki.widgets.jmaki.charting.plotkit.line.Widget = function(wargs) {

    jmaki.widgets.jmaki.charting.extend(this,jmaki.widgets.jmaki.charting.plotkit.base);
    
    this.prototype.shouldFill = false;                
    this.prototype.chartType = "line";

    function showDataFormatError() {
        jmaki.log("Improper data format. See the jMaki Charting Model page for Line charts for more details.");
        return;
    }
    
    this.prototype.publish = "/jmaki/charting/plotkit/line";
    this.prototype.subscribe = ["/jmaki/charting/plotkit/line", "/chart"]; 

    // set the plot to be the plot seriese
    this.prototype.plot = this.plotDatasets;
    this.init(wargs);
    this.prototype.subs = [];
    for (var _i=0; _i < this.prototype.subscribe.length; _i++) {
        this.doSubscribe(this.prototype.subscribe[_i]  + "/removeDataset", this.removeDataset);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/addDataset", this.addDataset);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/updateDataset", this.addDataset);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/updateAxes", this.updateAxes);
        this.doSubscribe(this.prototype.subscribe[_i] + "/clear", this.clear);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/addMarker", this.addMarker);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/removeMarker", this.removeMarker);        
        this.doSubscribe(this.prototype.subscribe[_i] + "/plot", this.prototype.plot);
    }    

}