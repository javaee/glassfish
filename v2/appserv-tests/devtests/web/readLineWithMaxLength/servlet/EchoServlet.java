package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EchoServlet extends HttpServlet {
     protected void doPost(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         BufferedReader br = req.getReader();
         Writer writer = res.getWriter();
         String line = null;
         int count = 0;
         while ((line = br.readLine()) != null) {
             count += line.length();
             writer.write(line);
         }
         writer.close();
         br.close();
     }
}
