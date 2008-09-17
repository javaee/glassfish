import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class AccessSession extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i=0; i<cookies.length; i++) {
                System.out.println("COOKIE=" + cookies[i].getValue());
            }
        }

        HttpSession session = req.getSession(false);
	if (session == null) {
            throw new ServletException("Unable to access login session");
        }

        res.getWriter().println("JSESSIONID=" + session.getId());
    }
}
