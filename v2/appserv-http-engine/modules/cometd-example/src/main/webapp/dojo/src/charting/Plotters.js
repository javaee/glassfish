dojo.provide("dojo.charting.Plotters");

/*	
 *	Plotters is the placeholder; what will happen is that the proper renderer types
 *	will be mixed into this object (as opposed to creating a new one).
 */

dojo.requireIf(dojo.render.svg.capable, "dojo.charting.svg.Plotters");
dojo.requireIf(dojo.render.vml.capable, "dojo.charting.vml.Plotters");
