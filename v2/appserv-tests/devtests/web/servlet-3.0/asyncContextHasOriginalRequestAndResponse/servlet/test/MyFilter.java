package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.*;

@WebFilter(urlPatterns = {"/test"}, asyncSupported = true)
public class MyFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
                throws IOException, ServletException {
        chain.doFilter(request, response);
        AsyncContext ac = request.getAsyncContext();
        String mode = request.getParameter("mode");
        if (!"noarg".equals(mode) && !"original".equals(mode)) {
            throw new ServletException("Invalid mode");
        }
        if ("noarg".equals(mode) && !ac.hasOriginalRequestAndResponse()) {
            throw new ServletException("AsyncContext supposed to have been " +
                                       "initialized with original " +
                                       "request/response");
        } else if ("original".equals(mode) &&
                !ac.hasOriginalRequestAndResponse()) {
            throw new ServletException("AsyncContext supposed to have been " +
                                       "initialized with original " +
                                       "request/response");
        }

        ac.complete();
    }

    public void destroy() {
        // Do nothing
    }
}
