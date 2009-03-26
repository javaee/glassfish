package test;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.*;
import javax.naming.*;

public class TestFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    public void doFilter (ServletRequest request,
                          ServletResponse response,
                          FilterChain chain)
            throws IOException, ServletException {

        request.setAttribute("abc", "def");
        chain.doFilter(request, response);
    }

    public void destroy() {
        // do nothing
    }
}
