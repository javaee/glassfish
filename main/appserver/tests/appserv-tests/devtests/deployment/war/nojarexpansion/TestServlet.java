/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package webinflib;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dummypkg.DummyLib;

public class TestServlet extends HttpServlet
{
    public void
    init () throws ServletException
    {
        super.init();
    }

    public void
    service (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        try {
			DummyLib d = new DummyLib();
            PrintWriter out = response.getWriter();
            response.setContentType("text/html");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>TestServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>");
            out.println("Dummy Lib returned a String");
            out.println("</p>");
            out.println("Value is [" + d.getDummyString() + "]");
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
}
