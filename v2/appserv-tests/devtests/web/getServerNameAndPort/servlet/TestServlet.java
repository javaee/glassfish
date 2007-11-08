import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException  {

        resp.getWriter().println("ServerName=" + req.getServerName() + ","
                                 + "ServerPort=" + req.getServerPort());
    }
}
