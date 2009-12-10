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


package org.glassfish.osgihttp;

import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardWrapper;
import org.glassfish.web.valve.GlassFishValve;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.Enumeration;

/**
 * Unlike Java EE Web Application model, there is no notion of "context path"
 * in OSGi HTTP service spec. Here the servlets can specify which context they
 * belong to by passing a {@link org.osgi.service.http.HttpContext} object.
 * Those HttpContext objects don't have any "path" attribute. As a result,
 * all the OSGi/HTTP servlets belonging to the same servlet context may not
 * have any of the path common to them. Internally, we register all the OSGi
 * servlets (actually we register {@link OSGiServletWrapper}
 * with the same {@link org.apache.catalina.Context} object. So we need a way to
 * demultiplex the OSGi servlet context.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiServletWrapper extends StandardWrapper implements Wrapper {

    // TODO(Sahoo): Logging

    private Servlet servlet;
    private OSGiServletConfig config;

    public OSGiServletWrapper(String name, Servlet servlet, OSGiServletConfig config, String urlMapping) {
        this.servlet = servlet;
        this.config = config;
        setServlet(servlet);
        setName(name);
        addMapping(urlMapping);
    }

    public Servlet getServlet() {
        return servlet;
    }

    /* package */ void initializeServlet() throws ServletException {
        servlet.init(config);
    }

    /* package */ void destroyServlet() {
        servlet.destroy();
    }

    // BEGIN: Override ServletConfig methods

    @Override
    public String getServletName() {
        return config.getServletName();
    }

    @Override
    public OSGiServletContext getServletContext() {
        // We can't use super.getServletContext, as that would get us the
        // ServletContext that's common for all OSGi/HTTP servlets, where as
        // we need the servlet context registered for each HttpContext.
        return config.getServletContext();
    }

    @Override
    public String getInitParameter(String name) {
        return config.getInitParameter(name);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return config.getInitParameterNames();
    }

    // END: Override ServletConfig methods

    // BEGIN: Override lifecycle methods of StandardWrapper...

    @Override
    public Servlet allocate() throws ServletException {
        return servlet;
    }

    @Override
    public synchronized void load() throws ServletException {
        // NOOP: We already have the Servlet instance.
    }

    @Override
    public synchronized void unload() throws ServletException {
        // NOOP: We don't have to do anything, as HttpService calls
        // destroServlet method directly from unregister() method.
        return;
    }
    // END: Override lifecycle methods of StandardWrapper...

    // Override addValve as StandardWrapper has put in an optimisation
    // and does not support adding any valve (see issue #1343).
    @Override
    public synchronized void addValve(GlassFishValve valve) {
        getPipeline().addValve(valve);
    }
}
