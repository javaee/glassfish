package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest extends HttpServlet{

    private ServletContext context;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
		ServletContext ctx = config.getServletContext();
		ctx.setAttribute("Attribut", "une valeur");
    }
    
    public void destroy(){
        System.out.println("########### destroy #############");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        System.out.println("########## StaticListener.removeAttribute: " + 
                                            StaticListener.removeAttribute);
        out.println("DESTROY:" 
                    + (StaticListener.removeAttribute == true ? "PASS" : "FAIL"));
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
    }
}



