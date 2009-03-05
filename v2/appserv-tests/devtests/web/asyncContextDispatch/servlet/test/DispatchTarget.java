package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class DispatchTarget extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        res.getWriter().println("Hello world");
        res.getWriter().flush();
    }
}
