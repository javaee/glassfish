import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;
import mypackage.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Digester digester = new Digester();

        digester.setValidating(false);
        digester.setNamespaceAware(false);

        digester.addObjectCreate("foo", "mypackage.Foo");
        digester.addSetProperties("foo");
        digester.addObjectCreate("foo/bar", "mypackage.Bar");
        digester.addSetProperties("foo/bar");
        digester.addSetNext("foo/bar", "addBar", "mypackage.Bar");

        try {
            Foo foo = (Foo) digester.parse(
                getServletContext().getResourceAsStream("input.txt"));
            res.getWriter().print(foo.getName());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

}
