import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        URL u = getServletContext().getResource("/abc.txt");
        if (u == null) {
            throw new ServletException("Resource not found");
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(u.openStream()));
            if (!"Hello World".equals(in.readLine())) {
                throw new ServletException("Missing content");
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
