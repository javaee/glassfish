/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: PaneTabbedTag.java,v 1.1 2005/11/03 03:00:13 SherryShen Exp $
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
