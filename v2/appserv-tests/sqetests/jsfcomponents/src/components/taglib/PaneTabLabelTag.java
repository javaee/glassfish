/*
 * $Id: PaneTabLabelTag.java,v 1.1 2005/11/03 03:00:11 SherryShen Exp $
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
 * that represents a tab button control on the tab pane.
 */
public class PaneTabLabelTag extends UIComponentTag {

    private static Log log = LogFactory.getLog(PaneTabLabelTag.class);


    private String commandName = null;


    public void setCommandName(String newCommandName) {
        commandName = newCommandName;
    }


    private String image = null;


    public void setImage(String newImage) {
        image = newImage;
    }


    private String label = null;


    public void setLabel(String newLabel) {
        label = newLabel;
    }


    public String getComponentType() {
        return ("Pane");
    }


    public String getRendererType() {
        return ("TabLabel");
    }

    protected String paneTabLabelClass;
    public String getPaneTabLabelClass() {
	return paneTabLabelClass;
    }

    public void setPaneTabLabelClass(String newPaneTabLabelClass) {
	paneTabLabelClass = newPaneTabLabelClass;
    }

    public void release() {
        super.release();
        this.commandName = null;
        this.image = null;
        this.label = null;
    }


    protected void setProperties(UIComponent component) {

        super.setProperties(component);

        if (commandName != null) {
            if (isValueReference(commandName)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(commandName);
                component.setValueBinding("commandName", vb);
            } else {
                component.getAttributes().put("commandName", commandName);
            }
        }

        if (image != null) {
            if (isValueReference(image)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(image);
                component.setValueBinding("image", vb);
            } else {
                component.getAttributes().put("image", image);
            }
        }

        if (label != null) {
            if (isValueReference(label)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(label);
                component.setValueBinding("label", vb);
            } else {
                component.getAttributes().put("label", label);
            }
        }

        if (paneTabLabelClass != null) {
            if (isValueReference(paneTabLabelClass)) {
                ValueBinding vb =
                    getFacesContext().getApplication().
                    createValueBinding(paneTabLabelClass);
                component.setValueBinding("paneTabLabelClass", vb);
            } else {
                component.getAttributes().put("paneTabLabelClass", 
					      paneTabLabelClass);
            }
        }

    }


}
