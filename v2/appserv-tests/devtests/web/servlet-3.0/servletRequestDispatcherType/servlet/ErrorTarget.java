import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ErrorTarget extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        res.setStatus(HttpServletResponse.SC_OK);
        if (req.getDispatcherType() != DispatcherType.ERROR) {
            throw new ServletException("Wrong dispatcher type: " +
                                       req.getDispatcherType() +
                                       ", should have been ERROR");
        }
    }
}
