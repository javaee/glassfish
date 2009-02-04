import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(TestServlet.class);

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {
        response.getWriter().println("OK!");
    }

}
