import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SimpleServlet extends HttpServlet {
    
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.getWriter().print(req.getServerPort());
    }
}
