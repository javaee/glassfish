package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest extends HttpServlet {
    private static boolean isRedirected = false;
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

        System.out.println("requestUri: " + request.getRequestURI());
       
        if (!isRedirected){
            String url = request.getParameter("url") + "?TEST=PASS";
            System.out.println("[URL] " + url);
            response.sendRedirect(url);
            isRedirected = true;
            out.println("TEST:FAIL");
            out.flush();
            return;
        }
       
        out.println("TEST:" + request.getParameter("TEST"));
        out.flush();
    }

}



