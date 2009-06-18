package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    private volatile boolean initialized;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (initialized) {
            throw new IllegalStateException("Trying to initialize Servlet " +
                "that is already initialized");
        }

        initialized = true;
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        // Do nothing
    }

}
