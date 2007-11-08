import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class From extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws IOException, ServletException {
	
        getServletContext().getRequestDispatcher("/To").forward(req, res);
    }
}
