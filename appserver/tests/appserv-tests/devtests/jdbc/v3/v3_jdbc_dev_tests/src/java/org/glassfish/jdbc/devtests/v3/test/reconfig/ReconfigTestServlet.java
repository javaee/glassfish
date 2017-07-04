/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.jdbc.devtests.v3.test.reconfig;

import java.io.*;
import java.net.*;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

/**
 *
 * @author shalini
 */
public class ReconfigTestServlet extends HttpServlet {
    @Resource(name = "jdbc/jdbc-dev-test-resource", mappedName = "jdbc/jdbc-dev-test-resource")
    DataSource ds;
    
    @Resource(name = "jdbc/jdbc-reconfig-test-resource-1", mappedName = "jdbc/jdbc-reconfig-test-resource-1")
    DataSource dsReconfig1;

    @Resource(name = "jdbc/jdbc-reconfig-test-resource-2", mappedName = "jdbc/jdbc-reconfig-test-resource-2")
    DataSource dsReconfig2;

    @Resource(name = "jdbc/res1", mappedName = "jdbc/res1")
    DataSource dsRes1;
    
    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        processRequest(arg0, arg1);
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ReconfigTestUtil reconfigTest = new ReconfigTestUtil();
        StringBuffer buf = new StringBuffer();
        Map<String, Boolean> mapReconfig = null;
        //Determine the test to be executed
        int testId = Integer.parseInt(request.getParameter("testId").trim());
        boolean throwException = Boolean.parseBoolean(request.getParameter("throwException").trim());
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>ReconfigTestServlet</title>");
            out.println("</head>");
            out.println("<body>");

            switch (testId) {
                case 1: //Attribute change test

                    int maxPoolSize = Integer.parseInt(request.getParameter("maxPoolSize").trim());
                    out.println("<h1>Reconfig Pool Attribute Test </h1>");
                    mapReconfig = reconfigTest.poolAttributeChangeTest(ds, out, maxPoolSize, throwException);
                    break;
                case 2: //Property change test

                    out.println("<h1>Reconfig Pool Property Test </h1>");
                    mapReconfig = reconfigTest.poolPropertyChangeTest(ds, out, throwException);
                    break;
                case 3: //Resource attribute change test

                    out.println("<h1>Reconfig Resource Attribute Test with DS : dsReconfig2</h1>");
                    mapReconfig = reconfigTest.resourceAttributeChangeTest(dsReconfig2, out, throwException);
                    break;
                case 4: //Resource attribute change test with another datasource
                    
                    out.println("<h1>Reconfig Resource Attribute Test with DS : dsRes1</h1>");
                    mapReconfig = reconfigTest.resourceAttributeChangeTest(dsRes1, out, throwException);
                    break;
            }
            buf.append("<table border=1><tr><th>Test Name</th><th> Pass </th></tr>");
            for (Map.Entry entry : mapReconfig.entrySet()) {
                buf.append("<tr> <td>");
                buf.append(entry.getKey());
                buf.append("</td>");
                buf.append("<td>");
                buf.append(entry.getValue());
                buf.append("</td></tr>");
            }
            buf.append("</table>");
            out.println(buf.toString());

            out.println("</body>");
            out.println("</html>");

        } finally {
            out.close();
            out.flush();
        }
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Tests the reconfiguration changes to JDBC Connection Pool and " +
                "JDBC Resource";
    }

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

}
