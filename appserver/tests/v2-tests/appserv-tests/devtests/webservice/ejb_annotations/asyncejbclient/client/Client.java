package client;

import javax.ejb.EJB;
import ejb.Hello; 

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @EJB(mappedName="ejb.Hello")
	static Hello hello;

        public static void main(String[] args) {
	    stat.addDescription("async-ejb-client");
            Client client = new Client();
            client.doSyncTest();
            client.doAsyncPollTest();
            client.doAsyncCallbackTest();
	    stat.printSummary("async-ejb-client");
       }

       public void doSyncTest() {
            try {
                String ret = hello.invokeSync("Hello Tester !");
		if(ret.indexOf("SYNC CALL") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("async-sync-ejb-client", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("async-sync-ejb-client", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-sync-ejb-client", stat.FAIL);
            }
       }

       public void doAsyncPollTest() {
            try {
                String ret = hello.invokeAsyncPoll("Hello Tester !");
		if(ret.indexOf("ASYNC POLL CALL") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("async-poll-ejb-client", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("async-poll-ejb-client", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-poll-ejb-client", stat.FAIL);
            }
       }

       public void doAsyncCallbackTest() {
            try {
                String ret = hello.invokeAsyncCallBack("Hello Tester !");
		if(ret.indexOf("ASYNC CALL BACK CALL") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus("async-callback-ejb-client", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("async-callback-ejb-client", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-callback-ejb-client", stat.FAIL);
            }
       }
}

