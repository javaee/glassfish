jmaki.namespace("jmaki.widgets.jmaki.charting.plotkit.bar");


jmaki.widgets.jmaki.charting.plotkit.bar.Widget = function(wargs) {
    
    jmaki.widgets.jmaki.charting.extend(this,jmaki.widgets.jmaki.charting.plotkit.base);
    
    this.prototype.shouldFill = true;         
    this.prototype.chartType = "bar";

    this.prototype.publish = "/jmaki/charting/plotkit/bar";    
    this.prototype.subscribe = ["/jmaki/charting/plotkit/bar", "/chart"]; 
    
   
    function showDataFormatError() {
        jmaki.log("Improper data format. See the jMaki Charting Model page for Bar charts for more details.");
        return;
    }
    
    // set the plot to be the plot seriese
    this.prototype.plot = this.plotDatasets;

    this.init(wargs);
        
    this.prototype.subs = [];
    for (var _i=0; _i < this.prototype.subscribe.length; _i++) {
        this.doSubscribe(this.prototype.subscribe[_i]  + "/removeDataset", this.removeDataset);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/addDataset", this.addDataset);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/updateAxes", this.updateAxes);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/updateDataset", this.addDataset);        
        this.doSubscribe(this.prototype.subscribe[_i] + "/clear", this.clear);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/addMarker", this.addMarker);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/removeMarker", this.removeMarker);        
        this.doSubscribe(this.prototype.subscribe[_i] + "/plot", this.prototype.plot);
    }    
    
}