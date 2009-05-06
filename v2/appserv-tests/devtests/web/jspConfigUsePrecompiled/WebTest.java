import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6181923 ("Add support for 'use-precompiled' JspServlet
 * option introduced by WS 6.0").
 *
 * This webapp contains a JSP (jsp/test.jsp) and also bundles the
 * precompiled servlet class of this JSP under WEB-INF/classes. Notice that
 * the output of the JSP and its precompiled servlet class are different, so
 * that this test program is able to distinguish which of the two provided the
 * output when the JSP is accessed.
 *
 * This webapp sets the "use-precompiled" jsp-config property (in its
 * sun-web.xml) to TRUE. As a result of this, when the JSP is accessed, the
 * JSP must not be compiled, and the output of the precompiled servlet must be
 * returned.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME
        = "jsp-config-use-precompiled";
    private static final String EXPECTED = "this is the precompiled and bundled jsp";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6181923");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void run() throws Exception {

        String bodyLine = null;

        // Access JSP
        bodyLine = getBodyLine("jsp/test.jsp");
        if (!EXPECTED.equals(bodyLine)) {
            throw new Exception("Wrong line: Expected: " + EXPECTED);
        }
    }

    private String getBodyLine(String resource) throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/" + resource + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String bodyLine = null;
            while ((line = bis.readLine()) != null) {
                bodyLine = line;
            }
            return bodyLine;
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (sock != null) sock.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }   
}
