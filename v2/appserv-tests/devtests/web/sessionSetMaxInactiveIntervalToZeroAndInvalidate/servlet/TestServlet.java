import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        boolean passed = false;

        try {
            HttpSession session = req.getSession(true);
            session.setMaxInactiveInterval(0);
            session.invalidate();   
        } catch (IllegalStateException ise) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}



