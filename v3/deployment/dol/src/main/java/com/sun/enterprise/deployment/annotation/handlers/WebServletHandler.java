/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.deployment.annotation.handlers;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServlet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.InitParam;

import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.EnvironmentProperty; 
import com.sun.enterprise.deployment.WebComponentDescriptor; 
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;

/**
 * This handler is responsible in handling
 * javax.servlet.annotation.WebServlet.
 *
 * @author Shing Wai Chan
 */
@Service
public class WebServletHandler extends AbstractWebHandler {
    public WebServletHandler() {
    }

    /**
     * @return the annotation type this annotation handler is handling
     */
    public Class<? extends Annotation> getAnnotationType() {
        return WebServlet.class;
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            WebComponentContext[] webCompContexts)
            throws AnnotationProcessorException {

        // web comp with the given class name exists, do nothing
        return getDefaultProcessedResult();
    }

    protected HandlerProcessingResult processAnnotation(
            AnnotationInfo ainfo, WebBundleContext webBundleContext)
            throws AnnotationProcessorException {

        Class webCompClass = (Class)ainfo.getAnnotatedElement();
        if (!HttpServlet.class.isAssignableFrom(webCompClass)) {
            log(Level.SEVERE, ainfo,
                localStrings.getLocalString(
                "enterprise.deployment.annotation.handlers.needtoextend",
                "The Class {0} having annotation {1} need to be a derived class of {2}.",
                new Object[] { webCompClass.getName(), WebServlet.class.getName(), HttpServlet.class.getName() }));
            return getDefaultFailedResult();
        }
        WebServlet webServletAn = (WebServlet)ainfo.getAnnotation();

        WebComponentDescriptor webCompDesc = new WebComponentDescriptor();
        webCompDesc.setServlet(true);
        webCompDesc.setWebComponentImplementation(webCompClass.getName());

        String servletName = webServletAn.name();
        if (servletName == null || servletName.length() == 0) {
            servletName = webCompClass.getName();
        }
        webCompDesc.setName(servletName);

        //XXX validate or default
        String[] urlPatterns = webServletAn.urlPatterns();
        if (urlPatterns == null || urlPatterns.length == 0) {
            urlPatterns = webServletAn.value();
        }

        if (urlPatterns != null && urlPatterns.length > 0) {
            Set<String> urlPatternsSet = webCompDesc.getUrlPatternsSet();
            for (String up : urlPatterns) {
                urlPatternsSet.add(up);
            }
        }

        webCompDesc.setLoadOnStartUp(webServletAn.loadOnStartup());

        InitParam[] initParams = webServletAn.initParams();
        if (initParams != null && initParams.length > 0) {
            for (InitParam initParam : initParams) {
                webCompDesc.addInitializationParameter(
                    new EnvironmentProperty(
                        initParam.name(), initParam.value(),
                        initParam.description()));
            }
        }

        //XXX small vs large
        webCompDesc.setSmallIconUri(webServletAn.icon());
        webCompDesc.setLargeIconUri(webServletAn.icon());
        webCompDesc.setDescription(webServletAn.description());

        webBundleContext.getDescriptor().addWebComponentDescriptor(webCompDesc);
        WebComponentContext webCompContext = new WebComponentContext(webCompDesc);
        // we push the new context on the stack...
        webBundleContext.getProcessingContext().pushHandler(webCompContext);

        return getDefaultProcessedResult();
    }
}
