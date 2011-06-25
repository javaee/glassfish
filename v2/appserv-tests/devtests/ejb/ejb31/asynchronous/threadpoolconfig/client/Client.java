package test;
import javax.ejb.*;
import javax.annotation.*;
import javax.naming.*;
import java.util.concurrent.*;
import java.util.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private static String appName;
    private static int numOfInvocations = 50;
    private static int maxPoolSize = 32;
    private static String threadNamePrefix = "__ejb-thread-pool";

    public static void main(String args[]) throws Exception {
	appName = args[0]; 
        if(args.length >= 2) {
            try {
                numOfInvocations = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {  //ignore
            }
        }
        if(args.length >= 3) {
            try {
                maxPoolSize = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {  //ignore
            }
        }
	stat.addDescription(appName);
	Client client = new Client();       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public void doTest() throws Exception {
        boolean failed = false;
        InitialContext ic = new InitialContext();
        Hello helloBean = (Hello) ic.lookup("java:global/" + appName + "/HelloBean");

        List<Future<String>> results = new ArrayList<Future<String>>();
        List<String> acceptableThreadNames = new ArrayList<String>();

        for(int i = 1; i <= maxPoolSize; i++) {
            acceptableThreadNames.add(threadNamePrefix + i);
        }
        for(int i = 0; i < numOfInvocations; i++) {
            results.add(helloBean.getThreadNameId());
        }
        
        for(Future<String> f : results) {
            String s = f.get();
            String threadName = s.split(" ")[0];
            if(acceptableThreadNames.contains(threadName)) {
                System.out.println("Thread name is in range: " + s);
            } else {
                failed = true;
                System.out.println("Thread name is NOT in range: " + s);
            }
        }
        System.out.println("Number of results: " + results.size());
        System.out.println("All " + acceptableThreadNames.size() + 
            " acceptable thread names: " + acceptableThreadNames);
        stat.addStatus(appName, (failed ? stat.FAIL: stat.PASS));
    }
}
