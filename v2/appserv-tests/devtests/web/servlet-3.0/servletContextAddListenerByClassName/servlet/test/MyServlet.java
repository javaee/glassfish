package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class MyServlet extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (!"def".equals(req.getAttribute("abc"))) {
            throw new ServletException("Missing ServletRequest parameter");
        }
    }
}
