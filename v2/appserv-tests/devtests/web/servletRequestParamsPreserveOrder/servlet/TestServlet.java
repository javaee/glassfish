import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {

        StringBuffer sb = new StringBuffer();

        Enumeration e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            String values = req.getParameter(name);
            sb.append(name + "=" + values);
        }

        PrintWriter out = res.getWriter();
        out.print(sb.toString());
    }
}
