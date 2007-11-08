/*
 * $Id: PaneTabbedTag.java,v 1.1 2005/11/03 03:00:13 SherryShen Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.taglib;


import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;


/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents a the overall tabbed pane control.
 */
public class PaneTabbedTag extends UIComponentTag {


    private static Log log = LogFactory.getLog(PaneTabbedTag.class);


    private String contentClass = null;


    public void setContentClass(String contentClass) {
        this.contentClass = contentClass;
    }


    private String paneClass = null;


    public void setPaneClass(String paneClass) {
        this.paneClass = paneClass;
    }


    private String selectedClass = null;


    public void setSelectedClass(String selectedClass) {
        this.selectedClass = selectedClass;
    }


    private String unselectedClass = null;


    public void setUnselectedClass(String unselectedClass) {
        this.unselectedClass = unselectedClass;
    }


    public String getComponentType() {
        return ("Pane");
    }


    public String getRendererType() {
        return ("Tabbed");
    }


    public void release() {
        super.release();
        contentClass = null;
        paneClass = null;
        selectedClass = null;
        unselectedClass = null;
    }


    protected void setProperties(UIComponent component) {

        super.setProperties(component);

        if (contentClass != null) {
            if (isValueReference(contentClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(contentClass);
                component.setValueBinding("contentClass", vb);
            } else {
                component.getAttributes().put("contentClass", contentClass);
            }
        }

        if (paneClass != null) {
            if (isValueReference(paneClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(paneClass);
                component.setValueBinding("paneClass", vb);
            } else {
                component.getAttributes().put("paneClass", paneClass);
            }
        }

        if (selectedClass != null) {
            if (isValueReference(selectedClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(selectedClass);
                component.setValueBinding("selectedClass", vb);
            } else {
                component.getAttributes().put("selectedClass", selectedClass);
            }
        }

        if (unselectedClass != null) {
            if (isValueReference(unselectedClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(unselectedClass);
                component.setValueBinding("unselectedClass", vb);
            } else {
                component.getAttributes().put("unselectedClass",
                                              unselectedClass);
            }
        }
    }


}
