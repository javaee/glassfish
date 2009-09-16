package org.glassfish.distributions.test.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.annotation.Resource;


@WebServlet(urlPatterns={"/hello"})
public class WebHello extends HttpServlet {

    public WebHello() {
        System.out.println("Servlet WEB-HELLO initialized");
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        PrintWriter pw = res.getWriter();
        try {
            pw.println("Hello World !");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}