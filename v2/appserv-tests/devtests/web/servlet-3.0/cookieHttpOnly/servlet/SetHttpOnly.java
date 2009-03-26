import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SetHttpOnly extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        Cookie cookie = new Cookie("abc", "def");
        cookie.setHttpOnly(true);
        res.addCookie(cookie);
    }
}
