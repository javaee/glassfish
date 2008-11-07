package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CreateSession extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        Map<String, String> map = new HashMap<String, String>();
        map.put("name1", "value1");
        map.put("name2", "value2");
        session.setAttribute("map", map);
    }
}
