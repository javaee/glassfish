import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        boolean passed = false;

        try {
            Class foo = this.getClass().getClassLoader().loadClass("com.acme.Foo");
            if (this.getClass().getClassLoader() == foo.getClassLoader()) {
                passed = true;
            }
        } catch (ClassNotFoundException e) {
        }

        res.getWriter().print(passed);
    }
}
