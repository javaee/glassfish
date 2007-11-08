/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.renderkit;


import components.components.MapComponent;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;


/**
 * <p>Renderer for {@link MapComponent} in an HTML environment.</p>
 */

public class MapRenderer extends BaseRenderer {


    // -------------------------------------------------------- Renderer Methods


    /**
     * <p>Decode the incoming request parameters to determine which
     * hotspot (if any) has been selected.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void decode(FacesContext context, UIComponent component) {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        MapComponent map = (MapComponent) component;

        String key = getName(context, map);
        String value = (String)
            context.getExternalContext().getRequestParameterMap().get(key);
        if (value != null) {
            map.setCurrent(value);
        }

    }


    /**
     * <p>Encode the beginning of this component.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        MapComponent map = (MapComponent) component;
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("map", map);
        writer.writeAttribute("name", map.getId(), "id");

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
     * <p>Encode the ending of this component.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        MapComponent map = (MapComponent) component;
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("input", map);
        writer.writeAttribute("type", "hidden", null);
        writer.writeAttribute("name", getName(context, map), "clientId");
        writer.endElement("input");
        writer.endElement("map");

    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Return the calculated name for the hidden input field.</p>
     *
     * @param context   Context for the current request
     * @param component Component we are rendering
     */
    private String getName(FacesContext context, UIComponent component) {
        return (component.getId() + "_current");
    }


    /**
     * <p>Return the context-relative path for the current page.</p>
     *
     * @param context Context for the current request
     */
    private String getURI(FacesContext context) {

        StringBuffer sb = new StringBuffer();
        sb.append(context.getExternalContext().getRequestContextPath());
        // PENDING(craigmcc) - will need to change if this is generalized
        sb.append("/faces");
        sb.append(context.getViewRoot().getViewId());
        return (sb.toString());

    }


}
