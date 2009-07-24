package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class RedirectTo extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            throw new ServletException("Missing session");
        }
        if (session.getAttribute("myname") == null) {
            throw new ServletException("Missing session attribute");
        }
    }
}
