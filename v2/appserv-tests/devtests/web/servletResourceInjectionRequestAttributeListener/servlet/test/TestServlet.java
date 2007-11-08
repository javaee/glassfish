package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;

        // Causes configured ServletRequestAttributeListener to be invoked
        req.setAttribute("foo", new Object());

        // Presence of request attribute with name "success" indicates that
        // configured ServletRequestAttributeListener has passed all resource
        // injection tests
        Object success = req.getAttribute("success");
        if (success != null) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}
