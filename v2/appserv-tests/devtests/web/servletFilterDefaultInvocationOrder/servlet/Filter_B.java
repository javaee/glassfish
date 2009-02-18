import java.io.*;
import javax.servlet.*;

public class Filter_B implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    public void doFilter (ServletRequest request,
                          ServletResponse response,
                          FilterChain chain)
            throws IOException, ServletException {

        if (request.getAttribute("Filter_A") == null) {
            throw new ServletException("Missing request attribute");
        }

        chain.doFilter(request, response);
    }

    public void destroy() {
        // do nothing
    }
}
