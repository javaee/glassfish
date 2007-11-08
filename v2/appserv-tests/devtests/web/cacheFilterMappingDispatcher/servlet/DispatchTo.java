import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class DispatchTo extends HttpServlet {

    private int count = 0;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        res.getWriter().print("RESPONSE-" + count++);
    }
}
