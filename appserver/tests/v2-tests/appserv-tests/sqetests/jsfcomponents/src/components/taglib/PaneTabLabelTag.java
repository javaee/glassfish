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
 * $Id: PaneTabLabelTag.java,v 1.1 2005/11/03 03:00:11 SherryShen Exp $
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
