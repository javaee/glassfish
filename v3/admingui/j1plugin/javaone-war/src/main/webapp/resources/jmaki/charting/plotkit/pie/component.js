jmaki.namespace("jmaki.widgets.jmaki.charting.plotkit.pie");

jmaki.widgets.jmaki.charting.plotkit.pie.Widget = function(wargs) {
      
  jmaki.widgets.jmaki.charting.extend(this,jmaki.widgets.jmaki.charting.plotkit.base);

  this.prototype.chartType = "pie";
  this.prototype.shouldFill = true;
  
  function showDataFormatError() {
      jmaki.log("Improper data format. See the jMaki Charting Model page for Pie charts for more details.");
      return;
  }  
    
    this.prototype.publish = "/jmaki/charting/plotkit/pie";
    this.prototype.subscribe = ["/jmaki/charting/plotkit/pie", "/chart"]; 

    // set the plot to be the plot series
    this.prototype.plot = this.plotPie;

    this.init(wargs);    
        
    for (var _i=0; _i < this.prototype.subscribe.length; _i++) {
        this.doSubscribe(this.prototype.subscribe[_i]  + "/addDataset", this.prototype.plotPieDataset);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/removeDataset", this.removeDataset);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/updateAxes", this.updateAxes);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/updateDataset", this.prototype.plotPieDataset);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/clear", this.clearPie);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/plot", this.prototype.plotPieDataset);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/addMarker", this.addMarker);
        this.doSubscribe(this.prototype.subscribe[_i]  + "/removeMarker", this.removeMarker);         
    }        
}
