package bank.test;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.lang.*;
import java.io.*;
import java.net.*;

/**
 * A quick'n'dirty test tool for security/simple quicklook.
 * The quicklook WebTest currently does not support a way
 * to specify required output, which is needed to validate
 * this test. Thus, this class is used.
 *
 * @author Jyri J. Virkki
 *
 */
public class WebTest {
    private static SimpleReporterAdapter reporter = new SimpleReporterAdapter();
    
    public static void main(String args[]) {
        reporter.addDescription("Test secure webservices in a JBI environment");
        
        String host = args[0];
        String portS = args[1];
        int port = new Integer(portS).intValue();
        String userPass = args[3];
        String context = args[2];
        
        System.out.println("Host ["+host+"] port ("+port+")");
        
        String testCase = context.substring(
                context.indexOf("="), context.length());
        
        String testId = "jbi.security.bank." + testCase;
        
        // GET with a user who maps directly to role
        try {
            goGet(testId, host, port, context, userPass);
        } catch (Exception ex) {
            ex.printStackTrace();
            reporter.addStatus(testId, reporter.FAIL);
        } finally {
            reporter.printSummary();
        }
    }
    
    /**
     * Connect to host:port and issue GET with given auth info.
     * This is hardcoded to expect the output that is generated
     * by the Test.jsp used in this test case.
     *
     */
    private static void goGet(String testId, String host, int port, 
            String context, String userPass) throws Exception {

        Socket socket = null;
        
        try {
            socket = new Socket(host, port);
            OutputStream os = socket.getOutputStream();
            
            String auth = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(userPass.getBytes());
            
            String authStr = "Authorization: Basic " + auth + "\n";
            System.out.println("auth : " + auth);
            String request = "GET " + context + " HTTP/1.0\n";
            os.write(request.getBytes());
            os.write(authStr.getBytes());
            os.write("\n".getBytes());
            os.flush();
            System.out.println("Calling Servlet");
            
            InputStream is = socket.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.contains("PASS") || line.contains("FAIL"))
                    addStatus(testId, line);
            }
        } finally {
            if (socket != null)
                try {
                    socket.close();
                } catch (Exception e) {}
        }
    }
    
    private static void addStatus(String testId, String line) {
        int index = line.indexOf("PASS");

        if (index != -1)
            reporter.addStatus(testId, reporter.PASS);
        else
            reporter.addStatus(testId, reporter.FAIL);
    }
}
