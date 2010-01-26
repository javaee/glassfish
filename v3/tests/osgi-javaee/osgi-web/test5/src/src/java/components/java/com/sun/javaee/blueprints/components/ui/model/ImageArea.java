/*
 * Copyright 2005-2010 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.javaee.blueprints.components.ui.model;


import java.io.Serializable;


/**
 * <p>{@link ImageArea} is a JavaBean that represents a hotspot in an
 * image map.  Within a particular image map, no two hotspots may have
 * the same alternate text, because this is treated as a key.</p>
 */

public class ImageArea implements Serializable {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct an uninitialized {@link ImageArea} instance.</p>
     */
    public ImageArea() {
    }


    /**
     * <p>Construct an {@link ImageArea} initialized with the specified
     * property values.</p>
     *
     * @param alt    Alternate text for this hotspot
     * @param coords Coordinate positions for this hotspot
     * @param shape  Shape of this hotspot (default, rect, circle, poly)
     */
    public ImageArea(String alt, String coords, String shape) {
        setAlt(alt);
        setCoords(coords);
        setShape(shape);
    }


    // -------------------------------------------------------------- Properties


    private String alt = null;


    /**
     * <p>Return the alternate text for this hotspot.</p>
     */
    public String getAlt() {
        return (this.alt);
    }


    /**
     * <p>Set the alternate text for this hotspot.</p>
     *
     * @param alt The new alternate text
     */
    public void setAlt(String alt) {
        this.alt = alt;
    }


    private String coords = null;


    /**
     * <p>Return the coordinate positions for this hotspot.</p>
     */
    public String getCoords() {
        return (this.coords);
    }


    /**
     * <p>Set the coordinate positions for this hotspot.</p>
     *
     * @param coords The new coordinate positions
     */
    public void setCoords(String coords) {
        this.coords = coords;
    }


    private String shape = null;


    /**
     * <p>Return the shape for this hotspot.</p>
     */
    public String getShape() {
        return (this.shape);
    }


    /**
     * <p>Set the shape for this hotspot.</p>
     *
     * @param shape The new shape
     */
    public void setShape(String shape) {
        this.shape = shape;
    }


}
