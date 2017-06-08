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

package justweb;

import justbean.JustBean;
import justbean.JustBeanHome;
import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JustWebServlet extends HttpServlet
{
  public void
  init ()
    throws ServletException
  {
    super.init();
    System.out.println("JustWebServlet : init()");
  }

  public void
  service (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    System.out.println("JustWebServlet : service()");

    JustBean bean = null;
    try {
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/JustBean");
        JustBeanHome home = (JustBeanHome)
            PortableRemoteObject.narrow(o, JustBeanHome.class);
        bean = home.create();
    }
    catch (Exception ex) {
        ex.printStackTrace();
    }        

    System.out.println("JustWebServlet.service()... JustBean created.");
    System.out.println("USERNAME = " + getInitParameter("USERNAME"));
    System.out.println("PASSWORD = " + getInitParameter("PASSWORD"));

    String[] marbles = bean.findAllMarbles();
    for (int i = 0; i < marbles.length; i++) {
        System.out.println(marbles[i]);
    }

    sendResponse(request, response);
  }

  private void
  sendResponse (HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    PrintWriter out = response.getWriter();
    response.setContentType("text/html");

    out.println("<html>");
    out.println("<head>");
    out.println("<title>Just Web Test</title>");
    out.println("</head>");
    out.println("<body>");
    out.println("<p>");
    out.println("Check log information on the server side.");
    out.println("<br>");
    out.println("Isn't this a wonderful life?");
    out.println("</p>");
    out.println("</body>");
    out.println("</html>");
  }
}
