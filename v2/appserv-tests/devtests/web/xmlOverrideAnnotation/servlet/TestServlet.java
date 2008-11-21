package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.InitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name="testServlet", urlPatterns={"/mytest"}, initParams={ @InitParam(name="n1", value="v1"), @InitParam(name="n2", value="v2") })
public class TestServlet extends HttpServlet {
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("filterMessage=" + req.getAttribute("filterMessage"));
        String msg = "";
        Enumeration en = getInitParameterNames();
        while (en.hasMoreElements()) {
            String name = (String)en.nextElement();
            String value = getInitParameter(name);
            msg += name + "=" + value + ", ";
        } 
        writer.write(", initParams: " + msg + "\n");
    }
}
