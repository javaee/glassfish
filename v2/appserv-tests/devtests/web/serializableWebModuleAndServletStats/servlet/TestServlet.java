import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.sun.enterprise.web.monitor.*;
import com.sun.enterprise.web.monitor.impl.*;

public class TestServlet extends HttpServlet {

    private static final String DOMAIN_NAME = "com.sun.appserv";
    private static final String FILE_NAME = "ser.tmp";
    private static final String VS = "server";
 
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException  {

        doWebModuleStats(req, res);
        doServletStats(req, res);
    }


    /*
     * Serializes and deserializes PwcWebModuleStatsImpl.
     */
    private void doWebModuleStats(HttpServletRequest req,
                                  HttpServletResponse res)
            throws ServletException, IOException  {

        String contextPath = req.getParameter("contextPath");

        String objName = DOMAIN_NAME + ":j2eeType=WebModule,"
                         + "name=//" + VS + contextPath
                         + ",J2EEApplication=null,J2EEServer=server";
       
        PwcWebModuleStats pwcWebModuleStats = new PwcWebModuleStatsImpl(
                                                        objName,
                                                        contextPath,
                                                        DOMAIN_NAME, 
                                                        VS,
                                                        null,
                                                        "server");

        int sessionsTotal = pwcWebModuleStats.getSessionsTotal();
        if (sessionsTotal != 0) {
            throw new ServletException("Sessions total != 0");
        }

        // Create session
        req.getSession();

        sessionsTotal = pwcWebModuleStats.getSessionsTotal();
        if (sessionsTotal != 1) {
            throw new ServletException("Sessions total != 1");
        }

        // Serialize
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(pwcWebModuleStats);

        // Deserialize
        FileInputStream fis = new FileInputStream(FILE_NAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        try {
            pwcWebModuleStats = (PwcWebModuleStats) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new ServletException(e);
        }

        // Make sure sesssionsTotal hasn't changed
        sessionsTotal = pwcWebModuleStats.getSessionsTotal();
        if (sessionsTotal != 1) {
            throw new ServletException("Sessions total != 1");
        }
    }


    /*
     * Serializes and deserializes PwcServletStatsImpl.
     */
    private void doServletStats(HttpServletRequest req,
                                HttpServletResponse res)
            throws ServletException, IOException  {

        String contextPath = req.getParameter("contextPath");

        PwcServletStats pwcServletStats = new PwcServletStatsImpl(
                                                    DOMAIN_NAME, 
                                                    VS,
                                                    contextPath,
                                                    "TestServlet",
                                                    "null",
                                                    "server");

        // Serialize
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(pwcServletStats);

        // Deserialize
        FileInputStream fis = new FileInputStream(FILE_NAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        try {
            pwcServletStats = (PwcServletStats) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new ServletException(e);
        }

    }
}
