/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: StylesheetRenderer.java,v 1.3 2005/11/04 04:40:39 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.renderkit;


import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;


/**
 * <p>Render a stylesheet link for the value of our component's
 * <code>path</code> attribute, prefixed by the context path of this
 * web application.</p>
 */

public class StylesheetRenderer extends BaseRenderer {


    public boolean supportsComponentType(UIComponent component) {
        return (component instanceof UIOutput);
    }


    public void decode(FacesContext context, UIComponent component) {
    }


    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {
        ;
    }


    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {
        ;
    }


    /**
     * <p>Render a relative HTML <code>&lt;link&gt;</code> element for a
     * <code>text/css</code> stylesheet at the specified context-relative
     * path.</p>
     *
     * @param context   FacesContext for the request we are processing
     * @param component UIComponent to be rendered
     *
     * @throws IOException          if an input/output error occurs while rendering
     * @throws NullPointerException if <code>context</code>
     *                              or <code>component</code> is null
     */
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        ResponseWriter writer = context.getResponseWriter();
        String contextPath = context.getExternalContext()
            .getRequestContextPath();
        writer.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");

        writer.write(contextPath);
	System.out.println("path "+component.getAttributes().get("path"));
	System.out.println("path class"+(component.getAttributes().get("path")).getClass().getName());
        writer.write((String)component.getAttributes().get("path"));
        writer.write("\">");

    }


}
