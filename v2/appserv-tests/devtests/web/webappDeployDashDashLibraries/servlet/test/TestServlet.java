package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        try {
            Class.forName("ddd.eee.fff.Test").newInstance();
        } catch (Throwable t) {
            throw new ServletException(t);
        }
    }
}
