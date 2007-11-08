package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest extends HttpServlet implements HttpSessionListener {

    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");        
        
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    private static boolean sessionCreated = false;


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.doPost]");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (!sessionCreated){
            System.out.println("Create session: " + request.getSession(true).getId());
            sessionCreated = true;
        }
        

        out.println("getRequestSessionId::" + request.getRequestedSessionId());
        System.out.println("getRequestSessionId::" + request.getRequestedSessionId());
        if (request.getSession(false) != null){
            out.println("getSession(false).getId::" + request.getSession(false).getId());
            System.out.println("getSession(false).getId()::" + request.getSession(false).getId());
        }
        out.println("getRequestURI::" + request.getRequestURI());
        System.out.println("getRequestURI::" + request.getRequestURI());
        
    }

    public void sessionCreated(javax.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionCreated]");
    }
    
    public void sessionDestroyed(javax.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionDestroyed]");
    }
}



