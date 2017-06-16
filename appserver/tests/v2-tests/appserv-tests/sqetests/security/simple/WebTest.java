
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * A quick'n'dirty test tool for security/simple quicklook.
 * The quicklook WebTest currently does not support a way
 * to specify required output, which is needed to validate
 * this test. Thus, this class is used.
 *
 * @author Jyri J. Virkki
 *
 */
public class WebTest
{

    /**
     * Must be invoked with (host,port) args.
     * Nothing else is parameterized, this is intended as
     * throwaway after the SQE web test framework exists.
     * User/authorization info is hardcoded and must match
     * the values in descriptors and build.xml.
     *
     */
    public static void main(String args[])
    {
        SimpleReporterAdapter stat=
            new SimpleReporterAdapter("appserv-tests");

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Basic Web Authentication/Authorization Test");

        String host = args[0];
        String portS = args[1];
        int port = new Integer(portS).intValue();
        String name;
        
        System.out.println("Host ["+host+"] port ("+port+")");

        // GET with a user who maps directly to role
        name="simpleauth: BASIC/access control: testuser3";
        try {
            System.out.println(name);
            String result="RESULT: principal: testuser3";
            goGet(host, port, result,
                  "Authorization: Basic dGVzdHVzZXIzOnNlY3JldA==\n");
            stat.addStatus(name, stat.PASS);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(name, stat.FAIL);
        }
        
        // GET with a user who maps through group
        name="simpleauth: BASIC/access control: testuser42";
        try {
            System.out.println(name);
            String result="RESULT: principal: testuser42";
            goGet(host, port, result,
                  "Authorization: Basic dGVzdHVzZXI0MjpzZWNyZXQ=\n");
            stat.addStatus(name, stat.PASS);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(name, stat.FAIL);
        }

        // GET with a valid user who is not authorized
        name="simpleauth: BASIC/access control: j2ee";
        try {
            System.out.println(name);
            String result="HTTP/1.1 403";
            goGet(host, port, result,
                  "Authorization: Basic ajJlZTpqMmVl\n");
            stat.addStatus(name, stat.PASS);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(name, stat.FAIL);
        }

        // GET with a valid user,bad password
        name="simpleauth: BASIC/authentication: j2ee (bad pwd)";
        try {
            System.out.println(name);
            String result="HTTP/1.1 401";
            goGet(host, port, result,
                  "Authorization: Basic ajJlZTo=\n");
            stat.addStatus(name, stat.PASS);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(name, stat.FAIL);
        }

        stat.printSummary("security/simple");
    }

    /**
     * Connect to host:port and issue GET with given auth info.
     * This is hardcoded to expect the output that is generated
     * by the Test.jsp used in this test case.
     *
     */
    private static void goGet(String host, int port,
                              String result, String auth)
         throws Exception
    {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        os.write("GET /simpleauth/Test.jsp HTTP/1.0\n".getBytes());
        os.write(auth.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        while ((line = bis.readLine()) != null) {
            if (line.indexOf(result) != -1) {
                System.out.println("  Found: "+line);
                s.close();
                return;
            }
        }

        s.close();
        throw new Exception("String not found: "+result);
    }
  
}
