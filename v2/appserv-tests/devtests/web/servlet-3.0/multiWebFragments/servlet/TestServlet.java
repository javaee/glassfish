import java.io.*;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

@WebServlet("/")
public class TestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String message = "filterMessage=" + req.getAttribute("filterMessage");
        message += ", filterMessage2=" + req.getAttribute("filterMessage2");
        message += ", filterMessage3=" + req.getAttribute("filterMessage3");
        res.getWriter().println(message);
    }
}
