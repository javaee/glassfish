/*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.javaee.blueprints.components.ui.components;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.component.UIOutput;
import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIViewRoot;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.sun.javaee.blueprints.components.ui.model.ChartItem;


/**
 * <p>{@link ChartComponent} is a JavaServer Faces component that renders
 * a given set of data as a bar or pie chart.</p>
 */

public class ChartComponent extends UIOutput {

    /**
     * <p>The standard component type for this component.</p>
     */
    public static final String COMPONENT_TYPE = "Chart";


    /**
     * <p>The standard component family for this component.</p>
     */
    public static final String COMPONENT_FAMILY = "Chart";
    
    /**
     * <p>Name of the servlet that renders the image.</p>
     */
    public static final String CHART_SERVLET_NAME = "ChartServlet";
    
    // ------------------------------------------------------ Instance Variables
    private String width = null;
    private String height = null;
    private String orientation = null;
    private String type = null;
    private String title = null;
    private String xlabel = null;
    private String ylabel = null;
    
    // --------------------------------------------------------------Constructors 

    public ChartComponent() {
        super();
        setRendererType(null);
    }

    
    // -------------------------------------------------------------- Properties
    /**
     * <p>Return the width of the chart
     */
    public String getWidth() {
        if (null != this.width) {
            return this.width;
        }
        ValueExpression _ve = getValueExpression("width");
        if (_ve != null) {
            return (java.lang.String) _ve.getValue(getFacesContext().getELContext());
        } else {
            return null;
        }
    }


    /**
     * <p>Set the width of the chart
     *
     * @param width The new width of the chart
     */
    public void setWidth(String width) {
        this.width = width;
        
    }
    
    /**
     * <p>Return the height of the chart
     */
    public String getHeight() {
        if (null != this.height) {
            return this.height;
        }
        ValueExpression _ve = getValueExpression("height");
        if (_ve != null) {
            return (java.lang.String) _ve.getValue(getFacesContext().getELContext());
        } else {
            return null;
        }
    }


    /**
     * <p>Set the height of the chart
     *
     * @param height The new height of the chart
     */
    public void setHeight(String height) {
        this.height = height;
    }
    
    /**
     * <p>Return the orientation of the chart
     */
    public String getOrientation() {
        if (null != this.orientation) {
            return this.orientation;
        }
        ValueExpression _ve = getValueExpression("orientation");
        if (_ve != null) {
            return (java.lang.String) _ve.getValue(getFacesContext().getELContext());
        } else {
            return null;
        }
    }


    /**
     * <p>Set the orientation of the chart
     *
     * @param orientation The new orientation of the chart
     */
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
    
    /**
     * <p>Return the type of the chart
     */
    public String getType() {
        if (null != this.type) {
            return this.type;
        }
        ValueExpression _ve = getValueExpression("type");
        if (_ve != null) {
            return (java.lang.String) _ve.getValue(getFacesContext().getELContext());
        } else {
            return null;
        }
    }


    /**
     * <p>Set the type of the chart
     *
     * @param type The new type of the chart
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * <p>Return the title of the chart
     */
    public String getTitle() {
        if (null != this.title) {
            return this.title;
        }
        ValueExpression _ve = getValueExpression("title");
        if (_ve != null) {
            return (java.lang.String) _ve.getValue(getFacesContext().getELContext());
        } else {
            return null;
        }
    }

    /**
     * <p>Set the title of the chart
     *
     * @param title The new title of the chart
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * <p>Return the x axis label of the chart
     */
    public String getXlabel() {
        if (null != this.xlabel) {
            return this.xlabel;
        }
        ValueExpression _ve = getValueExpression("xlabel");
        if (_ve != null) {
            return (java.lang.String) _ve.getValue(getFacesContext().getELContext());
        } else {
            return null;
        }
    }

    /**
     * <p>Set the x axis label of the chart
     *
     * @param xlabel The new x axis label of the chart
     */
    public void setXlabel(String xlabel) {
        this.xlabel = xlabel;
    }

    /**
     * <p>Return the y axis label of the chart
     */
    public String getYlabel() {
        if (null != this.ylabel) {
            return this.ylabel;
        }
        ValueExpression _ve = getValueExpression("ylabel");
        if (_ve != null) {
            return (java.lang.String) _ve.getValue(getFacesContext().getELContext());
        } else {
            return null;
        }
    }

    /**
     * <p>Set the y axis label of the chart
     *
     * @param ylabel The new y axis label of the chart
     */
    public void setYlabel(String ylabel) {
        this.ylabel = ylabel;
    }

