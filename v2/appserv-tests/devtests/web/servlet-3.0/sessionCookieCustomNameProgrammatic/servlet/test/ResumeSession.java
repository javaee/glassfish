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
        if (session == null) {
            throw new ServletException("Unable to resume session");
        }

        HashMap map = (HashMap) session.getAttribute("map");
        if (!"value1".equals(map.get("name1"))
                || !"value2".equals(map.get("name2"))) {
            throw new ServletException("Missing session attributes");
        }
    }
}
