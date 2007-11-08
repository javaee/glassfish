import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        /*
         * If getServletContext().getContextPath() were to return null,
         * the following would cause a NPE.
         */
        res.getWriter().print(getServletContext().getContextPath().length());
    }
}
