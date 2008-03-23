/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.web.tomcat;

import java.io.File;
import java.net.URI;

import javax.servlet.Servlet;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.v3.server.Globals;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;

/**
 * Listener responsible for configuring JSP compilation classpath.
 *
 * @author jluehe
 */
public class TomcatModuleListener implements LifecycleListener {

    private static final String JSP_SERVLET
        = "org.apache.jasper.servlet.JspServlet";
    private static final String JSP_URL_PATTERN="*.jsp";

    private String javacClassPath;


    public TomcatModuleListener() {
        ModulesRegistry mr = Globals.getDefaultHabitat().getByContract(ModulesRegistry.class);
        Module module = mr.find(LifecycleListener.class);
        ModuleDefinition moduleDef = module.getModuleDefinition();
        URI[] uris = moduleDef.getLocations();
        javacClassPath = uris[0].getPath() + File.pathSeparator;
        for (int i=1; i<uris.length; i++) {
            javacClassPath += uris[i].getPath() + File.pathSeparator;
        }
        moduleDef = module.getModuleDefinition();
        uris = moduleDef.getLocations();
        for (int i=0; i<uris.length; i++) {
            javacClassPath += uris[i].getPath() + File.pathSeparator;
        }
        javacClassPath += ".";
    }


    public void lifecycleEvent(LifecycleEvent event) {

        Context ctx = (Context) event.getLifecycle();

        if (event.getType().equals(Lifecycle.START_EVENT)) {
            configureJspParameters(ctx);
        }
    }


    /**
     * Configures the JSP compilation path for the given context.
     *
     * @param ctx The context whose JSP compilation path is to be configured
     */
    private void configureJspParameters(Context ctx) {

        // Find the default jsp servlet
        String name = ctx.findServletMapping(JSP_URL_PATTERN);
        Wrapper wrapper = (Wrapper) ctx.findChild(name);
        if (wrapper == null) {
            return;
        }

        String servletClass = wrapper.getServletClass();
        if (servletClass != null && servletClass.equals(JSP_SERVLET)) {
            wrapper.addInitParameter("com.sun.appserv.jsp.classpath",
                                     javacClassPath);
        }
    }
}
