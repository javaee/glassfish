/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.javaee.blueprints.components.ui.popup;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.webapp.UIComponentTag;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.FactoryFinder;

/**
 * This tag is the glue between the JSF tag and the renderer and JSF Component
 *
 * @author  basler
 */

public class PopupTag extends UIComponentTag {
    
    private String id=null, xmlHttpRequestURL=null, elementNamePairs=null;
    
    public String getComponentType() {
        return ("javax.faces.Output");
    }
    
    public String getRendererType() {
        return ("Popup");
    }

    public void setId(String sxId) {
        id=sxId;
    }
    public String getId() {
        return id;
    }

    public void setXmlHttpRequestURL(String sxURL) {
        xmlHttpRequestURL=sxURL;
    }
    public String getXmlHttpRequestURL() {
        return xmlHttpRequestURL;
    }

    public void setElementNamePairs(String sxNames) {
        elementNamePairs=sxNames;
    }
    public String getElementNamePairs() {
        return elementNamePairs;
    }


    /**
     * This method takes the properties from the JSP and populates the UIOutput components properties
     * for use in the PopupRenderer
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UIOutput outComp = (UIOutput) component;
        
        // pull out id attribute
        if (id != null) {
            if (isValueReference(id)) {
                outComp.setValueBinding("id", getValueBinding(id));
            } else {
                outComp.getAttributes().put("id", id);
            }
        }

        // pull out xmlHttpRequestURL attribute
        if (xmlHttpRequestURL != null) {
            if (isValueReference(xmlHttpRequestURL)) {
                outComp.setValueBinding("xmlHttpRequestURL", getValueBinding(xmlHttpRequestURL));
            } else {
                outComp.getAttributes().put("xmlHttpRequestURL", xmlHttpRequestURL);
            }
        }

        // pull out elementNamePairs attribute
        if (elementNamePairs != null) {
            if (isValueReference(elementNamePairs)) {
                outComp.setValueBinding("elementNamePairs", getValueBinding(elementNamePairs));
            } else {
                outComp.getAttributes().put("elementNamePairs", elementNamePairs);
            }
        }
    }
    
    
    private ValueBinding getValueBinding(String valueRef) {
        ApplicationFactory af=(ApplicationFactory)FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        Application a = af.getApplication();
        return (a.createValueBinding(valueRef));
    }
    
}
