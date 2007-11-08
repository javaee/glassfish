package test;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean success = false;
        
        AtomicInteger count = (AtomicInteger) req.getAttribute("filterCount");
        if (count != null && count.intValue() == 2) {
            success = true;
        }

        res.getWriter().print(success);
    }
}
