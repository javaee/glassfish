package test;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ServletTest extends HttpServlet {


    public void doPost(HttpServletRequest req,
                      HttpServletResponse resp)
      throws IOException, ServletException {


        BufferedReader reader = req.getReader();

        StringBuffer sb = new StringBuffer();
        for (String line = reader.readLine(); line != null; line = reader.readLine())
        {
            sb.append(line);
        }
        reader.close();

        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        bw.write("PASSED");
        bw.newLine();
        bw.flush();
        bw.close();
        resp.setBufferSize(sw.toString().length());
        resp.setContentLength(sw.toString().length());
        resp.setContentType("text/plain");
        Writer writer = resp.getWriter();
        writer.write(sw.toString());
        writer.flush();
        writer.close();
    }

}
