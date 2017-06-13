/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This is a simple servlet that demonstrates some of most commonly used i18n features of
 * the Application Server's Java Servlet API Implementation
 * @author  Chand Basha
 * @version	1.0
 */
public class SimpleI18nServlet extends HttpServlet {

    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

    }

    /** Destroys the servlet.
     */
    public void destroy() {

    }

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *  @param request servlet request
     *  @param response servlet response
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, java.io.IOException {
        try {
			HttpSession session	=	req.getSession(false);
			if(session != null) {
				String sessioncharset = (String)session.getAttribute("charset");
				try {
					req.setCharacterEncoding(sessioncharset);
				} catch (Exception e) {}
			}
			String func		=	req.getParameter("func");
			String action	=	req.getParameter("action");

			if(func.equals("sendInput")) {
				String charsetval	=	req.getParameter("charsetval");
				sendInput (req, res, charsetval, action);
			} else {
				if ( action.equals("formatDate") ) {
					formatDate (req, res);
				} else if ( action.equals("setCharEncoding") ) {
					setCharEncoding (req, res);
				} else if ( action.equals("includeServlet") ) {
					includeServlet (req, res);
				} else if ( action.equals("forwardServlet") ) {
					forwardServlet (req, res);
				} else if ( action.equals("sendErrorMessage") ) {
					sendErrorMessage (req, res);
				} else if ( action.equals("useResourceBundle") ) {
					useResourceBundle (req, res);
				} else if ( action.equals("forwardJsp") ) {
					forwardJsp (req, res);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** Sets the charset attribute to the session and generates an input form
	 *  @param request servlet request
	 *  @param response servlet response
     */
	protected void sendInput (HttpServletRequest req, HttpServletResponse res, String charsetval, String action)
    throws ServletException, IOException {
		try {
			HttpSession session	=	req.getSession(true);
			session.setAttribute("charset", charsetval);
			res.setContentType("text/html; charset=" + charsetval);
			PrintWriter out = res.getWriter();
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Servlet i18n samples</title></head>");
			out.println("<body><br>");
			out.println("<P><FONT FACE=\"Times New Roman\"><FONT SIZE=6><B>Servlet i18n samples</B></FONT></FONT></P>");
			out.println("<form name=\"i18n-simple\" method=\"post\" action=\"/i18n-simple/SimpleI18nServlet\">");
			out.println("<table>");
			out.println("<tr>");
			out.println("<td><H3>Please enter your name:</H3></td>");
			out.println("<td>");
			out.println("<input type=\"text\" name=\"name\" size=\"20\">");
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");
			out.println("<pre>");
			out.println("<input type=submit value=Submit>");
			out.println("</pre>");
			out.println("<input type=hidden name=charsetval value=" + charsetval + ">");
			out.println("<input type=hidden name=action value=" + action + ">");
			out.println("<input type=hidden name=func value=exec>");
			out.println("<br>");
			out.println("<P><BR><A HREF=\"/i18n-simple\">Back to sample home</A></P>");
			out.println("</form>");
			out.println("</body>");
			out.println("</html>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /** Gets the locale information from browser settings and sets the locale to
     *  response object using res.setLocale() method. It then retrieves the locale from response
     *  object using res.getLocale() method and formats today's date
     *  @param request servlet request
     *  @param response servlet response
     */
    protected void formatDate (HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        try {
			String name				=	req.getParameter("name");
			String charsetval		=	req.getParameter("charsetval");

            // Get's the locale of the browser from the request object
            Locale browserLocale	=	req.getLocale();

            // Default locale of the response object
            Locale resLocale		=	res.getLocale();
            String resCharset		=	res.getCharacterEncoding();

            // Set's the browser's locale to the response object
            res.setLocale(browserLocale);

            Locale resLocaleAfter	=	res.getLocale();
            String resCharsetAfter	=	res.getCharacterEncoding();

            res.setContentType("text/html; charset=" + resCharsetAfter);
            // Output stream for the response
            java.io.PrintWriter out	=	res.getWriter();

            out.println("<html>");
            out.println("<head><title>Formatting date as per browser's locale</title></head>");
            out.println("<body>");
            out.println("<H4>Welcome " + name + "</H4>");
            out.println("Your Browser's preferred locale : " + browserLocale + "<br>");
            out.println("Response Locale : " + resLocale + "<br>");
            out.println("Response charset : " + resCharset + "<br>");

            out.println("Response Locale after setting it from the request locale: " + resLocaleAfter + "<br>");
            out.println("Response charset after setting it from request locale: " + resCharsetAfter + "<br>");

            // Get today's date and formats it using the browser locale
            java.util.Date today	=	new java.util.Date();
            DateFormat formatter	=	DateFormat.getDateInstance(DateFormat.FULL, browserLocale);
            String formattedDate	=	formatter.format(today);

            out.println("Today's date for " + resLocaleAfter + " is : " + formattedDate + "<br>");
            out.println("<br>");
			out.println("<P><BR><A HREF=\"/i18n-simple\">Back to sample home</A></P>");
            out.println("</body></html>");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    /** Sets request's character encoding using setCharacterEncoding method of HttpServletRequest interface.
     *  The setCharacterEncoding method is used to inform the servlet container to read request parameters using specified encoding.
     *  @param request servlet request
     *  @param response servlet response
     */
    protected void setCharEncoding (HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        try {
			String name			=	req.getParameter("name");
			String charsetval	=	req.getParameter("charsetval");
			res.setContentType("text/html; charset=" + charsetval);

			// Output stream for the response
			PrintWriter out		=	res.getWriter();

			out.println("<html>");
			out.println("<head><title>Setting request object's character encoding to " + charsetval + "</title></head>");
			out.println("<body>");
			out.println("<br><H3> The name entered was : " + name + " and the associated encoding was : " + charsetval + "</H3><br>");
			out.println("<br>");
			out.println("<P><BR><A HREF=\"/i18n-simple\">Back to sample home</A></P>");
			out.println("</body></html>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /** Includes a servlet using RequestDispatcher's include method. Here the data display behaviour
     *  changes based on the content type set. If the included servlet sets the content type to a different value then the new content type
     *  should be set for the output stream
     *  @param request servlet request
     *  @param response servlet response
     */
    protected void includeServlet (HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        try {
			String name = req.getParameter("name");
			String charsetval = req.getParameter("charsetval");
			res.setContentType("text/html; charset=" + charsetval);

			// Output stream for the response
			PrintWriter out = res.getWriter();

			out.println("<html>");
			out.println("<head><title>Including another servlet from a servlet</title></head>");
			out.println("<body>");

			RequestDispatcher dispatcher;
			dispatcher = getServletContext().getRequestDispatcher("/IncludedServlet");
			dispatcher.include(req, res);

			out.println("<br>Name from the including servlet: " + name + "<br>");
			out.println("<br>");
			out.println("<P><BR><A HREF=\"/i18n-simple\">Back to sample home</A></P>");
			out.println("</body></html>");
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /** Forwards a servlet using RequestDispatcher's forward method. Here the data display behaviour
     *  changes based on the content type set. If the forwarded servlet sets the content type to a different value then the new content type
     *  should be set for the output stream
     *  @param request servlet request
     *  @param response servlet response
     */
    protected void forwardServlet (HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

        try {
			String name			= req.getParameter("name");
			String charsetval	= req.getParameter("charsetval");
			res.setContentType("text/html; charset=" + charsetval);

			// Output stream for the response
			PrintWriter out		= res.getWriter();

			out.println("<html>");
			out.println("<head><title>Including another servlet from a servlet</title></head>");
			out.println("<body>");
			out.println("<br>The name from forwarding servlet : " + name );

			RequestDispatcher dispatcher;
			dispatcher = getServletContext().getRequestDispatcher("/ForwardedServlet");
			dispatcher.forward(req, res);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /** Send error messages using response's sendError method. The sendError method sends multi byte error messages correctly
     *  based on the content type set to appropriate encoding.
     *  @param request servlet request
     *  @param response servlet response
     */
    protected void sendErrorMessage (HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        try {
			String name			=	req.getParameter("name");
			String charsetval	=	req.getParameter("charsetval");
			res.setContentType("text/html; charset=" + charsetval);
			res.sendError(404, "I18n error message from servlet "+ name);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	/** Load resource bundle based on client's locale. If there is no resource bundle available, the default resource bundle
     *  will be picked up
     *  @param request servlet request
     *  @param response servlet response
     */
    protected void useResourceBundle (HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        try {
			String name			=	req.getParameter("name");
			String charsetval	=	req.getParameter("charsetval");
			res.setContentType("text/html; charset=" + charsetval);
			ResourceBundle rb	=	null;
			rb					=	ResourceBundle.getBundle("LocalStrings",req.getLocale());
			String msg;
			PrintWriter out		=	res.getWriter();

			out.println("<html>");
			out.println("<head>");

			try {
				msg				=	rb.getString ("title");
				out.println("<title>" + msg + "</title>");
				out.println("</head>");
				out.println("<body>");
				out.println("<br><H1> Hello " + name + ", the following messages are displayed from a resource bundle</H1>");
				msg = rb.getString ("msg");
				out.println("<br><br><H2>" + msg + "</H2>");
				msg = rb.getString ("thanks");
				out.println("<br><br><H3>" + msg + "</H3>");

			} catch (MissingResourceException e) {
				e.printStackTrace();
				} catch (Exception e) {
				e.printStackTrace();
        	}
			out.println("<br>");
			out.println("<P><BR><A HREF=\"/i18n-simple\">Back to sample home</A></P>");
        	out.println("</body></html>");
        	out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	/** Forwards request from servlet to jsp using RequestDispatcher's forward method. Here the data display behaviour
     *  changes based on the content type set. If the forwarded jsp sets the content type to a different value then the new content type
     *  should be set for the output stream
     *  @param request servlet request
     *  @param response servlet response
     */
    protected void forwardJsp (HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        try {
			RequestDispatcher dispatcher;
			dispatcher = getServletContext().getRequestDispatcher("/ForwardedJsp.jsp");
      		dispatcher.forward(req, res);
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

    /** Simple servlet to demonstrate i18n capabilities of the Application Server
     */
    public String getServletInfo() {
        return "This is a simple servlet to demonstrate i18n capabilities of the Application Server";
    }
}
