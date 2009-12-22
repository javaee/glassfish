/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
 */

/*
 * AjaxTextFieldTag.java
 *
 * Created on April 29, 2005, 12:44 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package com.sun.javaee.blueprints.components.ui.textfield;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentTag;

import com.sun.faces.util.Util;


/**
 * <p>This class is the tag handler class for a <code>AjaxTextField</code>
 * component associated with a <code>AjaxTextFieldRenderer</code> renderer.
 * 
 * @todo XXX what about the binding property? I need this for Creator...
 * </p>
 */
public class AjaxTextFieldTag extends UIComponentTag {
    private String completionMethod;
    private String onchoose;
    private String ondisplay;


    /**
     * Gets the type of the component associated wit hthe tag.
     * @return the name of the component type
     */
    public String getComponentType() {
        return "bpcatalog.ajax.AjaxTextField";
        //return "javax.faces.Input";
    }

    /**
     * Gets the type of the renderer that will render this tag.
     * @return the type of the renderer that renders this tag
     */
    public String getRendererType() {
        return "AjaxTextField";
    }

    /**
     * Releases resources allocated during the execution of this tag handler.
     */
    public void release() {
        super.release();
        this.completionMethod = null;
    }

    /**
     * Sets the properties of the specified component.
     * @param component the component associated with this tag
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        AjaxTextField input = null;

        try {
            input = (AjaxTextField)component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException("Component " + component.toString() +
                " not expected type.  Expected: AjaxTextField.  Perhaps you're missing a tag?");
        }

        if (completionMethod != null) {
            input.setCompletionMethod(completionMethod);
        }

        if (onchoose != null) {
            if (isValueReference(onchoose)) {
                ValueBinding vb = Util.getValueBinding(onchoose);
                input.setValueBinding("onchoose", vb);
            } else {
                input.setOnchoose(onchoose);
            }
        }
        if (ondisplay != null) {
            if (isValueReference(ondisplay)) {
                ValueBinding vb = Util.getValueBinding(ondisplay);
                input.setValueBinding("ondisplay", vb);
            } else {
                input.setOndisplay(ondisplay);
            }
        }
        
        if (converter != null) {
            if (isValueReference(converter)) {
                      ValueBinding vb = 
                          Util.getValueBinding(converter);
                  input.setValueBinding("converter", vb);
            } else {
                Converter _converter = FacesContext.getCurrentInstance().
                    getApplication().createConverter(converter);
                input.setConverter(_converter);
            }
        }

        if (immediate != null) {
            if (isValueReference(immediate)) {
                ValueBinding vb = Util.getValueBinding(immediate);
                input.setValueBinding("immediate", vb);
            } else {
                boolean _immediate = new Boolean(immediate).booleanValue();
                input.setImmediate(_immediate);
            }
        }
        if (required != null) {
            if (isValueReference(required)) {
                ValueBinding vb = Util.getValueBinding(required);
                input.setValueBinding("required", vb);
            } else {
                boolean _required = new Boolean(required).booleanValue();
                input.setRequired(_required);
            }
        }
        if (validator != null) {
            if (isValueReference(validator)) {
                Class args[] = { FacesContext.class, UIComponent.class, Object.class };
                MethodBinding vb = FacesContext.getCurrentInstance().getApplication().createMethodBinding(validator, args);
                input.setValidator(vb);
            } else {
              Object params [] = {validator};
              throw new javax.faces.FacesException(Util.getExceptionMessageString(Util.INVALID_EXPRESSION_ID, params));
            }
            }
        if (value != null) {
            if (isValueReference(value)) {
                ValueBinding vb = Util.getValueBinding(value);
                input.setValueBinding("value", vb);
            } else {
                input.setValue(value);
            }
        }
        if (valueChangeListener != null) {
            if (isValueReference(valueChangeListener)) {
                Class args[] = { ValueChangeEvent.class };
                MethodBinding vb = FacesContext.getCurrentInstance().getApplication().createMethodBinding(valueChangeListener, args);
                input.setValueChangeListener(vb);
            } else {
              Object params [] = {valueChangeListener};
              throw new javax.faces.FacesException(Util.getExceptionMessageString(Util.INVALID_EXPRESSION_ID, params));
            }
        }
        if (maxlength != null) {
            if (isValueReference(maxlength)) {
                ValueBinding vb = Util.getValueBinding(maxlength);
                input.setValueBinding("maxlength", vb);
            } else {
                int _maxlength = new Integer(maxlength).intValue();
                if (_maxlength != Integer.MIN_VALUE) {
                    input.getAttributes().put("maxlength", new Integer(_maxlength));
                }
            }
        }
        if (size != null) {
            if (isValueReference(size)) {
                ValueBinding vb = Util.getValueBinding(size);
                input.setValueBinding("size", vb);
            } else {
                int _size = new Integer(size).intValue();
                if (_size != Integer.MIN_VALUE) {
                    input.getAttributes().put("size", new Integer(_size));
                }
            }
        }
        if (style != null) {
            if (isValueReference(style)) {
                ValueBinding vb = Util.getValueBinding(style);
                input.setValueBinding("style", vb);
            } else {
                input.getAttributes().put("style", style);
            }
        }
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                ValueBinding vb = Util.getValueBinding(styleClass);
                input.setValueBinding("styleClass", vb);
            } else {
                input.getAttributes().put("styleClass", styleClass);
            }
        }
        if (title != null) {
            if (isValueReference(title)) {
                ValueBinding vb = Util.getValueBinding(title);
                input.setValueBinding("title", vb);
            } else {
                input.getAttributes().put("title", title);
            }
        }
    }

    
    public void setCompletionMethod(String completionMethod) {
        this.completionMethod = completionMethod;
    }
    
    public void setOnchoose(String onchoose) {
        this.onchoose = onchoose;
    }
    
    public void setOndisplay(String ondisplay) {
        this.ondisplay = ondisplay;
    }
    
    private String converter;
    private String immediate;
    private String required;
    private String validator;
    private String value;
    private String valueChangeListener;
    private String maxlength;
    private String size;
    private String style;
    private String styleClass;
    private String title;

    //
    // Setter Methods
    //

    public void setConverter(String converter) {
        this.converter = converter;
    }

    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValueChangeListener(String valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    public void setMaxlength(String maxlength) {
        this.maxlength = maxlength;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
