import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        for(int i=0; i < 4096 ; i++){
            res.addHeader("header" + i, "value" + i);
        }

    }
}
