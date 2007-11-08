/*
 * $Id: PaneTabTag.java,v 1.1 2005/11/03 03:00:12 SherryShen Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.taglib;


import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;
import javax.faces.el.ValueBinding;


/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents an individual tab on the overall control.
 */
public class PaneTabTag extends UIComponentTag {


    private static Log log = LogFactory.getLog(PaneTabTag.class);


    public String getComponentType() {
        return ("Pane");
    }


    public String getRendererType() {
        return ("Tab");
    }


    public void release() {
        super.release();
    }

    protected String paneClass;
    public String getPaneClass() {
	return paneClass;
    }

    public void setPaneClass(String newPaneClass) {
	paneClass = newPaneClass;
    }



    protected void setProperties(UIComponent component) {
        super.setProperties(component);

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

    }
}
