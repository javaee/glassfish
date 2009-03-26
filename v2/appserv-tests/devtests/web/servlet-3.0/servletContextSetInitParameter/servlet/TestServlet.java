import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (!"bbb".equals(getServletContext().getInitParameter("aaa"))) {
            throw new ServletException("Missing servlet init param");
        }
    }
}
