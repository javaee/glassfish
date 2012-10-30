/*
 * SimpleFilterServlet.java
 *
 * =================================================================================================
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * =================================================================================================
 *
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
