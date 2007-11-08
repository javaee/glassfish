import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CheckAccessLog extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String location = req.getParameter("location");
        String[] files = new File(location + "/domains/domain1/logs/access").list();
        if (files != null && files.length == 2) {
            resp.getWriter().println("SUCCESS!");
        }    
    }
}
