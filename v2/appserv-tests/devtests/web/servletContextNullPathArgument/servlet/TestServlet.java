import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;

        ServletContext sc = getServletConfig().getServletContext();
        String realPath = sc.getRealPath(null);
        Set paths = sc.getResourcePaths(null);
        if (realPath == null && paths == null) {
            passed = true;
        }

        res.getWriter().print(passed);
    }
}
