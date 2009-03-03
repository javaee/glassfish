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
        boolean isWrap = Boolean.parseBoolean(req.getParameter("wrap"));
        if (isWrap) {
            ac = req.startAsync(req, res);
        } else {
            ac = req.startAsync();
        }
    }
}
