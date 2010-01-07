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

package com.sun.javaee.blueprints.components.ui.renderkit;


import com.sun.javaee.blueprints.components.ui.components.AreaComponent;
import com.sun.javaee.blueprints.components.ui.components.MapComponent;
import com.sun.javaee.blueprints.components.ui.model.ImageArea;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import java.io.IOException;


/**
 * This class converts the internal representation of a <code>UIArea</code>
 * component into the output stream associated with the response to a
 * particular request.
 */

public class AreaRenderer extends Renderer {


    // -------------------------------------------------------- Renderer Methods


    /**
     * <p>No decoding is required.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void decode(FacesContext context, UIComponent component) {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

    }


    /**
     * <p>No begin encoding is required.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

    }


    /**
     * <p>No children encoding is required.</p>
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
     * <p>Encode this component.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        AreaComponent area = (AreaComponent) component;
        String targetImageId =
            area.findComponent(area.getTargetImage()).getClientId(context);
        ImageArea iarea = (ImageArea) area.getValue();
        ResponseWriter writer = context.getResponseWriter();
        StringBuffer sb = null;

        writer.startElement("area", area);
        writer.writeAttribute("alt", iarea.getAlt(), "alt");
        writer.writeAttribute("coords", iarea.getCoords(), "coords");
        writer.writeAttribute("shape", iarea.getShape(), "shape");
        // PENDING(craigmcc) - onmouseout only works on first form of a page
        sb =
            new StringBuffer("document.forms[0]['").append(targetImageId)
            .append("'].src='");
        sb.append(
            getURI(context, (String) area.getAttributes().get("onmouseout")));
        sb.append("'");
        writer.writeAttribute("onmouseout", sb.toString(), "onmouseout");
        // PENDING(craigmcc) - onmouseover only works on first form of a page
        sb =
            new StringBuffer("document.forms[0]['").append(targetImageId)
            .append("'].src='");
        sb.append(
            getURI(context, (String) area.getAttributes().get("onmouseover")));
        sb.append("'");
        writer.writeAttribute("onmouseover", sb.toString(), "onmouseover");
        // PENDING(craigmcc) - onclick only works on first form of a page
        sb = new StringBuffer("document.forms[0]['");
        sb.append(getName(context, area));
        sb.append("'].value='");
        sb.append(iarea.getAlt());
        sb.append("'; document.forms[0].submit()");
        writer.writeAttribute("onclick", sb.toString(), "value");
        writer.endElement("area");

    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Return the calculated name for the hidden input field.</p>
     *
     * @param context   Context for the current request
     * @param component Component we are rendering
     */
    private String getName(FacesContext context, UIComponent component) {
        while (component != null) {
            if (component instanceof MapComponent) {
                return (component.getId() + "_current");
            }
            component = component.getParent();
        }
        throw new IllegalArgumentException();
    }


    /**
     * <p>Return the path to be passed into JavaScript for the specified
     * value.</p>
     *
     * @param context Context for the current request
     * @param value   Partial path to be (potentially) modified
     */
    private String getURI(FacesContext context, String value) {
        if (value.startsWith("/")) {
            return (context.getExternalContext().getRequestContextPath() +
                value);
        } else {
            return (value);
        }
    }


}
