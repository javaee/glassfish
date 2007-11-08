package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class ServletTest extends HttpServlet implements HttpSessionListener {

    private ServletContext context;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.doPost]");
    }


    public void sessionCreated(javax.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionCreated]");
    }
    
    public void sessionDestroyed(javax.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionDestroyed]");
    }
}



