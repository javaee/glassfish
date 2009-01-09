import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ForwardTarget extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
System.out.println("FF=" + req.getClass());
        if (req.getDispatcherType() != DispatcherType.FORWARD) {
            throw new ServletException("Wrong dispatcher type: " +
                                       req.getDispatcherType() +
                                       ", should have been FORWARD");
        }
    }
}
