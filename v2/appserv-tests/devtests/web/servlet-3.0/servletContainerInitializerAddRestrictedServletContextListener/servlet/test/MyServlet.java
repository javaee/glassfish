package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class MyServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        Object attrValue = getServletContext().getAttribute("testname");
        if (attrValue == null || !"testvalue".equals(attrValue)) {
            throw new ServletException("Missing ServletContext attribute");
        }
    }
}
