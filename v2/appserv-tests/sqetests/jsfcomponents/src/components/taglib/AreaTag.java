/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.taglib;

import components.components.AreaComponent;
import components.renderkit.Util;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.webapp.UIComponentTag;


/**
 * <p>{@link UIComponentTag} for an image map hotspot.</p>
 */

public class AreaTag extends UIComponentTag {


    private String alt = null;


    public void setAlt(String alt) {
        this.alt = alt;
    }


    private String targetImage = null;


    public void setTargetImage(String targetImage) {
        this.targetImage = targetImage;
    }


    private String coords = null;


    public void setCoords(String coords) {
        this.coords = coords;
    }


    private String onmouseout = null;


    public void setOnmouseout(String newonmouseout) {
        onmouseout = newonmouseout;
    }


    private String onmouseover = null;


    public void setOnmouseover(String newonmouseover) {
        onmouseover = newonmouseover;
    }


    private String shape = null;


    public void setShape(String shape) {
        this.shape = shape;
    }


    private String styleClass = null;


    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }


    private String value = null;


    public void setValue(String newValue) {
        value = newValue;
    }


    public String getComponentType() {
        return ("DemoArea");
    }


    public String getRendererType() {
        return ("DemoArea");
    }


    public void release() {
        super.release();
        this.alt = null;
        this.coords = null;
        this.onmouseout = null;
        this.onmouseover = null;
        this.shape = null;
        this.styleClass = null;
        this.value = null;
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        AreaComponent area = (AreaComponent) component;
        if (alt != null) {
            if (isValueReference(alt)) {
                area.setValueBinding("alt", Util.getValueBinding(alt));
            } else {
                area.getAttributes().put("alt", alt);
            }
        }
        if (coords != null) {
            if (isValueReference(coords)) {
                area.setValueBinding("coords", Util.getValueBinding(coords));
            } else {
                area.getAttributes().put("coords", coords);
            }
        }
        if (onmouseout != null) {
            if (isValueReference(onmouseout)) {
                area.setValueBinding("onmouseout",
                                     Util.getValueBinding(onmouseout));
            } else {
                area.getAttributes().put("onmouseout", onmouseout);
            }
        }
        if (onmouseover != null) {
            if (isValueReference(onmouseover)) {
                area.setValueBinding("onmouseover",
                                     Util.getValueBinding(onmouseover));
            } else {
                area.getAttributes().put("onmouseover", onmouseover);
            }
        }
        if (shape != null) {
            if (isValueReference(shape)) {
                area.setValueBinding("shape", Util.getValueBinding(shape));
            } else {
                area.getAttributes().put("shape", shape);
            }
        }
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                area.setValueBinding("styleClass",
                                     Util.getValueBinding(styleClass));
            } else {
                area.getAttributes().put("styleClass", styleClass);
            }
        }
        if (area instanceof ValueHolder) {
            ValueHolder valueHolder = (ValueHolder) component;
            if (value != null) {
                if (isValueReference(value)) {
                    area.setValueBinding("value", Util.getValueBinding(value));
                } else {
                    valueHolder.setValue(value);
                }
            }
        }
        // target image is required
        area.setTargetImage(targetImage);
    }
}
