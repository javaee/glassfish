/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package versionedservlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleVersionedServlet extends HttpServlet
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
            // retrieve the version information
            Properties prop = new Properties();
            InputStream in = this.getClass().getResource("version-infos.properties").openStream();
            prop.load(in);
            in.close();

            // print the version information
            PrintWriter out = response.getWriter();
            response.setContentType("text/html");
            out.println(prop.getProperty("version.identifier", ""));
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
