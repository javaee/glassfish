package test;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.sun.appserv.security.ProgrammaticLogin;

public class ServletTest extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {
        String user = request.getParameter("user");
        String password = request.getParameter("password"); 
        System.out.println("[user] " + user + " [password] " + password);
        ProgrammaticLogin login = new ProgrammaticLogin();
        if (!login.login(user, password, request, response).booleanValue()) {
            throw new ServletException("Login failed");
        }
    }
}
