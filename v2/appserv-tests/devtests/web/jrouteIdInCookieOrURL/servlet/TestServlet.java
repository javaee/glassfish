import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.catalina.connector.RequestFacade;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException  {

        res.getWriter().print("jrouteId=" +
            ((RequestFacade)req).getUnwrappedCoyoteRequest().getJrouteId());
    }
}
