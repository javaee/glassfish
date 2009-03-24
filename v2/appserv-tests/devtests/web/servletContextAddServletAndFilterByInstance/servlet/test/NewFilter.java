package test;

import java.io.*;
import javax.servlet.*;

public class NewFilter implements Filter {

    private String initParamValue;
    private String myParamValue;

    public void setMyParameter(String value) {
        myParamValue = value;
    }

    public void init(FilterConfig config) throws ServletException {
        initParamValue = config.getInitParameter("filterInitParamName");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        request.setAttribute("filterInitParamName", initParamValue);
        request.setAttribute("myFilterParamName", myParamValue);
        chain.doFilter(request, response);
    }

    public void destroy() {
        // do nothing
    }
}
