package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CreateSession extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getSession(true);
    }
}
