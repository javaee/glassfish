import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;

import java.io.*;
import java.net.*;

public class WebTestNG {

    private static final String TEST_NAME =
        "tag-plugin-for-each";

    @Parameters({ "host", "port", "contextroot" })
    @Test(groups ={ "even"} ) // test method
    public void webtest(String host, String port, String contextroot) throws Exception{

        boolean success = false;
        String hostPortRoot = host  + ":" + port + contextroot;
        success = doTest("http://" + hostPortRoot + "/jsp/iterator.jsp",
                         "OneTwoThree");

        if (success) {
            success = doTest("http://" + hostPortRoot + "/jsp/map.jsp",
                             "Three=ThreeTwo=TwoOne=One");
        }

        if (success) {
            success = doTest("http://" + hostPortRoot + "/jsp/enum.jsp",
                             "OneTwoThree");
        }

        assert success == true;

    }

    /*
     * Returns true in case of success, false otherwise.
     */
    private static boolean doTest(String urlString, String expected) {
        try { 
            URL url = new URL(urlString);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                System.out.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                return false;
            }

            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            boolean found = false;
            while ((line = input.readLine()) != null) {
                if (expected.equals(line)) {
                    found = true;
                }
            }

            if (!found) {
                System.out.println("Invalid response. Response did not " +
                                   "contain expected string: " + expected);
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

}
