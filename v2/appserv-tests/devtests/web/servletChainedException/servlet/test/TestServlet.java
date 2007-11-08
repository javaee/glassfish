package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    ServletContext sc;

    public void init(ServletConfig config) throws ServletException {
        sc = config.getServletContext();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;

        Throwable t = new Throwable("test");

        ServletException se1 = new ServletException("test", t);
        ServletException se2 = new ServletException(t);        
        if (se1.getCause() == t && se2.getCause() == t) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}
