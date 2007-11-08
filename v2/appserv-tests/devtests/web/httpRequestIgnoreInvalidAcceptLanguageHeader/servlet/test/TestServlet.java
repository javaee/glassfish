package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        int num = 0;
        boolean deLocaleSeen = false;
        boolean frLocaleSeen = false;
        Enumeration e = req.getLocales();
        while (e.hasMoreElements()) {
            Locale loc = (Locale) e.nextElement();
            if ("de".equalsIgnoreCase(loc.getLanguage())) {
                deLocaleSeen = true;
            } else if ("FR".equalsIgnoreCase(loc.getLanguage())) {
                frLocaleSeen = true;
            }
            num++;
        }

        boolean passed = false;
        if (num == 2 && deLocaleSeen && frLocaleSeen) {
            passed = true;
        }    
        res.getWriter().print(passed);
    }
}
