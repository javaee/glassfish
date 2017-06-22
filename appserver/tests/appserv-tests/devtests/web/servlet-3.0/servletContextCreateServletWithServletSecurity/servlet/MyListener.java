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

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

@javax.servlet.annotation.WebListener
public class MyListener implements ServletContextListener {

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("MyListener.contextInitialized");
        try {
            ServletContext sc = sce.getServletContext();

            Class<NewServlet> servletCl = (Class<NewServlet>)Class.forName("NewServlet");
            NewServlet servlet = sc.createServlet(servletCl);
            servlet.setMyParameter("myServletParamValue");
            ServletRegistration.Dynamic sr = sc.addServlet("NewServlet", servlet);
            sr.setInitParameter("servletInitParamName", "servletInitParamValue");
            sr.addMapping("/newServlet");

            HttpConstraintElement constraint = new HttpConstraintElement();
            List<HttpMethodConstraintElement> methodConstraints = new ArrayList<HttpMethodConstraintElement>();
            methodConstraints.add(new HttpMethodConstraintElement("GET"));
            methodConstraints.add(new HttpMethodConstraintElement("POST",
                    new HttpConstraintElement(TransportGuarantee.NONE, new String[] {"javaee"})));
            methodConstraints.add(new HttpMethodConstraintElement("OPTIONS",
                    new HttpConstraintElement(EmptyRoleSemantic.DENY)));
            ServletSecurityElement servletSecurityElement =
                new ServletSecurityElement(constraint, methodConstraints);
            sr.setServletSecurity(servletSecurityElement);


            Class<NewServlet2> servletCl2 = (Class<NewServlet2>)Class.forName("NewServlet2");
            NewServlet2 servlet2 = sc.createServlet(servletCl2);
            servlet2.setMyParameter("myServletParamValue2");
            ServletRegistration.Dynamic sr2 = sc.addServlet("NewServlet2", servlet2);
            sr2.setInitParameter("servletInitParamName", "servletInitParamValue2");
            sr2.addMapping("/newServlet2");


            NewServlet2 servlet2_1 = sc.createServlet(servletCl2);
            servlet2_1.setMyParameter("myServletParamValue2");
            ServletRegistration.Dynamic sr2_1 = sc.addServlet("NewServlet2_1", servlet2_1);
            sr2_1.setInitParameter("servletInitParamName", "servletInitParamValue2");
            HttpConstraintElement constraint2_1 = new HttpConstraintElement(TransportGuarantee.NONE, "javaee");
            List<HttpMethodConstraintElement> methodConstraint2_1 = new ArrayList<HttpMethodConstraintElement>();
            methodConstraint2_1.add(new HttpMethodConstraintElement("GET",
                    new HttpConstraintElement(EmptyRoleSemantic.DENY)));
            methodConstraint2_1.add(new HttpMethodConstraintElement("OPTIONS"));
            ServletSecurityElement servletSecurityElement2_1 =
                new ServletSecurityElement(constraint2_1, methodConstraint2_1);
            sr2_1.setServletSecurity(servletSecurityElement2_1);
            sr2_1.addMapping("/newServlet2_1");

        } catch (Exception e) {
            sce.getServletContext().log("Error during contextInitialized");
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
}
