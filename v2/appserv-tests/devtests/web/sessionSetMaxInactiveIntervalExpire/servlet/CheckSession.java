import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CheckSession extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        boolean passed = false;

        HttpSession session = req.getSession(false);
        if (req.isRequestedSessionIdFromCookie() && session == null) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}



