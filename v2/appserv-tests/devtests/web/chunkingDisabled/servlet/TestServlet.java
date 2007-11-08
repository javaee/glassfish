import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    private static final int BUFFER_SIZE = 16*1024;

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {

        PrintWriter out = res.getWriter();
 
        /*
         * Check to see if we are in SE/EE, by trying to load a class that's
         * available only in SE/EE.
         * If running on SE/EE, return a message that does fit in the
         * response buffer, as to guarantee a Content-Length response header.
         */
        Class cl = null;
        try {
            Class.forName(
                    "com.sun.enterprise.ee.web.authenticator.HASingleSignOn");
            // EE
            out.println("This is EE");
	} catch (ClassNotFoundException e) {
            // PE
            for (int i=0; i<BUFFER_SIZE; i++) {
                out.print("X");
            }
        }
    }
}
