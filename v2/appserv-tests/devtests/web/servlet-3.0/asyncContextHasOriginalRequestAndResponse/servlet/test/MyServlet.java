package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(urlPatterns = {"/test"}, asyncSupported = true)
public class MyServlet extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        AsyncContext ac = null;
        String mode = req.getParameter("mode");
        if ("noarg".equals(mode)) {
            ac = req.startAsync();
        } else if ("original".equals(mode)) {
            ac = req.startAsync(req, res);
        } else {
            throw new ServletException("Invalid mode");
        }
    }
}
