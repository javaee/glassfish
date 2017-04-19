package com.acme.ejb32.timer.opallowed;

import javax.annotation.Resource;
import javax.ejb.EJB;
import java.io.IOException;
import java.io.PrintWriter;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns="/VerifyServlet", loadOnStartup=1)
public class VerifyServlet extends HttpServlet {

    @EJB private SingletonTimeoutLocal sgltTimerBeanLocal;
    @EJB private SingletonTimeout sgltTimerBean;

    @Resource(lookup = "java:module/MngBean") MngTimeoutBean mngBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        String type = req.getQueryString();

        PrintWriter out = resp.getWriter();
    	resp.setContentType("text/html");

        out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet VerifyServlet</title>");
            out.println("</head>");
            out.println("<body>");
        try {
            if ("managedbean".equals(type)) {
                mngBean.cancelTimer();
                out.println("RESULT: PASS" );
            } else {
                // this is to test the APIs allowed to be invoked
                // the return values are not interested
                Timer t = sgltTimerBeanLocal.createLocalTimer("webapp");
                TimerHandle handle = t.getHandle();
                handle.getTimer();
                t.getInfo();
                t.getNextTimeout();
                t.getSchedule();
                t.getTimeRemaining();
                t.isCalendarTimer();
                t.isPersistent();
                t.cancel();

                // this is blocked by JIRA19546 now
                //boolean remoteSuc = testRemoteInterface(out);
                boolean remoteSuc = true;
                if(remoteSuc) {
                    out.println("RESULT: PASS" );
                } else {
                    out.println("RESULT: FAIL" );
                }
            }
        } catch(Throwable e){
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

    private boolean testRemoteInterface(PrintWriter out) {
        try {
            TimerHandle thremote = sgltTimerBean.createTimer("webappremote");
            out.println("shouldn't get TimerHandle through remote interface!");
            return false;
        } catch (Exception e) {
        }
        return true;
    }

}
