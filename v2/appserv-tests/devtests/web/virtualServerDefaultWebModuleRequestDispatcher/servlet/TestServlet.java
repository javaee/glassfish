import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        ServletContext sc = getServletContext().getContext("/");
        RequestDispatcher rd = sc.getRequestDispatcher("/test.txt");
        rd.forward(req, res);
    }
}
