/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.ejb30.ear.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RunAs(value="sunuser")
public class Servlet extends HttpServlet {
    @EJB private Sless sless;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<HTML><HEAD><TITLE>Servlet Output</TTILE></HEAD><BODY>");
        if (req.isUserInRole("j2ee")) {
            out.println("in j2ee role:  " + req.isUserInRole("j2ee") + "<br>");
            out.println("in sunuser role:  " + req.isUserInRole("sunuser") + "<br>");
            out.println("Calling sless.hello()=" + sless.hello());
            if (req.getSession(true) != null) {
                out.println(req.getSession(true).getAttribute(
                    "deployment.ejb30.ear.security"));
            } else {
                out.println("<br>No session attr!");
            }
        }
        out.println("</BODY></HTML>");
    }
}
