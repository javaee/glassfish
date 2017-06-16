/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.taglib;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;


/**
 * This class is the tag handler that evaluates the <code>stylesheet</code>
 * custom tag.
 */

public class StylesheetTag extends UIComponentTag {


    private String path = null;


    public void setPath(String path) {
        this.path = path;
    }


    public String getComponentType() {
        return ("javax.faces.Output");
    }


    public String getRendererType() {
        return "Stylesheet";
    }


    protected void setProperties(UIComponent component) {

        super.setProperties(component);

        if (path != null) {
            component.getAttributes().put("path", path);
        }

    }


}
