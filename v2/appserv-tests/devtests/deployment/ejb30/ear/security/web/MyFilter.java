/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.ejb30.ear.security;

import java.io.IOException;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

@DeclareRoles({ "j2ee", "nobody", "sunuser" })
public class MyFilter implements Filter {

    private @Resource(mappedName="jdbc/__default") DataSource ds1;

    public void init(FilterConfig filterConfig) throws ServletException {

        ServletContext sc = filterConfig.getServletContext();
    
        try {

            int loginTimeout = ds1.getLoginTimeout();
            sc.log("ds1-login-timeout=" + loginTimeout);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doFilter (ServletRequest request,
                          ServletResponse response,
                          FilterChain chain)
            throws IOException, ServletException {
        int loginTimeout = 0;
        try {
            loginTimeout = ds1.getLoginTimeout();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new IOException(ex.toString());
        }
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpSession httpSession = httpRequest.getSession(true);
        if (httpRequest.isUserInRole("j2ee")) {
            httpSession.putValue("deployment.ejb30.ear.security",
                "filterMessage=hello world: " + loginTimeout);

        }
        chain.doFilter(request, response);
    }

    public void destroy() {
        // do nothing
    }
}
