package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class NewServlet extends HttpServlet {

    public void init() throws ServletException {
        
    }

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (!"servletInitParamValue".equals(
                getServletConfig().getInitParameter("servletInitParamName"))) {
            throw new ServletException("Missing servlet init param");
        }

        if (!"filterInitParamValue".equals(
                req.getAttribute("filterInitParamName"))) {
            throw new ServletException("Missing filter init param");
        }
    }
}
