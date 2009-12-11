import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        File logFile = new File("mylog4j.log");
        logFile.delete();

        /*
         * Use commons-logging APIs for logging. Message will be logged into
         * the file specified in log4j.properties resource.
         */
        Log log = LogFactory.getLog(this.getClass());
        log.error(log.getClass() + ": This is my test log message");

        FileInputStream fis = new FileInputStream(logFile);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            res.getWriter().println(br.readLine());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        logFile.delete();
    }
}
