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


import components.components.MapComponent;
import components.renderkit.Util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.webapp.UIComponentTag;


/**
 * <p>{@link UIComponentTag} for an image map.</p>
 */

public class MapTag extends UIComponentTag {


    private String current = null;


    public void setCurrent(String current) {
        this.current = current;
    }


    private String actionListener = null;


    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }


    private String action = null;


    public void setAction(String action) {
        this.action = action;
    }


    private String immediate = null;


    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }


    private String styleClass = null;


    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }


    public String getComponentType() {
        return ("DemoMap");
    }


    public String getRendererType() {
        return ("DemoMap");
    }


    public void release() {
        super.release();
        current = null;
        styleClass = null;
        actionListener = null;
        action = null;
        immediate = null;
        styleClass = null;
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        MapComponent map = (MapComponent) component;
        //        if (current != null) {
        //            map.setCurrent(current);
        //        }
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().
                    createValueBinding(styleClass);
                map.setValueBinding("styleClass", vb);
            } else {
                map.getAttributes().put("styleClass", styleClass);
            }
        }
        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                Class args[] = {ActionEvent.class};
                MethodBinding mb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(actionListener, args);
                map.setActionListener(mb);
            } else {
                Object params [] = {actionListener};
                throw new javax.faces.FacesException();
            }
        }

        if (action != null) {
            if (isValueReference(action)) {
                MethodBinding vb = FacesContext.getCurrentInstance()
                    .getApplication()
                    .createMethodBinding(action, null);
                map.setAction(vb);
            } else {
                map.setAction(Util.createConstantMethodBinding(action));
            }
        }
        if (immediate != null) {
            if (isValueReference(immediate)) {
                ValueBinding vb = FacesContext.getCurrentInstance()
                    .getApplication().
                    createValueBinding(immediate);
                map.setValueBinding("immediate", vb);
            } else {
                boolean _immediate = new Boolean(immediate).booleanValue();
                map.setImmediate(_immediate);
            }
        }

    }


}
