import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class From extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
	throws IOException, ServletException {
	
        getServletContext().getRequestDispatcher("/To").forward(req, res);
        try {
            Thread.currentThread().sleep(10 * 1000);
        } catch (InterruptedException ie) {
            throw new ServletException(ie);
        }
    }
}
