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

/* $Id: SessionExample.java,v 1.2 2005/12/08 01:13:36 kchung Exp $
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

public class SessionExample extends HttpServlet {

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

        String title = rb.getString("sessions.title");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body>");

        // img stuff not req'd for source code html showing
	// relative links everywhere!

        // XXX
        // making these absolute till we work out the
        // addition of a PathInfo issue 
	
        out.println("<a href=\"../sessions.html\">");
        out.println("<img src=\"../images/code.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"view code\"></a>");
        out.println("<a href=\"../index.html\">");
        out.println("<img src=\"../images/return.gif\" height=24 " +
                    "width=24 align=right border=0 alt=\"return\"></a>");

        out.println("<h3>" + title + "</h3>");

        HttpSession session = request.getSession(true);
        out.println(rb.getString("sessions.id") + " " + session.getId());
        out.println("<br>");
        out.println(rb.getString("sessions.created") + " ");
        out.println(new Date(session.getCreationTime()) + "<br>");
        out.println(rb.getString("sessions.lastaccessed") + " ");
        out.println(new Date(session.getLastAccessedTime()));

        String dataName = request.getParameter("dataname");
        String dataValue = request.getParameter("datavalue");
        if (dataName != null && dataValue != null) {
            session.setAttribute(dataName, dataValue);
        }

        out.println("<P>");
        out.println(rb.getString("sessions.data") + "<br>");
        Enumeration names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement(); 
            String value = session.getAttribute(name).toString();
            out.println(HTMLFilter.filter(name) + " = " 
                        + HTMLFilter.filter(value) + "<br>");
        }

        out.println("<P>");
        out.print("<form action=\"");
	out.print(response.encodeURL("SessionExample"));
        out.print("\" ");
        out.println("method=POST>");
        out.println(rb.getString("sessions.dataname"));
        out.println("<input type=text size=20 name=dataname>");
        out.println("<br>");
        out.println(rb.getString("sessions.datavalue"));
        out.println("<input type=text size=20 name=datavalue>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");

        out.println("<P>GET based form:<br>");
        out.print("<form action=\"");
	out.print(response.encodeURL("SessionExample"));
        out.print("\" ");
        out.println("method=GET>");
        out.println(rb.getString("sessions.dataname"));
        out.println("<input type=text size=20 name=dataname>");
        out.println("<br>");
        out.println(rb.getString("sessions.datavalue"));
        out.println("<input type=text size=20 name=datavalue>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");

        out.print("<p><a href=\"");
	out.print(response.encodeURL("SessionExample?dataname=foo&datavalue=bar"));
	out.println("\" >URL encoded </a>");
	
        out.println("</body>");
        out.println("</html>");
        
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
