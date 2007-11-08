import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        req.getRequestDispatcher("/subdir1/subdir2/").forward(req, res);
    }
}
