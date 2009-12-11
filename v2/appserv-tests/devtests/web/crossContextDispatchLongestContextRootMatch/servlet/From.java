import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class From extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        ServletContext sc = getServletContext().getContext("/123/456/789");
        RequestDispatcher rd = sc.getRequestDispatcher("/test.txt");
        rd.forward(req, res);
    }
}
