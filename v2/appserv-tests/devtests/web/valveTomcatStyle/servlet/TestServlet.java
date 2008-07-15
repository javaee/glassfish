import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (!"VALUE_1".equals(req.getAttribute("ATTR_1")) ||
                !"VALUE_2".equals(req.getAttribute("ATTR_2")) ||
                !"VALUE_3".equals(req.getAttribute("ATTR_3"))) {
            throw new ServletException("Missing request attributes");
        } else {
            res.getWriter().print(true);
        }
    }
}

