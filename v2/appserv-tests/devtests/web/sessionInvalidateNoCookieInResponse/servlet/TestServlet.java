import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        HttpSession session = req.getSession();
        res.addCookie(new Cookie("JSESSIONIDSSO", "123456"));
        res.addCookie(new Cookie("myCookieHeader", "myCookieValue"));
        session.invalidate();
    }
}
