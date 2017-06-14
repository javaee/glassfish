import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {
    
    private static SimpleReporterAdapter stat
            = new SimpleReporterAdapter("appserv-tests");
    
    private static final String TEST_NAME = "callflow-simple-jsp";
    
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
            if (args.length == 5){
                if (args[3].equalsIgnoreCase("report")){
                    stat.addDescription("Callflow Simple JSP Test");
                    webTest.analyseResult(args[4]);
                    stat.printSummary(TEST_NAME);
                    
                }
            }else if (args.length == 4){ 
                if (args[3].equalsIgnoreCase ("clean-db")){
			webTest.cleandb ();
		}
            }else
                webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }
    
    public void doTest() throws Exception {
        
        URL url = new URL("http://" + host  + ":" + port
                + contextRoot + "/including.jsp");
        System.out.println("Connecting to: " + url.toString());
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
        }
	conn.disconnect ();
    }
    
    public void analyseResult(String result) throws Exception {
        
        URL url = new URL("http://" + host  + ":" + port
                +"/dbReader/dbReader?servletName=callflow-simple-jsp");
        System.out.println("Analysing Result .... Connecting to: " + url.toString());
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
	    System.out.println ("Expected Result :" + result);
            System.out.println ("Actual Result   :" + line);
	    if(result.equals (line))
            	stat.addStatus(TEST_NAME, stat.PASS);
	    else
		stat.addStatus(TEST_NAME, stat.FAIL);		

        }
	conn.disconnect ();
    }

    public void cleandb() throws Exception {
        
        URL url = new URL("http://" + host  + ":" + port
                +"/dbReader/dbReader?cleandb=true");
        System.out.println("Cleaning DB .... Connecting to: " + url.toString());
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
	    System.out.println (line);
	    System.out.println (input.readLine());
	}
	
	conn.disconnect ();
    } 
}
