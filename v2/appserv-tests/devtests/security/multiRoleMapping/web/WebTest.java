package com.sun.s1asdev.security.multiRoleMapping.web;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class WebTest extends HttpServlet {
    
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // check roles 1 through 7
        boolean found = false;
        for (int i=0; i<8; i++) {
            if (request.isUserInRole("role" + i)) {
                found = true;
                out.println("Hello role" + i);
            }
        }
        if (!found) {
            out.println("User '" + request.getRemoteUser() +
                "' is not in expected role. Something's messed up.");
        }
        out.close();
    }
    
}
