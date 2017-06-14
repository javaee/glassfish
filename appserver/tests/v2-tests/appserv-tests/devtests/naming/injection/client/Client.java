package test;

import javax.ejb.*;
import javax.annotation.*;
import javax.naming.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String args[]) {
	appName = args[0]; 
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {}

    public void doTest() {
	try {
            InitialContext ic = new InitialContext();
            Hello helloBean = (Hello) ic.lookup("java:global/" + appName + "/HelloBean");
            System.out.println("Invoking " + helloBean + ", " + helloBean.injectedURL());
	    stat.addStatus(appName, stat.PASS);
	} catch(Exception e) {
	    e.printStackTrace();
	    stat.addStatus(appName, stat.FAIL);
	}
    }
}
