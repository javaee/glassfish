package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class DispatchFrom extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if (!req.isAsyncSupported()) {
            throw new ServletException("Async not supported when it should");
        }

        final ServletContext toContext = getServletContext().getContext("/toContext");
        if (toContext == null) {
            throw new ServletException("Unable to acquire target ServletContext");
        }
        final AsyncContext ac = req.startAsync();

        Timer asyncTimer = new Timer("TestTimer", true);
        asyncTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    ac.dispatch(toContext, "/dispatchTo");
                }
            },
	    5000);
    }
}
