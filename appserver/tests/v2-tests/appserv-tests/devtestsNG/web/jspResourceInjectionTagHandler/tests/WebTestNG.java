import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;
import org.testng.Assert;

import java.io.*;
import java.net.*;

public class WebTestNG {

    private static final String TEST_NAME =
        "jsp-resource-injection-tag-handler";

    private static final String EXPECTED_RESPONSE =
        "ds1-login-timeout=0,ds2-login-timeout=0,ds3-login-timeout=0,"
        + "ds4-login-timeout=0,ds5-login-timeout=0,ds6-login-timeout=0";

    static String result = "";


    @Parameters({ "host", "port", "contextroot" })
    @Test(groups ={ "even"} ) // test method
    public void webtest(String host, String port, String contextroot) throws Exception{

        String testurl = "http://" + host  + ":" + port + contextroot + "/jsp/test.jsp";

        URL url = new URL(testurl);
        echo("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        Assert.assertEquals(responseCode, 200);

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String lastLine = null;
        while ((line = input.readLine()) != null) {
             lastLine = line;
        }
        
        Assert.assertEquals(lastLine, EXPECTED_RESPONSE);

    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

}
