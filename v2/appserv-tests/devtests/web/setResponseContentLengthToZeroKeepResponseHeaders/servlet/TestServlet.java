import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        res.setHeader("A", "a");
        res.setHeader("Content-Length", "0");
        res.setHeader("B", "b");
    }
}
