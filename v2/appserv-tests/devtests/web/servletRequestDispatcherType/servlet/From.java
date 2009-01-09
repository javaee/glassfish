import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class From extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (req.getDispatcherType() != DispatcherType.REQUEST) {
            throw new ServletException("Wrong dispatcher type, should be REQUEST");
        }

        String mode = req.getParameter("mode");
        if ("forward".equals(mode)) {
            getServletContext().getRequestDispatcher("/ForwardTarget").forward(req, res);
        } else if ("include".equals(mode)) {
            getServletContext().getRequestDispatcher("/IncludeTarget").include(req, res);
        } else {
            throw new ServletException("Invalid dispatching mode");
        }
    }
}
