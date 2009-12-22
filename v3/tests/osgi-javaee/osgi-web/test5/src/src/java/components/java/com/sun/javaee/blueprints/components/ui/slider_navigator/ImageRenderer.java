/* Copyright 2005 Sun Microsystems, Inc. All rights reserved.
   You may not modify, use, reproduce, or distribute this software except in
   compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
   $Id: ImageRenderer.java,v 1.1 2005/11/19 00:53:48 inder Exp $  */

package com.sun.javaee.blueprints.components.ui.slider_navigator;

import java.io.*;
import java.util.*;
import java.net.URL;
import javax.faces.component.*;
import javax.faces.el.*;
import javax.faces.context.*;
import javax.servlet.http.*;
import javax.servlet.*;
import javax.faces.render.Renderer;
import com.sun.faces.util.Util;

public class ImageRenderer extends Renderer {
    
    public void encodeBegin(FacesContext context, UIComponent component) 
        throws IOException {
        
        Image ss = (Image)component;
        Slideshow slideParent = (Slideshow)ss.getParent();
        String xmlDataPath = (String)slideParent.getAttributes().get("data");
        List<UIComponent> childs = slideParent.getChildren();
        String thisId = component.getId();
        int index = childs.indexOf(component);
        String num = Integer.toString(index);
        String link = (String)ss.getAttributes().get("link");
        String src = (String)ss.getAttributes().get("src");
        String name = (String)ss.getAttributes().get("name");
        
        String path = getResourcePath(context, src);
        
        ResponseWriter writer = context.getResponseWriter();
        
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write("imgArray["+num+"]=");
        writer.write("'");
        //writer.startElement("a", component);
        //writer.writeAttribute("href", link, null);
        writer.startElement("img", component);
        writer.writeAttribute("src", path, null);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("onClick", "popupImage(\""+path+"\")", null);
        String moverFunc = "retrieveXml(\""+xmlDataPath+"\", \""+name+"\", \"description\");";
        writer.writeAttribute("onMouseOver", moverFunc, null);
        //writer.endElement("a");
        writer.write("'");
        writer.endElement("script");
        writer.write("\n");
 
    }

    public void encodeEnd(FacesContext context, UIComponent component)
                throws IOException {
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }



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
    
    private String getResourcePath(FacesContext context, String path) {
        HttpServletRequest req = (HttpServletRequest)context.getExternalContext().getRequest();
        StringBuffer spath = req.getRequestURL();
        String ret = spath.substring(0, spath.indexOf("faces")) + path;
        return (ret);
    }
    /**
     * <p>Return the context path for the current request.</p>
     *
     * @param context Context for the current request
     */
    private String getContextPath(FacesContext context) {
        return (context.getExternalContext().getRequestContextPath());
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
