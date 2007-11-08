package test;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class HelloJapan extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html");
        res.setCharacterEncoding("Shift_JIS");
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                      "BEGIN_JAPANESE"
                      + "\u4eca\u65e5\u306f\u4e16\u754c"  // Hello World
                      + "END_JAPANESE");
    }
}
