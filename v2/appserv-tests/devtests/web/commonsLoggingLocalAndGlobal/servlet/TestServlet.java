import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        try {
            Object o = Class.forName("test.MyTest").newInstance();
            res.getWriter().println(o.toString());
	} catch (Exception e) {
	    throw new ServletException(e);
	}
    }
}
