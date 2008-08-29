import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CheckRequestPath extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        int port = req.getLocalPort();
        String servletPath = req.getServletPath();
        String host = req.getLocalName();

        String expectedCtxtRoot = null;
        String expectedRequestURL = null;
        String expectedRequestURI = null;
        
        String run = req.getParameter("run");
        if ("first".equals(run)) {
            expectedCtxtRoot = "";
            expectedRequestURI = servletPath;
            expectedRequestURL = "http://" + host + ":" + port +
                    expectedRequestURI;
        } else if ("second".equals(run)) {
            expectedCtxtRoot =
                "/web-virtual-server-default-web-module-request-path";
            expectedRequestURI = expectedCtxtRoot + servletPath;
            expectedRequestURL = "http://" + host + ":" + port + 
                    expectedRequestURI;
        } else {
            throw new ServletException();
        }

        if (!expectedCtxtRoot.equals(req.getContextPath()) ||
                !expectedRequestURL.equals(req.getRequestURL().toString()) ||
                !expectedRequestURI.equals(req.getRequestURI())) {
            throw new ServletException();
        }
    }
}



