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

public class ServletTest extends HttpServlet{

    private ServletContext context;
    private static String status = "ApplicationDispatcher::PASS";
   
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");

        try{
            RequestDispatcher requestD = 
                            getServletContext().getRequestDispatcher("/test.jsp");

            if ( requestD == null){
                status = "ApplicationDispatcher::FAIL";          
            }
            System.out.println("[Servlet.RequestDispatcher: " + requestD + "]");
        } catch (Throwable t){
                status = "ApplicationDispatcher::FAIL";          
        }
        System.out.println("status: " + status); 
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.doPost]");

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.println(status);
    }

}
