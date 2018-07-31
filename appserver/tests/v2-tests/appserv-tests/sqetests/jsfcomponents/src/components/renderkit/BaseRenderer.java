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
 * $Id: BaseRenderer.java,v 1.3 2004/11/14 07:33:14 tcfujii Exp $
 */

package components.renderkit;


import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>Convenient base class for <code>Renderer</code> implementations.</p>
 */

public abstract class BaseRenderer extends Renderer {

    public static final String BUNDLE_ATTR = "com.sun.faces.bundle";


    public String convertClientId(FacesContext context, String clientId) {
        return clientId;
    }


    protected String getKeyAndLookupInBundle(FacesContext context,
                                             UIComponent component,
                                             String keyAttr)
        throws MissingResourceException {
        String key = null, bundleName = null;
        ResourceBundle bundle = null;

        key = (String) component.getAttributes().get(keyAttr);
        bundleName = (String) component.getAttributes().get(BUNDLE_ATTR);

        // if the bundleName is null for this component, it might have
        // been set on the root component.
        if (bundleName == null) {
            UIComponent root = context.getViewRoot();

            bundleName = (String) root.getAttributes().get(BUNDLE_ATTR);
        }
        // verify our component has the proper attributes for key and bundle.
        if (null == key || null == bundleName) {
            throw new MissingResourceException("Can't load JSTL classes",
                                               bundleName, key);
        }

        // verify the required Class is loadable
        // PENDING(edburns): Find a way to do this once per ServletContext.
        if (null == Thread.currentThread().getContextClassLoader().
            getResource("javax.servlet.jsp.jstl.fmt.LocalizationContext")) {
            Object[] params = {
                "javax.servlet.jsp.jstl.fmt.LocalizationContext"
            };
            throw new MissingResourceException("Can't load JSTL classes",
                                               bundleName, key);
        }

        // verify there is a ResourceBundle in scoped namescape.
        javax.servlet.jsp.jstl.fmt.LocalizationContext locCtx = null;
        if (null == (locCtx = (javax.servlet.jsp.jstl.fmt.LocalizationContext)
            (Util.getValueBinding(bundleName)).getValue(context)) ||
            null == (bundle = locCtx.getResourceBundle())) {
            throw new MissingResourceException("Can't load ResourceBundle ",
                                               bundleName, key);
        }

        return bundle.getString(key);
    }


    protected void encodeRecursive(FacesContext context, UIComponent component)
        throws IOException {

        component.encodeBegin(context);
        if (component.getRendersChildren()) {
            component.encodeChildren(context);
        } else {
            Iterator kids = component.getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                encodeRecursive(context, kid);
            }
        }
        component.encodeEnd(context);

    }


}
