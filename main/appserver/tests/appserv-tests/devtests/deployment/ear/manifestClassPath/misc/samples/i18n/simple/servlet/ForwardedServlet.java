/*
 * ForwardedServlet.java
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
 * Servlet forwarded from SimpleI18nServlet
 * @author  Chand Basha
 * @version	1.0
 */
public class ForwardedServlet extends HttpServlet {

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
        String charsetval			=	req.getParameter("charsetval");
        req.setCharacterEncoding(charsetval);
        res.setContentType("text/html;charset=" + charsetval + "");
        java.io.PrintWriter out		=	res.getWriter();
        String name					=	req.getParameter("name");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<H3> This is the name from forwarded servlet </H3>");
        out.println("<H4> The name entered was:" + name + "</h4>");
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

    /** Servlet to display content from the forwarding servlet
     */
    public String getServletInfo() {
        return "Servlet to display content from the forwarding servlet";
    }
}
