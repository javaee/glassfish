import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {

        Object cert = req.getAttribute(
            "javax.servlet.request.X509Certificate");
        String clName = null;
        if (cert != null) {
            clName = cert.getClass().getName();
	}
        res.getWriter().print(clName);
    }
}
