package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class NewServlet extends HttpServlet {

    private String initParamValue;
    private String myParamValue;

    public void setMyParameter(String value) {
        myParamValue = value;
    }

    public void init(ServletConfig config) throws ServletException {
        initParamValue = config.getInitParameter("servletInitParamName");
    }

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (!"myServletParamValue".equals(myParamValue)) {
            throw new ServletException("Wrong servlet instance");
        }

        if (!"servletInitParamValue".equals(initParamValue)) {
            throw new ServletException("Missing servlet init param");
        }

        if (!"myFilterParamValue".equals(
                req.getAttribute("myFilterParamName"))) {
            throw new ServletException("Wrong filter instance");
        }

        if (!"filterInitParamValue".equals(
                req.getAttribute("filterInitParamName"))) {
            throw new ServletException("Missing filter init param");
        }
    }
}
