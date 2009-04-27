import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/myurl")
public class TestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        StringBuilder b = new StringBuilder("g:Hello, ");
        b.append((req.getRemoteUser() == null) + ", ");
        b.append(req.isUserInRole("javaee") + ", ");

        req.login("javaee", "javaee");
        b.append(req.getRemoteUser() + ", ");
        b.append(req.isUserInRole("javaee") + ", ");
        req.logout();

        b.append((req.getRemoteUser() == null) + ", ");
        b.append(req.isUserInRole("javaee") + "\n");
        PrintWriter writer = res.getWriter();
        writer.write(b.toString());
    }
}
