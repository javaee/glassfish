/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;

public class MyListener implements ServletContextListener {

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        try {
            doContextInitialized(sce);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

    private void doContextInitialized(ServletContextEvent sce)
            throws ClassNotFoundException {

        ServletContext sc = sce.getServletContext();

        /*
         * Register servlet
         */
        NewServlet servlet = new NewServlet();
        servlet.setMyParameter("myServletParamValue");
        ServletRegistration sr = sc.addServlet("NewServlet", servlet);
        sr.setInitParameter("servletInitParamName", "servletInitParamValue");
        sr.addMapping("/newServlet");

        /*
         * Make sure that if we register a different servlet instance
         * under the same name, null is returned
         */
        if (sc.addServlet("NewServlet", new NewServlet()) != null) {
            throw new RuntimeException(
                "Duplicate servlet name not detected by " +
                "ServletContext#addServlet");
        }

        /*
         * Make sure that if we register the same servlet instance again
         * (under a different name), null is returned
         */
        if (sc.addServlet("AgainServlet", servlet) != null) {
            throw new RuntimeException(
                "Duplicate servlet instance not detected by " +
                "ServletContext#addServlet");
        }

        /*
         * Register filter
         */
        NewFilter filter = new NewFilter();
        filter.setMyParameter("myFilterParamValue");
        FilterRegistration fr = sc.addFilter("NewFilter", filter);
        fr.setInitParameter("filterInitParamName", "filterInitParamValue");
        fr.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST),
                                     true, "NewServlet"); 

        /*
         * Make sure that if we register a different filter instance
         * under the same name, null is returned
         */
        if (sc.addFilter("NewFilter", new NewFilter()) != null) {
            throw new RuntimeException(
                "Duplicate filter name not detected by " +
                "ServletContext#addFilter");
        }

        /*
         * Make sure that if we register the same filter instance again
         * (under a different name), null is returned
         */
        if (sc.addFilter("AgainFilter", filter) != null) {
            throw new RuntimeException(
                "Duplicate filter instance not detected by " +
                "ServletContext#addFilter");
        }
    }
}
