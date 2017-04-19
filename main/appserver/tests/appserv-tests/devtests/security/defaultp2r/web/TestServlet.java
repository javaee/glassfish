/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.defaultp2r;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;

public class TestServlet extends HttpServlet {
    
    public void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        
        String method = req.getMethod();
        if (method.equals("FOO")) {
            doFoo(req, resp);
        } else {
            super.service(req, resp);
        }
    }
    
    public void doFoo(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        
        PrintWriter out = resp.getWriter();
        out.println("doFoo with " + req.getUserPrincipal());
        out.close();
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        
        PrintWriter out = resp.getWriter();
        out.println("doGet with " + req.getUserPrincipal());
        out.close();
    }
}
