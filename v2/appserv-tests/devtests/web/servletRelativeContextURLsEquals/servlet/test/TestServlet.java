package test;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    private ServletContext context;

    public void init(ServletConfig sconfig) {
	this.context = sconfig.getServletContext();
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;
	
	try {
            URL main = context.getResource("/test/res1.jsp");
            URL sub = new URL(main, "res2.jsp");
            URL sub1 = context.getResource("/test/res2.jsp");
            if (sub.equals(sub1) && sub.toString().equals(sub1.toString())) {
                passed = true;
            }
        } catch (Throwable t) {
            throw new ServletException(t);
        }

        res.getWriter().print(passed);
    }
}
