package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ServletTest3 extends HttpServlet {

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("================ In Servlet3 ===============");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String host = request.getParameter("host");
        String port = request.getParameter("port");
        String contextRoot = request.getParameter("contextRoot");

        URL url = new URL("http://" + host  + ":" + port + contextRoot + "/ServletTest2?host="+ host + "&port=" + port + "&contextRoot=" + contextRoot);
        System.out.println("\n Servlet3 url: " + url.toString());
        System.out.println("================ End Servlet3 ===============");
    }
}



