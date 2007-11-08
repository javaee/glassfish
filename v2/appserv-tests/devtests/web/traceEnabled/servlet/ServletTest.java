package test;

import java.io.IOException;
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

    public void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.doTrace]");
        
        response.setContentType("text/html");
    }

}



