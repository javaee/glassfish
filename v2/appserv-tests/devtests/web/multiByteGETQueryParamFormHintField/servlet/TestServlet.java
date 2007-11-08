import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {
    
    public void doGet (HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        boolean passed = false;

        String name = req.getParameter("japaneseName");
        if ("\u3068\u4eba\u6587".equals(name)){
            passed = true;
        }

        res.getWriter().print(passed);
    }
}
