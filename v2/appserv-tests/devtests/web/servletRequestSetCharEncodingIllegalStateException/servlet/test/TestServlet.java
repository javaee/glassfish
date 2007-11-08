package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;

        req.getReader();

        // The specified char encoding must be ignored, since getReader() has
        // already been called
        req.setCharacterEncoding("Shift_JIS");
        if (req.getCharacterEncoding() == null) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}
