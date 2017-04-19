/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package servletonly;

import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet
{
    public void
    init () throws ServletException
    {
        super.init();
        log("init()...");
    }

    public void
    service (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        log("service()...");
        try {
            Context ic = new InitialContext();

            //test: looking up the env-entry
            String name = (String) ic.lookup("java:comp/env/name");
            Integer value = (Integer) ic.lookup("java:comp/env/value");
            log("[" + name + "] = [" + value + "]");

            /*
             * The following line will compile correctly only when the ant
             * devtest is running.  The ChangeableClass class is generated
             * by the ant task (two different ways at two different points
             * in the processing).  
             */
            final String changeableValue = ChangeableClass.changeableValue();
            PrintWriter out = response.getWriter();
            response.setContentType("text/html");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>TestServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>");
            out.println("So what is your lucky number?");
            out.println("</p>");
            out.println("Mine is [" + value + "]");
            out.println("<p>");
            out.println("changeableValue=" + changeableValue);
            out.println("</body>");
            out.println("</html>");
            out.flush();
            out.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            ServletException se = new ServletException();
            se.initCause(ex);
            throw se;
        }
    }

    public void log (String message) {
       System.out.println("[war.servletonly.TestServlet]:: " + message);
    }
}
