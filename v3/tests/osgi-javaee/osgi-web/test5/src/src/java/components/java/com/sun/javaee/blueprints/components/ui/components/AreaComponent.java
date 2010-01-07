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

package com.sun.javaee.blueprints.components.ui.components;


import com.sun.javaee.blueprints.components.ui.model.ImageArea;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;

import java.io.IOException;


/**
 * <p>{@link AreaComponent} is a JavaServer Faces component that represents
 * a particular hotspot in a client-side image map defined by our parent
 * {@link MapComponent}.  The <code>valueRef</code> property (if present)
 * must point at a JavaBean of type <code>components.model.ImageArea</code>;
 * if not present, an <code>ImageArea</code> instance will be synthesized
 * from the values of the <code>alt</code>, <code>coords</code>, and
 * <code>shape</code> properties, and assigned to the <code>value</code>
 * property.</p>
 */

public class AreaComponent extends UIOutput {


    // ------------------------------------------------------ Instance Variables


    private String alt = null;
    private String coords = null;
    private String shape = null;
    private String targetImage = null;



    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the alternate text for our synthesized {@link ImageArea}.</p>
     */
    public String getAlt() {
        return (this.alt);
    }


    /**
     * <p>Set the alternate text for our synthesized {@link ImageArea}.</p>
     *
     * @param alt The new alternate text
     */
    public void setAlt(String alt) {
        this.alt = alt;
    }


    /**
     * <p>Return the hotspot coordinates for our synthesized {@link ImageArea}.
     * </p>
     */
    public String getCoords() {
        return (this.coords);
    }


    /**
     * <p>Set the hotspot coordinates for our synthesized {@link ImageArea}.</p>
     *
     * @param coords The new coordinates
     */
    public void setCoords(String coords) {
        this.coords = coords;
    }


    /**
     * <p>Return the shape for our synthesized {@link ImageArea}.</p>
     */
    public String getShape() {
        return (this.shape);
    }


    /**
     * <p>Set the shape for our synthesized {@link ImageArea}.</p>
     *
     * @param shape The new shape (default, rect, circle, poly)
     */
    public void setShape(String shape) {
        this.shape = shape;
    }


    /**
     * <p>Set the image that is the target of this <code>AreaComponent</code>.</p>
     *
     * @return the target image of this area component.
     */
    public String getTargetImage() {
        return targetImage;
    }


    /**
     * <p>Set the image that is the target of this <code>AreaComponent</code>.</p>
     *
     * @param targetImage the ID of the target of this <code>AreaComponent</code>
     */
    public void setTargetImage(String targetImage) {
        this.targetImage = targetImage;
    }


    /**
     * <p>Return the component family for this component.</p>
     */
    public String getFamily() {

        return ("Area");

    }

    // -------------------------------------------------------- UIOutput Methods


    /**
     * <p>Synthesize and return an {@link ImageArea} bean for this hotspot,
     * if there is no <code>valueRef</code> property on this component.</p>
     */
    public Object getValue() {

        if (super.getValue() == null) {
            setValue(new ImageArea(getAlt(), getCoords(), getShape()));
        }
        return (super.getValue());

    }


    // ----------------------------------------------------- StateHolder Methods


    /**
     * <p>Return the state to be saved for this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */

    public Object saveState(FacesContext context) {
        Object values[] = new Object[5];
        values[0] = super.saveState(context);
        values[1] = alt;
        values[2] = coords;
        values[3] = shape;
        values[4] = targetImage;
        return (values);
    }


    /**
     * <p>Restore the state for this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param state   State to be restored
     *
     * @throws IOException if an input/output error occurs
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        alt = (String) values[1];
        coords = (String) values[2];
        shape = (String) values[3];
        targetImage = (String) values[4];
    }


}
