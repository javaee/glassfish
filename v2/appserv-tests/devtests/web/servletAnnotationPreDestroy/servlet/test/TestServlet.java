package test;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.annotation.PreDestroy;

public class TestServlet extends HttpServlet {

    @PreDestroy
    public void myPreDestroy() {
        try {
            FileOutputStream fos = new FileOutputStream("/tmp/mytest");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write("SUCCESS");
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
