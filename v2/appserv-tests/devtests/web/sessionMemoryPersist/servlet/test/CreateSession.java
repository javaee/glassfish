package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;

public class CreateSession extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(true);

        HashMap map = new HashMap();
        map.put("name1", "value1");   
        map.put("name2", "value2");   
        session.setAttribute("map", map);

        InitialContext ic = null;
        try {
            ic = new InitialContext();
        } catch (Exception e) {
            throw new ServletException(e);
        }
        session.setAttribute("JNDIInitialContext", ic);
    }
}
