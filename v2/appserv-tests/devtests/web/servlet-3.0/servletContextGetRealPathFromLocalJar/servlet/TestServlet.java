import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String path = getServletContext().getRealPath("/abc.txt");
        if (path == null || 
                path.indexOf("/META-INF/resources") == -1) {
            throw new ServletException("Wrong resource path");
        }

        File f = new File(path);
        if (!f.exists()) {
            throw new ServletException("Resource does not exist");
        }
    }
}
