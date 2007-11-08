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
        context = config.getServletContext();     
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        System.out.println("queryString: " + request.getQueryString());
        out.println("FILTER-QUERYSTRING:" +
                (request.getQueryString() != null ? "PASS" : "FAIL"));
    }

}



