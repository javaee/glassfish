import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String combinedValues = "";

        String[] values = req.getParameterValues("param1");
        if (values != null) {
            for (int i=0; i<values.length; i++) {
                combinedValues += values[i];
                if (i<values.length-1) {
                    combinedValues += ",";
                }
            }
        }

        res.getWriter().print(combinedValues);
    }
}
