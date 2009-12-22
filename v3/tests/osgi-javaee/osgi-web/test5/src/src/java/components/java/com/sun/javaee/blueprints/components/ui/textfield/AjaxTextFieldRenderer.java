/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
 */

/*
 * AjaxTextFieldRenderer.java
 *
 * Created on April 29, 2005, 12:30 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.sun.javaee.blueprints.components.ui.textfield;

import java.beans.Beans;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.faces.RIConstants;
import com.sun.faces.renderkit.html_basic.HtmlBasicInputRenderer;
import com.sun.faces.util.Util;


/**
 * This renderer generates HTML (including JavaScript) for AjaxTextFields,
 * emitting the necessary markup to provide auto completion for the textfield.
 *
 * This component relies on a cooperating servlet that responds to asynchronous
 * requests.
 *
 * @author Tor Norbye
 */
public class AjaxTextFieldRenderer extends HtmlBasicInputRenderer {
    static int nextId = 0;
    private static final String RENDERED_SCRIPT_KEY = "bpcatalog-ajax-script-rendered";

    /** Creates a new instance of AjaxTextFieldRenderer */
    public AjaxTextFieldRenderer() {
    }

    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {
    }

    public void encodeChildren(FacesContext context, UIComponent component) {
    }

    protected void getEndTextToRender(FacesContext context, UIComponent component,
        String currentValue) throws IOException {
        assert component instanceof AjaxTextField;

        ResponseWriter writer = context.getResponseWriter();

        if (!Beans.isDesignTime()) {
            renderScriptOnce(writer, component, context);
        }

        String menuId = "menu-popup" + (nextId);
        nextId++;

        if (!Beans.isDesignTime()) {
            // Render menu popup (will be positioned etc. by CSS)
            writer.startElement("div", component);
            writer.writeAttribute("id", menuId, null);
            writer.writeAttribute("style",
                "position: absolute; top:170px;left:140px;visibility:hidden", null);
            writer.writeAttribute("class", "popupFrame", null);
            writer.endElement("div");
            writer.write("\n");
        }

        writer.startElement("input", component);
        writeIdAttributeIfNecessary(context, writer, component);

        // Set the autocomplete attribute to "off" to disable browser 
        // textfield completion with previous entries
        writer.writeAttribute("autocomplete", "off", null);

        writer.writeAttribute("type", "text", null);
        writer.writeAttribute("name", component.getClientId(context), "clientId");

        if (currentValue != null) {
            writer.writeAttribute("value", currentValue, "value");
        }

        String styleClass = (String)component.getAttributes().get("styleClass");

        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }

        Util.renderPassThruAttributes(context, writer, component);
        Util.renderBooleanPassThruAttributes(writer, component);

        // Emit the javascript for auto completion
        AjaxTextField comp = (AjaxTextField)component;
        String methodName = comp.getCompletionMethod();
        String startScript =
            "doCompletion('" + comp.getClientId(context) + "','" + menuId + "','" + methodName +
            "'," + ((comp.getOnchoose() != null) ? comp.getOnchoose() : "null") + "," +
            ((comp.getOndisplay() != null) ? comp.getOndisplay() : "null") + ");";
        String stopScript = "stopCompletionDelayed('" + menuId + "');";
        writer.writeAttribute("onfocus", startScript, null);
        writer.writeAttribute("onkeyup", startScript, null);
        writer.writeAttribute("onblur", stopScript, null);

        writer.endElement("input");
    }

    /** Render the &lt;script&gt; tag which contains supporting JavaScript
     * for this text field.
     */
    private void renderScriptOnce(ResponseWriter writer, UIComponent component, FacesContext context)
        throws IOException {
        // Only render the generic <style> and <script> sections once per page:
        // Store attribute in request map when we've rendered the script such
        // that we only do this once per page
        Map requestMap = context.getExternalContext().getRequestMap();
        Boolean scriptRendered = (Boolean)requestMap.get(RENDERED_SCRIPT_KEY);

        if (scriptRendered == Boolean.TRUE) {
            return;
        }

        requestMap.put(RENDERED_SCRIPT_KEY, Boolean.TRUE);

        // CSS
        writer.write("\n");

        writer.startElement("link", component);
        writer.writeAttribute("type", "text/css", null);
        writer.writeAttribute("rel", "stylesheet", null);

        //String href = AjaxPhaseListener.CSS_VIEW_ID + ".faces";
        String href = "faces/" + AjaxPhaseListener.CSS_VIEW_ID;
        writer.writeAttribute("href", href, null);

        writer.endElement("link");
        writer.write("\n");

        // JavaScript
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);

        //String src = AjaxPhaseListener.SCRIPT_VIEW_ID + ".faces";
        String src = "faces/" + AjaxPhaseListener.SCRIPT_VIEW_ID;
        writer.writeAttribute("src", src, null);

        writer.endElement("script");
        writer.write("\n");
    }
}
