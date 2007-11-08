package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        try {
            Object o = getClass().getClassLoader().loadClass("aaa.bbb.ccc.Test").newInstance();
            res.getWriter().println(o.toString());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
