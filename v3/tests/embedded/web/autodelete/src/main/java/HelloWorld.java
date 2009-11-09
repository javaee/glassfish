import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet; 
import javax.servlet.*;
import javax.servlet.http.*;

@WebServlet(urlPatterns={"/hello"})
public class HelloWorld extends HttpServlet {

  
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        
        PrintWriter pw = res.getWriter();
        try {
			pw.println("Hello World !<br>");
  		} catch(Exception e) {
        	e.printStackTrace();
        }
    }
}

