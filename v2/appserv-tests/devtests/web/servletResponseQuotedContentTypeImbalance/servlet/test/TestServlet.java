package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    private static final String CONTENT_TYPE
        = "Multipart/Related; type=\"application/xop+xml\"; boundary=\"----=_Part_2_26726924.1124669253624\"; start-info=\"text/xml;charset=utf-8\"";

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;

        res.setContentType(CONTENT_TYPE);
        if (CONTENT_TYPE.equals(res.getContentType())
                && "utf-8".equals(res.getCharacterEncoding())) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}
