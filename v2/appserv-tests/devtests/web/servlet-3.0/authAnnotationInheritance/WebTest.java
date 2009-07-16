import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Inheritance @RolesAllowed, @DenyAll, @PermitAll
 */
public class WebTest {

    private static final String TEST_NAME = "auth-annotation-inheritance";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for @RolesAllowed, @DenyAll, @PermitAll Inheritance");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void run() throws Exception {
        String contextPath = contextRoot + "/myurl";
        doWebMethod("POST", host, port, contextPath, true, "simple_@RolesAllowed", 200, "p:Hello, javaee");
        doWebMethod("TRACE", host, port, contextPath, false, "simple_override_@PermitAll", 200, "t:Hello");

        contextPath = contextRoot + "/myurl2";
        doWebMethod("TRACE", host, port, contextPath, false, "c_@PermitAll", 200, "t:Hello");
        doWebMethod("PUT", host, port, contextPath, true, "c_dervied_@RolesAllowed", 200, "put:Hello, javaee");
        doWebMethod("POST", host, port, contextPath, true, "c_nonoverride_@RolesAllowed", 200, "p:Hello, javaee");
        doWebMethod("GET", host, port, contextPath, true, "c_nonoverride_@RolesAllowed_2", 403, null);

        contextPath = contextRoot + "/myurl2b";
        doWebMethod("GET", host, port, contextPath, true, "base_method_@RolesAllowed", 403, null);
        doWebMethod("POST", host, port, contextPath, true, "base_class_@RolesAllowed", 200, "p:Hello, javaee");
        doWebMethod("TRACE", host, port, contextPath, true, "base_method_@DenyAll", 403, null);

    }

    private static void doWebMethod(String webMethod, String host, int port,
            String contextPath, boolean requireAuthenticate, String testSuffix,
            int responseCode, String expected) throws Exception {

        String urlStr = "http://" + host + ":" + port + contextPath;
        System.out.println(webMethod + " " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        urlConnection.setRequestMethod(webMethod);
        if (requireAuthenticate) {
            urlConnection.setRequestProperty("Authorization", "Basic amF2YWVlOmphdmFlZQ==");
        }
        urlConnection.connect();

        int code = urlConnection.getResponseCode();
        boolean ok = (code == responseCode);
        if (!ok) {
            System.out.println("Get response code: " + code + ", expected " + responseCode);
        }
        if (expected != null) {
            InputStream is = null;
            BufferedReader bis = null;
            String line = null;

            try{
                is = urlConnection.getInputStream();
                bis = new BufferedReader(new InputStreamReader(is));
                int lineNum = 1;
                while ((line = bis.readLine()) != null) {
                    System.out.println(lineNum + ":  " + line);
                    lineNum++;
                    ok = ok && expected.equals(line);
                }
            } catch( Exception ex){
                ex.printStackTrace();   
                throw new Exception("Test UNPREDICTED-FAILURE");
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }
        }

        stat.addStatus(TEST_NAME + ":" + testSuffix, ((ok)? stat.PASS : stat.FAIL));
    }
}
