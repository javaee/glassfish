import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (req.getAttribute("name_1") == null ||
                req.getAttribute("name_2") == null) {
            throw new ServletException("Missing request attributes");
        }
    }
}
