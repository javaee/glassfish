/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
