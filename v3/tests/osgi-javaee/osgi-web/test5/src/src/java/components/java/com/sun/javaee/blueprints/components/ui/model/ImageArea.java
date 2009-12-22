/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: ImageArea.java,v 1.2 2005/09/22 00:11:28 inder Exp $ */

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
