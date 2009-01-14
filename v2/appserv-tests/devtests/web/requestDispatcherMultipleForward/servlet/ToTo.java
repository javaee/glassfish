import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ToTo extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (req.getAttribute("javax.servlet.forward.request_uri") == null) {
            throw new ServletException("Missing request attribute in " +
                                       "second forward target");
        }
    }
}
