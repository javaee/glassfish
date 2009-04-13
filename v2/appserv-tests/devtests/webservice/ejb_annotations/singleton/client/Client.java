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

        //@WebServiceRef
       // static HelloImplService service;

        public static void main(String[] args) {
	    stat.addDescription("ejb-singleton-service");
            Client client = new Client();
            client.doSyncTest();
	    stat.printSummary("ejb-singleton-service");
        }

        public void doSyncTest() {
            try {
                HelloImplService service = new HelloImplService();
                HelloImpl port = service.getHelloImplPort();
                RetVal ret = port.sayHello("Hi Singleton");
		if(ret.getRetVal().indexOf("Sing") == -1) {
		    System.out.println("WRONG GREETING " + ret.getRetVal());
                    stat.addStatus("ejb-singleton-service-test", stat.FAIL);
		    return;
		}
                System.out.println(ret);
                System.out.println(ret.getRetVal());
                stat.addStatus("ejb-singleton-service-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ejb-singleton-service-test", stat.FAIL);
            }
        }
}

