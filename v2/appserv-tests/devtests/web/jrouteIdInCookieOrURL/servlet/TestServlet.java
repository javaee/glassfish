import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.sun.enterprise.web.monitor.*;
import com.sun.enterprise.web.monitor.impl.*;
import org.apache.coyote.tomcat5.CoyoteRequestFacade;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException  {

        res.getWriter().print("jrouteId=" +
            ((CoyoteRequestFacade)req).getUnwrappedCoyoteRequest().getJrouteId());
    }
}
