import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Foo extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException {

System.out.println("FOOFOO: " + req.getRequestURL());
        RequestDispatcher rd = getServletConfig().getServletContext().getNamedDispatcher("jsp");
        rd.forward(req, res);
       
    }
}
