package myfilter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

public class MyFilter implements Filter {

    public void doFilter (ServletRequest request,
                          ServletResponse response,
                          FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(request,
                       new MyServletResponseWrapper((HttpServletResponse)response));
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    public void destroy() {
        // do nothing
    }
}
