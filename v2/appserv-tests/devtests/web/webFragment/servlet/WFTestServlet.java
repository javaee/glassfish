package wftest;

import java.io.*;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.InitParam;
import javax.servlet.annotation.WebServlet;

@WebServlet(name="wftestServlet", initParams={ @InitParam(name="mesg", value="hello a"), @InitParam(name="mesg3", value="hello3 a") })
public class WFTestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String message = "filterMessage=" + req.getAttribute("filterMessage");
        message += ", mesg=" + getInitParameter("mesg") +
            ", mesg2=" + getInitParameter("mesg2") + ", mesg3=" + getInitParameter("mesg3");
        res.getWriter().println(message);
    }
}
