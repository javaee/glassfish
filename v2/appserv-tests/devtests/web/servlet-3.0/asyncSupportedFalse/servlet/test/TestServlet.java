package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if (req.getAttribute("abc") == null) {
            throw new ServletException("Filter not invoked");
        }

        if (req.isAsyncSupported()) {
            throw new ServletException("Async supported when it should not");
        }
    }

}
