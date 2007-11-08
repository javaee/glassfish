package test;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.*;
import javax.naming.*;

public class TestFilter implements Filter {

    private AtomicInteger count;

    public void init(FilterConfig filterConfig) throws ServletException {
        count = new AtomicInteger();    
    }

    public void doFilter (ServletRequest request,
                          ServletResponse response,
                          FilterChain chain)
            throws IOException, ServletException {

        count.getAndIncrement();
        request.setAttribute("filterCount", count);
        chain.doFilter(request, response);
    }

    public void destroy() {
        // do nothing
    }
}
