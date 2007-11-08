package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ServletTest extends HttpServlet {

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String host = request.getParameter("host");
        String port = request.getParameter("port");
        String contextRoot = request.getParameter("contextRoot");

        URL url = new URL("http://" + host  + ":" + port + "/web-multiServletRequests/ServletTest2?host="+ host + "&port=" + port + "&contextRoot=" + contextRoot);
        System.out.println("\n Servlet1 Invoking url: " + url.toString());
        URLConnection conn = url.openConnection();
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection urlConnection = (HttpURLConnection)conn;
            urlConnection.setDoOutput(true);

            DataOutputStream dout = 
               new DataOutputStream(urlConnection.getOutputStream());
                                    dout.writeByte(1);

            int responseCode=  urlConnection.getResponseCode();
            System.out.println("responseCode: " + responseCode);
            if (responseCode == 200){
               out.println("multiServletRequests::PASS");
            } else {
               out.println("multiServletRequests::FAIL");
            }
        }
    }
}



