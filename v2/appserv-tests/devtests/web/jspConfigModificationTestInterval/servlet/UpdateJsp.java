import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class UpdateJsp extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String path = getServletContext().getRealPath("/jsp/test.jsp");
        File f = new File(path);
        PrintStream ps = new PrintStream(new FileOutputStream(f));
        ps.println("updated jsp");
        ps.close();

        /*
         * Manually update timestamp on JSP file to a value far into the
         * future, so that the corresponding servlet class file will be 
         * guaranteed to be out-of-date
         */
        f.setLastModified(System.currentTimeMillis() + 9000000000L);

        res.getWriter().println("Done");
    }
}
