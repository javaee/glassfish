import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CreateCookie extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.addCookie(new Cookie("mycookie", "aaa=bbb=ccc"));
    }
}
