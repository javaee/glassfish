/*
 * $Id: DataRepeaterTag.java,v 1.3 2004/11/14 07:33:16 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.taglib;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

/**
 * <p>DataRepeaterTag is the tag handler class for a <code>UIData</code>
 * component associated with a <code>RepeaterRenderer</code>.</p>
 */

public class DataRepeaterTag extends UIComponentTag {


    // -------------------------------------------------------------- Attributes


    private String first = null;


    public void setFirst(String first) {
        this.first = first;
    }


    private String rows = null;


    public void setRows(String rows) {
        this.rows = rows;
    }


    private String styleClass = null;


    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }


    private String value = null;


    public void setValue(String value) {
        this.value = value;
    }


    private String var = null;


    public void setVar(String var) {
        this.var = var;
    }


    // -------------------------------------------------- UIComponentTag Methods


    public String getComponentType() {
        return ("javax.faces.Data");
    }


    public String getRendererType() {
        return ("Repeater");
    }


    public void release() {
        super.release();
        first = null;
        rows = null;
        styleClass = null;
        value = null;
        var = null;
    }


    protected void setProperties(UIComponent component) {

        super.setProperties(component);

        if (first != null) {
            if (isValueReference(first)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(first);
                component.setValueBinding("first", vb);
            } else {
                ((UIData) component).setFirst(Integer.parseInt(first));
            }
        }

        if (rows != null) {
            if (isValueReference(rows)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(rows);
                component.setValueBinding("rows", vb);
            } else {
                ((UIData) component).setRows(Integer.parseInt(rows));
            }
        }

        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(styleClass);
                component.setValueBinding("styleClass", vb);
            } else {
                component.getAttributes().put("styleClass", styleClass);
            }
        }

        if (value != null) {
            if (isValueReference(value)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(value);
                component.setValueBinding("value", vb);
            } else {
                ((UIData) component).setValue(value);
            }
        }

        if (var != null) {
            if (isValueReference(var)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(var);
                component.setValueBinding("var", vb);
            } else {
                ((UIData) component).setVar(var);
            }
        }

    }


}
