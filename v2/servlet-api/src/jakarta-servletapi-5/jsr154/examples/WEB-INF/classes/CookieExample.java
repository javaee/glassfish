/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

/* $Id: CookieExample.java,v 1.2 2005/12/08 01:13:35 kchung Exp $
 *
 */

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import util.HTMLFilter;

/**
 * Example servlet showing request headers
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 */

public class CookieExample extends HttpServlet {

    ResourceBundle rb = ResourceBundle.getBundle("LocalStrings");
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body bgcolor=\"white\">");
        out.println("<head>");

        String title = rb.getString("cookies.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body>");

	// relative links

        // XXX
        // making these absolute till we work out the
        // addition of a PathInfo issue 
	
        out.println("<a href=\"../cookies.html\">");
        out.println("<img src=\"../images/code.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"../index.html\">");
        out.println("<img src=\"../images/return.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"return\"></a>");

        out.println("<h3>" + title + "</h3>");

        Cookie[] cookies = request.getCookies();
        if ((cookies != null) && (cookies.length > 0)) {
            out.println(rb.getString("cookies.cookies") + "<br>");
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                out.print("Cookie Name: " + HTMLFilter.filter(cookie.getName())
                          + "<br>");
                out.println("  Cookie Value: " 
                            + HTMLFilter.filter(cookie.getValue())
                            + "<br><br>");
            }
        } else {
            out.println(rb.getString("cookies.no-cookies"));
        }

        String cookieName = request.getParameter("cookiename");
        String cookieValue = request.getParameter("cookievalue");
        if (cookieName != null && cookieValue != null) {
            Cookie cookie = new Cookie(cookieName, cookieValue);
            response.addCookie(cookie);
            out.println("<P>");
            out.println(rb.getString("cookies.set") + "<br>");
            out.print(rb.getString("cookies.name") + "  " 
                      + HTMLFilter.filter(cookieName) + "<br>");
            out.print(rb.getString("cookies.value") + "  " 
                      + HTMLFilter.filter(cookieValue));
        }
        
        out.println("<P>");
        out.println(rb.getString("cookies.make-cookie") + "<br>");
        out.print("<form action=\"");
        out.println("CookieExample\" method=POST>");
        out.print(rb.getString("cookies.name") + "  ");
        out.println("<input type=text length=20 name=cookiename><br>");
        out.print(rb.getString("cookies.value") + "  ");
        out.println("<input type=text length=20 name=cookievalue><br>");
        out.println("<input type=submit></form>");
            
            
        out.println("</body>");
        out.println("</html>");
    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        doGet(request, response);
    }

}


