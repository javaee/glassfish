package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class NewServlet extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (!"servletInitValue".equals(getServletConfig().getInitParameter(
                        "servletInitName"))) {
            throw new ServletException("Missing servlet init param");
        }

        if (!"filterInitValue".equals(req.getAttribute("filterInitName"))) {
            throw new ServletException("Missing filter init param");
        }

    }
}
