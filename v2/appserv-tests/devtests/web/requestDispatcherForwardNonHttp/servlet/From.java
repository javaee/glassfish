import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class From extends GenericServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        getServletContext().getRequestDispatcher("/To").forward(
            new MyServletRequestWrapper(req),
            new MyServletResponseWrapper(res));
    }

    private static class MyServletRequestWrapper extends ServletRequestWrapper {
        public MyServletRequestWrapper(ServletRequest request) {
            super(request);
        }
    }

    private static class MyServletResponseWrapper extends ServletResponseWrapper {
        public MyServletResponseWrapper(ServletResponse response) {
            super(response);
        }
    }

}
