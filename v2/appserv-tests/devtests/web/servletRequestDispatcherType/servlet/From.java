import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class From extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if (req.getDispatcherType() != DispatcherType.REQUEST) {
            throw new ServletException("Wrong dispatcher type: " +
                                       req.getDispatcherType() +
                                       ", should be REQUEST");
        }

        MyRequestWrapper wreq = new MyRequestWrapper(req);

        String mode = req.getParameter("mode");
        if ("forward".equals(mode)) {
            getServletContext().getRequestDispatcher("/ForwardTarget").forward(wreq, res);
        } else if ("include".equals(mode)) {
            getServletContext().getRequestDispatcher("/IncludeTarget").include(wreq, res);
        } else if ("error".equals(mode)) {
            res.sendError(555);
        } else {
            throw new ServletException("Invalid dispatching mode");
        }
    }

    static class MyRequestWrapper extends HttpServletRequestWrapper {
        public MyRequestWrapper(HttpServletRequest req) {
            super(req);
        }
    }
}
