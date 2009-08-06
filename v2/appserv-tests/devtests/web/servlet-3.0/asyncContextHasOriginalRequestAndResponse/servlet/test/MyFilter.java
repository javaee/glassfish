package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebFilter(urlPatterns = {"/test"}, asyncSupported = true)
public class MyFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
                throws IOException, ServletException {

        String mode = request.getParameter("mode");
        if (!"noarg".equals(mode) && !"original".equals(mode) &&
                !"wrap".equals(mode)) {
            throw new ServletException("Invalid mode");
        }

        if ("wrap".equals(mode)) {
            chain.doFilter(new MyWrapper((HttpServletRequest) request),
                response);
        } else {
            chain.doFilter(request, response);
        }

        AsyncContext ac = request.getAsyncContext();
        if ("noarg".equals(mode) && !ac.hasOriginalRequestAndResponse()) {
            throw new ServletException(
                "AsycContext#hasOriginalRequestAndResponse returned false, " +
                "should have returned true");
        } else if ("original".equals(mode) &&
                !ac.hasOriginalRequestAndResponse()) {
            throw new ServletException(
                "AsycContext#hasOriginalRequestAndResponse returned false, " +
                "should have returned true");
        } else if ("wrap".equals(mode) &&
                ac.hasOriginalRequestAndResponse()) {
            throw new ServletException(
                "AsycContext#hasOriginalRequestAndResponse returned true, " +
                "should have returned false");
        }

        ac.complete();
    }

    public void destroy() {
        // Do nothing
    }

    private static class MyWrapper extends HttpServletRequestWrapper {

        public MyWrapper(HttpServletRequest req) {
            super(req);
        }
    }
}
