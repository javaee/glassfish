/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.tools.admingui.component;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.Map;
import java.lang.StringBuffer;
import java.util.List;
import java.util.Iterator;
import com.sun.enterprise.tools.admingui.util.GuiUtil;

/**
 *  <p>	This factory is responsible for instantiating a <code>jMaki
 *	UIComponent</code>.</p>
 *
 *  <p>	The {@link com.sun.jsftemplating.layout.descriptors.ComponentType}
 *	id for this factory is: "jmaki:ajax".</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */

@UIComponentFactory("jmaki:chart")
public class JMakiChartFactory extends ComponentFactoryBase {
    private JSONObject savedArgs = null;
    private JSONObject savedValue = null;

    /**
     *	<p> This is the factory method responsible for creating the
     *	    <code>UIComponent</code>.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	descriptor  The {@link LayoutComponent} descriptor associated
     *			    with the requested <code>UIComponent</code>.
     *	@param	parent	    The parent <code>UIComponent</code>
     *
     *	@return	The newly created <code>AjaxWrapper</code>.
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {

	// Create the UIComponent
	UIComponent comp = createComponent(context, COMPONENT_TYPE, descriptor, parent);
        comp.setId((String) descriptor.getId(context, parent));
        
	// Set all the attributes / properties
	setOptions(context, descriptor, comp);
        

        Map<String, Object> attrs = comp.getAttributes();

        String name = getChartType(context, descriptor, comp);
        attrs.put("name", name );
        //System.out.println(" name = " + name);
        
        JSONObject args = getArgs( context,  descriptor,  comp);
	savedArgs = args;
        //System.out.println(" args = " + args);
        attrs.put("args", args.toString());
                
        JSONObject value = getValue( context,  descriptor,  comp);
	savedValue = value;
        //System.out.println(" value = " + value.toString());
        attrs.put("value", value.toString());
                            
	return comp;
    }

    private String getChartType(FacesContext context, LayoutComponent descriptor, UIComponent comp){
        String type = (String) descriptor.getEvaluatedOption(context, "type", comp);
        if (type == null){
            System.out.println("!!!! WARNING, type is a required attribute but not set.  Default to line chart");
            return CHART_TYPE_LINE;
        }
        if (!(type.equals(CHART_TYPE_BAR) || type.equals(CHART_TYPE_LINE))){
            System.out.println("!!!! WARNING, type " + type + "is not supported.  Default to line chart");
            return CHART_TYPE_LINE;
        }
        return type;
    }
    
    private JSONObject getArgs(FacesContext context, LayoutComponent descriptor, UIComponent comp){
       
        JSONObject args = new JSONObject();
        try{
            setJObjectStr(args, "paddingBottom", (String) descriptor.getEvaluatedOption(context, "paddingBottom", comp));
            setJObjectStr(args, "paddingLeft", (String) descriptor.getEvaluatedOption(context, "paddingLeft", comp));
            setJObjectStr(args, "paddingRight", (String) descriptor.getEvaluatedOption(context, "paddingRight", comp));
            setJObjectStr(args, "legend", (String) descriptor.getEvaluatedOption(context, "legend", comp));
            JSONObject xAxis = getXAxis( context,  descriptor,  comp);
            JSONObject yAxis = getYAxis( context, descriptor, comp);
            args.put( "xAxis", xAxis);
            args.put( "yAxis", yAxis);
         }catch (JSONException ex){
            System.out.println("Exception in constructing XAxis" );
            ex.printStackTrace();
        }
        return args;
    }
    
    private JSONObject getValue(FacesContext context, LayoutComponent descriptor, UIComponent comp){
        
        JSONObject data = new JSONObject();
        List<Map>  valueList = null;
        Object vo = (Object) descriptor.getEvaluatedOption(context, "valueList", comp);
        if (vo instanceof List) {
            valueList = (List<Map>) vo;
        }else{
            System.out.println("jmaki:chart  valueList is ignored, should be a List<Map> ");
            return data;
        }
        try{
            JSONArray ja = new JSONArray();
            for(Map oneSeries : valueList){
                JSONObject oneS = new JSONObject();
                setJObjectStr(oneS, "color", (String) (String) oneSeries.get("color"));
                setJObjectStr(oneS, "label", (String) (String) oneSeries.get("label"));

                JSONArray sa = new JSONArray();
                List<Integer> series = (List<Integer>) oneSeries.get("values");
                for(Integer oneV : series ){
                    sa.put(oneV.intValue());
                }
                oneS.put("values", sa);

                ja.put(oneS);
            }
            data.put("data", ja);
        }catch(JSONException ex){
            System.out.println("jmaki:chart Exception in constructing data" );
            ex.printStackTrace();
        }
        return data;
    }
    
    private JSONObject getXAxis(FacesContext context, LayoutComponent descriptor, UIComponent comp){

        JSONObject xa = new JSONObject();
        try{
            setJObjectStr(xa, "title", (String) descriptor.getEvaluatedOption(context, "xTitle", comp));
            setJObjectStr(xa, "rotate", (String) descriptor.getEvaluatedOption(context, "xRotate", comp));

            setRange(xa, descriptor.getEvaluatedOption(context, "xRange", comp));
            setLabels(xa, descriptor.getEvaluatedOption(context, "xLabels", comp));
           
        }catch (JSONException ex){
            System.out.println("Exception in constructing XAxis" );
            ex.printStackTrace();
        }
        return xa;
    }
      
    
     private JSONObject getYAxis(FacesContext context, LayoutComponent descriptor, UIComponent comp){
        
        
        JSONObject ya = new JSONObject();
        try{
            setJObjectStr(ya, "title", (String) descriptor.getEvaluatedOption(context, "yTitle", comp));
            setJObjectInt(ya, "tickCount", (String) descriptor.getEvaluatedOption(context, "yTickCount", comp));
            setRange(ya, descriptor.getEvaluatedOption(context, "yRange", comp));
            
            }catch(JSONException ex){
                System.out.println("Exception in constructing YAxis" );
                ex.printStackTrace();
            }
        return ya;
    }
     
     private void setRange(JSONObject jObj, Object rangeMap ) throws JSONException {
         
         if (rangeMap == null)
             return;
         if (rangeMap instanceof Map){
            JSONObject rangeObj = new JSONObject();
            Integer lower = (Integer) ((Map)rangeMap).get("lower");
            if (lower != null){
                rangeObj.put("lower", lower.intValue());
            }
            Integer upper =  (Integer)  ((Map)rangeMap).get("upper");
            if (upper != null){
                rangeObj.put("upper", upper.intValue());
            }
            jObj.put("range", rangeObj);
         }else{
             throw new JSONException("Error occured in setRange.  Range Must be a Map with 'lower' and 'upper' key.");
         }
     }
     
     private void setLabels( JSONObject jObj, Object labels ) throws JSONException {
         
         if (labels == null)  return;
         if ( !(labels instanceof List)){
             throw new JSONException("xLabels is ignored, must be List<String> or List<Map>");
         }
      if (((List)labels).size() == 0)  return;
         
         Object testLabel = ((List)labels).get(0);
         JSONArray labelArray = new JSONArray();
         
         if (testLabel instanceof Map){
             for(Map oneLabel : (List<Map>) labels ){
                JSONObject labelObj = new JSONObject();
                setJObjectStr(labelObj, "label", (String) oneLabel.get("label") );
                setJObjectStr(labelObj, "value", (String) oneLabel.get("value") );
                setJObjectStr(labelObj, "title", (String) oneLabel.get("title") );
                labelArray.put(labelObj);
             }
             jObj.put("labels", labelArray);
         }else
         if (testLabel instanceof String){
             for(String  oneLabel  : (List<String>) labels){
                labelArray.put(oneLabel);
            }
            jObj.put("labels", labelArray);
         }else{
             throw new JSONException("xLabels is ignored, must be List<String> or List<Map>");
         }
     }
    
     private void setJObjectStr(JSONObject jObj, String attr, String value) throws JSONException {
        if (!GuiUtil.isEmpty(value))
            jObj.put(attr, value);
    }
    
     private void setJObjectInt(JSONObject jObj, String attr, String value) throws JSONException {
        if (!GuiUtil.isEmpty(value)){
            jObj.put(attr, Integer.parseInt(value));
        }
    }
    
    
    /**
     *
     *	<p> The <code>UIComponent</code> type that must be registered in the
     *	    <code>faces-config.xml</code> file mapping to the UIComponent class
     *	    to use for this <code>UIComponent</code>.</p>
     */
    public static final String COMPONENT_TYPE	= "AjaxWrapper";
    public static final String CHART_TYPE_LINE	= "jmaki.charting.line";
    public static final String CHART_TYPE_BAR	= "jmaki.charting.bar";
}
