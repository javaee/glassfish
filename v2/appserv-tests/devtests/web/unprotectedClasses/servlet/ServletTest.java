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

public class ServletTest extends HttpServlet {

    private ServletContext context;
    private static String status = "EXPIRED:FAIL";

   
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

        PrintWriter out = response.getWriter();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try{
            loader.loadClass("org.apache.catalina.realm.RealmBase");
            out.println("catalinaClasses::FAIL");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            out.println("catalinaClasses::PASS");
        }

        Class clazz;
        try{
            clazz = loader.loadClass("com.sun.web.security.RealmAdapter");
            out.println("appservClasses::FAIL");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            out.println("appservClasses::PASS");
        }
    }

}
