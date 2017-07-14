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
        static HelloImplService service;

        public static void main(String[] args) {
	    stat.addDescription("ejb-libdependent-2-service");
            Client client = new Client();
            client.doSyncTest();
	    stat.printSummary("ejb-libdependent-2-service");
        }

        public void doSyncTest() {
            try {
                HelloImpl port = service.getHelloImplPort();
                RetVal ret = port.sayHello("Hi LibDependent");
		if(ret.getRetVal().indexOf("LibDep") == -1) {
		    System.out.println("WRONG GREETING " + ret.getRetVal());
                    stat.addStatus("ejb-libdependent-2-service-test", stat.FAIL);
		    return;
		}
                System.out.println(ret);
                System.out.println(ret.getRetVal());
                stat.addStatus("ejb-libdependent-2-service-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ejb-libdependent-2-service-test", stat.FAIL);
            }
        }
}

