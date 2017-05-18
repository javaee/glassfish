/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jacc.test.uncoveredmethods;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class BaseServlet extends HttpServlet {

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<HTML> <HEAD> <TITLE> Servlet Output </TITLE> </HEAD> <BODY>");
            out.println("Uncovered HTTP Methods Servlet<br>");
            out.println("<table border=\"2\"><caption>HTTP Request Values</caption>");
            out.println("<thead><tr><th>HTTP</th><th>Value</th></tr></thead><tbody>");
            out.println("<tr><td>URL</td><td>" + request.getRequestURL() + "</td>/<tr>");
            out.println("<tr><td>Method</td><td>" + request.getMethod() + "</td>/<tr>");
            out.println("<tr><td>Servlet</td><td>" + request.getServletPath() + "</td>/<tr>");
            out.println("<tr><td>Context</td><td>" + request.getContextPath() + "</td>/<tr>");
            out.println("<tr><td>Secure</td><td>" + (request.isSecure() ? "true" : "false") + "</td>/<tr>");
            out.println("<tr><td>UserPrincipal</td><td>"
                    + (request.getUserPrincipal() == null ? "null" : request.getUserPrincipal().getName()) + "</td>/<tr>");
            out.println("<tr><td>AuthType</td><td>" + request.getAuthType() + "</td>/<tr>");
            out.println("</tbody></table>");
            out.println("</BODY> </HTML>");
        } catch (Throwable t) {
            out.println("Something went wrong: " + t);
        } finally {
            out.close();
        }
    }

    public String getServletInfo() {
        return "Base Servlet implementation class of Test Servlet";
    }
}
