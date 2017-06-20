/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package samples.i18n.simple.servlet;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * A Simple Servlet to test the filter SimpleFilter
 */
public class SimpleFilterServlet extends HttpServlet {

    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

    }

    /** Destroys the servlet.
     */
    public void destroy() {

    }

    /**Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * Generates response with the information obtained from the forwarding servlet.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, java.io.IOException {
        String charsetval = req.getCharacterEncoding();
        if (charsetval != null) {
        	res.setContentType("text/html;charset=" + charsetval + "");
		} else res.setContentType("text/html;charset=UTF-8");
        java.io.PrintWriter out		=	res.getWriter();
        String name					=	req.getParameter("name");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Simple servlet to verify the filter</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<H3> This is the name you have entered " + name + "</H3>");
        out.println("<H4> The character encoding set by the filter is: " + charsetval + "</h4>");
		out.println("<br>");
		out.println("<P><BR><A HREF=\"/i18n-simple\">Back to sample home</A></P>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /** Servlet to verify that the filter SimpleFilter is working on this servlet request
     */
    public String getServletInfo() {
        return "Servlet to verify that the filter SimpleFilter is working on this servlet request";
    }
}
