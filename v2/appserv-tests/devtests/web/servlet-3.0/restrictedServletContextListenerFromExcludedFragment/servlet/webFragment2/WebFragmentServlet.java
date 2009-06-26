package webFragment2;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class WebFragmentServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        Object attr = getServletContext().getAttribute("exception");
        if (attr == null ||
                !attr.getClass().equals(IllegalStateException.class)) {
            throw new ServletException("Missing ServletContext attribute");
        }
    }
}
