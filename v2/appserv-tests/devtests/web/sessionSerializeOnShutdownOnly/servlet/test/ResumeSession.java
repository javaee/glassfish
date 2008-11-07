package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ResumeSession extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Map map = session == null ? new HashMap() : (HashMap) session.getAttribute("map");
        if ("value1".equals(map.get("name1")) && "value2".equals(map.get("name2"))) {
            res.getWriter().print("Found map");
        } else {
            res.getWriter().print("No map found");
        }
    }
}
