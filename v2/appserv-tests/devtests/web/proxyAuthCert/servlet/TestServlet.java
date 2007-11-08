import java.io.*;
import java.security.cert.X509Certificate;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;

        X509Certificate[] certChain = (X509Certificate[])
            req.getAttribute("javax.servlet.request.X509Certificate");
        if (certChain != null && certChain.length == 1) {
            String serial = certChain[0].getSerialNumber().toString(16);
            if ("4276a40a".equals(serial)) {
                passed = true;
            }
        }

        res.getWriter().print(passed);
    }
}
