package webFragment1;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class WebFragmentServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        res.getWriter().println("Hello world");
    }
}
