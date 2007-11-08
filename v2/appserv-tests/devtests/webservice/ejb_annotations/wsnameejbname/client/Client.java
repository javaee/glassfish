package client;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.ws.WebServiceRef;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import endpoint.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef
        static AddNumbersImplService service;

        public static void main(String[] args) {
	    stat.addDescription("wsname-ejbname-service");
            Client client = new Client();
            client.doSyncTest();
	    stat.printSummary("wsname-ejbname-service");
        }

        public void doSyncTest() {
            try {
                ThisShouldBeIgnored port = service.getThisShouldBeIgnoredPort();
                int ret = port.addNumbers(2222, 1234);
		if(ret!=(2222+1234)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("wsname-ejbname-service-test", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("wsname-ejbname-service-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("wsname-ejbname-service-test", stat.FAIL);
            }
        }
}

