package test;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        URL url = getClass().getClassLoader().getResource(".");
        if (url == null || !(url.toString().startsWith("file:/"))) {
            throw new ServletException("getResource wrong result");
        }
    }
}
