package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;

public class ResumeSession extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Class[] value = (Class[]) session.getAttribute("klazz");

        if (value != null && value.length == 1) {
            res.getWriter().print("Test passed!");
        } else {
            res.getWriter().print("Test failed!");
        }
    }
}
