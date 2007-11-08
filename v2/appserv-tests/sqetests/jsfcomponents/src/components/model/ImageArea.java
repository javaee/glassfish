/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.model;


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
