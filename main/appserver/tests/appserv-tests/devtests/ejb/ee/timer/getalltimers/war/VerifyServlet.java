package com.acme;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.naming.*;

@WebServlet(urlPatterns="/VerifyServlet", loadOnStartup=1)
public class VerifyServlet extends HttpServlet {

    @EJB private SgltTimerBean sgltTimerBean;
    @EJB private StlesTimerBean stlesTimerBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

        PrintWriter out = resp.getWriter();
	resp.setContentType("text/html");
        String param = req.getQueryString();

        out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet VerifyServlet</title>");
            out.println("</head>");
            out.println("<body>");
        try {
            stlesTimerBean.createProgrammaticTimer();
            sgltTimerBean.createProgrammaticTimer();
            out.println("RESULT:" + sgltTimerBean.countAllTimers(param));
        }catch(Throwable e){
            out.println("got exception");
            out.println(e);
            e.printStackTrace();
        } finally {
            out.println("</body>");
            out.println("</html>");

            out.close();
            out.flush();

        }

    }

}
