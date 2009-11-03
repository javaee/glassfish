import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;

public class To extends GenericServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        res.getWriter().print("Hello world");
    }
}
