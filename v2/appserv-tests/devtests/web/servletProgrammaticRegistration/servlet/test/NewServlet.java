package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class NewServlet extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (!"myInitValue".equals(getServletConfig().getInitParameter(
                        "myInitName"))) {
            throw new ServletException("Missing init param");
        }
    }
}
