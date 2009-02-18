import java.io.*;
import javax.servlet.*;

public class Filter_A implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    public void doFilter (ServletRequest request,
                          ServletResponse response,
                          FilterChain chain)
            throws IOException, ServletException {

        if (request.getAttribute("Filter_B") != null) {
            throw new ServletException("Unexpected request attribute");
        }

        request.setAttribute("Filter_A", "Filter_A");

        chain.doFilter(request, response);
    }

    public void destroy() {
        // do nothing
    }
}
