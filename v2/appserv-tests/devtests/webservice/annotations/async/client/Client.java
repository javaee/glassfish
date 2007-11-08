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
	    stat.addDescription("async-service");
            Client client = new Client();
            client.doSyncTest();
            client.doASyncPollTest();
            client.doASyncCallBackTest();
	    stat.printSummary("async-service");
        }

        public void doSyncTest() {
            try {
                AddNumbersImpl port = service.getAddNumbersImplPort();
                int ret = port.addNumbers(2222, 1234);
		if(ret!=(2222+1234)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("async-service-sync-test", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("async-service-sync-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-service-sync-test", stat.FAIL);
            }
        }

        private void doASyncPollTest () {
            System.out.println ("Invoking Asynchronous Polling addNumbers");
            try {
                AddNumbersImpl port = service.getAddNumbersImplPort();
                Response<AddNumbersResponse> resp = port.addNumbersAsync(1234, 5678);
                Thread.sleep (2000);
                AddNumbersResponse output = resp.get();
                int ret = output.getReturn();
		if(ret!=(1234+5678)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("async-service-poll-test", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("async-service-poll-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-service-poll-test", stat.FAIL);
            }
        }

        public void doASyncCallBackTest () {
            System.out.println ("Invoking Asynchronous Callback addNumbers");
            try {
                AddNumbersImpl port = service.getAddNumbersImplPort();
                AddNumbersCallbackHandler cbh = new AddNumbersCallbackHandler();
                Future<?> response = port.addNumbersAsync(9876, 5432, cbh);
                Thread.sleep (2000);
                AddNumbersResponse output = cbh.getResponse ();
                int ret = output.getReturn();
		if(ret!=(9876+5432)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("async-service-callbackhandler-test", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("async-service-callbackhandler-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-service-callbackhandler-test", stat.FAIL);
            }
        }

        // The actual call back handler
        private class AddNumbersCallbackHandler implements AsyncHandler<AddNumbersResponse> {
            private AddNumbersResponse output;
            public void handleResponse (Response<AddNumbersResponse> response) {
                try {
                    output = response.get ();
                } catch (ExecutionException e) {
                    e.printStackTrace ();
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            }

            AddNumbersResponse getResponse (){
                return output;
            }
        }
}

