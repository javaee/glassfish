package com.sun.s1asdev.security.simpleMultiRoleMapping.web;

import com.sun.s1asdev.security.simpleMultiRoleMapping.ejb.MessageLocal;
import java.io.*;
import java.net.*;
import javax.ejb.EJB;

import javax.servlet.*;
import javax.servlet.http.*;

public class EjbTest extends HttpServlet {

    @EJB
    private MessageLocal messageBean;
    
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet WebTest</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>" + messageBean.getMessage() + "</h2>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
    
}
