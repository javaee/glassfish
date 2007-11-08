/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.ejb30.war.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@Resource(name="myDataSource", type=DataSource.class, mappedName="jdbc/__default")
public class Servlet extends HttpServlet {
    ThreadLocal pconstruct = new ThreadLocal();

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<HTML><HEAD><TITLE>Servlet Output</TTILE></HEAD><BODY>");
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource)ic.lookup("java:comp/env/myDataSource");
            int loginTimeout = ds.getLoginTimeout();
            out.println("ds login timeout = " + loginTimeout);
        } catch(Exception ex) {
            out.println("myDataSource Exception: " + ex);
        }
        if (req.isUserInRole("j2ee") && !req.isUserInRole("guest")) {
            out.println("Hello World");
            if (req.getSession(false) != null &&
                    Boolean.TRUE.equals(pconstruct.get())) { 
                out.println(req.getSession(false).getAttribute(
                    "deployment.ejb30.war.servlet"));
            }
        }
        out.println("in role j2ee = " + req.isUserInRole("j2ee"));
        out.println("in role guest = " + req.isUserInRole("guest"));
        out.println("</BODY></HTML>");
    }

    @PostConstruct
    private void afterAP() {
        pconstruct.set(Boolean.TRUE);
    }
}
