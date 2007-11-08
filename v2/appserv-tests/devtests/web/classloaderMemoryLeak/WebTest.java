import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6273998
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "classloaderMemoryLeak";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("classloaderMemoryLeak");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            long memory1 = invoke();
            System.out.println("mem1: " + memory1);
            executeAnt();
            long memory2 = invoke();
            System.out.println("mem2: " + memory2);
            System.out.println("mem used: " + (memory2 - memory1));
            if ( memory2 - memory1 > 10000){ 
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private long invoke() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        sock.setSoTimeout(1000);
        OutputStream os = sock.getOutputStream();
        String get = "GET /web-classloaderMemoryLeak/ServletTest HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: localhost\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        boolean found = false;
        String line = null;
        int i =0;
        try{
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                if ( line.startsWith("USAGE:") ){
                    return new Long(line.substring("USAGE:".length())).longValue();
                }
            }
        }catch (SocketTimeoutException t){
            ;
        }
        sock.close();
        return 0L;
    }

    public static void executeAnt() throws IOException,InterruptedException {
        String osName = System.getProperty("os.name" );
        String[] cmd = new String[3];

        Process proc = null;
        if( osName.equals( "Windows NT" ) ) {
            cmd[0] = "cmd.exe" ;
            cmd[1] = "/C" ;
            cmd[2] = "ant undeploy deploy";
            Runtime rt = Runtime.getRuntime();
            System.out.println("Execing " + cmd[0] + " " + cmd[1]
                               + " " + cmd[2]);
            proc = rt.exec(cmd);
        } else {
            proc = Runtime.getRuntime().exec("ant undeploy deploy");
        }

       try {
            InputStreamReader isr = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                System.out.println(line);    
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }

    
}
