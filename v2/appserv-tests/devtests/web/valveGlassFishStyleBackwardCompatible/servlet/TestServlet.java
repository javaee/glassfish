import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (!"VALUE".equals(req.getAttribute("ATTR"))) {
            throw new ServletException("Missing request attributes");
        } else {
            res.getWriter().print(true);
        }
    }
}

