import java.io.*;
import java.net.*;

/**
 * Unit test for annotation associated to jsp.
 */
public class WebTest {

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch(Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port + "/"
                          + contextRoot + "/index.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            System.exit(-1);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            int count = 0;
            int lineNum = 0;
            while ((line = input.readLine()) != null) {
                System.out.println(lineNum + ": " + line);
                int index = line.indexOf("Hello World");
                if (index != -1) {
                       count++;
                }
                lineNum++;
            }

            int correctCount = 2;
            if (count != correctCount) {
                System.err.println("Incorrect Message count: " + count +
                    ", should be " + correctCount);
                System.exit(-1);
            }
        }
    }
}
