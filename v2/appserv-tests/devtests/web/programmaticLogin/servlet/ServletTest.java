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

import com.sun.appserv.security.ProgrammaticLogin;

public class ServletTest extends HttpServlet {

    private ServletContext context;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.Programmatin Login Test]");

		String user = request.getParameter("user");
		String password = request.getParameter("password"); 
        System.out.println("[user] " + user + " [password] " + password);

        ProgrammaticLogin login = new ProgrammaticLogin();
        boolean value = ((Boolean)login.login(user, password, request, response)).booleanValue();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

		if (value){
			out.println("WEB-Programmatic-Login:" + "PASS");
		} else {
			out.println("WEB-Programmatic-Login:" + "FAIL");
		}

    }
}
