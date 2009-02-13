import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (!"testValue".equals(req.getAttribute("valve1")) ||
                !"empty_value".equals(req.getAttribute("valve2")) ||
                !"null_value".equals(req.getAttribute("valve3")) ||
                !"testValue".equals(req.getAttribute("valve4")) ||
                !"empty_value".equals(req.getAttribute("valve5")) ||
                !"null_value".equals(req.getAttribute("valve6"))) {
            throw new ServletException("request attributes do not equal expected values");
        } else {
            res.getWriter().print(true);
        }
    }
}

