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

package mypackage;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1peqe.transaction.txstress.ejb.beanA.*;



/**
 * Simple servlet to validate that the Hello, World example can
 * execute servlets.  In the web application deployment descriptor,
 * this servlet must be mapped to correspond to the link in the
 * "index.html" file.
 *
 * @author Craig R. McClanahan <Craig.McClanahan@eng.sun.com>
 */

public final class Hello extends HttpServlet {


    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are producing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
      throws IOException, ServletException {
        TxRemoteHomeA home = null;
        Class homeClass = TxRemoteHomeA.class;
        int identity = 100;
        try {
            // Initialize the Context
            Context context = new InitialContext();
            System.out.println("Context Initialized...");

            // Create Home object
            java.lang.Object obj = context.lookup("java:comp/env/ejb/TxBeanA");
            home = (TxRemoteHomeA) PortableRemoteObject.narrow(obj, homeClass);
            System.out.println("Home Object Initialized...");
            TxRemoteA beanA = home.create();
            if(request.getParameter("input") != null && !(request.getParameter("input").equals("")))
                identity = Integer.parseInt(request.getParameter("input"));
            System.out.println("Before Calling txCommit on BeanA");
            beanA.txCommit(identity);
            System.out.println("Before Calling remove on BeanA");
            beanA.remove();

        } catch (Throwable ex) {
            // Thread.dumpStack();
            //if (ex != null)
            //   System.out.println("Exception is not null");
            //System.out.println(" Exception is  " + ex.getClass());
            //System.out.println("Exception in setup: " + ex.toString());
            //ex.printStackTrace();
        }


	response.setContentType("text/html");
	PrintWriter writer = response.getWriter();

	writer.println("<html>");
	writer.println("<head>");
	writer.println("<title>Sample Transaction Stress Test Application</title>");
	writer.println("</head>");
	writer.println("<body bgcolor=white>");

	writer.println("<table border=\"0\">");
	writer.println("<tr>");
	writer.println("<td>");
	//writer.println("<img src=\"images/tomcat.gif\">");
	writer.println("</td>");
	writer.println("<td>");
	writer.println("<h1>Sample Transaction Stress Test Application</h1>");
	writer.println("This is the output of a servlet that is part of");
	writer.println("the Hello, World application.  It displays the");
	writer.println("Number that you input.");
	writer.println("</td>");
	writer.println("</tr>");
	writer.println("</table>");

	writer.println("<table border=\"0\" width=\"100%\">");
	/*Enumeration names = request.getHeaderNames();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    writer.println("<tr>");
	    writer.println("  <th align=\"right\">" + name + ":</th>");
	    writer.println("  <td>" + request.getHeader(name) + "</td>");
	    writer.println("</tr>");
	}*/
	    writer.println("<tr>");
	    writer.println("  <td>" + "Value supplied as Input param is " + identity+"</td>");
	    writer.println("</tr>");
	writer.println("</table>");

	writer.println("</body>");
	writer.println("</html>");

    }


}

