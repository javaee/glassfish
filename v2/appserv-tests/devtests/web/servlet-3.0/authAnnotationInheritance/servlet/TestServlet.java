import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.security.PermitAll;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/myurl")
public class TestServlet extends BaseTestServlet {
    @PermitAll
    protected void doTrace(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("t:Hello");
    }
}
