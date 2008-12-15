import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class GetCookie extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i=0; i<cookies.length; i++) {
                if ("aaa=bbb=ccc".equals(cookies[i].getValue())) {
                    response.getWriter().println("SUCCESS");
                    break;
                }
            }
        }
    }
}