    /**
     * <p>Return the component family for this component.
     */
    public String getFamily() {

        return (COMPONENT_FAMILY);

    }
   
    // ----------------------------------------------------- StateHolder Methods
    /**
     * <p>Return the state to be saved for this component.
     *
     * @param context <code>FacesContext</code> for the current request
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[8];
        values[0] = super.saveState(context);
        values[1] = width;
        values[2] = height;
        values[3] = orientation;
        values[4] = type;
        values[5] = title;
        values[6] = xlabel;
        values[7] = ylabel;
        return (values);
    }


    /**
     * <p>Restore the state for this component.
     *
     * @param context <code>FacesContext</code> for the current request
     * @param state   State to be restored
     *
     * @throws IOException if an input/output error occurs
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        width = (String) values[1];
        height = (String) values[2];
        orientation = (String) values[3];
        type = (String) values[4];
        title = (String) values[5];
        xlabel = (String) values[6];
        ylabel = (String) values[7];
    }
    
    public void encodeEnd(FacesContext context) throws IOException {
        placeChartDataInScope(context);
        // render an image that would initiate a request to a URL pointing 
        // back into the webapp passing in whatever parameters are needed to 
        // create the dynamic image.
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("img", this);
        writeIdAttributeIfNecessary(context, writer, this);
        writer.writeAttribute("src", src(context, this), "value");
        writer.endElement("img");
    }
    
    // ----------------------------------------------------- Private Methods
    
    protected void writeIdAttributeIfNecessary(FacesContext context,
                                               ResponseWriter writer,
                                               UIComponent component) {
        String id;
        if ((id = component.getId()) != null &&
            !id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
            try {
                writer.writeAttribute("id", component.getClientId(context),
                                      "id");
            } catch (IOException e) {
               /* if (log.isDebugEnabled()) {
                    log.debug("Can't write ID attribute" + e.getMessage());
                } */
            }
        }
    }
    
    private String src(FacesContext context, UIComponent component) {
        String contextPath = context.getExternalContext().getRequestContextPath();
        StringBuffer result = new StringBuffer(contextPath);
        result.append("/");
        result.append(CHART_SERVLET_NAME);
        
        // append parameters to be passed to be servlet
        // ChartServlet will use clientId as the attribute name to get the chart 
        // data from session.
        result.append("?chartId=");
        result.append(getClientId(context));
        result.append("&");
        
        result.append("height=");
        if ( getHeight() != null ) {
            result.append(getHeight());
        }
        result.append("&");
        
        result.append("width=");
        if ( getWidth() != null ) {
            result.append(getWidth());
        }
        result.append("&");
     
        result.append("orientation=");
        if ( getOrientation() != null ) {
            result.append(getOrientation());
        }
        result.append("&");
        
        result.append("type=");
        if ( type != null ) {
            result.append(type);
        }
        result.append("&");

        result.append("title=");
        if ( title != null ) {
            result.append(title);
        }
        result.append("&");

        result.append("xlabel=");
        if ( xlabel != null ) {
            result.append(xlabel);
        }
        result.append("&");
      
        result.append("ylabel=");
        if ( ylabel != null ) {
            result.append(ylabel);
        }

        return (result.toString());
     }
    
    /** Place the appropriate data for chart in session scope, so that
     * it will be there when the separate request for the image is
     * processed by the chart servlet. This servlet is responsible for 
     * writing out the chart as an image into the respone stream.
     */
    protected void placeChartDataInScope(FacesContext context) {
        int i = 0;
        ChartItem[] chartItems = null;
        // if there is a value attribute set on the bean, data for the chart is
        // retrieved from the bean. If not, we build an array of ChartItem
        // using the children of this component.
        chartItems = (ChartItem[]) getValue();
        if (chartItems == null || chartItems.length == 0 ) {
            chartItems = new ChartItem[getChildCount()];
            Iterator kids = this.getChildren().iterator();
            while ( kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                if (kid instanceof ChartItemComponent) {
                    ChartItemComponent ci = (ChartItemComponent) kid;
                    ChartItem item = (ChartItem) ci.getValue();
                    if (item == null) {
                        int itemVal = 
                            (new Integer((String)ci.getItemValue())).intValue();
                        item = new ChartItem(ci.getItemLabel(),itemVal,
                            ci.getItemColor());
                    }
                    chartItems[i] = item;
                    ++i;
                }
            }
        }
        // store the chart data against the clientId in session. 
        Map sessionMap =
            getFacesContext().getExternalContext().getSessionMap();
        sessionMap.put(getClientId(context), chartItems);
    }
    
}
