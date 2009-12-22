/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html  
$Id: TabLabelRenderer.java,v 1.4 2005/11/04 04:40:39 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.renderkit;


import com.sun.javaee.blueprints.components.ui.components.PaneSelectedEvent;
import com.sun.javaee.blueprints.components.ui.util.Util;

import javax.faces.component.UIComponent;
import javax.faces.context.*;

import java.io.IOException;
import java.util.*;

/**
 * <B>TabLabelRenderer</B> is a class that renders a button control
 * on an individual pane of a tabbed pane control.  The button can
 * be rendered with a label or an image as its face.
 *
 * <B>Lifetime And Scope</B> <P>
 *
 * @version $Id: TabLabelRenderer.java,v 1.4 2005/11/04 04:40:39 jenniferb Exp $
 *
 */

public class TabLabelRenderer extends BaseRenderer {

    public TabLabelRenderer() {
        super();
    }
    /**
     * Follow the UE Spec for Button:
     * http://javaweb.sfbay.sun.com/engineering/jsue/j2ee/WebServices/
     * JavaServerFaces/uispecs/UICommand_Button.html
     */
    protected String padLabel(String label) {
        if (label.length() == 3) {
            label = "&nbsp;&nbsp;" + label + "&nbsp;&nbsp;";
        } else if (label.length() == 2) {
            label = "&nbsp;&nbsp;&nbsp;" + label + "&nbsp;&nbsp;&nbsp;";
        }
        return label;
    }


    /**
     * @return the image src if this component is configured to display
     *         an image label, null otherwise.
     */

    protected String getImageSrc(FacesContext context,
                                 UIComponent component) {
        String result = (String) component.getAttributes().get("image");

        if (result != null) {
            if (!result.startsWith("/")) {
                result = "/" + result;
                component.getAttributes().put("image", result);
            }
        }

        if (result == null) {
            try {
                result = getKeyAndLookupInBundle(context, component,
                                                 "imageKey");
            } catch (MissingResourceException e) {
                // Do nothing since the absence of a resource is not an
                // error.
            }
        }
        if (result == null) {
            return result;
        }
        String contextPath = context.getExternalContext()
            .getRequestContextPath();
        StringBuffer sb = new StringBuffer();
        if (result.startsWith("/")) {
            sb.append(contextPath);
        }
        sb.append(result);
        return (context.getExternalContext().encodeResourceURL(sb.toString()));
    }

    protected String getLabel(FacesContext context,
                              UIComponent component) throws IOException {
        String result = null;

        try {
            result = getKeyAndLookupInBundle(context, component, "key");
        } catch (MissingResourceException e) {
            // Do nothing since the absence of a resource is not an
            // error.
        }
        if (null == result) {
            result = (String) component.getAttributes().get("label");
        }
        return result;
    }


    public void decode(FacesContext context, UIComponent component) {
        if (context == null || component == null) {
            throw new NullPointerException(
                "Null Faces context or component parameter");
        }

        String clientId = component.getClientId(context);
        Map requestParameterMap = (Map) context.getExternalContext().
            getRequestParameterMap();
        String value = (String) requestParameterMap.get(clientId);
        if (value == null) {
            if (requestParameterMap.get(clientId + ".x") == null &&
                requestParameterMap.get(clientId + ".y") == null) {
                return;
            }
        }

        // Search for this component's parent "tab" component..
        UIComponent tabComponent = findParentForRendererType(component, "Tab");
        
        // set the "tab" component's "id" in the event...

        tabComponent.queueEvent(new PaneSelectedEvent(component,
                                                      tabComponent.getId()));


        return;
    }


    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException(
                "Null Faces context or component parameter");
        }
        // suppress rendering if "rendered" property on the component is
        // false.
        if (!component.isRendered()) {
            return;
        }
       String paneTabLabelClass = null;

        ResponseWriter writer = context.getResponseWriter();

        String imageSrc = getImageSrc(context, component);
        String label = getLabel(context, component);
        String type = "submit";

        if (imageSrc != null || label != null) {
            writer.write("<input type=");
            if (null != imageSrc) {
                writer.write("\"image\" src=\"");
                writer.write(imageSrc);
                writer.write("\"");
                writer.write(" name=\"");
                writer.write(component.getClientId(context));
                writer.write("\"");
            } else {
                writer.write("\"");
                writer.write(type.toLowerCase());
                writer.write("\"");
                writer.write(" name=\"");
                writer.write(component.getClientId(context));
                writer.write("\"");
                writer.write(" value=\"");
                writer.write(padLabel(label));
                writer.write("\"");
            }
        }

        writer.write(Util.renderPassthruAttributes(context, component));
        writer.write(Util.renderBooleanPassthruAttributes(context, component));
        if (null != (paneTabLabelClass = (String)
            component.getAttributes().get("paneTabLabelClass"))) {
            writer.write(" class=\"" + paneTabLabelClass + "\" ");
        }
        writer.write(">");
    }


    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException(
                "Null Faces context or component parameter.");
        }
    }


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException(
                "Null Faces context or component parameter.");
        }
    }


    private UIComponent findParentForRendererType(UIComponent component, String rendererType) {
        Object facetParent = null;
        UIComponent currentComponent = component;
        
        // Search for an ancestor that is the specified renderer type; 
        while (null != (currentComponent = currentComponent.getParent())) {
            if (currentComponent.getRendererType().equals(rendererType)) {
                break;
            }
        }
        return currentComponent;
    }
}


