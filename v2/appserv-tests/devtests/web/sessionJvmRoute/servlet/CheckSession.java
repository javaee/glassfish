import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CheckSession extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        boolean passed = false;

        HttpSession session = req.getSession(false);
        if (session == null || session.getId().indexOf(".MYINSTANCE") != -1) {
            throw new ServletException("Missing or invalid session");
        }

        res.addCookie(new Cookie("MYNAME", "MYVALUE"));
    }
}
