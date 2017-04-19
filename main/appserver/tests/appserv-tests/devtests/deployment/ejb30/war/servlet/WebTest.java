import java.io.*;
import java.net.*;

/**
 * Unit test for annotation associated to jsp.
 */
public class WebTest {

    private String host;
    private int port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
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
     
        String contextPath = "/" + contextRoot + "/servlet";
        Socket s = new Socket(host, port);

        OutputStream os = s.getOutputStream();
        
        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("Authorization: Basic ajJlZTpqMmVl\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int count = 0;
        int lineNum = 0;
        while ((line = bis.readLine()) != null) {
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
