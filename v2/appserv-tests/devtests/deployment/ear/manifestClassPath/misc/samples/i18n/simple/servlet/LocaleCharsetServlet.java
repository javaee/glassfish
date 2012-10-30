/*

 * LocaleCharsetServlet.java

 *

 * =================================================================================================

 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.

 * =================================================================================================

 *

 */



package samples.i18n.simple.servlet;



import javax.servlet.*;

import javax.servlet.http.*;

import java.util.*;

import java.io.*;

import java.net.*;



/**

 * Servlet used to verify the locale-charset table mapping implementation in the Application Server

 * @author  Chand Basha

 * @version	1.0

 */

public class LocaleCharsetServlet extends HttpServlet {



    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.

     * @param request servlet request

     * @param response servlet response

     */

    protected void processRequest(HttpServletRequest req, HttpServletResponse res)

    throws ServletException, java.io.IOException {



        String name			=	req.getParameter("name");

        String charsetval	=	req.getCharacterEncoding();

        if (charsetval != null) {

        	res.setContentType("text/html;charset=" + charsetval + "");

		} else res.setContentType("text/html;charset=UTF-8");

        PrintWriter out = res.getWriter();

        out.println("<html>");

        out.println("<head>");

        out.println("<title>Sample servlet to verify the locale-charset mapping table</title>");

        out.println("</head>");

        out.println("<body><br>");

        out.println("<H3> The name you have entered: " + name + "</H3>");

        out.println("<br>");

        out.println("<H4> Charset set by the server: " + charsetval + "</H4>");

        out.println("<br>");

		out.println("<P><BR><A HREF=\"/i18n-simple\">Back to sample home</A></P>");

		out.println("</body></html>");

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



    /** Servlet used to verify the locale-charset table mapping implementation in the Application Server

     */

    public String getServletInfo() {

        return "Servlet used to verify the locale-charset table mapping implementation in the Application Server";

    }



}

