import java.io.*;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

@WebServlet(name="hello2", urlPatterns="/hello2")
public class TestServlet2 extends HttpServlet {
    private volatile int count = 0;

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        if ((count % 2) == 0) {
            count++;
            throw new IOException();
        }
    }
}
