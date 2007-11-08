package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest2 extends HttpServlet implements HttpSessionListener {

    private ServletContext context;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet2.init]");        
        context = config.getServletContext();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet2.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet2.doPost]");
      
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        request.getSession().setAttribute("FILTER-FORWARD",request.getSession().getAttribute("FILTER"));
        request.getSession().setAttribute("FILTER", "FAIL");        
        
        RequestDispatcher rd = request.getRequestDispatcher("/ServletTest3");
        rd.include(request, response);     
    }
 
    public void sessionCreated(javax.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionCreated]");
    }
    
    public void sessionDestroyed(javax.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionDestroyed]");
        System.out.println("Attributes: " + httpSessionEvent.getSession().getAttribute("test"));
    }

}


