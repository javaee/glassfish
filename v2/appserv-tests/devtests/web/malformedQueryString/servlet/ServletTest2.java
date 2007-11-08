package test;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest2 extends HttpServlet {

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
        
        out.println("TEST::PASS");
    }
}


