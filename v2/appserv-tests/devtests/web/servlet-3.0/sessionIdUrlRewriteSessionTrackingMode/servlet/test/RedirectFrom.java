package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class RedirectFrom extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(true);
        session.setAttribute("myname", new String("MY_SESSION_ATTRIBUTE"));
        res.sendRedirect(res.encodeRedirectURL("redirectTo"));
    }
}



