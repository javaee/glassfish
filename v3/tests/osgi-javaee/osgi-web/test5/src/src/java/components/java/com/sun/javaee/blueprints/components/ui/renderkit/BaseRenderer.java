/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html  
$Id: BaseRenderer.java,v 1.5 2005/11/04 17:23:58 edburns Exp $ */

package com.sun.javaee.blueprints.components.ui.renderkit;


import com.sun.javaee.blueprints.components.ui.util.Util;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.FactoryFinder;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Convenient base class for <code>Renderer</code> implementations.</p>
 */

public abstract class BaseRenderer extends Renderer {

    public static final String BUNDLE_ATTR = "com.sun.faces.bundle";


    public String convertClientId(FacesContext context, String clientId) {
        return clientId;
    }
    
    public ResponseWriter setupResponseWriter(FacesContext context) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        if (writer == null) {
	    HttpServletRequest request = (HttpServletRequest)
		context.getExternalContext().getRequest();
	    HttpServletResponse response = (HttpServletResponse)
		context.getExternalContext().getResponse();


	    RenderKitFactory renderFactory = (RenderKitFactory)
		FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
	    RenderKit renderKit = 
		renderFactory.getRenderKit
                (context, context.getViewRoot().getRenderKitId());
	    // PENDING(edburns): spec: add content/type and character
	    // encoding to external context?
            writer = 
		renderKit.createResponseWriter(response.getWriter(),
                                               "text/html",
					       request.getCharacterEncoding());
	    
            context.setResponseWriter(writer);
        }
	return writer;
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
