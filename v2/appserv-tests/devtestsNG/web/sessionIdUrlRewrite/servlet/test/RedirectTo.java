package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class RedirectTo extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null) {
            res.getWriter().println(session.getAttribute("myname"));
        }
    }
}
