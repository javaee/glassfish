package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet implements AsyncListener {

    private boolean onTimeoutCalled = false;

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if (!req.isAsyncSupported()) {
            throw new ServletException("Async not supported when it should");
        }

        req.addAsyncListener(this);
        req.setAsyncTimeout(10000);

        AsyncContext ac = req.startAsync();

        try {
            Thread.currentThread().sleep(15000);
        } catch (InterruptedException ie) {
            throw new ServletException(ie);
        }

        if (!onTimeoutCalled) {
            throw new ServletException("AsyncListener#onTimeout not called");
        }
    }


    public void onComplete(AsyncEvent event) throws IOException {
        // do nothing
    }


    public void onTimeout(AsyncEvent event) throws IOException {
        onTimeoutCalled = true;
    }
}
