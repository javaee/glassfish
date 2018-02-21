/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package components.components;

import javax.faces.context.FacesContext;
import javax.faces.component.UIOutput;
import javax.faces.el.ValueBinding;
import javax.faces.component.UIComponent;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIViewRoot;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import components.model.ChartItem;


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
        ValueBinding _vb = getValueBinding("width");
        if (_vb != null) {
            return (java.lang.String) _vb.getValue(getFacesContext());
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
        ValueBinding _vb = getValueBinding("height");
        if (_vb != null) {
            return (java.lang.String) _vb.getValue(getFacesContext());
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
        ValueBinding _vb = getValueBinding("orientation");
        if (_vb != null) {
            return (java.lang.String) _vb.getValue(getFacesContext());
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
        ValueBinding _vb = getValueBinding("type");
        if (_vb != null) {
            return (java.lang.String) _vb.getValue(getFacesContext());
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
        ValueBinding _vb = getValueBinding("title");
        if (_vb != null) {
            return (java.lang.String) _vb.getValue(getFacesContext());
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
        ValueBinding _vb = getValueBinding("xlabel");
        if (_vb != null) {
            return (java.lang.String) _vb.getValue(getFacesContext());
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
        ValueBinding _vb = getValueBinding("ylabel");
        if (_vb != null) {
            return (java.lang.String) _vb.getValue(getFacesContext());
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
