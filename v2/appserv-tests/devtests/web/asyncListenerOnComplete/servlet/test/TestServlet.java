package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet implements AsyncListener {

    private boolean onCompleteCalled = false;

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if (!req.isAsyncSupported()) {
            throw new ServletException("Async not supported when it should");
        }

        req.addAsyncListener(this);

        AsyncContext ac = req.startAsync();
        ac.complete();

        if (!onCompleteCalled) {
            throw new ServletException("AsyncListener#onComplete not called");
        }
    }


    public void onComplete(AsyncEvent event) throws IOException {
        onCompleteCalled = true;
    }


    public void onTimeout(AsyncEvent event) throws IOException {
        // do nothing
    }
}
