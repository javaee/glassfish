package test;

import java.io.*;
import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

@WebServlet("/mytest2")
public class TestServlet2 extends HttpServlet {
    @Resource(name="wfmin") private Integer min;
    @Resource(name="wfmax") private Integer max;

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        res.getWriter().println("min=" + min + ", max=" + max);
    }
}
