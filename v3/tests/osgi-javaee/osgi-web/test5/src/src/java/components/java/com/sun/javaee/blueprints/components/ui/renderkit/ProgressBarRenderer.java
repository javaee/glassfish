/*
 * $Id: ProgressBarRenderer.java,v 1.4 2005/11/04 17:23:58 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * [Name of File] [ver.__] [Date]
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.javaee.blueprints.components.ui.renderkit;

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;

import com.sun.javaee.blueprints.components.ui.components.ProgressBarComponent;
import com.sun.javaee.blueprints.components.ui.util.Util;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletResponse;


/**
 * <p>Render our associated {@link PaneComponent} as a tabbed control, with
 * each of its immediate child {@link PaneComponent}s representing a single
 * tab.  Measures are taken to ensure that exactly one of the child tabs is
 * selected, and only the selected child pane's contents will be rendered.
 * </p>
 */

public class ProgressBarRenderer extends BaseRenderer {


    private boolean logging = true;    

    public void decode(FacesContext context, UIComponent component) {
        String clientId = component.getClientId(context);
        String paramValue = (String) 
            context.getExternalContext().getRequestParameterMap().get(clientId);
        if (null == paramValue) {
            return;
        }
        
        // Determine if this is an ajax postback, or a real postback
        if (-1 != paramValue.indexOf("ajax")) {
            
            ProgressBarComponent comp = (ProgressBarComponent) component;
            
            // get the current value from the component, may call managed
            // bean.
            
            Object result = comp.getValue();
            
            // write the XML to the response
            // PENDING(edburns): spec: add methods to ExternalContext to set
            // this without downcasting to http, for portlet purposes.
            HttpServletResponse response = (HttpServletResponse)
            context.getExternalContext().getResponse();
            
            // set the header information for the response
            response.setContentType("text/xml");
            response.setHeader("Cache-Control", "no-cache");
            
            try {
                ResponseWriter writer = setupResponseWriter(context);
                writer.startElement("message", comp);
                writer.startElement("percentage", comp);
                writer.writeText(result, null);
                writer.endElement("percentage");
                writer.startElement("clientId", comp);
                writer.writeText(component.getClientId(context), null);
                writer.endElement("clientId");
                writer.endElement("message");
            } catch (IOException e) {
                // PENDING(edburns): log message
            }
        }
        else {
            getButtonRenderer(context).decode(context, component);
        }
    }


    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {

        ResponseWriter writer = context.getResponseWriter();
	String clientId = component.getClientId(context);
        
        Util.renderMainScriptOnce(context, writer, component);
        
        // render a script element that enqueues the clientId of this progress
        // bar into a list 
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        String idScript = "g_progressBars[g_progressBars.length] = " +
                "\"" + clientId + "\";";
        writer.writeText(idScript, null);
        writer.endElement("script");
	
	// Write out the div that gets replaced by the progress bar 
	writer.startElement("div", component);
	writer.writeAttribute("id", clientId, null);
	writer.endElement("div");
        // write out the hidden field that is this command's value which
        // will be processed when the progress bar is complete
        writer.startElement("input", component);
	writer.writeAttribute("id", clientId, null);
        writer.writeAttribute("type", "hidden", "type");
        writer.writeAttribute("name", clientId, "clientId");
        writer.writeAttribute("value", "value", "value");
        writer.endElement("input");
        

    }


    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {

        if (logging) {
            log("encodeChildren(" + component.getId() + ")");
        }

    }


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if (logging) {
            log("encodeEnd(" + component.getId() + ")");
        }

    }
    
    private void log(String message) {
        if (logging) {
            System.out.println(message);
        }
    }

    private Renderer commandButtonRenderer = null;

    public Renderer getButtonRenderer(FacesContext context) {
        if (null != commandButtonRenderer) {
            return commandButtonRenderer;
        }
        
        RenderKitFactory fact = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit basic = fact.getRenderKit(context, 
                RenderKitFactory.HTML_BASIC_RENDER_KIT);
        commandButtonRenderer = basic.getRenderer("javax.faces.Command",
                "javax.faces.Button");
        return commandButtonRenderer;
        
    }
    
    

}
