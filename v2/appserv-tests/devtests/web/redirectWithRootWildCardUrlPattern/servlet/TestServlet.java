import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        PrintWriter writer = res.getWriter();
        writer.print("uri = " + req.getRequestURI() + ", ");
        writer.println("pathInfo = " + req.getPathInfo());
    }
}

