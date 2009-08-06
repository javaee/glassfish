package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest extends HttpServlet {

    private ServletContext context;
    
    public void init(ServletConfig config) throws ServletException {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        for (Part p: request.getParts()) {

            out.write("Part name: " + p.getName()+ "\n");
            out.write("Size: " + p.getSize() + "\n");
            out.write("Content Type: " + p.getContentType() + "\n");
            out.write("Header Names:");
            for (String name: p.getHeaderNames()) {
                out.write(" " + name);
            }
            out.write("\n");
        }
        out.flush();
    }

}



