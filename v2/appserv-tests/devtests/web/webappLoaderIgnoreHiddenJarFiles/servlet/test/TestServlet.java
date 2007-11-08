package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;

        try {
            getClass().getClassLoader().loadClass("foo.Bar");
        } catch (ClassNotFoundException cnfe) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}
