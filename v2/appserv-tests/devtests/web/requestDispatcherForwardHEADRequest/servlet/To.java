import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;

public class To extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        res.getWriter().print("Hello World");
    }
}
