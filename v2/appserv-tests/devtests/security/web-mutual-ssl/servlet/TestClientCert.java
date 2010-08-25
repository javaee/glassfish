package devtests.security;

import java.io.*;
import java.security.cert.X509Certificate;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestClientCert extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {

        System.out.println("start service(...) of TestClientCert" );

        X509Certificate[] certs = (X509Certificate[])req.getAttribute(
            "javax.servlet.request.X509Certificate");
        String clName = null;
        if (certs != null) {
            for (X509Certificate cert : certs) {
               getServletContext().log(cert.toString());
            }            
            clName = certs.getClass().getName();
	} else {
            clName = "cert is null";
        }
        res.getWriter().print(clName);
    }
}
