dojo.provide("dojo.charting.vml.PlotArea");
dojo.require("dojo.lang.common");

if(dojo.render.vml.capable){
	dojo.extend(dojo.charting.PlotArea, {
		resize: function(){
			var a = this.getArea();
			this.nodes.area.style.width = this.size.width+"px";
			this.nodes.area.style.height = this.size.height+"px";
			this.nodes.background.style.width = this.size.width+"px";
			this.nodes.background.style.height = this.size.height+"px";
			this.nodes.plots.width = this.size.width+"px";
			this.nodes.plots.height = this.size.height+"px";
			this.nodes.plots.style.clip="rect("
				+ a.top+" "
				+ a.right+" "
				+ a.bottom+" "
				+ a.left
				+")";
			if(this.nodes.axes){
				this.nodes.area.removeChild(this.nodes.axes);
			}
			var axes = this.nodes.axes = document.createElement("div");
			axes.id = this.getId() + "-axes";
			this.nodes.area.appendChild(axes);
			var ax = this.getAxes();
			for(var p in ax){
				var obj = ax[p];
				axes.appendChild(obj.axis.initialize(this, obj.plot, obj.drawAgainst, obj.plane));
			}
		},
		initializePlot: function(plot){
			//	summary
			//	Initialize the plot node for data rendering.
			plot.destroy();
			plot.dataNode = document.createElement("div");
			plot.dataNode.id  = plot.getId();
			return plot.dataNode;	//	HTMLDivElement
		},
		initialize:function(){
			//	summary
			//	Initialize the PlotArea.
		
			this.destroy();	//	kill everything first.
			var main = this.nodes.main = document.createElement("div");
			
			//	start with the background
			var area = this.nodes.area = document.createElement("div");
			area.id = this.getId();
			area.style.position="absolute";
			main.appendChild(area);
		
			var bg = this.nodes.background = document.createElement("div");
			bg.id = this.getId()+"-background";
			bg.style.position="absolute";
			bg.style.top="0px";
			bg.style.left="0px";
			bg.style.backgroundColor="#fff";
			area.appendChild(bg);

			//	the plot group
			var a=this.getArea();
			var plots = this.nodes.plots = document.createElement("div");
			plots.id = this.getId()+"-plots";
			plots.style.position="absolute";
			plots.style.top="0px";
			plots.style.left="0px";
			area.appendChild(plots);
			for(var i=0; i<this.plots.length; i++){
				plots.appendChild(this.initializePlot(this.plots[i]));
			}

			this.resize();
			return main;	//	HTMLDivElement
		}
	});
}
