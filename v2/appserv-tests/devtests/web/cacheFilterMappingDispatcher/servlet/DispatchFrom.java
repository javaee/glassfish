import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class DispatchFrom extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        RequestDispatcher reqDis = getServletContext().getRequestDispatcher("/dispatchTo");
        String action = req.getParameter("action");
        if ("include".equals(action)) {
            reqDis.include(req, res);
        } else if ("forward".equals(action)) {
            reqDis.forward(req, res);
        } else {
            throw new ServletException("Invalid action param: " + action);
        }
    }
}
