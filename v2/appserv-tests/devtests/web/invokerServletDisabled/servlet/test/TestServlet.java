package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException {

	getServletContext().log("This servlet should not have been invoked");
    }
}
