/* Copyright 2005 Sun Microsystems, Inc. All rights reserved.
   You may not modify, use, reproduce, or distribute this software except in
   compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
   $Id: SlideshowRenderer.java,v 1.1 2005/11/19 00:53:51 inder Exp $  */

package com.sun.javaee.blueprints.components.ui.slider_navigator;

import java.io.*;
import java.util.*;
import javax.faces.component.*;
import javax.faces.el.*;
import javax.faces.context.*;
import javax.servlet.http.*;
import javax.servlet.*;
import javax.faces.render.Renderer;
import com.sun.faces.util.Util;
/**
 *
 * @author yuta
 */
public class SlideshowRenderer extends Renderer {
    
    private static final String RENDERED_SCRIPT_KEY = "bpcatalog-ajax-script-slideshow";
    private String left;
    private String top;
    private String width;
    private String height;
    private String pause;
    
    public void encodeBegin(FacesContext context, UIComponent component) 
        throws IOException {

        Slideshow ss = (Slideshow)component;        
        left = (String)ss.getAttributes().get("left");
        top = (String)ss.getAttributes().get("top");
        width = (String)ss.getAttributes().get("width");
        height = (String)ss.getAttributes().get("height");
        pause = (String)ss.getAttributes().get("pause");
        if (pause == null) {
            pause = "3000";
        }
        String speed = (String)ss.getAttributes().get("speed");
        if (speed == null) {
            speed = "60";
        }
        
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write("\n");
        writer.write("var xsltObj = null\n");
        //writer.write("djConfig = { isDebug:true };");
        writer.write("\n");
        writer.write("var layerLeft="+left+"\n");
        writer.write("var layerTop="+top+"\n");
        writer.write("var layerWidth="+width+"\n");
        writer.write("var layerHeight="+height+"\n");
        writer.write("var pause="+pause+"\n");
        writer.write("var speed="+speed+"\n");
        writer.write("var imgArray=new Array()");
        writer.endElement("script");
        writer.write("\n");
        
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        String src = "faces/" + SlideshowPhaseListener.AJAX_SCRIPT_VIEW_ID;
        writer.writeAttribute("src", SlideshowPhaseListener.AJAX_SCRIPT_VIEW_ID, null);
        writer.endElement("script");
        writer.write("\n");
        
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        src = "faces/" + SlideshowPhaseListener.XSL_SCRIPT_VIEW_ID;
        writer.writeAttribute("src", SlideshowPhaseListener.XSL_SCRIPT_VIEW_ID, null);
        writer.endElement("script");
        writer.write("\n");
        
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        src = "faces/" + SlideshowPhaseListener.SCRIPT_VIEW_ID;
        writer.writeAttribute("src", SlideshowPhaseListener.SCRIPT_VIEW_ID, null);
        
        writer.endElement("script");
        writer.write("\n");
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write("xsltObj = new Xslt.createLocal(\"faces/cats.xsl\");");
        writer.endElement("script");
        writer.write("\n\n");
        /*
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        String dojosrc = "faces/" + SlideshowPhaseListener.DOJO_VIEW_ID;
        writer.writeAttribute("src", dojosrc, null);
        writer.endElement("script");
        
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write("\n");
        writer.write("dojo.require(\"dojo.xml.*\");");
        writer.write("\n");
        writer.write("dojo.require(\"dojo.graphics.*\");");
        writer.endElement("script");
        writer.write("\n");
        writer.write("\n\n");
         */
    }

    public void encodeEnd(FacesContext context, UIComponent component)
                throws IOException {
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        //Render only once
        Map reqMap = context.getExternalContext().getRequestMap();
        Boolean scriptRendered = (Boolean)reqMap.get(RENDERED_SCRIPT_KEY);
        
        if (scriptRendered == Boolean.TRUE) {
            return;
        }
        reqMap.put(RENDERED_SCRIPT_KEY, Boolean.TRUE);

        ResponseWriter writer = context.getResponseWriter();
        
        // the main layer
        writer.startElement("div", component);
        String divatt = "position:relative; left:" + left + "; top:" + top +
                "; width:" + width + "; height:" + height + 
                "; overflow:hidden; background-color:#ffffff";
        writer.writeAttribute("style", divatt, null);
        writer.write("\n\n");
        writer.startElement("div", component);
        // 1st layer
        writer.writeAttribute("id", "layerOne", null);
        writer.writeAttribute("style", "position:absolute;width:"+width+";left:1px;top:0px;", null);
        
        writer.endElement("div");
        writer.write("\n");
        
        // 2nd layer
        writer.startElement("div", component);
        writer.writeAttribute("id", "layerTwo", null);
        writer.writeAttribute("style", "position:absolute;width:"+width+";left:0px;top:0px", null);
        
        writer.endElement("div");
        writer.endElement("div");
        writer.write("\n");
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write("window.onLoad = startscroll()");
        writer.endElement("script");
        
    }
    
    /**
     * <p>Encode the children of this component.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
    }
    
    /**
     * <p>Return the context-relative path for the current page.</p>
     *
     * @param context Context for the current request
     */
    private String getURI(FacesContext context) {
        StringBuffer sb = new StringBuffer();
        sb.append(context.getExternalContext().getRequestContextPath());

        // if this mapping is changed, the following lines must be changed as well.
        sb.append("/faces");
        sb.append(context.getViewRoot().getViewId());

        return (sb.toString());
    }
}
