package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class MyFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        PrintWriter pw = response.getWriter();
        pw.println(((HttpServletRequest) request).getPathInfo());
        pw.println(((HttpServletRequest) request).getPathTranslated());
        response.flushBuffer();
    }

    public void destroy() {
        // do nothing
    }
}
