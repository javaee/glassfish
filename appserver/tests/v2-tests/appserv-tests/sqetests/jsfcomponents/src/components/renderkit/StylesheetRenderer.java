/*
 * $Id: StylesheetRenderer.java,v 1.3 2004/11/14 07:33:15 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.renderkit;


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
        writer.write((String) component.getAttributes().get("path"));
        writer.write("\">");

    }


}
