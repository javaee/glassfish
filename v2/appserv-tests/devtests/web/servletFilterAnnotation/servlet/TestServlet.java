import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/mytest")
public class TestServlet extends HttpServlet {
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        res.getWriter().write("filterMessage=" + req.getAttribute("filterMessage"));
    }
}
