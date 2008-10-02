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
        HashMap map = (HashMap) session.getAttribute("map");
        InitialContext ic = (InitialContext)
            session.getAttribute("JNDIInitialContext");

        boolean passed = false; 
        if ("value1".equals(map.get("name1"))
                && "value2".equals(map.get("name2"))
                && (ic != null)) {
            passed = true;
        }

        if (passed) {
            res.getWriter().print("Test passed!");
        } else {
            res.getWriter().print("Test failed!");
        }
    }
}
