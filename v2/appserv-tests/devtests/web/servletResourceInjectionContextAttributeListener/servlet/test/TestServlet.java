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

        // This will cause configured ServletContextAttributeListener to be
        // invoked. If it passes all resource injection tests, it will set
        // context attribute with name "success".
        sc.setAttribute("foo", new Object());

        boolean passed = false;
        Object success = sc.getAttribute("success");
        if (success != null) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}
