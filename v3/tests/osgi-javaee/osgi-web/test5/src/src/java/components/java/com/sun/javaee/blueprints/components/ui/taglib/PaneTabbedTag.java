/*
 * Copyright 2005 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.javaee.blueprints.components.ui.taglib;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentELTag;


/**
 * This class creates a <code>PaneComponent</code> instance
 * that represents a the overall tabbed pane control.
 */
public class PaneTabbedTag extends UIComponentELTag {

    private ValueExpression contentClass = null;


    public void setContentClass(ValueExpression contentClass) {
        this.contentClass = contentClass;
    }

    private ValueExpression paneClass = null;


    public void setPaneClass(ValueExpression paneClass) {
        this.paneClass = paneClass;
    }


    private ValueExpression selectedClass = null;


    public void setSelectedClass(ValueExpression selectedClass) {
        this.selectedClass = selectedClass;
    }


    private ValueExpression unselectedClass = null;


    public void setUnselectedClass(ValueExpression unselectedClass) {
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
            if (!contentClass.isLiteralText()) {
                component.setValueExpression("contentClass", contentClass);
            } else {
                component.getAttributes().put("contentClass", contentClass.getExpressionString());
            }
        }

        if (paneClass != null) {
            if (!paneClass.isLiteralText()) {
                component.setValueExpression("paneClass", paneClass);
            } else {
                component.getAttributes().put("paneClass", paneClass.getExpressionString());
            }
        }

        if (selectedClass != null) {
            if (!selectedClass.isLiteralText()) {
                component.setValueExpression("selectedClass", selectedClass);
            } else {
                component.getAttributes().put("selectedClass", selectedClass.getExpressionString());
            }
        }

        if (unselectedClass != null) {
            if (!unselectedClass.isLiteralText()) {
                component.setValueExpression("unselectedClass", unselectedClass);
            } else {
                component.getAttributes().put("unselectedClass", unselectedClass.getExpressionString());
            }
        }
    }

}
