/*
 * Copyright 2005-2010 Sun Microsystems, Inc. All Rights Reserved.
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
 * that represents a tab button control on the tab pane.
 */
public class PaneTabLabelTag extends UIComponentELTag {

    private ValueExpression commandName = null;


    public void setCommandName(ValueExpression newCommandName) {
        commandName = newCommandName;
    }

    private ValueExpression image = null;

    public void setImage(ValueExpression newImage) {
        image = newImage;
    }

    private ValueExpression label = null;


    public void setLabel(ValueExpression newLabel) {
        label = newLabel;
    }

    public String getComponentType() {
        return ("Pane");
    }

    public String getRendererType() {
        return ("TabLabel");
    }

    protected ValueExpression paneTabLabelClass;
    public ValueExpression getPaneTabLabelClass() {
	return paneTabLabelClass;
    }

    public void setPaneTabLabelClass(ValueExpression newPaneTabLabelClass) {
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
            if (!commandName.isLiteralText()) {
                component.setValueExpression("commandName", commandName);
            } else {
                component.getAttributes().put("commandName", commandName.getExpressionString());
            }
        }

        if (image != null) {
            if (!image.isLiteralText()) {
                component.setValueExpression("image", image);
            } else {
                component.getAttributes().put("image", image.getExpressionString());
            }
        }

        if (label != null) {
            if (!label.isLiteralText()) {
                component.setValueExpression("label", label);
            } else {
                component.getAttributes().put("label", label.getExpressionString());
            }
        }

        if (paneTabLabelClass != null) {
            if (!paneTabLabelClass.isLiteralText()) {
                component.setValueExpression("paneTabLabelClass", paneTabLabelClass);
            } else {
                component.getAttributes().put("paneTabLabelClass", paneTabLabelClass.getExpressionString());
            }
        }
    }
}
