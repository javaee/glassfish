package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        InitialContext ic = null;
        try {
            ic = new InitialContext();
        } catch (Exception e) {
            throw new ServletException(e);
        }

	HttpSession httpSession = req.getSession();
        httpSession.setAttribute("JNDIInitialContext", ic);

    }
}
