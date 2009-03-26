package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if (!req.isAsyncSupported()) {
            throw new ServletException("Async not supported when it should");
        }

        final AsyncContext ac = req.startAsync();
        final String target = req.getParameter("target");

        Timer asyncTimer = new Timer("TestTimer", true);
        asyncTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    ac.dispatch(target);
                }
            },
	    5000);
    }
}
