/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package components.taglib;

import components.components.ChartItemComponent;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

/**
 * <p><strong>ChartItemTag</strong> is the tag handler that processes the 
 * <code>chartItem</code> custom tag.</p>
 */

public class ChartItemTag extends UIComponentTag {

    public ChartItemTag() {
        super();
    }

    //
    // Class methods
    //

    // 
    // Accessors
    //

    /**
     * <p>The label for this item</p>
     */
    private String itemLabel = null;
    /**
     *<p>Set the label for this item.
     */
    public void setItemLabel(String label) {
        this.itemLabel = label;
    }

    /**
     * <p>The color for this item.</p>
     */
    private String itemColor = null;
    /**
     *<p>Set the color for this item.
     */
    public void setItemColor(String color) {
        this.itemColor = color;
    }
    
    /**
     * <p>The value for this item.</p>
     */
    private String itemValue = null;
    /**
     *<p>Set the ualue for this item.
     */
    public void setItemValue(String itemVal) {
        this.itemValue = itemVal;
    }

    private String value = null;
    public void setValue(String value) {
        this.value = value;
    }

    //
    // General Methods
    //

    /**
     * <p>Return the type of the component.
     */
    public String getComponentType() {
        return "ChartItem";
    }

    /**
     * <p>Return the renderer type (if any)
     */
    public String getRendererType() {
        return null;
    }

    /**
     * <p>Release any resources used by this tag handler
     */
    public void release() {
        super.release();
        itemLabel = null;
        itemValue = null;
        itemColor = null;
    }

    //
    // Methods from BaseComponentTag
    //

    /**
     * <p>Set the component properties
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        ChartItemComponent chartItem = (ChartItemComponent) component;
        if (null != value) {
            if (isValueReference(value)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(value);
                chartItem.setValueBinding("value", vb);
            } else {
                chartItem.setValue(value);
            }
        }

        if (null != itemLabel) {
            if (isValueReference(itemLabel)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(itemLabel);
                chartItem.setValueBinding("itemLabel", vb);
            } else {
                chartItem.setItemLabel(itemLabel);
            }
        }
        
        if (null != itemColor) {
            if (isValueReference(itemColor)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(itemColor);
                chartItem.setValueBinding("itemColor", vb);
            } else {
                chartItem.setItemColor(itemColor);
            }
        }
        
        if (null != itemValue) {
            if (isValueReference(itemValue)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().createValueBinding(itemValue);
                chartItem.setValueBinding("itemValue", vb);
            } else {
                chartItem.setItemValue(itemValue);
            }
        }
    }

}
