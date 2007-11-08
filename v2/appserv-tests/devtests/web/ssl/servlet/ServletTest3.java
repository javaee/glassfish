package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest3 extends HttpServlet{

    private ServletContext context;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet3.init]");        
        context = config.getServletContext();     
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet3.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet3.doPost]");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("FILTER-REQUEST:" + request.getSession().getAttribute("FILTER-REQUEST"));
        out.println("FILTER-FORWARD:" + request.getSession().getAttribute("FILTER-FORWARD"));
        out.println("FILTER-INCLUDE:" + request.getSession().getAttribute("FILTER"));
    }

}



