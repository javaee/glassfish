jmaki.namespace("jmaki.widgets.jmaki.charting.plotkit.area");


jmaki.widgets.jmaki.charting.plotkit.area.Widget = function(wargs) {

    jmaki.widgets.jmaki.charting.extend(this,jmaki.widgets.jmaki.charting.plotkit.base);
    
    this.prototype.shouldFill = true;         
    this.prototype.chartType = "line";
    
    this.prototype.publish = "/jmaki/charting/plotkit/area";
    this.prototype.subscribe = ["/jmaki/charting/plotkit/area", "/chart"]; 
   
    function showDataFormatError() {
        jmaki.log("Improper data format. See the jMaki Charting Model page for Area charts for more details.");
        return;
    }
    
    // set the plot to be the plot series
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