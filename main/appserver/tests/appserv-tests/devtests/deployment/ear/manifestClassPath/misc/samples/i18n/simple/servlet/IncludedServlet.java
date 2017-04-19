/*
 * IncludedServlet.java
 *
 * =================================================================================================
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * =================================================================================================
 *
 */

package samples.i18n.simple.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Servlet included in SimpleI18nServlet
 * @author  Chand Basha
 * @version	1.0
 */
public class IncludedServlet extends HttpServlet {

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
     * Generates response with the information obtained from the including servlet.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, java.io.IOException {
        try {
			java.io.PrintWriter out	=	res.getWriter();
			String name				=	req.getParameter("name");

			out.println("<H3> This is the name from included servlet </H3>");
			out.println("<H4> The name entered was:" + name + "</h4>");

		} catch (Exception e) {
			e.printStackTrace();
		}
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

    /** Servlet to display content from the including servlet.
     */
    public String getServletInfo() {
        return "Servlet to display content from the including servlet";
    }
}
