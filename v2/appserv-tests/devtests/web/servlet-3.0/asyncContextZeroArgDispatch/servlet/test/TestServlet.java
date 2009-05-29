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
        if (!"MYVALUE".equals(req.getAttribute("MYNAME"))) {
            final AsyncContext ac = req.startAsync();
            req.setAttribute("MYNAME", "MYVALUE");

            Timer asyncTimer = new Timer("TestTimer", true);
            asyncTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        ac.dispatch();
                    }
                },
	        5000);
        } else {
            // Async re-dispatched request
            res.getWriter().println("Hello world");
        }
    }

}
