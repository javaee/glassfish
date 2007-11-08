import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {

        res.getWriter().println("PASSED");
    }
}
