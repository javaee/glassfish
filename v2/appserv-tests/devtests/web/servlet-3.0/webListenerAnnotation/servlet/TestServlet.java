import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/mytest")
public class TestServlet extends HttpServlet {
    private String aword = null;
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        aword = (String)config.getServletContext().getAttribute("myattr");
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        res.getWriter().write(aword);
    }
}
