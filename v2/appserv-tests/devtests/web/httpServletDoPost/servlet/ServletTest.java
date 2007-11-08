package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest extends HttpServlet {

    private ServletContext context;
    
    public void init(ServletConfig config) throws ServletException {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        System.out.println("Invoking super!");
        super.doPost(request,response);
        out.println("httpServletDoPost::FAILED");
        out.flush();
    }

}



