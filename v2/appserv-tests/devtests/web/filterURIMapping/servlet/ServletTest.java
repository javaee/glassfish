package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest extends HttpServlet {

    private ServletContext context;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");        
        context = config.getServletContext();
        System.out.println("[Servlet.init] " + context.getMajorVersion());
        
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.doPost]");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        Object obj = request.getSession().getAttribute("FILTER");
        System.out.println(obj);
        if ((obj != null) && (obj.equals("PASS"))) {
            out.println("Filter invoked");
        }
        	
    }

}



