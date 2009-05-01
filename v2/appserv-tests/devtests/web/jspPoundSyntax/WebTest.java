import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for handling of EL expressions #{...} in tag attributes
 * This includes bugs 6372687, 6377689, 6380354
 *
 * Make sure that is treated as a literal when jspversion of the tld is
 * 2.0 or less.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit tests for pound syntax as literal");
        WebTest webTest = new WebTest(args);
        webTest.doTest1("/testjsp12.jsp",
                        "jsp12-pound-compatibility-test");
        webTest.doTest2("/testjsp21x.jsp",
                        "pound-in-literal-test");
        webTest.doTest2("/testjsp21y.jsp",
                        "pound-in-exprval-test");
        webTest.doTest3("/testjsp21w.jsp",
                        "pound-default-test");
        webTest.doTest4("/testjsp21z.jsp",
                        "pound-syntax-allowed-as-literal-true-test");

	stat.printSummary();
    }

    private boolean checkValue(BufferedReader input,
                               String key, String expectedValue) 
            throws Exception {
        String line = input.readLine();
        while (line.equals(""))
            line = input.readLine();
        if (line == null)
            return true;
        if (!line.equals(key))
            return true;
        line = input.readLine();
        if (line == null)
            return true;
        if (!line.equals(expectedValue)) {
            System.err.println("Wrong response. Expected value for attribute "
                + key + ": " + expectedValue + ", received: " + line);
            return true;
        }
        return false; 
    }

    public void doTest1(String path, String testName) {

        InputStream is = null;
        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ':' + port + '/' + contextRoot + path);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(testName, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(
is));
                boolean error = false;
                if (checkValue(input, "val1", "#{abc}"))
                    error = true;
                if (checkValue(input, "val2", "#{xyz}"))
                    error = true;
                if (checkValue(input, "val1", "\\#{abc}"))
                    error = true;
                if (checkValue(input, "val2", "\\#{xyz}"))
                    error = true;
                if (checkValue(input, "val1", "\\abc"))
                    error = true;
                if (checkValue(input, "val2", "\\xyz"))
                    error = true;
                if (error) {
                    stat.addStatus(testName, stat.FAIL);
                } else {
                    stat.addStatus(testName, stat.PASS);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, stat.FAIL);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }


    public void doTest2(String path, String testName) {
     
        try { 
            URL url = new URL("http://" + host  + ':' + port + '/' + contextRoot + path);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { 
                System.err.println("Wrong response code. Expected: 500"
                                   + ", received: " + responseCode);
                stat.addStatus(testName, stat.FAIL);
            } else {
                stat.addStatus(testName, stat.PASS);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, stat.FAIL);
        }
    }

    public void doTest3(String path, String testName) {

        InputStream is = null;
        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ':' + port + '/' + contextRoot
 + path);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(testName, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(
is));
                boolean error = false;
                if (checkValue(input, "val1", "#{abc}"))
                    error = true;
                if (checkValue(input, "val2", "#{xyz}"))
                    error = true;
                if (checkValue(input, "expr", "FooValue"))
                    error = true;
                if (checkValue(input, "val1", "#{abc}"))
                    error = true;
                if (checkValue(input, "val2", "#{xyz}"))
                    error = true;
                if (checkValue(input, "expr", "#{foo}"))
                    error = true;
                if (checkValue(input, "exprString", "#map"))
                    error = true;
                if (error) {
                    stat.addStatus(testName, stat.FAIL);
                } else {
                    stat.addStatus(testName, stat.PASS);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, stat.FAIL);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }

    public void doTest4(String path, String testName) {

        InputStream is = null;
        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ':' + port + '/' + contextRoot
 + path);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(testName, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(
is));
                boolean error = false;
                if (checkValue(input, "val1", "\\#{abc}"))
                    error = true;
                if (checkValue(input, "val2", "\\#{xyz}"))
                    error = true;
                if (checkValue(input, "expr", "\\#{foo}"))
                    error = true;
                if (checkValue(input, "val1", "#{abc}"))
                    error = true;
                if (checkValue(input, "val2", "#{xyz}"))
                    error = true;
                if (checkValue(input, "expr", "#{foo}"))
                    error = true;
                if (error) {
                    stat.addStatus(testName, stat.FAIL);
                } else {
                    stat.addStatus(testName, stat.PASS);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, stat.FAIL);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }

}
